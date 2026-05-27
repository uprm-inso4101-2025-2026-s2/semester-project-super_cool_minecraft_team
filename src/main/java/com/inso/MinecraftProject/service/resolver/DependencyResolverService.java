package com.inso.MinecraftProject.service.resolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import com.inso.MinecraftProject.dto.ValidationResponse;
import com.inso.MinecraftProject.entity.Mod;
import com.inso.MinecraftProject.service.cache.DependencyCacheService;
import com.inso.MinecraftProject.service.client.DependencyApiClient;
import com.inso.MinecraftProject.service.parser.DependencyParser;
import com.inso.MinecraftProject.service.ModRepository;

/**
 * Coordinates the dependency validation flow.
 *
 * Responsibilities here are limited to:
 *   - iterating over a mod's declared dependencies
 *   - delegating missing-dependency resolution to the client / parser / cache layers
 *   - running circular-dependency detection
 *   - assembling the final {@link ValidationResponse}
 *
 * All HTTP, JSON-parsing, and cache concerns are handled by the injected services.
 *
 * Flow:  validate()
 *           └─> resolveExternalMetadata()
 *                   ├─> DependencyCacheService.get()          (cache hit → return early)
 *                   ├─> DependencyApiClient.fetchVersionMetadata()
 *                   ├─> DependencyParser.extractPreferredUrl()
 *                   ├─> DependencyParser.buildDto()
 *                   └─> DependencyCacheService.put()
 */
@Service
public class DependencyResolverService {

    /**
     * Fallback interface declaration to satisfy compilation when the
     * concrete ModRepository type is not available at compile-time.
     * The real repository bean should match this shape; leaving this
     * here avoids compile errors in constrained editing environments.
     */
    

    private final ModRepository            modRepository;
    private final DependencyApiClient      apiClient;
    private final DependencyParser         parser;
    private final DependencyCacheService   cacheService;

    public DependencyResolverService(
            ModRepository          modRepository,
            DependencyApiClient    apiClient,
            DependencyParser       parser,
            DependencyCacheService cacheService) {

        this.modRepository = modRepository;
        this.apiClient     = apiClient;
        this.parser        = parser;
        this.cacheService  = cacheService;
    }

    /**
     * Validates a mod's dependency tree.
     *
     * @param mod the root mod to validate
     * @return a {@link ValidationResponse} containing missing deps, circular deps,
     *         and resolved metadata for any missing mandatory dependencies
     */
    public ValidationResponse validate(Mod mod) {
        List<String>               missingDependencies  = new ArrayList<>();
        List<String>               circularDependencies = new ArrayList<>();
        List<ResolvedDependencyDto> resolvedDependencies = new ArrayList<>();

        for (Mod dep : mod.getDepends()) {
            Optional<Mod> dependencyMod = modRepository.findById(dep.getId());

            if (dependencyMod.isEmpty() && dep.isMandatory()) {
                missingDependencies.add(dep.getId());

                ResolvedDependencyDto resolved = resolveExternalMetadata(dep, mod);
                resolvedDependencies.add(resolved);
            }
        }

        detectCircularDependency(mod, new HashSet<>(), circularDependencies);

        return new ValidationResponse(missingDependencies, circularDependencies, resolvedDependencies);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves external metadata for a single missing dependency.
     * Returns a cached result immediately if one exists; otherwise fetches,
     * parses, caches, and returns a freshly built DTO.
     */
    private ResolvedDependencyDto resolveExternalMetadata(Mod dep, Mod parentContext) {
        ResolvedDependencyDto cached = cacheService.get(dep.getId());
        if (cached != null) {
            return cached;
        }

        String loader    = "fabric";
        String mcVersion = (parentContext.getVersion() != null)
                           ? parentContext.getVersion()
                           : "1.20.1";

        String jsonResponse  = apiClient.fetchVersionMetadata(dep.getId(), loader, mcVersion);
        String preferredUrl  = parser.extractPreferredUrl(jsonResponse, dep.getId());

        System.out.println("[DependencyResolverService] Resolved URL for '"
                           + dep.getId() + "': " + preferredUrl);

        ResolvedDependencyDto dto = parser.buildDto(dep, preferredUrl);
        cacheService.put(dep.getId(), dto);
        return dto;
    }

    /**
     * Recursively detects circular dependencies using a DFS with backtracking.
     * A mod ID is added to {@code circular} if it is encountered while already
     * present in the current traversal path.
     */
    private void detectCircularDependency(Mod mod, Set<String> visited, List<String> circular) {
        if (visited.contains(mod.getId())) {
            circular.add(mod.getId());
            return;
        }

        visited.add(mod.getId());

        for (Mod dep : mod.getDepends()) {
            modRepository.findById(dep.getId())
                    .ifPresent(m -> detectCircularDependency(m, visited, circular));
        }

        visited.remove(mod.getId());
    }
}