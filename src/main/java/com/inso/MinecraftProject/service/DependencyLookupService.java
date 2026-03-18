package com.inso.MinecraftProject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.inso.MinecraftProject.dto.MissingDependencyDto;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
        String key = buildCacheKey(dependency);

        return cache.get(key).orElseGet(() -> {
            Optional<ResolvedDependencyDto> result = fetchFromExternalSource(dependency);
            cache.put(key, result);
            return result;
        });
    }

    private Optional<ResolvedDependencyDto> fetchFromExternalSource(MissingDependencyDto dependency) {
        String query = firstNonBlank(dependency.id(), dependency.name());
        if (query == null) {
            return Optional.empty();
        }

        try {
            JsonNode searchResults = modrinthServiceWrapper.searchProject(query, 5);
            JsonNode hits = searchResults.path("hits");
            if (!hits.isArray() || hits.isEmpty()) {
                return Optional.empty();
            }

            List<JsonNode> candidates = new ArrayList<>();
            hits.forEach(candidates::add);
            candidates.sort(Comparator.comparingInt(hit -> scoreCandidate(dependency, hit)));

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
                return Optional.empty();
            }

            JsonNode preferredCandidate = candidates.get(0);
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
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
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
