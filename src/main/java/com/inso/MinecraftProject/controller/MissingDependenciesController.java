package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.dto.MissingDependenciesResponse;
import com.inso.MinecraftProject.dto.MissingDependencyDto;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import com.inso.MinecraftProject.entity.Mod;
import com.inso.MinecraftProject.service.DependencyLookupService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class MissingDependenciesController {

    private static final String DEFAULT_LOADER = "Fabric";
    private static final String SESSION_GRAPH_DTO = "graphDto";

    private final DependencyLookupService dependencyLookupService;

    public MissingDependenciesController(DependencyLookupService dependencyLookupService) {
        this.dependencyLookupService = dependencyLookupService;
    }

    @GetMapping("/missing-dependencies")
    public String missingDependenciesPage(Model model) {
        model.addAttribute("loader", DEFAULT_LOADER);
        return "missing-dependencies";
    }

    @GetMapping("/api/missing-dependencies")
    public ResponseEntity<?> missingDependenciesData(HttpSession session) {
        DTO dto = session.getAttribute(SESSION_GRAPH_DTO) instanceof DTO graphDto ? graphDto : null;

        if (dto == null) {
            return ResponseEntity.ok(Map.of(
                    "missingDependencies", List.of(),
                    "resolvedDependencies", List.of(),
                    "error", "No analysis result found. Run graph analysis first."
            ));
        }

        List<MissingDependencyDto> missingDependencies = normalizeMissingDependencies(dto.getMissingDependencies());
        List<ResolvedDependencyDto> resolvedDependencies = resolveMissingDependencies(missingDependencies);
        boolean analysisHasPartialResults = !resolvedDependencies.isEmpty()
                && resolvedDependencies.size() < missingDependencies.size();

        return ResponseEntity.ok(new MissingDependenciesResponse(
                missingDependencies,
                resolvedDependencies,
                analysisHasPartialResults
        ));
    }

    private List<MissingDependencyDto> normalizeMissingDependencies(List<Mod> missingDependencies) {
        if (missingDependencies == null || missingDependencies.isEmpty()) {
            return List.of();
        }

        return missingDependencies.stream()
                .filter(mod -> mod != null && mod.getId() != null && !mod.getId().isBlank())
                .map(this::toMissingDependencyDto)
                .toList();
    }

    private MissingDependencyDto toMissingDependencyDto(Mod mod) {
        return new MissingDependencyDto(
                mod.getId(),
                mod.getId(),
                normalizeVersion(mod.getVersion()),
                null,
                null
        );
    }

    private String normalizeVersion(String version) {
        if (version == null || version.isBlank() || "unknown".equalsIgnoreCase(version)) {
            return null;
        }
        return version;
    }

    private List<ResolvedDependencyDto> resolveMissingDependencies(List<MissingDependencyDto> missingDependencies) {
        return missingDependencies.stream()
                .map(this::tryResolveDependency)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ResolvedDependencyDto> tryResolveDependency(MissingDependencyDto dependency) {
        try {
            return dependencyLookupService.resolveDependency(dependency);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
