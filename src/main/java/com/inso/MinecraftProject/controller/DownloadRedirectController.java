package com.inso.MinecraftProject.controller;

import com.inso.MinecraftProject.dto.MissingDependencyDto;
import com.inso.MinecraftProject.dto.ResolvedDependencyDto;
import com.inso.MinecraftProject.service.DependencyLookupService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        if (id == null || id.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Optional<ResolvedDependencyDto> resolved = dependencyLookupService.resolveDependencyById(id);
            if (resolved.isEmpty() || resolved.get().preferred() == null || resolved.get().preferred().isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, resolved.get().preferred())
                    .build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
