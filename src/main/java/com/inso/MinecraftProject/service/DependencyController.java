package com.inso.MinecraftProject.service;

import com.inso.MinecraftProject.dto.DTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
public class DependencyController {

    @GetMapping("/api/dependencies/missing")
    public DTO getMissingDependencies() {

        DTO response = DTO.builder()
            .missingDependencies(Collections.emptyList())
            .resolvedDependencies(Collections.emptyList())
            .build();

        return response;
    }
}