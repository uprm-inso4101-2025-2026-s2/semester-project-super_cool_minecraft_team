package com.inso.MinecraftProject.service;

import java.io.IOException;
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

    /**
     * Main function that processes the modpack zip and returns the completed DTO.
     */
    public DTO processModpackZip(ZipFile modpackZip) throws IOException {
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
            throw new IOException(
                    "No Fabric mods found. Use a .zip whose root contains mods/*.jar with fabric.mod.json inside each jar."
            );
        }

        detectMissingDependencies(discoveredMods, missingDependencies);

        return DTO.builder()
                .mods(new ArrayList<>(discoveredMods.values()))
                .edges(edges)
                .missingDependencies(missingDependencies)
                .resolvedDependencies(new ArrayList<>(resolvedDependencies))
                .build();
    }

    /**
     * Function 1:
     * Extract the main zip file into a temporary directory.
     */
    public Path extractZipFile(ZipFile zipFile) throws IOException {
        Path tempDir = Files.createTempDirectory("modpack-extracted-");

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            Path outputPath = tempDir.resolve(entry.getName()).normalize();

            if (!outputPath.startsWith(tempDir)) {
                throw new IOException("Invalid zip entry path: " + entry.getName());
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
    }

    /**
     * Function 2:
     * Iterate through extracted modpack files, find mod archives (.jar or .zip),
     * extract each one if needed, and return the list of archive paths.
     */
    public List<Path> iterateThroughEachModAndExtract(Path modpackExtractedDir) throws IOException {
        List<Path> modArchives = new ArrayList<>();

        boolean hasModsFolder = Files.isDirectory(modpackExtractedDir.resolve("mods"));

        try (var paths = Files.walk(modpackExtractedDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> isModArchiveCandidate(path, modpackExtractedDir, hasModsFolder))
                    .forEach(modArchives::add);
        }

        return modArchives;
    }

    /**
     * Function 3:
     * Extract a mod archive, find fabric.mod.json, parse it, and add
     * dependencies/relationships into the DTO structures.
     */
    public void processSingleModArchive(
            Path modArchivePath,
            Map<String, Mod> discoveredMods,
            List<Edge> edges,
            Set<String> resolvedDependencies
    ) throws IOException {

        JsonNode root = readFabricModJsonFromArchive(modArchivePath);
        if (root == null) {
            return;
        }

        String modId = getTextOrDefault(root, "id", stripExtension(modArchivePath.getFileName().toString()));
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
    }

    /**
     * Reads {@code fabric.mod.json} directly from a mod jar/zip without full extraction.
     * Skips invalid archives (e.g. macOS {@code ._*} resource-fork files).
     */
    private JsonNode readFabricModJsonFromArchive(Path archivePath) throws IOException {
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
            if (entry == null) {
                return null;
            }
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                return objectMapper.readTree(inputStream);
            }
        } catch (ZipException ex) {
            return null;
        }
    }

    private boolean isModArchiveCandidate(Path path, Path modpackRoot, boolean hasModsFolder) {
        String fileName = path.getFileName().toString();
        String lowerName = fileName.toLowerCase();
        if (!lowerName.endsWith(".jar") && !lowerName.endsWith(".zip")) {
            return false;
        }
        if (fileName.startsWith(".")) {
            return false;
        }

        String relative = modpackRoot.relativize(path).toString().replace('\\', '/');
        if (relative.contains("__MACOSX") || relative.contains("/._")) {
            return false;
        }
        if (hasModsFolder && !relative.startsWith("mods/")) {
            return false;
        }
        try {
            return Files.size(path) > 0;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Parse one relationship group like depends, breaks, suggests, recommends, conflicts.
     */
    private void parseRelationship(
            JsonNode root,
            String fieldName,
            Mod sourceMod,
            Map<String, Mod> discoveredMods,
            List<Edge> edges,
            Set<String> resolvedDependencies
    ) {
        JsonNode relationshipNode = root.get(fieldName);

        if (relationshipNode == null || relationshipNode.isNull()) {
            return;
        }

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
                String dependencyId = item.isTextual() ? item.asText() : null;
                if (dependencyId == null || dependencyId.isBlank()) {
                    continue;
                }

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

    /**
     * Add dependency/conflict/etc to the correct list in the source mod.
     */
    private void addRelationshipToSource(Mod sourceMod, Mod targetMod, String relationshipType) {
        switch (relationshipType) {
            case "depends" -> sourceMod.getDepends().add(targetMod);
            case "breaks" -> sourceMod.getBreaks().add(targetMod);
            case "suggests" -> sourceMod.getSuggests().add(targetMod);
            case "recommends" -> sourceMod.getRecommends().add(targetMod);
            case "conflicts" -> sourceMod.getConflicts().add(targetMod);
            default -> {
            }
        }
    }

    /**
     * Detect dependencies referenced by mods but not actually present as real mod archives.
     * This treats placeholder-only nodes as missing.
     */
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

    private void checkAndAddMissing(List<Mod> modsToCheck, Set<String> realMods, List<Mod> missingDependencies) {
        for (Mod mod : modsToCheck) {
            if (!realMods.contains(mod.getId())) {
                boolean alreadyExists = missingDependencies.stream()
                        .anyMatch(existing -> existing.getId().equals(mod.getId()));

                if (!alreadyExists) {
                    missingDependencies.add(mod);
                }
            }
        }
    }

    private String getTextOrDefault(JsonNode root, String fieldName, String defaultValue) {
        JsonNode node = root.get(fieldName);
        return (node != null && !node.isNull()) ? node.asText() : defaultValue;
    }

    private String stripExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? filename : filename.substring(0, lastDot);
    }
}