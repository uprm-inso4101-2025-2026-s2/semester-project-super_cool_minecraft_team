# Sequence Diagram Description for the Missing Dependencies Flow

## Purpose
This artifact provides a text-based version of the sequence diagram for the Missing Dependencies flow in the Minecraft Mod Dependency Visualizer project.

It documents one concrete end-to-end interaction scenario showing how the user reaches the Missing Dependencies page, how the frontend requests dependency analysis data from the backend, how the backend coordinates validation and lookup work across its main components, and how the redirect flow is used to open an external dependency source.

---

## Scope
This sequence description focuses on the following user-visible scenario:
- the user opens the Missing Dependencies page
- the frontend requests missing dependency analysis data
- the backend locates the requested mod
- the backend validates dependencies and identifies missing ones
- the backend resolves dependency source information
- the frontend renders dependency results
- the user clicks a resolved dependency link
- the redirect flow validates and forwards the user to an external source

This artifact stays at the behavioral interaction level and is not intended to be a full code-level trace of every implementation detail.

---

## Main Participants

- **User**
- **MainController**
- **MD-Page** (Missing Dependencies Page)
- **DependencyController**
- **ModRepo**
- **DependencyResolver**
- **DependencyLookup**
- **ModrinthServiceWrapper**
- **RedirectController**
- **External Download Site**

---

## Main Success Scenario

1. **User -> MainController**  
   `GET /missing-dependencies`

2. **MainController -> MD-Page**  
   `return missing-dependencies view`

3. **MD-Page -> User**  
   `render page shell`

4. **MD-Page -> DependencyController**  
   `GET /api/dependencies/missing?modId=...`

5. **DependencyController -> ModRepo**  
   `findById(modId)`

6. **ModRepo -> DependencyController**  
   `mod / not found`

7. **DependencyController -> DependencyResolver**  
   `validate(mod)`

8. **DependencyResolver -> ModRepo**  
   `findById(dependencyId)`

9. **ModRepo -> DependencyResolver**  
   `dependency exists / missing`

10. **DependencyResolver -> DependencyController**  
    `ValidationResponse(missingDependencies, circularDependencies)`

11. **DependencyController -> DependencyLookup**  
    `resolveDependencies(missingDependencies)`

12. **DependencyLookup -> ModrinthServiceWrapper**  
    `searchProject(...) / getProjectById(...)`

13. **ModrinthServiceWrapper -> DependencyLookup**  
    `candidate projects / resolved links`

14. **DependencyLookup -> DependencyController**  
    `resolved dependency results`

15. **DependencyController -> MD-Page**  
    `200 OK + dependency analysis results`

16. **MD-Page -> User**  
    `render dependency cards`

17. **User -> MD-Page**  
    `click View Download Link`

18. **MD-Page -> RedirectController**  
    `GET /r?u=...`

19. **RedirectController -> External Download Site**  
    `validate URL, scheme, host, allowlist`

20. **External Download Site -> User**  
    `302 redirect`

---

## Textual Sequence Representation

```text
User -> MainController: GET /missing-dependencies
MainController -> MD-Page: return missing-dependencies view
MD-Page -> User: render page shell

MD-Page -> DependencyController: GET /api/dependencies/missing?modId=...
DependencyController -> ModRepo: findById(modId)
ModRepo -> DependencyController: mod / not found

DependencyController -> DependencyResolver: validate(mod)
DependencyResolver -> ModRepo: findById(dependencyId)
ModRepo -> DependencyResolver: dependency exists / missing
DependencyResolver -> DependencyController: ValidationResponse(missingDependencies, circularDependencies)

DependencyController -> DependencyLookup: resolveDependencies(missingDependencies)
DependencyLookup -> ModrinthServiceWrapper: searchProject(...) / getProjectById(...)
ModrinthServiceWrapper -> DependencyLookup: candidate projects / resolved links
DependencyLookup -> DependencyController: resolved dependency results

DependencyController -> MD-Page: 200 OK + dependency analysis results
MD-Page -> User: render dependency cards

User -> MD-Page: click View Download Link
MD-Page -> RedirectController: GET /r?u=...
RedirectController -> External Download Site: validate URL, scheme, host, allowlist
External Download Site -> User: 302 redirect
```

---

## Participant Roles

### User
Initiates page access and later triggers the dependency link action.

### MainController
Serves the Missing Dependencies page view.

### MD-Page
Represents the frontend page that requests backend data and renders dependency results.

### DependencyController
Acts as the backend orchestration point for the missing dependency analysis API.

### ModRepo
Provides mod retrieval support during mod lookup and dependency validation.

### DependencyResolver
Checks the selected mod’s dependencies and determines which dependencies are missing.

### DependencyLookup
Attempts to resolve missing dependencies into candidate external sources or download-oriented results.

### ModrinthServiceWrapper
Encapsulates communication with the external Modrinth API.

### RedirectController
Validates redirect requests and controls safe outbound navigation.

### External Download Site
Represents the approved external destination ultimately reached by the user.

---

## Notes
This artifact is intended to preserve the sequence-diagram work in a text-based format suitable for repository inclusion. It documents the interaction order among the main participants and keeps the focus on one concrete feature scenario rather than the entire system.
