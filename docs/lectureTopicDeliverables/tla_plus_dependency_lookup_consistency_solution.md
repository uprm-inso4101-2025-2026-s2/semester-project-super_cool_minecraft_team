# 📝 [Lecture Topic Task] TLA+ for Dependency Lookup Consistency

### 🎯 Objective
Analyze how TLA+ can formally model and verify the correctness of the dependency lookup subsystem, especially regarding cache consistency, external API requests, and dependency resolution states.

---

## Introduction
TLA+ is a formal specification language commonly used to model concurrent and distributed systems. It allows developers to describe system states, transitions, and correctness properties mathematically. In the Minecraft modpack dependency lookup subsystem, TLA+ can help verify whether dependency requests always move through valid states without corrupting cache data or creating inconsistent results.

The backend dependency lookup system relies on multiple components working together:
- `DependencyLookupService`
- `DependencyLookupCache`
- `ModrinthServiceWrapper`

These components coordinate validation, cache lookup, external API communication, dependency resolution, and cache storage.

---

## Main System States
The dependency lookup process can be modeled as a sequence of system states:

1. Request Initialized
2. Request Validated
3. Cache Checked
4. Dependency Found in Cache
5. Dependency Requested from Modrinth
6. Dependency Successfully Resolved
7. Dependency Cached
8. Lookup Failure

---

## TLA+ State Transition Modeling
The dependency lookup flow can be modeled as:

RequestInitialized → RequestValidated → CacheChecked

### Successful Cached Lookup
RequestInitialized → RequestValidated → CacheChecked → DependencyFoundInCache → DependencyResolved

### Successful API Lookup
RequestInitialized → RequestValidated → CacheChecked → APIRequestSent → DependencyResolved → DependencyCached

### Failed Lookup Path
RequestInitialized → RequestValidated → CacheChecked → APIRequestSent → LookupFailure

---

## Backend File Responsibilities

### DependencyLookupService
- Receives dependency requests
- Validates request data
- Coordinates lookup operations
- Returns dependency results

### DependencyLookupCache
- Stores resolved dependencies
- Returns cached entries
- Prevents unnecessary API requests

### ModrinthServiceWrapper
- Sends API requests
- Receives API responses
- Handles failures and timeouts

---

## Safety Properties
### Example Safety Property
“A dependency is only cached after a successful API response.”

Another important property:
“A failed lookup must never overwrite valid cached data.”

---

## Liveness Properties
### Example Liveness Property
“Every valid dependency request eventually reaches either a resolved state or a failed state.”

Another liveness property:
“If the dependency exists and the API is reachable, the system eventually resolves the dependency.”

---

## Why TLA+ Is Useful
TLA+ helps developers reason about:
- Cache correctness
- Request synchronization
- Failure recovery
- Invalid state transitions
- Data consistency under concurrency

Formal verification reduces the chance of hidden backend logic errors.

---

## Simple State Transition Example

### Successful API Lookup
1. Request received
2. Request validated
3. Cache checked
4. Cache miss detected
5. API request sent
6. Dependency resolved
7. Dependency cached
8. Result returned

### Failed Lookup
1. Request received
2. Request validated
3. Cache checked
4. Cache miss detected
5. API request sent
6. API timeout occurs
7. Lookup failure returned
8. Cache remains unchanged

---

## Conclusion
TLA+ provides a strong method for modeling and verifying the dependency lookup subsystem. By representing the backend as states and transitions, developers can better analyze cache consistency, API coordination, and failure handling within the system.

