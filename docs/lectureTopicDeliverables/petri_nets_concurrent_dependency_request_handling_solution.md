# 📝 [Lecture Topic Task] Petri Nets for Concurrent Dependency Request Handling
INSO 4101 — Lecture Topic Task | Victor E. Ravelo Santana 

### 🎯 Objective
Analyze how Petri nets can model concurrent dependency lookup requests, cache synchronization, and external API coordination within the backend subsystem.

---

## Introduction
Petri nets are mathematical models used to represent concurrent systems involving conditions, transitions, shared resources, and synchronized processes. They are useful for backend systems where multiple requests operate simultaneously while competing for shared resources.

The Minecraft modpack dependency lookup subsystem involves:
- Concurrent dependency requests
- Shared cache access
- External API communication
- Dependency resolution coordination

---

## Main Petri Net Components
Petri nets use:
- Places
- Transitions
- Tokens
- Arcs

### Places
Important places include:
- Request Received
- Request Validated
- Cache Available
- Cache Miss
- API Request Pending
- API Response Received
- Dependency Resolved
- Dependency Cached
- Lookup Failed

### Transitions
Important transitions include:
- Validate Request
- Check Cache
- Send API Request
- Receive API Response
- Store Dependency in Cache
- Return Dependency
- Return Failure

### Tokens
Tokens represent active dependency requests moving through the system.

---

## Modeling Concurrent Requests
Petri nets are useful because multiple dependency requests may occur simultaneously.

Example:
- Two users request the same dependency at the same time.
- Both requests check the cache.
- The dependency is missing.
- Both requests attempt external API access.

Without synchronization:
- Duplicate API requests may occur
- Cache conflicts may happen
- Backend performance may decrease

Petri nets help visualize these concurrency problems.

---

## Backend File Coordination

### DependencyLookupService
Responsibilities:
- Receives requests
- Validates requests
- Coordinates lookup workflow
- Returns dependency results

### DependencyLookupCache
Responsibilities:
- Stores dependency information
- Returns cached entries
- Prevents unnecessary API requests
- Supports synchronization

### ModrinthServiceWrapper
Responsibilities:
- Sends requests to Modrinth
- Receives API responses
- Handles failures and timeouts

---

## Successful Concurrent Lookup Example

### Request A
1. Request received
2. Request validated
3. Cache checked
4. Cache miss detected
5. API request sent
6. Dependency resolved
7. Dependency cached
8. Result returned

### Request B
1. Request received
2. Request validated
3. Cache checked
4. Dependency found in cache
5. Cached dependency returned

---

## Failed Concurrent Lookup Example

### Request A
1. Request received
2. Cache checked
3. Cache miss detected
4. API request sent
5. API timeout occurs
6. Lookup failure returned

### Request B
1. Request received
2. Cache checked
3. Cache still empty
4. API request retried
5. Dependency resolved
6. Dependency cached

---

## Why Petri Nets Are Effective
Petri nets help developers analyze:
- Concurrency behavior
- Synchronization issues
- Shared resource coordination
- Duplicate API requests
- Cache consistency
- Failure handling
- Backend bottlenecks

They provide both visual and mathematical analysis of backend workflow behavior.

---

## Simple Textual Petri Net Example

(Request Received)
        ↓
[Validate Request]
        ↓
(Request Validated)
        ↓
[Check Cache]
      ↙      ↘
(Cache Hit)  (Cache Miss)
     ↓             ↓
[Return Data]  [Send API Request]
                     ↓
             (API Response Received)
                  ↙          ↘
        (Dependency Found)  (Lookup Failed)
                ↓
        [Store in Cache]
                ↓
        (Dependency Cached)
                ↓
         [Return Dependency]

---

## Conclusion
Petri nets provide an effective way to model concurrent dependency lookup requests, cache synchronization, and external API coordination. The interaction between `DependencyLookupService`, `DependencyLookupCache`, and `ModrinthServiceWrapper` demonstrates how Petri nets help developers analyze backend concurrency, synchronization, and system reliability.

