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
    public ResponseEntity<?> getMissingDependencies(@RequestParam(defaultValue = "ok") String mode) {
        if ("fail429".equalsIgnoreCase(mode)) {
            return ResponseEntity.status(429).body(Map.of("message", "Dependency lookup was rate limited."));
        }

        if ("fail500".equalsIgnoreCase(mode)) {
            return ResponseEntity.status(500).body(Map.of("message", "Dependency lookup failed on the server."));
        }

        if ("timeout".equalsIgnoreCase(mode)) {
            return ResponseEntity.status(504).body(Map.of("message", "Dependency lookup timed out."));
        }

        List<MissingDependencyDto> missingDependencies = buildMissingDependencies(mode);
        List<ResolvedDependencyDto> resolvedDependencies = dependencyLookupService.resolveDependencies(missingDependencies);

        if ("partial".equalsIgnoreCase(mode) && resolvedDependencies.size() > 1) {
            resolvedDependencies = resolvedDependencies.subList(0, 1);
        }

        boolean hasPartialResults = !resolvedDependencies.isEmpty()
                && resolvedDependencies.size() < missingDependencies.size();

        MissingDependenciesResponse response = new MissingDependenciesResponse(
                missingDependencies,
                resolvedDependencies,
                hasPartialResults
        );

        return ResponseEntity.ok(response);
    }

    private List<MissingDependencyDto> buildMissingDependencies(String mode) {
        if ("empty".equalsIgnoreCase(mode)) {
            return List.of();
        }

        if ("partial".equalsIgnoreCase(mode)) {
            return List.of(
                    new MissingDependencyDto("fabric-api", "Fabric API", "0.100.1", "Fabric", "1.20.1"),
                    new MissingDependencyDto("cloth-config", "Cloth Config", "11.1.136", "Fabric", "1.20.1")
            );
        }

        return List.of(
                new MissingDependencyDto("fabric-api", "Fabric API", "0.100.1", "Fabric", "1.20.1"),
                new MissingDependencyDto("modmenu", "Mod Menu", "10.0.0", "Fabric", "1.20.1")
        );
    }
}
//justfortest