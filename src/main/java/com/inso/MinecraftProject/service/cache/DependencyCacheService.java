package com.inso.MinecraftProject.service.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.inso.MinecraftProject.dto.ResolvedDependencyDto;

/**
 * Responsible for caching resolved dependency metadata in memory.
 * Prevents redundant Modrinth API calls for the same mod ID within the TTL window.
 *
 * Cache key  : mod ID (as returned by {@link com.inso.MinecraftProject.entity.Mod#getId()})
 * TTL        : 30 minutes (configurable via {@link #TTL})
 * Eviction   : lazy — stale entries are removed on the next read for that key
 */
@Service
public class DependencyCacheService {

    private static final Duration TTL = Duration.ofMinutes(30);

    private record CacheEntry(ResolvedDependencyDto dto, Instant expiresAt) {}

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Returns the cached {@link ResolvedDependencyDto} for the given mod ID
     * if a non-expired entry exists, otherwise returns {@code null}.
     *
     * @param modId the dependency mod ID to look up
     * @return the cached DTO, or {@code null} on a miss or expiry
     */
    public ResolvedDependencyDto get(String modId) {
        CacheEntry entry = cache.get(modId);

        if (entry == null) {
            return null;
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            cache.remove(modId);
            System.out.println("[DependencyCacheService] Expired entry evicted for mod: " + modId);
            return null;
        }

        System.out.println("[DependencyCacheService] Cache hit for mod: " + modId);
        return entry.dto();
    }

    /**
     * Stores a resolved DTO in the cache under the given mod ID.
     * Any existing entry for the same key is overwritten.
     *
     * @param modId the dependency mod ID to use as the cache key
     * @param dto   the resolved DTO to cache
     */
    public void put(String modId, ResolvedDependencyDto dto) {
        cache.put(modId, new CacheEntry(dto, Instant.now().plus(TTL)));
        System.out.println("[DependencyCacheService] Cached resolved metadata for mod: " + modId);
    }

    /**
     * Removes the cached entry for a specific mod ID.
     *
     * @param modId the mod ID whose cache entry should be invalidated
     */
    public void invalidate(String modId) {
        cache.remove(modId);
        System.out.println("[DependencyCacheService] Cache invalidated for mod: " + modId);
    }

    /**
     * Clears all entries from the cache.
     * Useful for testing or when a full modpack re-analysis is requested.
     */
    public void invalidateAll() {
        cache.clear();
        System.out.println("[DependencyCacheService] Full cache cleared.");
    }

    /**
     * Returns the number of entries currently held in the cache,
     * including potentially expired entries not yet evicted.
     */
    public int size() {
        return cache.size();
    }
}
