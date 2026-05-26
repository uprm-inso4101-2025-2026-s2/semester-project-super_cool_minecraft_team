# Design Patterns in ModrinthServiceWrapper Service

## Introduction

This document presents a design patterns analysis for the `ModrinthServiceWrapper` service in the Minecraft Dependency Visualizer project. The analysis identifies and explains how design patterns improve code maintainability and reduce complexity.

## Identified Design Patterns

### Pattern 1: Facade Pattern (Primary)

#### Description

The **Facade Pattern** provides a simplified interface to complex subsystems, hiding internal complexity from clients.

#### Evidence in ModrinthServiceWrapper

The wrapper encapsulates all HTTP communication.

#### Example

```java
public JsonNode getProjectById(String slug) {
    return get("/project/" + URLEncoder.encode(slug, StandardCharsets.UTF_8));
}

public JsonNode searchProject(String name, int limit) {
    return get("/search?query=" + URLEncoder.encode(name, StandardCharsets.UTF_8) + "&limit=" + limit);
}
```

Without Facade, every service would need:

- `HttpClient` initialization with timeout configuration
- Request building with proper headers
- Status code handling for responses like `200`, `404`, `429`, `500`, and `503`
- JSON parsing and error handling

With Facade:

```java
JsonNode project = modrinthServiceWrapper.getProjectById("sodium");
```

#### Problems Solved

- Eliminates code duplication across services
- Centralizes error handling because failures produce `ApiException`
- Creates a single point of change if the Modrinth API evolves
- Improves testability because services can mock one wrapper instead of low-level HTTP components

---

### Pattern 2: Adapter Pattern (Secondary)

#### Description

The **Adapter Pattern** converts external interfaces into interfaces our application expects.

#### Evidence in ModrinthServiceWrapper

It adapts Modrinth's REST API into semantic Java methods.

#### Example

```java
public JsonNode getProjectDependencies(String slug) {
    JsonNode versions = getProjectVersions(slug);
    var dependencies = mapper.createArrayNode();

    for (JsonNode version : versions) {
        JsonNode deps = version.get("dependencies");
        if (deps != null && deps.isArray()) {
            for (JsonNode dep : deps) {
                dependencies.add(dep);
            }
        }
    }

    return dependencies;
}
```

#### Benefits

- Method names clearly express intent
- URL encoding is handled transparently
- Complex multi-step operations are wrapped in single methods
- Developers do not need detailed Modrinth API knowledge

## How Other Services Benefit

### DependencyLookupService Example

```java
@Service
public class DependencyLookupService {
    private final ModrinthServiceWrapper modrinthServiceWrapper;

    public Optional<ResolvedDependencyDto> resolveDependencyById(String projectId) {
        JsonNode project = modrinthServiceWrapper.getProjectById(projectId);

        String slug = project.path("slug").asText("");
        String title = project.path("title").asText("");

        return Optional.of(new ResolvedDependencyDto(...));
    }
}
```

#### Benefits

- The service focuses on dependency resolution, not HTTP details
- No HTTP client setup, error handling, or request building is needed in the service
- It is easier to test by mocking `ModrinthServiceWrapper`
- If the Modrinth API changes, only `ModrinthServiceWrapper` needs to be updated

## Connection to Lecture Material

This task applies to the **Modularisation (Objects)** topic from the course. `ModrinthServiceWrapper` is a good example of modularisation because it creates a single, independent module that handles all Modrinth API communication. Other services can use it without needing to know how HTTP communication works internally.

## Conclusion

The `ModrinthServiceWrapper` exemplifies practical application of design patterns by reducing complexity through abstraction, improving maintainability through centralization, and enhancing testability by enabling easy mocking. The Facade and Adapter patterns prevent code duplication across services and create a single point of change for API updates. This demonstrates that design patterns are not only theoretical concepts but practical tools for building maintainable and scalable software systems, which is a core principle from the Software Architecture and Design Patterns lecture.