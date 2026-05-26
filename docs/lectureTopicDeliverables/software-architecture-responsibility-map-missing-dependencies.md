# Software Architecture Responsibility Map for the Missing Dependencies Flow

## Purpose
This artifact documents how the main responsibilities of the Missing Dependencies feature are allocated across the primary architectural components of the Minecraft Mod Dependency Visualizer project.

The goal is to clarify which components are responsible for the major concerns in the feature flow, how those responsibilities are distributed, and how the components collaborate to support user-visible behavior on the Missing Dependencies page.

---

## Scope
This responsibility map focuses only on the Missing Dependencies flow, including:
- serving the Missing Dependencies page
- requesting missing dependency analysis data
- locating the selected mod context
- identifying missing dependencies
- resolving download links or external source data
- handling partial, empty, and error outcomes
- validating and performing safe redirect behavior
- exposing user-facing results through the UI

This artifact stays at the architecture/design level and does not attempt to document every class, method, or implementation detail in the project.

---

## Main Components in Scope

- **MainController**
- **Missing Dependencies Page** (`missing-dependencies.html`)
- **frontend-missing-dependencies.js**
- **backend-missing-dependencies.js**
- **DependencyController**
- **ModRepository**
- **DependencyResolverService**
- **DependencyLookupService**
- **ModrinthServiceWrapper**
- **RedirectController**
- **External Source** (e.g. Modrinth or another approved external destination)

---

## Responsibility Allocation Table

| Responsibility / Concern | Primary Component | Supporting / Collaborating Components | Why this allocation makes sense |
|---|---|---|---|
| Serve the Missing Dependencies page view | **MainController** | `missing-dependencies.html` | The controller is responsible for routing the user to the page and providing initial model attributes such as loader and Minecraft version. |
| Render user-facing dependency results | **Missing Dependencies Page** | `frontend-missing-dependencies.js` | The template provides the page structure and UI regions where results, banners, and messages are shown. |
| Drive page-side UI behavior | **frontend-missing-dependencies.js** | Missing Dependencies Page | The frontend script coordinates loading, rendering, search/filtering, status badges, and user interactions such as clicking download links. |
| Request missing dependency analysis data from the backend | **backend-missing-dependencies.js** | `frontend-missing-dependencies.js`, `DependencyController` | The page-side data-access layer is responsible for fetching the analysis results consumed by the UI. |
| Expose the missing dependency analysis endpoint | **DependencyController** | `DependencyResolverService`, `DependencyLookupService`, `ModRepository` | The controller acts as the main orchestration point for the backend API used by the page. |
| Validate the requested mod context and locate the selected mod | **DependencyController** | `ModRepository` | The controller checks the input request and uses the repository to retrieve the referenced mod before deeper analysis occurs. |
| Store and retrieve mod entities by identifier | **ModRepository** | `DependencyController`, `DependencyResolverService` | The repository is the architectural boundary for mod lookup and retrieval. |
| Identify missing dependencies and detect circular dependency conditions | **DependencyResolverService** | `ModRepository` | This service owns dependency validation logic and is the main place where missing dependencies are identified. |
| Resolve dependency download links or source metadata | **DependencyLookupService** | `ModrinthServiceWrapper` | This service is responsible for turning missing dependency items into usable external lookup results. |
| Communicate with the external Modrinth API | **ModrinthServiceWrapper** | `DependencyLookupService` | The wrapper isolates external API interaction from higher-level dependency resolution flow. |
| Build the backend response returned to the page | **DependencyController** | `DependencyResolverService`, `DependencyLookupService` | The controller combines validation results and lookup results into the response structure consumed by the frontend. |
| Display partial-results, empty-state, and error outcomes in the UI | **frontend-missing-dependencies.js** | Missing Dependencies Page | The frontend determines which visible state or banner to show based on the returned data or failure condition. |
| Filter visible dependency results through search | **frontend-missing-dependencies.js** | Missing Dependencies Page | The search interaction is a UI-layer responsibility because it changes which already-rendered entries remain visible. |
| Construct user-facing link actions for resolved dependencies | **frontend-missing-dependencies.js** | RedirectController | The frontend builds the redirect request used when the user clicks a resolved dependency’s action. |
| Validate redirect targets and enforce safe redirect rules | **RedirectController** | External Source | The redirect controller is responsible for validating the target URL before allowing navigation outside the system. |
| Send the user to the approved external destination | **RedirectController** | External Source | The redirect flow crosses the system boundary and should therefore be controlled at a dedicated controller boundary. |
| Provide the final downloadable or viewable resource destination | **External Source** | RedirectController | The external site is outside the system but remains part of the end-to-end user flow. |

---

## Simple Architectural View

```text
User
  |
  v
MainController
  |
  v
Missing Dependencies Page (HTML)
  |
  v
frontend-missing-dependencies.js
  |
  v
backend-missing-dependencies.js
  |
  v
DependencyController
  |--------------------> ModRepository
  |--------------------> DependencyResolverService
  |                           |
  |                           v
  |                       ModRepository
  |
  |--------------------> DependencyLookupService
                              |
                              v
                     ModrinthServiceWrapper

Resolved results returned to frontend
  |
  v
frontend-missing-dependencies.js
  |
  v
RedirectController
  |
  v
External Source
```

---

## Responsibility Notes by Component

### MainController
`MainController` is responsible for serving the Missing Dependencies page route and providing the basic page context needed for initial rendering. At the design level, this makes it the entry-point component for the feature’s UI view.

### Missing Dependencies Page
The HTML page is responsible for the visual structure of the feature. It defines where the list, banners, loading state, empty state, warnings, and guidance panels are presented to the user.

### frontend-missing-dependencies.js
This component is responsible for the main client-side UI lifecycle of the page. It starts loading behavior, requests data, interprets returned results, renders dependency cards, displays partial/empty/error outcomes, and handles search/filter behavior.

### backend-missing-dependencies.js
This file acts as a small page-side integration layer that requests missing dependency data from the backend API. At the architectural level, it helps separate UI rendering behavior from backend communication behavior.

### DependencyController
`DependencyController` is the central backend orchestration component for the Missing Dependencies API flow. It validates incoming requests, retrieves the target mod, invokes dependency validation and link resolution services, and returns the combined response used by the frontend.

### ModRepository
`ModRepository` is responsible for retrieving stored mod data. It supports both the initial retrieval of the requested mod and dependency checks performed during validation.

### DependencyResolverService
This service is responsible for analyzing the selected mod’s dependency structure and determining which dependencies are missing. It also contributes to detecting circular dependency conditions, which makes it the main validation-oriented logic component in the flow.

### DependencyLookupService
This service is responsible for resolving missing dependencies into usable candidate sources or download-oriented metadata. It sits between the controller and the external API wrapper, which helps keep lookup logic separate from request orchestration.

### ModrinthServiceWrapper
This component encapsulates communication with the external Modrinth API. At the architectural level, this is useful because it isolates third-party API interaction behind a dedicated service boundary.

### RedirectController
`RedirectController` is responsible for safe outbound navigation. It validates redirect targets and decides whether the user may be forwarded to an external URL. This keeps external-link validation out of the UI layer and centralizes the security-related responsibility.

### External Source
The external source is outside the system boundary but remains part of the full user-visible feature flow because resolved dependencies ultimately direct users there for download or additional information.

---

## Why the Responsibilities Are Allocated This Way
The current allocation follows a reasonable architectural separation of concerns for the Missing Dependencies feature:

- **page-serving responsibility** is separated from **page behavior**
- **page behavior** is separated from **backend request orchestration**
- **request orchestration** is separated from **dependency validation**
- **dependency validation** is separated from **external lookup logic**
- **external lookup logic** is separated from **third-party API access**
- **outbound redirect validation** is separated from both the UI and the general dependency-analysis flow

This separation helps the feature remain easier to understand, refine, test, and integrate during Milestone 3.

---

## Conclusion
The Missing Dependencies flow is supported by a small set of architectural components, each with a distinct primary responsibility. The frontend side is responsible for rendering and interaction, the controller layer is responsible for orchestration, the services are responsible for validation and lookup logic, the repository is responsible for data retrieval, and the redirect controller is responsible for safe navigation to external dependency sources.

By documenting responsibilities at this level, the team gains a clearer design-oriented view of how the system supports the Missing Dependencies feature without needing a full code-level breakdown.
