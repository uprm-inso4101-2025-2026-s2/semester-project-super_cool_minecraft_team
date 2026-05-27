package com.inso.MinecraftProject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.inso.MinecraftProject.dto.MissingDependencyDto;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;


import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class DependencyLookupService {

    private static final Logger logger = LoggerFactory.getLogger(DependencyLookupService.class);

    private final DependencyLookupCache<ResolvedDependencyDto> cache;
    private final ModrinthServiceWrapper modrinthServiceWrapper;

    public DependencyLookupService(
            ModrinthServiceWrapper modrinthServiceWrapper,
            @Value("${cache.dependency.ttl-minutes:10}")long ttlMinutes) {
        this.cache = new DependencyLookupCache<>(Duration.ofMinutes(ttlMinutes));
        this.modrinthServiceWrapper = modrinthServiceWrapper;
            

    }

    public List<ResolvedDependencyDto> resolveDependencies(List<MissingDependencyDto> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return List.of();
        }

        List<ResolvedDependencyDto> resolvedDependencies = new ArrayList<>();
        for (MissingDependencyDto dependency : dependencies) {
            resolveDependency(dependency).ifPresent(resolvedDependencies::add);
        }
        return resolvedDependencies;
    }

    public Optional<ResolvedDependencyDto> resolveDependency(MissingDependencyDto dependency) {
        validateDependency(dependency);

        String key = buildCacheKey(dependency);

        Optional<ResolvedDependencyDto> cached = cache.get(key);
        if (cached.isPresent()) {
            ResolvedDependencyDto dto = cached.get();
            if (isValidCacheEntry(dto)) {
                return cached;
            }
            logger.debug("Invalidating cache entry with invalid download links: {}", key);
            cache.invalidate(key);
        }

        Optional<ResolvedDependencyDto> result = fetchFromExternalSource(dependency);

        if (result.isPresent() && isValidCacheEntry(result.get())) {
            cache.put(key, result.get());
        } else {
            logger.debug("Not caching invalid or empty result for: {}", key);
        }

        return result;
    }

    private boolean isValidCacheEntry(ResolvedDependencyDto dto) {
        if (dto == null) {
            return false;
        }
        if (dto.links() == null || dto.links().isEmpty()) {
            return false;
        }
        if (dto.preferred() == null || dto.preferred().isBlank()) {
            return false;
        }
        return true;
    }

    public Optional<ResolvedDependencyDto> resolveDependencyById(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("Project id cannot be blank.");
        }

        try {
            JsonNode project = modrinthServiceWrapper.getProjectById(projectId);

            if (project == null || project.isMissingNode() || project.isEmpty()) {
                throw new NoSuchElementException("Dependency not found: " + projectId);
            }

            String slug = project.path("slug").asText("");
            String title = project.path("title").asText("");
            String projectType = project.path("project_type").asText("");

            if (slug.isBlank()) {
                throw new NoSuchElementException("Dependency not found: " + projectId);
            }

            String baseUrl = switch (projectType.toLowerCase(Locale.ROOT)) {
                case "mod" -> "https://modrinth.com/mod/";
                case "modpack" -> "https://modrinth.com/modpack/";
                case "resourcepack" -> "https://modrinth.com/resourcepack/";
                case "shader" -> "https://modrinth.com/shader/";
                default -> "https://modrinth.com/project/";
            };

            String url = baseUrl + slug;

            return Optional.of(new ResolvedDependencyDto(
                    projectId,
                    title.isBlank() ? slug : title,
                    List.of(url),
                    url
            ));
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to resolve dependency by id: " + projectId, ex);
        }
    }

    private void validateDependency(MissingDependencyDto dependency) {
        if (dependency == null) {
            throw new IllegalArgumentException("Dependency request cannot be null.");
        }

        String query = firstNonBlank(dependency.name(), dependency.id());
        if (query == null) {
            throw new IllegalArgumentException("Dependency must include a valid id or name.");
        }

        String version = dependency.requiredVersion();
        if (version != null && version.isBlank()) {
            throw new IllegalArgumentException("Required version cannot be blank.");
        }

        if (version != null && !version.matches("^[0-9A-Za-z._\\-+]+$")) {
            throw new IllegalArgumentException("Invalid dependency version format: " + version);
        }
    }

    private Optional<ResolvedDependencyDto> fetchFromExternalSource(MissingDependencyDto dependency) {
        String primaryQuery = firstNonBlank(dependency.name(), dependency.id());
        if (primaryQuery == null) {
            throw new IllegalArgumentException("Dependency must include a valid id or name.");
        }

        Optional<ResolvedDependencyDto> directFallback = resolveDirectProjectFallback(dependency);
        if (directFallback.isPresent()) {
            return directFallback;
        }

        NoSuchElementException lastLookupError = null;
        for (String query : buildLookupQueries(dependency)) {
            try {
                return Optional.of(resolveDependencyFromQuery(dependency, query));
            } catch (NoSuchElementException ex) {
                lastLookupError = ex;
            } catch (IllegalArgumentException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new RuntimeException("Failed to resolve dependency: " + primaryQuery, ex);
            }
        }

        throw lastLookupError != null
                ? lastLookupError
                : new NoSuchElementException("Dependency not found: " + primaryQuery);
    }

    public List<MissingDependencyDto> fetchMissingDependencies(String slug, String loader, String mcVersion) {
        JsonNode deps = modrinthServiceWrapper.getProjectDependencies(slug);

        Map<String, String> seen = new LinkedHashMap<>();
        for (JsonNode dep : deps) {
            String type = dep.path("dependency_type").asText("");
            if (!"required".equals(type)) continue;
            String projectId = dep.path("project_id").asText("");
            if (projectId.isBlank()) continue;
            seen.putIfAbsent(projectId, dep.path("version_id").asText(null));
        }

        List<MissingDependencyDto> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : seen.entrySet()) {
            String projectId = entry.getKey();
            String versionId = entry.getValue();
            String name = projectId;
            try {
                JsonNode project = modrinthServiceWrapper.getProjectById(projectId);
                name = project.path("title").asText(projectId);
            } catch (RuntimeException ignored) {
            }
            result.add(new MissingDependencyDto(projectId, name, versionId, loader, mcVersion));
        }
        return result;
    }

    private String buildCacheKey(MissingDependencyDto dependency) {
        return String.join("|",
                defaultString(dependency.id()),
                defaultString(dependency.requiredVersion()),
                defaultString(dependency.loader()),
                defaultString(dependency.mcVersion()));
    }

    private int scoreCandidate(MissingDependencyDto dependency, JsonNode candidate) {
        String dependencyId = defaultString(dependency.id()).toLowerCase(Locale.ROOT);
        String dependencyName = defaultString(dependency.name()).toLowerCase(Locale.ROOT);
        String slug = candidate.path("slug").asText("").toLowerCase(Locale.ROOT);
        String title = candidate.path("title").asText("").toLowerCase(Locale.ROOT);

        if (!dependencyId.isBlank() && dependencyId.equals(slug)) {
            return 0;
        }
        if (!dependencyName.isBlank() && dependencyName.equals(title)) {
            return 1;
        }
        if (!dependencyId.isBlank() && title.contains(dependencyId)) {
            return 2;
        }
        return 3;
    }

    private Optional<ResolvedDependencyDto> resolveDirectProjectFallback(MissingDependencyDto dependency) {
        ProjectFallback projectFallback = mapDependencyToProjectFallback(dependency.id());
        if (projectFallback == null) {
            return Optional.empty();
        }

        String preferred = "https://modrinth.com/mod/" + projectFallback.projectSlug();
        return Optional.of(new ResolvedDependencyDto(
                dependency.id(),
                projectFallback.displayName(),
                List.of(preferred),
                preferred
        ));
    }

    private List<String> buildLookupQueries(MissingDependencyDto dependency) {
        LinkedHashSet<String> queries = new LinkedHashSet<>();

        String primaryQuery = firstNonBlank(dependency.name(), dependency.id());
        if (primaryQuery != null) {
            queries.add(primaryQuery);
        }

        ProjectFallback aliasedProject = mapDependencyToProjectFallback(dependency.id());
        String aliasedProjectQuery = aliasedProject != null ? aliasedProject.projectSlug() : null;
        if (aliasedProjectQuery != null) {
            queries.add(aliasedProjectQuery);
        }

        return List.copyOf(queries);
    }

    private ProjectFallback mapDependencyToProjectFallback(String dependencyId) {
        if (dependencyId == null || dependencyId.isBlank()) {
            return null;
        }

        String normalized = dependencyId.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("fabric-") && normalized.contains("-api-")) {
            return new ProjectFallback("fabric-api", "Fabric API");
        }

        return null;
    }

    private ResolvedDependencyDto resolveDependencyFromQuery(MissingDependencyDto dependency, String query) {
        JsonNode searchResults = modrinthServiceWrapper.searchProject(query, 5);
        JsonNode hits = searchResults.path("hits");
        if (!hits.isArray() || hits.isEmpty()) {
            throw new NoSuchElementException("Dependency not found: " + query);
        }

        List<JsonNode> candidates = new ArrayList<>();
        hits.forEach(candidates::add);
        candidates.sort(Comparator.comparingInt(hit -> scoreCandidateForQuery(dependency, query, hit)));

        JsonNode preferredCandidate = candidates.get(0);
        String candidateSlug = preferredCandidate.path("slug").asText("").toLowerCase(Locale.ROOT);
        String candidateTitle = preferredCandidate.path("title").asText("").toLowerCase(Locale.ROOT);
        String normalizedQuery = query.toLowerCase(Locale.ROOT);

        boolean matchesDependency = candidateSlug.equals(normalizedQuery)
                || candidateTitle.equals(normalizedQuery)
                || candidateTitle.contains(normalizedQuery)
                || candidateSlug.contains(normalizedQuery);

        if (!matchesDependency) {
            throw new NoSuchElementException("Dependency not found: " + query);
        }

        LinkedHashSet<String> links = new LinkedHashSet<>();
        for (JsonNode hit : candidates) {
            String slug = hit.path("slug").asText("");
            if (!slug.isBlank()) {
                links.add("https://modrinth.com/mod/" + slug);
            }
            if (links.size() == 3) {
                break;
            }
        }

        if (links.isEmpty()) {
            throw new NoSuchElementException("Dependency not found: " + query);
        }

        String preferredName = resolveDisplayName(dependency, preferredCandidate);
        String preferred = links.iterator().next();

        return new ResolvedDependencyDto(
                dependency.id(),
                preferredName,
                List.copyOf(links),
                preferred
        );
    }

    private int scoreCandidateForQuery(MissingDependencyDto dependency, String query, JsonNode candidate) {
        int dependencyScore = scoreCandidate(dependency, candidate);
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        String slug = candidate.path("slug").asText("").toLowerCase(Locale.ROOT);
        String title = candidate.path("title").asText("").toLowerCase(Locale.ROOT);

        if (slug.equals(normalizedQuery)) {
            return -2;
        }
        if (title.equals(normalizedQuery)) {
            return -1;
        }

        return dependencyScore;
    }

    private String resolveDisplayName(MissingDependencyDto dependency, JsonNode preferredCandidate) {
        String dependencyName = dependency.name();
        String candidateTitle = preferredCandidate.path("title").asText(null);

        if (dependencyName == null || dependencyName.isBlank()) {
            return firstNonBlank(candidateTitle, dependency.id());
        }

        if (dependency.id() != null && dependencyName.equalsIgnoreCase(dependency.id())) {
            return firstNonBlank(candidateTitle, dependencyName, dependency.id());
        }

        return dependencyName;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private record ProjectFallback(String projectSlug, String displayName) {
    }

    @Scheduled(fixedRateString = "${cache.dependency.cleanup-interval-ms:60000}")
    public void evictExpiredCacheEntries() {
        cache.evictExpired();
    }
}