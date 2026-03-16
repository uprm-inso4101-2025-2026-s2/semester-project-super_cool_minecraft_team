package com.inso.MinecraftProject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DependencyController {

    @GetMapping("/api/dependencies/missing")
    public Map<String, Object> getMissingDependencies() {

        Map<String, Object> response = new HashMap<>();

        response.put("missingDependencies", new String[]{"example-mod"});
        response.put("resolvedDependencies", new String[]{"fabric-api"});

        return response;
    }
}