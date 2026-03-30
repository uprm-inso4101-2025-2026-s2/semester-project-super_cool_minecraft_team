package com.inso.MinecraftProject.service;

import com.inso.MinecraftProject.dto.MissingDependenciesResponse;
import com.inso.MinecraftProject.dto.MissingDependencyDto;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DependencyController {

    private final DependencyLookupService dependencyLookupService;

    public DependencyController(DependencyLookupService dependencyLookupService) {
        this.dependencyLookupService = dependencyLookupService;
    }

    @GetMapping("/api/dependencies/missing")
    public ResponseEntity<?> getMissingDependencies(
            @RequestParam String slug,
            @RequestParam(required = false) String loader,
            @RequestParam(required = false) String mcVersion,
            @RequestParam(defaultValue = "ok") String mode) {

        if ("fail429".equalsIgnoreCase(mode)) {
            return ResponseEntity.status(429).body(Map.of("message", "Dependency lookup was rate limited."));
        }

        if ("fail500".equalsIgnoreCase(mode)) {
            return ResponseEntity.status(500).body(Map.of("message", "Dependency lookup failed on the server."));
        }

        if ("timeout".equalsIgnoreCase(mode)) {
            return ResponseEntity.status(504).body(Map.of("message", "Dependency lookup timed out."));
        }

        List<MissingDependencyDto> missingDependencies = dependencyLookupService.fetchMissingDependencies(slug, loader, mcVersion);
        List<ResolvedDependencyDto> resolvedDependencies = dependencyLookupService.resolveDependencies(missingDependencies);

        boolean hasPartialResults = !resolvedDependencies.isEmpty()
                && resolvedDependencies.size() < missingDependencies.size();

        MissingDependenciesResponse response = new MissingDependenciesResponse(
                missingDependencies,
                resolvedDependencies,
                hasPartialResults
        );

        return ResponseEntity.ok(response);
    }
}
