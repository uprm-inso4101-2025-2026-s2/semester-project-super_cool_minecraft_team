package com.inso.MinecraftProject.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.MinecraftProject.dto.DTO;
import com.inso.MinecraftProject.dto.Edge;
import com.inso.MinecraftProject.entity.Mod;

@Service
public class ZipProcessingService {

    private static final String FABRIC_MOD_JSON = "fabric.mod.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DTO processModpackZip(ZipFile modpackZip) {
        try {
            Path modpackExtractedDir = extractZipFile(modpackZip);

            List<Path> modArchives = iterateThroughEachModAndExtract(modpackExtractedDir);

            Map<String, Mod> discoveredMods = new HashMap<>();
            List<Edge> edges = new ArrayList<>();
            List<Mod> missingDependencies = new ArrayList<>();
            Set<String> resolvedDependencies = new HashSet<>();

            for (Path modArchivePath : modArchives) {
                processSingleModArchive(
                        modArchivePath,
                        discoveredMods,
                        edges,
                        resolvedDependencies
                );
            }

            if (discoveredMods.isEmpty()) {
                throw new RuntimeException("No Fabric mods found in zip");
            }

            detectMissingDependencies(discoveredMods, missingDependencies);

            return DTO.builder()
                    .mods(new ArrayList<>(discoveredMods.values()))
                    .edges(edges)
                    .missingDependencies(missingDependencies)
                    .resolvedDependencies(new ArrayList<>(resolvedDependencies))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to process modpack zip", e);
        }
    }

    public Path extractZipFile(ZipFile zipFile) {
        try {
            Path tempDir = Files.createTempDirectory("modpack-extracted-");

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path outputPath = tempDir.resolve(entry.getName()).normalize();

                if (!outputPath.startsWith(tempDir)) {
                    throw new RuntimeException("Invalid zip entry path: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(outputPath);
                } else {
                    Files.createDirectories(outputPath.getParent());
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

            return tempDir;

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract zip file", e);
        }
    }

    public List<Path> iterateThroughEachModAndExtract(Path modpackExtractedDir) {
        try {
            List<Path> modArchives = new ArrayList<>();

            boolean hasModsFolder = Files.isDirectory(modpackExtractedDir.resolve("mods"));

            try (var paths = Files.walk(modpackExtractedDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> isModArchiveCandidate(path, modpackExtractedDir, hasModsFolder))
                        .forEach(modArchives::add);
            }

            return modArchives;

        } catch (Exception e) {
            throw new RuntimeException("Failed to scan mod archives", e);
        }
    }

    public void processSingleModArchive(
            Path modArchivePath,
            Map<String, Mod> discoveredMods,
            List<Edge> edges,
            Set<String> resolvedDependencies
    ) {
        try {
            JsonNode root = readFabricModJsonFromArchive(modArchivePath);
            if (root == null) return;

            String modId = getTextOrDefault(root, "id",
                    stripExtension(modArchivePath.getFileName().toString()));
            String version = getTextOrDefault(root, "version", "unknown");

            Mod currentMod = discoveredMods.computeIfAbsent(modId, id ->
                    Mod.builder()
                            .id(id)
                            .version(version)
                            .depends(new ArrayList<>())
                            .breaks(new ArrayList<>())
                            .suggests(new ArrayList<>())
                            .recommends(new ArrayList<>())
                            .conflicts(new ArrayList<>())
                            .build()
            );

            currentMod.setVersion(version);

            parseRelationship(root, "depends", currentMod, discoveredMods, edges, resolvedDependencies);
            parseRelationship(root, "breaks", currentMod, discoveredMods, edges, resolvedDependencies);
            parseRelationship(root, "suggests", currentMod, discoveredMods, edges, resolvedDependencies);
            parseRelationship(root, "recommends", currentMod, discoveredMods, edges, resolvedDependencies);
            parseRelationship(root, "conflicts", currentMod, discoveredMods, edges, resolvedDependencies);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process mod archive: " + modArchivePath, e);
        }
    }

    private JsonNode readFabricModJsonFromArchive(Path archivePath) {
        try (ZipFile zipFile = new ZipFile(archivePath.toFile())) {

            ZipEntry entry = zipFile.getEntry(FABRIC_MOD_JSON);

            if (entry == null) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry candidate = entries.nextElement();
                    if (FABRIC_MOD_JSON.equalsIgnoreCase(candidate.getName())
                            || candidate.getName().endsWith("/" + FABRIC_MOD_JSON)) {
                        entry = candidate;
                        break;
                    }
                }
            }

            if (entry == null) return null;

            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                return objectMapper.readTree(inputStream);
            }

        } catch (ZipException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read fabric.mod.json from archive: " + archivePath, e);
        }
    }

    private boolean isModArchiveCandidate(Path path, Path modpackRoot, boolean hasModsFolder) {
        try {
            String fileName = path.getFileName().toString();
            String lowerName = fileName.toLowerCase();

            if (!lowerName.endsWith(".jar") && !lowerName.endsWith(".zip")) return false;
            if (fileName.startsWith(".")) return false;

            String relative = modpackRoot.relativize(path).toString().replace('\\', '/');

            if (relative.contains("__MACOSX") || relative.contains("/._")) return false;
            if (hasModsFolder && !relative.startsWith("mods/")) return false;

            return Files.size(path) > 0;

        } catch (Exception e) {
            return false;
        }
    }

    private void parseRelationship(
            JsonNode root,
            String fieldName,
            Mod sourceMod,
            Map<String, Mod> discoveredMods,
            List<Edge> edges,
            Set<String> resolvedDependencies
    ) {
        JsonNode relationshipNode = root.get(fieldName);
        if (relationshipNode == null || relationshipNode.isNull()) return;

        if (relationshipNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = relationshipNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();

                String dependencyId = entry.getKey();
                String dependencyVersion = entry.getValue().isTextual()
                        ? entry.getValue().asText()
                        : "unknown";

                Mod targetMod = discoveredMods.computeIfAbsent(dependencyId, id ->
                        Mod.builder()
                                .id(id)
                                .version(dependencyVersion)
                                .depends(new ArrayList<>())
                                .breaks(new ArrayList<>())
                                .suggests(new ArrayList<>())
                                .recommends(new ArrayList<>())
                                .conflicts(new ArrayList<>())
                                .build()
                );

                addRelationshipToSource(sourceMod, targetMod, fieldName);

                edges.add(Edge.builder()
                        .from(sourceMod)
                        .to(targetMod)
                        .type(fieldName)
                        .build());

                resolvedDependencies.add(dependencyId);
            }

        } else if (relationshipNode.isArray()) {
            for (JsonNode item : relationshipNode) {
                String dependencyId = item.asText(null);
                if (dependencyId == null || dependencyId.isBlank()) continue;

                Mod targetMod = discoveredMods.computeIfAbsent(dependencyId, id ->
                        Mod.builder()
                                .id(id)
                                .version("unknown")
                                .depends(new ArrayList<>())
                                .breaks(new ArrayList<>())
                                .suggests(new ArrayList<>())
                                .recommends(new ArrayList<>())
                                .conflicts(new ArrayList<>())
                                .build()
                );

                addRelationshipToSource(sourceMod, targetMod, fieldName);

                edges.add(Edge.builder()
                        .from(sourceMod)
                        .to(targetMod)
                        .type(fieldName)
                        .build());

                resolvedDependencies.add(dependencyId);
            }
        }
    }

    private void addRelationshipToSource(Mod sourceMod, Mod targetMod, String type) {
        switch (type) {
            case "depends" -> sourceMod.getDepends().add(targetMod);
            case "breaks" -> sourceMod.getBreaks().add(targetMod);
            case "suggests" -> sourceMod.getSuggests().add(targetMod);
            case "recommends" -> sourceMod.getRecommends().add(targetMod);
            case "conflicts" -> sourceMod.getConflicts().add(targetMod);
        }
    }

    private void detectMissingDependencies(Map<String, Mod> discoveredMods, List<Mod> missingDependencies) {
        Set<String> realMods = new HashSet<>();

        for (Mod mod : discoveredMods.values()) {
            if (!"unknown".equals(mod.getVersion())) {
                realMods.add(mod.getId());
            }
        }

        for (Mod mod : discoveredMods.values()) {
            checkAndAddMissing(mod.getDepends(), realMods, missingDependencies);
            checkAndAddMissing(mod.getBreaks(), realMods, missingDependencies);
            checkAndAddMissing(mod.getSuggests(), realMods, missingDependencies);
            checkAndAddMissing(mod.getRecommends(), realMods, missingDependencies);
            checkAndAddMissing(mod.getConflicts(), realMods, missingDependencies);
        }
    }

    private void checkAndAddMissing(List<Mod> modsToCheck, Set<String> realMods, List<Mod> missing) {
        for (Mod mod : modsToCheck) {
            if (!realMods.contains(mod.getId())) {
                boolean exists = missing.stream()
                        .anyMatch(m -> m.getId().equals(mod.getId()));

                if (!exists) missing.add(mod);
            }
        }
    }

    private String getTextOrDefault(JsonNode root, String field, String def) {
        JsonNode node = root.get(field);
        return (node != null && !node.isNull()) ? node.asText() : def;
    }

    private String stripExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? filename : filename.substring(0, lastDot);
    }
}