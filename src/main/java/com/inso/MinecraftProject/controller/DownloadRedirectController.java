package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.dto.MissingDependencyDto;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import com.inso.MinecraftProject.service.DependencyLookupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/dependencies")
public class DownloadRedirectController {

    private final DependencyLookupService dependencyLookupService;

    public DownloadRedirectController(DependencyLookupService dependencyLookupService) {
        this.dependencyLookupService = dependencyLookupService;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Void> redirectToDownload(@PathVariable String id) {
        try {
            MissingDependencyDto dependency = new MissingDependencyDto(id, null, null, null, null);
            Optional<ResolvedDependencyDto> resolved = dependencyLookupService.resolveDependency(dependency);

            if (resolved.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String downloadUrl = resolved.get().preferred();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", downloadUrl)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
