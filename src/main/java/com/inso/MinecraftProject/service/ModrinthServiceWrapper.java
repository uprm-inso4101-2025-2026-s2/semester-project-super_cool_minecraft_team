package com.inso.MinecraftProject.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.inso.MinecraftProject.exception.ApiException;
import org.springframework.http.HttpStatus;
@Component
public class ModrinthServiceWrapper {
    public static final String Base_URL = "https://api.modrinth.com/v2";
    private static final String agent = "com.inso.MinecraftProject/1.0";
    private static final int timeout = 10;

    private final HttpClient client;
    private final ObjectMapper mapper;

    public ModrinthServiceWrapper() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();
        this.mapper = new ObjectMapper();
    }

    private JsonNode get(String endpoint) {
        String url = Base_URL + endpoint;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", agent)
                .timeout(Duration.ofSeconds(timeout))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ApiException("Failed to send request to Modrinth API: " + e.getMessage(), e);
        }

        switch (response.statusCode()) {
            case 200 -> {
            }
            case 401 -> throw new ApiException("Unauthorized access to Modrinth API at " + url);
            case 404 -> throw new ApiException("Resource not found at endpoint: " + url);
            case 429 -> throw new ApiException("Rate limited by Modrinth API at " + url);
            case 500 -> throw new ApiException("Server issues with Modrinth API at " + url);
            case 503 -> throw new ApiException("Service unavailable at Modrinth API at " + url);
            default -> throw new ApiException("Unexpected response code " + response.statusCode() + " from Modrinth API at " + url);
        }

        try {
            return mapper.readTree(response.body());
        } catch (Exception e) {
            throw new ApiException("Invalid response from Modrinth API", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public JsonNode searchProject(String name, int limit) {
        return get("/search?query=" + URLEncoder.encode(name, StandardCharsets.UTF_8) + "&limit=" + limit);
    }

    public JsonNode searchProject(String name) {
        return searchProject(name, 10);
    }

    public JsonNode getProjectById(String slug) {
        return get("/project/" + URLEncoder.encode(slug, StandardCharsets.UTF_8));
    }

    public JsonNode getProjectVersions(String slug) {
        return get("/project/" + URLEncoder.encode(slug, StandardCharsets.UTF_8) + "/version");
    }

    public JsonNode getVersionById(String versionId) {
        return get("/version/" + versionId);
    }

    public JsonNode getProjectDependencies(String slug) {
        JsonNode versions = getProjectVersions(slug);
        var dependencies = mapper.createArrayNode();

        for (JsonNode version : versions) {
            JsonNode deps = version.get("dependencies");
            if (deps != null && deps.isArray()) {
                for (JsonNode dep : deps) {
                    dependencies.add(dep);
                }
            }
        }

        return dependencies;
    }
}
