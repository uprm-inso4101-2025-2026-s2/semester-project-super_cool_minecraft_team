package com.inso.MinecraftProject.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.dto.Edge;
import com.inso.MinecraftProject.dto.MissingDependencyDto;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import com.inso.MinecraftProject.dto.ValidationResponse;
import com.inso.MinecraftProject.entity.Mod;

@RestController
public class DependencyController {

    private final DependencyLookupService dependencyLookupService;
    private final DependencyResolverService resolverService;
    private final ModRepository modRepository;

    public DependencyController(
            DependencyLookupService lookupService,
            DependencyResolverService resolverService,
            ModRepository modRepository
    ) {
        this.dependencyLookupService = lookupService;
        this.resolverService = resolverService;
        this.modRepository = modRepository;
    }

    @GetMapping("/api/dependencies/missing")
    public ResponseEntity<?> getMissingDependencies(@RequestParam String modId) {

        // Fetch mod from repository
        Mod mod = modRepository.findById(modId).orElse(null);

        if (mod == null) {
            return ResponseEntity.notFound().build();
        }

        // Validate dependencies
        ValidationResponse validation = resolverService.validate(mod);

        // Prepare missing dependency DTOs
        List<MissingDependencyDto> missingDtos = validation.getMissingDependencies().stream()
                .map(id -> new MissingDependencyDto(id, id, null, null, null))
                .toList();

        // Resolve external links
        List<ResolvedDependencyDto> resolved = dependencyLookupService.resolveDependencies(missingDtos);

        // Convert MissingDependencyDto -> Mod
        List<Mod> missingMods = missingDtos.stream()
                .map(dto -> Mod.builder()
                        .id(dto.id())
                        .version(dto.requiredVersion() != null ? dto.requiredVersion() : "unknown")
                        .depends(new ArrayList<>())
                        .breaks(new ArrayList<>())
                        .suggests(new ArrayList<>())
                        .recommends(new ArrayList<>())
                        .conflicts(new ArrayList<>())
                        .build())
                .toList();

        // Convert ResolvedDependencyDto -> String
        List<String> resolvedLinks = resolved.stream()
                .map(ResolvedDependencyDto::preferred)
                .toList();

        // Build edges from current mod dependencies
        List<Edge> edges = new ArrayList<>();
        if (mod.getDepends() != null) {
            for (Mod dep : mod.getDepends()) {
                edges.add(Edge.builder()
                        .from(mod)
                        .to(dep)
                        .type("depends")
                        .build());
            }
        }

        // Build DTO response
        DTO response = DTO.builder()
                .mods(List.of(mod))
                .edges(edges)
                .missingDependencies(missingMods)
                .resolvedDependencies(resolvedLinks)
                .build();

        return ResponseEntity.ok(response);
    }
}