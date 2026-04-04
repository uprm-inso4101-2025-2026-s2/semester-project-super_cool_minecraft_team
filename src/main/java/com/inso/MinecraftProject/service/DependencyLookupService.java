package com.inso.MinecraftProject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.inso.MinecraftProject.dto.MissingDependencyDto;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import org.springframework.stereotype.Service;

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

    private final DependencyLookupCache<Optional<ResolvedDependencyDto>> cache;
    private final ModrinthServiceWrapper modrinthServiceWrapper;

    public DependencyLookupService(ModrinthServiceWrapper modrinthServiceWrapper) {
        this.cache = new DependencyLookupCache<>();
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

        return cache.get(key).orElseGet(() -> {
            Optional<ResolvedDependencyDto> result = fetchFromExternalSource(dependency);
            cache.put(key, result);
            return result;
        });
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
        String query = firstNonBlank(dependency.name(), dependency.id());
        if (query == null) {
            throw new IllegalArgumentException("Dependency must include a valid id or name.");
        }

        try {
            JsonNode searchResults = modrinthServiceWrapper.searchProject(query, 5);
            JsonNode hits = searchResults.path("hits");
            if (!hits.isArray() || hits.isEmpty()) {
                throw new NoSuchElementException("Dependency not found: " + query);
            }

            List<JsonNode> candidates = new ArrayList<>();
            hits.forEach(candidates::add);
            candidates.sort(Comparator.comparingInt(hit -> scoreCandidate(dependency, hit)));

            JsonNode preferredCandidate = candidates.get(0);

            String candidateSlug = preferredCandidate.path("slug").asText("").toLowerCase(Locale.ROOT);
            String candidateTitle = preferredCandidate.path("title").asText("").toLowerCase(Locale.ROOT);

            String dependencyId = defaultString(dependency.id()).toLowerCase(Locale.ROOT);
            String dependencyName = defaultString(dependency.name()).toLowerCase(Locale.ROOT);

            boolean matchesId = !dependencyId.isBlank() &&
                    (candidateSlug.equals(dependencyId) || candidateTitle.contains(dependencyId));

            boolean matchesName = !dependencyName.isBlank() &&
                    (candidateTitle.equals(dependencyName) || candidateSlug.contains(dependencyName));

            if (!matchesId && !matchesName) {
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

            String preferredName = firstNonBlank(
                    dependency.name(),
                    preferredCandidate.path("title").asText(null),
                    dependency.id()
            );
            String preferred = links.iterator().next();

            return Optional.of(new ResolvedDependencyDto(
                    dependency.id(),
                    preferredName,
                    List.copyOf(links),
                    preferred
            ));
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to resolve dependency: " + query, ex);
        }
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
}