package com.inso.MinecraftProject.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyLookupCache<T> {

    private static final Logger logger = LoggerFactory.getLogger(DependencyLookupCache.class);
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
            logger.debug("CACHE MISS: {}", key);
            return Optional.empty();
        }

        if (System.currentTimeMillis() > entry.expiresAt) {
            cache.remove(key);
            logger.debug("CACHE EXPIRED: {}", key);
            return Optional.empty();
        }

        logger.debug("CACHE HIT: {}", key);
        return Optional.of(entry.value);
    }

    public void put(String key, T value) {
        if (value == null) {
            logger.debug("CACHE SKIP: {} (null value)", key);
            return;
        }
        long expiresAt = System.currentTimeMillis() + ttl.toMillis();
        cache.put(key, new CacheEntry<>(value, expiresAt));
        logger.debug("CACHE PUT: {}", key);
    }

    public void invalidate(String key) {
        CacheEntry<T> removed = cache.remove(key);
        if (removed != null) {
            logger.debug("CACHE INVALIDATE: {}", key);
        }
    }

    public void clear() {
        int size = cache.size();
        cache.clear();
        logger.debug("CACHE CLEAR: removed {} entries", size);
    }

    public void evictExpired() {
        long now = System.currentTimeMillis();
        int evicted = 0;
        for (String key : cache.keySet()) {
            CacheEntry<T> entry = cache.get(key);
            if (entry != null && now > entry.expiresAt) {
                cache.remove(key);
                evicted++;
            }
        }
        if (evicted > 0) {
            logger.debug("CACHE EVICT: removed {} expired entries", evicted);
        }
    }

    public int size() {
        return cache.size();
    }

    public void evictExpired(){
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> now > entry.getValue().expiresAt);
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