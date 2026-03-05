package com.inso.MinecraftProject.service;

import java.util.List;

public class DependencyLookupService {

    private final DependencyLookupCache<List<String>> cache;

    public DependencyLookupService() {
        this.cache = new DependencyLookupCache<>();
    }

    public List<String> resolveDependency(String dependency, String version, String loader) {

        String key = dependency + "|" + version + "|" + loader;

        return cache.get(key).orElseGet(() -> {

            // TODO: Replace with real Modrinth API call
            List<String> result = fetchFromExternalSource(dependency, version, loader);

            if (result != null) {
                cache.put(key, result);
            }

            return result;
        });
    }

    private List<String> fetchFromExternalSource(String dependency, String version, String loader) {
        throw new UnsupportedOperationException("External dependency lookup not implemented yet.");
    }
}