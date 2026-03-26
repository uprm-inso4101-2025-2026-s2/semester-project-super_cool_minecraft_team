package com.inso.MinecraftProject.service;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.dto.MissingDependencyDto;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import com.inso.MinecraftProject.dto.ValidationResponse;
import com.inso.MinecraftProject.entity.Mod;

@RestController
public class DependencyController {

    private final DependencyLookupService dependencyLookupService;
    private final DependencyResolverService resolverService;
    private final ModRepository modRepository;

public DependencyController(DependencyLookupService lookupService, 
                                DependencyResolverService resolverService,
                                ModRepository modRepository) {
        this.dependencyLookupService = lookupService;
        this.resolverService = resolverService;
        this.modRepository = modRepository;
    }

    @GetMapping("/api/dependencies/missing")
    public ResponseEntity<?> getMissingDependencies(@RequestParam String modId) 
    {

        // mod fetch
        Mod mod = modRepository.findById(modId).orElseThrow(() -> new RuntimeException("Mod not found: " + modId));

        // Get validation data 
        ValidationResponse validation = resolverService.validate(mod);

        // Prep the lookup for external data
        List<MissingDependencyDto> missingDtos = validation.getMissingDependencies().stream().map(id -> new MissingDependencyDto(id, id, null, null, null)).toList();

        // Resolve external links
        List<ResolvedDependencyDto> resolved = dependencyLookupService.resolveDependencies(missingDtos);

        // DTO build
        DTO response = DTO.builder()
                .mods(List.of(mod)) 
                .edges(new ArrayList<>())
                .missingDependencies(missingDtos)
                .resolvedDependencies(resolved)
                .build();

        return ResponseEntity.ok(response);
    }
}