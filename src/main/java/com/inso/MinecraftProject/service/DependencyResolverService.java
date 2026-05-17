package com.inso.MinecraftProject.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import com.inso.MinecraftProject.dto.ValidationResponse;
import com.inso.MinecraftProject.entity.Mod;

@Service
public class DependencyResolverService {

    private final ModRepository modRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public DependencyResolverService(ModRepository modRepository) {
        this.modRepository = modRepository;
        this.objectMapper = new ObjectMapper();
    }

    public ValidationResponse validate(Mod mod) {

        List<String> missingDependencies = new ArrayList<>();
        List<String> circularDependencies = new ArrayList<>();
        List<ResolvedDependencyDto> resolvedDependencies = new ArrayList<>();

        for (Mod dep : mod.getDepends()) {
            Optional<Mod> dependencyMod = modRepository.findById(dep.getId());

            if (dependencyMod.isEmpty() && dep.isMandatory()) {
                missingDependencies.add(dep.getId());
                
                ResolvedDependencyDto resolved = resolveExternalMetadata(dep, mod);
                resolvedDependencies.add(resolved);
            }
        }

        detectCircularDependency(mod, new HashSet<>(), circularDependencies);

        return new ValidationResponse(missingDependencies, circularDependencies, resolvedDependencies);
    }

    private void detectCircularDependency(Mod mod, Set<String> visited, List<String> circular) {
        if (visited.contains(mod.getId())) {
            circular.add(mod.getId());
            return;
        }

        visited.add(mod.getId());

        for (Mod dep : mod.getDepends()) {
            modRepository.findById(dep.getId())
                    .ifPresent(m -> detectCircularDependency(m, visited, circular));
        }

        visited.remove(mod.getId());
    }

    private ResolvedDependencyDto resolveExternalMetadata(Mod dep, Mod parentContext) {
        String encodedId = URLEncoder.encode(dep.getId(), StandardCharsets.UTF_8);
        List<String> links = new ArrayList<>();
        String preferredUrl = null;

        try {
            String targetLoader = "fabric";
            String targetMcVersion = (parentContext.getVersion() != null) ? parentContext.getVersion() : "1.20.1";

            String loaderFilter = URLEncoder.encode("[\"" + targetLoader + "\"]", StandardCharsets.UTF_8);
            String gameVersionFilter = URLEncoder.encode("[\"" + targetMcVersion + "\"]", StandardCharsets.UTF_8);
            
            String apiUrl = String.format(
                "https://api.modrinth.com/v2/project/%s/version?loaders=%s&game_versions=%s",
                encodedId, loaderFilter, gameVersionFilter
            );
            
            String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
            
            if (jsonResponse != null) {
                JsonNode rootArray = objectMapper.readTree(jsonResponse);
                
                if (rootArray.isArray() && !rootArray.isEmpty()) {
                    JsonNode latestCompatibleVersion = rootArray.get(0);
                    JsonNode filesNode = latestCompatibleVersion.get("files");
                    
                    if (filesNode != null && filesNode.isArray() && !filesNode.isEmpty()) {
                        JsonNode targetFile = filesNode.get(0);
                        
                        for (JsonNode file : filesNode) {
                            if (file.has("primary") && file.get("primary").asBoolean()) {
                                targetFile = file;
                                break;
                            }
                        }
                        
                        if (targetFile.has("url") && !targetFile.get("url").asText().isBlank()) {
                            preferredUrl = targetFile.get("url").asText();
                        }
                    }
                }
            }
            
            System.out.println("Resolved URL via API for " + encodedId + ": " + preferredUrl);
            
        } catch (Exception e) {
            System.err.println("API parsing error for dependency: " + dep.getId() + ". Msg: " + e.getMessage());
        }

        if (preferredUrl == null || preferredUrl.isBlank()) {
            preferredUrl = "https://modrinth.com/mod/" + encodedId;
        }

        if (preferredUrl != null && !preferredUrl.isBlank()) {
            links.add(preferredUrl);
        }

        return new ResolvedDependencyDto(dep.getId(), dep.getId(), links, preferredUrl);
    }
}