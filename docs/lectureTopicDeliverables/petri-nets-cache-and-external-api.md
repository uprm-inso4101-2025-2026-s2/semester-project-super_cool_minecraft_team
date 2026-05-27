# Lecture Topic Task: Petri Nets for Cache and External API Coordination

## Selected lecture topic

The lecture topic used here is **Petri Nets**.

Petri nets model activities that access shared resources.

They describe conditions, events, markings, and firing rules.

A condition is fulfilled when it is marked.

An event can fire only when its preconditions are fulfilled and its postconditions are ready to be changed.

This fits the backend dependency lookup subsystem because dependency data moves through several states.

The shared resources are the dependency cache, dependency keys, resolved dependency results, and the external Modrinth API.

## Project files involved

This relation mainly involves these files:

- `src/main/java/com/inso/MinecraftProject/service/DependencyLookupService.java`
- `src/main/java/com/inso/MinecraftProject/service/DependencyLookupCache.java`
- `src/main/java/com/inso/MinecraftProject/service/ModrinthServiceWrapper.java`
- `src/main/java/com/inso/MinecraftProject/dto/MissingDependencyDto.java`
- `src/main/java/com/inso/MinecraftProject/dto/ResolvedDependencyDto.java`

The code receives missing dependency data and tries to turn it into usable Modrinth links.

The process includes validation, cache lookup, external API search, result scoring, result construction, cache insertion, and scheduled cache cleanup.


## Petri net conditions

A condition-event network for this subsystem could use these conditions:

- `DependencyRequestReceived`
- `DependencyRequestValidated`
- `DependencyRequestRejected`
- `CacheEntryAbsent`
- `CacheEntryPresent`
- `CacheEntryExpired`
- `CacheEntryValid`
- `ExternalLookupNeeded`
- `ModrinthResponseReceived`
- `CandidateMatched`
- `CandidateRejected`
- `ResolvedDependencyConstructed`
- `ResolvedDependencyCached`
- `ResolvedDependencyReturned`
- `LookupFailed`

These conditions do not need to exist as Java classes.

They are logical states inferred from the implementation.

For example, `DependencyRequestValidated` is reached when `validateDependency` finishes without throwing an exception.

`CacheEntryValid` is reached when `cache.get(key)` returns an entry that passes `isValidCacheEntry`.

`ExternalLookupNeeded` occurs when the cache misses, expires, or contains invalid data.

## Petri net events

The main events would be:

- `validateDependency`
- `buildCacheKey`
- `getCachedResult`
- `invalidateInvalidCacheEntry`
- `fetchFromExternalSource`
- `searchModrinthProject`
- `scoreCandidate`
- `constructResolvedDependencyDto`
- `putResultInCache`
- `returnResolvedDependency`
- `throwLookupException`
- `evictExpiredCacheEntries`

The method `resolveDependency` orchestrates most of these events.

`DependencyLookupCache` supplies the resource-changing operations such as `get`, `put`, `invalidate`, `clear`, and `evictExpired`.

`ModrinthServiceWrapper` supplies the event that contacts the external service.

## Simplified net sketch

```text
[DependencyRequestReceived]
        -> (validateDependency)
[DependencyRequestValidated]
        -> (buildCacheKey)
[CacheKeyAvailable]
        -> (getCachedResult)
```

After the cache lookup, the net branches:

```text
[CacheEntryValid] -> (returnCachedResult) -> [ResolvedDependencyReturned]
[CacheEntryAbsent] -> (fetchFromExternalSource) -> [ModrinthResponseReceived]
[CacheEntryExpired] -> (invalidateEntry) -> [ExternalLookupNeeded]
```

This shows why the cache matters.

A valid cache entry prevents an external Modrinth request.

An absent or expired entry causes the system to consume the external API resource.


## Conclusion

The dependency lookup subsystem is a good fit for Petri net reasoning.

It contains marked conditions, firing events, shared resources, and meaningful state transitions.

This perspective makes cache expiration, cache hits, cache misses, external lookups, and lookup failures easier to describe and test.
