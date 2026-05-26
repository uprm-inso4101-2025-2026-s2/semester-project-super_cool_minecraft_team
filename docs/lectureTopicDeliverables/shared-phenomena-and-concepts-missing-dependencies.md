# Shared Phenomena and Concepts for the Missing Dependencies UI

## Purpose
This artifact identifies the shared concepts and phenomena relevant to the Missing Dependencies page in the Minecraft Mod Dependency Visualizer project.

The goal is to clarify which domain and system elements are exposed, referenced, or interacted with through the UI, and to distinguish abstract concepts from concrete observable phenomena. This helps document the interface-level understanding of the feature and supports better frontend/backend alignment.

---

## Scope
This analysis focuses only on the Missing Dependencies page and its immediate UI behavior, including:
- loading missing dependency results
- showing resolved or unresolved dependency entries
- displaying partial, empty, or error outcomes
- filtering visible dependencies through search
- opening dependency links through the redirect flow

---

## Shared Concepts

| Item | Type | Shared with UI? | Description | Relevance to the Missing Dependencies page |
|------|------|-----------------|-------------|--------------------------------------------|
| Mod | Concept | Yes | A mod analyzed by the system. | The page exists to help users resolve missing dependencies related to a selected mod or modpack context. |
| Dependency | Concept | Yes | A mod or project required by another mod. | The page lists dependencies that are needed for proper modpack operation. |
| Missing Dependency | Concept | Yes | A required dependency that is not currently available or present in the analyzed context. | This is the central concept shown by the page. |
| Resolved Dependency | Concept | Yes | A missing dependency for which the system found one or more candidate links or source entries. | Determines whether the UI can show a usable download action. |
| Unresolved Dependency | Concept | Yes | A missing dependency for which no usable download link was found. | Determines whether the UI shows a “Not found” status and unresolved message. |
| Preferred Download Link | Concept | Yes | The chosen primary link used for the dependency action. | Used by the UI to build the redirect/open-link behavior. |
| Redirect Target | Concept | Yes | The external URL that the redirect endpoint attempts to open after validation. | Relevant because the page does not directly expose arbitrary links; it routes through safe redirect logic. |
| Partial Result | Concept | Yes | A result set in which some dependencies are resolved and others are not. | The page shows a partial-results banner when this occurs. |
| Error State | Concept | Yes | A failure condition in which the page cannot successfully retrieve usable dependency data. | The UI exposes this through an error banner or message. |
| Empty Result | Concept | Yes | A valid result in which no missing dependencies were found. | The UI has a dedicated empty-state message for this case. |
| Loader | Concept | Yes | The mod loader relevant to compatibility and installation guidance, such as Fabric or Forge. | The page displays loader-related information and uses it as part of dependency context and guidance. |
| Minecraft Version | Concept | Yes | The game version relevant to dependency compatibility and installation guidance. | The page uses this as part of dependency metadata and user-facing installation context. |
| Dependency Result Entry | Concept | Yes | A single dependency item as represented in the page’s rendered results. | This is the unit the page displays as a card or row with status and actions. |
| Search Filter | Concept | Yes | The idea of filtering visible dependencies based on a user-provided text query. | Relevant because the page allows the user to narrow visible results interactively. |

---

## Shared Phenomena

| Item | Type | Shared with UI? | Description | Relevance to the Missing Dependencies page |
|------|------|-----------------|-------------|--------------------------------------------|
| Opening the Missing Dependencies page | Phenomenon | Yes | A user navigates to the page and triggers its lifecycle. | Starts the UI flow and data loading behavior. |
| A fetch request to load missing dependency data | Phenomenon | Yes | The page sends a request to retrieve dependency analysis results. | This is the concrete event that drives content rendering. |
| A specific returned dependency list | Phenomenon | Yes | The backend returns a concrete set of missing dependency entries. | The page renders this list into dependency cards or rows. |
| A specific dependency identifier (e.g. modId or dependency id) | Phenomenon | Yes | A concrete identifier used in requests or matching logic. | Important for linking UI entries to returned or resolved data. |
| A concrete resolved dependency entry returned by the system | Phenomenon | Yes | A returned resolved item containing fields such as id, name, links, or preferred link. | Allows the page to mark a dependency as resolved and display a usable action. |
| A specific preferred URL | Phenomenon | Yes | A concrete external URL used for the dependency action. | The UI uses it to generate the redirect link. |
| A user click on “View Download Link” | Phenomenon | Yes | A concrete user interaction on a dependency card or result entry. | Triggers redirect/open-link behavior. |
| A redirect attempt through `/r?u=...` | Phenomenon | Yes | A concrete request to the redirect endpoint with a URL parameter. | This is how the UI opens external dependency links. |
| A successful redirect outcome | Phenomenon | Yes | The redirect endpoint accepts the target and sends the user to the external destination. | Represents the successful completion of the dependency access action. |
| A blocked redirect outcome | Phenomenon | Yes | The redirect endpoint rejects a target URL because it is invalid or disallowed. | Relevant to user-facing safety and failure behavior. |
| A malformed or unsupported redirect request | Phenomenon | Yes | The redirect attempt fails because the parameter is missing, malformed, or uses an unsupported scheme. | Represents a distinct observable failure mode in the redirect flow. |
| A partial-results banner being shown | Phenomenon | Yes | The UI displays a message indicating only some dependencies were resolved. | Makes the system’s incomplete resolution visible to the user. |
| An empty-state message being shown | Phenomenon | Yes | The page shows “No missing dependencies were found.” | Represents a valid UI outcome with no list items. |
| An error message being shown | Phenomenon | Yes | The page displays an error such as a failed request or backend message. | Represents a failure case visible to the user. |
| An unresolved dependency card showing “Not found” | Phenomenon | Yes | A concrete dependency entry is rendered without a usable link. | Represents unresolved lookup behavior in the UI. |
| A loading message or loading state being shown | Phenomenon | Yes | The page initially shows a loading state before data is returned. | Represents the initial visible system/UI interaction phase. |
| Search input value / dependency search query | Phenomenon | Yes | A concrete text value entered by the user to filter dependency cards shown on the page. | It affects which dependency items remain visible in the current UI view. |
| Filtered visibility of dependency cards | Phenomenon | Yes | A subset of dependency entries remains visible after the search query is applied. | Represents a concrete UI consequence of the search behavior. |

---

## Shared UI-Relevant Data Items
The following data items are especially important because they are shared between backend results and frontend behavior:

- dependency id
- dependency name
- required version
- loader
- Minecraft version
- resolved dependency id
- preferred link
- list of candidate links
- partial-results flag
- error message text
- search query text

These data items are interface-relevant because they directly affect what the page renders, how it matches dependencies, how it filters visible entries, and whether the user is given a valid action.

---

## Ambiguities and Clarification Needs

### 1. Missing dependency vs unresolved dependency
These should not be treated as the same thing.
- A **missing dependency** is required but absent.
- An **unresolved dependency** is one for which the system could not find a usable link.

A dependency may be missing regardless of whether it was resolved successfully.

### 2. Resolved dependency vs preferred download link
These are related but not identical.
- A **resolved dependency** is the full result object or entry.
- A **preferred download link** is only one field inside that result.

This distinction matters because the UI may need metadata even when only one link is ultimately used.

### 3. Direct external URL vs redirect target
The page does not directly send the user to an arbitrary link. Instead, it constructs a redirect request using a URL parameter. This means the user-visible action is not simply “open URL,” but “request a validated redirect target.”

### 4. Expected response structure alignment
The frontend behavior depends on a clear relationship between missing dependency entries and resolved dependency entries. If the backend and frontend do not agree on the result structure, the concepts remain valid, but the phenomena observed in the UI may not align correctly.

### 5. Redirect outcome granularity
The redirect flow does not only produce a successful external navigation. It may also result in:
- successful redirect,
- blocked domain,
- malformed URL,
- missing parameter,
- unsupported scheme.

These are distinct observable outcomes that may matter for future UI feedback or integration refinement.

### 6. Loader and version as display data vs compatibility data
Loader and Minecraft version may appear in the UI as simple informational fields, but they also represent compatibility-related concepts that may affect installation guidance and user interpretation of dependency results.

---

## Conclusion
The Missing Dependencies page depends on a small but important set of shared concepts and shared phenomena. The central concepts are the dependency-related abstractions that the page presents to the user, while the phenomena are the concrete requests, returned values, visible banners, search interactions, user clicks, and redirect outcomes that make the feature operational.

Documenting these shared items helps clarify the interface between frontend behavior and system behavior, and supports better terminology, alignment, and future refinement of the feature.
