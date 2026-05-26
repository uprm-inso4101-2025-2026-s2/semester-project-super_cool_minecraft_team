package com.inso.MinecraftProject.service.parser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import com.inso.MinecraftProject.entity.Mod;

/**
 * Responsible for parsing raw JSON API responses into DTOs.
 * No HTTP logic lives here — only JSON traversal, URL extraction, and DTO mapping.
 */
@Component
public class DependencyParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a raw Modrinth version-list JSON response and extracts the
     * preferred download URL from the first (latest) compatible version.
     *
     * The primary file is preferred when the "primary" flag is true;
     * otherwise the first file in the array is used as a fallback.
     *
     * @param jsonResponse raw JSON string from the Modrinth API
     * @param modId        mod ID used only for fallback URL construction and logging
     * @return the direct download URL, or null if none could be extracted
     */
    public String extractPreferredUrl(String jsonResponse, String modId) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            return null;
        }

        try {
            JsonNode rootArray = objectMapper.readTree(jsonResponse);

            if (!rootArray.isArray() || rootArray.isEmpty()) {
                return null;
            }

            JsonNode latestVersion = rootArray.get(0);
            JsonNode filesNode     = latestVersion.get("files");

            if (filesNode == null || !filesNode.isArray() || filesNode.isEmpty()) {
                return null;
            }

            JsonNode targetFile = filesNode.get(0);

            for (JsonNode file : filesNode) {
                if (file.has("primary") && file.get("primary").asBoolean()) {
                    targetFile = file;
                    break;
                }
            }

            String url = targetFile.has("url") ? targetFile.get("url").asText() : null;
            return (url != null && !url.isBlank()) ? url : null;

        } catch (Exception e) {
            System.err.println("[DependencyParser] JSON parse error for mod '" + modId + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Builds a fully-populated {@link ResolvedDependencyDto} for the given
     * dependency mod.  If a direct download URL was resolved it is used;
     * otherwise a Modrinth browse link is constructed as a fallback.
     *
     * @param dep          the missing dependency mod
     * @param preferredUrl the resolved direct download URL (may be null)
     * @return a populated DTO ready to include in the validation response
     */
    public ResolvedDependencyDto buildDto(Mod dep, String preferredUrl) {
        String encodedId = URLEncoder.encode(dep.getId(), StandardCharsets.UTF_8);

        if (preferredUrl == null || preferredUrl.isBlank()) {
            preferredUrl = "https://modrinth.com/mod/" + encodedId;
        }

        List<String> links = new ArrayList<>();
        links.add(preferredUrl);

        return new ResolvedDependencyDto(dep.getId(), dep.getId(), links, preferredUrl);
    }
}