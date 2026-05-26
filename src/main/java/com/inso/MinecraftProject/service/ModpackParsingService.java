package com.inso.MinecraftProject.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.springframework.stereotype.Service;

import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.dto.Edge;
import com.inso.MinecraftProject.entity.Mod;

@Service
public class ModpackParsingService {

    private static final Set<String> EXCLUDED_MOD_IDS = Set.of("minecraft", "fabric-api", "fabricloader", "java");

    /**
     * MAIN FUNCTION:
     * Parses a single mod jar pack and returns DTO graph.
     */
    public DTO parseModpack(ZipFile zipFile) {
        ZipProcessingService processing = new ZipProcessingService();
        DTO dto = processing.processModpackZip(zipFile);
        return excludeRuntimeDependencies(dto);
    }

    private DTO excludeRuntimeDependencies(DTO dto) {
        List<Mod> filteredMods = dto.getMods().stream()
                .filter(mod -> !isExcludedMod(mod.getId()))
                .peek(this::removeExcludedFromRelationships)
                .collect(Collectors.toCollection(ArrayList::new));

        List<Edge> filteredEdges = dto.getEdges().stream()
                .filter(edge -> !isExcludedMod(edge.getFrom().getId())
                        && !isExcludedMod(edge.getTo().getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Mod> filteredMissing = dto.getMissingDependencies().stream()
                .filter(mod -> !isExcludedMod(mod.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<String> filteredResolved = dto.getResolvedDependencies().stream()
                .filter(id -> !isExcludedMod(id))
                .collect(Collectors.toCollection(ArrayList::new));

        dto.setMods(filteredMods);
        dto.setEdges(filteredEdges);
        dto.setMissingDependencies(filteredMissing);
        dto.setResolvedDependencies(filteredResolved);

        return dto;
    }

    private void removeExcludedFromRelationships(Mod mod) {
        mod.setDepends(filterRelationshipList(mod.getDepends()));
        mod.setBreaks(filterRelationshipList(mod.getBreaks()));
        mod.setSuggests(filterRelationshipList(mod.getSuggests()));
        mod.setRecommends(filterRelationshipList(mod.getRecommends()));
        mod.setConflicts(filterRelationshipList(mod.getConflicts()));
    }

    private List<Mod> filterRelationshipList(List<Mod> relationships) {
        if (relationships == null) {
            return new ArrayList<>();
        }
        return relationships.stream()
                .filter(rel -> !isExcludedMod(rel.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean isExcludedMod(String modId) {
        return modId != null && EXCLUDED_MOD_IDS.contains(modId.toLowerCase(Locale.ROOT));
    }
}
