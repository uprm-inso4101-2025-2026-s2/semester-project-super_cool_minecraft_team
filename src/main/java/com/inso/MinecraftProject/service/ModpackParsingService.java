package com.inso.MinecraftProject.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.MinecraftProject.dto.DTO;

@Service
public class ModpackParsingService {

    private final ZipProcessingService zipProcessingService;

    public ModpackParsingService(ZipProcessingService zipProcessingService) {
        this.zipProcessingService = zipProcessingService;
    }

    /**
     * Empty graph placeholder (no uploaded modpack).
     */
    public DTO parseModpack() {
        return DTO.builder()
                .mods(List.of())
                .edges(List.of())
                .missingDependencies(null)
                .resolvedDependencies(null)
                .build();
    }

    /**
     * Parses a Fabric modpack zip and builds the dependency graph DTO for the visualizer.
     */
    public DTO parseModpack(ZipFile modpackZip) throws IOException {
        return zipProcessingService.processModpackZip(modpackZip);
    }

    /**
     * Reads dependency metadata from a mod jar's {@code fabric.mod.json}.
     */
    public List<List<String>> ReadJson(JarFile jarFile) {
        List<String> depends = new ArrayList<>();
        List<String> breaks = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();

        try {
            Enumeration<JarEntry> entries = jarFile.entries();
            ObjectMapper mapper = new ObjectMapper();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!"fabric.mod.json".equals(entry.getName())) {
                    continue;
                }

                try (InputStream is = jarFile.getInputStream(entry);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }

                    JsonNode json = mapper.readTree(content.toString());
                    if (json.has("breaks")) {
                        json.get("breaks").fieldNames().forEachRemaining(breaks::add);
                    }
                    if (json.has("conflicts")) {
                        json.get("conflicts").fieldNames().forEachRemaining(conflicts::add);
                    }
                    if (json.has("depends")) {
                        json.get("depends").fieldNames().forEachRemaining(depends::add);
                    }
                } catch (Exception ignored) {
                    // Not all JSON files are mod metadata.
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of(depends, conflicts, breaks);
    }
}