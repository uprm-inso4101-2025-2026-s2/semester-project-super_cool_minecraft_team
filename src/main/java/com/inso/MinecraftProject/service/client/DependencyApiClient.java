package com.inso.MinecraftProject.service.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Responsible for all external HTTP communication with the Modrinth API.
 * No parsing or business logic lives here — only raw response retrieval.
 */
@Component
public class DependencyApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches the raw JSON response from Modrinth for a given mod ID,
     * filtered by loader and Minecraft version.
     *
     * @param modId         the mod slug or project ID (will be URL-encoded)
     * @param loader        the mod loader (e.g. "fabric", "forge")
     * @param mcVersion     the Minecraft version (e.g. "1.20.1")
     * @return raw JSON string, or null if the request fails
     */
    public String fetchVersionMetadata(String modId, String loader, String mcVersion) {
        try {
            String encodedId      = URLEncoder.encode(modId,                          StandardCharsets.UTF_8);
            String loaderFilter   = URLEncoder.encode("[\"" + loader + "\"]",         StandardCharsets.UTF_8);
            String versionFilter  = URLEncoder.encode("[\"" + mcVersion + "\"]",      StandardCharsets.UTF_8);

            String apiUrl = String.format(
                "https://api.modrinth.com/v2/project/%s/version?loaders=%s&game_versions=%s",
                encodedId, loaderFilter, versionFilter
            );

            return restTemplate.getForObject(apiUrl, String.class);

        } catch (Exception e) {
            System.err.println("[DependencyApiClient] HTTP error for mod '" + modId + "': " + e.getMessage());
            return null;
        }
    }
}
