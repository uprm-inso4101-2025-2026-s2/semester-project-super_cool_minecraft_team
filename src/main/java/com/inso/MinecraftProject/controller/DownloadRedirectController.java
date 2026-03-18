package com.inso.MinecraftProject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dependencies")
public class DownloadRedirectController {

    private static final Map<String, String> resolvedDependencies = Map.of(
            "fabric-api", "https://cdn.modrinth.com/data/fabric-api/fabric-api.jar",
            "example", "https://cdn.modrinth.com/data/example/example.jar"
    );

    @GetMapping("/{id}/download")
    public ResponseEntity<Void> redirectToDownload(@PathVariable String id) {

        try {
            if (!resolvedDependencies.containsKey(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String downloadUrl = resolvedDependencies.get(id);

            if (id.equals("incompatible")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", downloadUrl)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
