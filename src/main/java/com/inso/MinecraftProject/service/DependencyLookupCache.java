package com.inso.MinecraftProject.service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyLookupCache<T> {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private final ConcurrentHashMap<String, CacheEntry<T>> cache = new ConcurrentHashMap<>();
    private final Duration ttl;

    public DependencyLookupCache() {
        this(DEFAULT_TTL);
    }

    public DependencyLookupCache(Duration ttl) {
        this.ttl = ttl;
    }

    public Optional<T> get(String key) {
        CacheEntry<T> entry = cache.get(key);

        if (entry == null) {
            return Optional.empty();
        }

        if (System.currentTimeMillis() > entry.expiresAt) {
            cache.remove(key);
            return Optional.empty();
        }

        return Optional.of(entry.value);
    }

    public void put(String key, T value) {
        long expiresAt = System.currentTimeMillis() + ttl.toMillis();
        cache.put(key, new CacheEntry<>(value, expiresAt));
    }

    private static class CacheEntry<T> {
        T value;
        long expiresAt;

        CacheEntry(T value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}