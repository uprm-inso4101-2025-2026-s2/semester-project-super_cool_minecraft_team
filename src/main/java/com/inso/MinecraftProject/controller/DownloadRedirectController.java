package com.inso.MinecraftProject.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inso.MinecraftProject.exception.ApiException;

@RestController
@RequestMapping("/dependencies")
public class DownloadRedirectController {

    private static final Map<String, String> resolvedDependencies = Map.of(
            "fabric-api", "https://cdn.modrinth.com/data/fabric-api/fabric-api.jar",
            "example", "https://cdn.modrinth.com/data/example/example.jar"
    );

    @GetMapping("/{id}/download")
public ResponseEntity<Void> redirectToDownload(@PathVariable String id) {

    if (!resolvedDependencies.containsKey(id)) {
        throw new ApiException("Dependency not found", HttpStatus.NOT_FOUND);
    }

    if (id.equals("incompatible")) {
        throw new ApiException("Invalid version for dependency", HttpStatus.BAD_REQUEST);
    }

    try {
        String downloadUrl = resolvedDependencies.get(id);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", downloadUrl)
                .build();

    } catch (Exception e) {
        throw new ApiException("Failed to redirect", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
}
