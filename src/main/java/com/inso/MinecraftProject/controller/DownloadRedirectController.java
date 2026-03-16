package com.inso.MinecraftProject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dependencies")
public class DownloadRedirectController {

    @GetMapping("/{id}/download")
    public ResponseEntity<Void> redirectToDownload(@PathVariable String id) {

        // Example dependency resolution
        if (id.equals("example")) {
            String downloadUrl = "https://cdn.modrinth.com/data/example/example.jar";

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", downloadUrl)
                    .build();
        }

        // dependency not found
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}