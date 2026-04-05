# Frontend Architecture Overview - Team A

## Additional Documentation

- [Dependency Resolution and Download Flow](docs/integration-dependency-download-flow.md)

## Introduction

This README documents the frontend architecture for the dependency-graph UI. It is a living document: update it as new attributes and features are added. The goal is to make it easy for frontend contributors to understand how data flows, how the graph is built and updated, and where to edit code.

## Key Concepts (at-a-glance)

 **Data flow:** JSON (mock/backend) → Validation → Graph Renderer → Interaction → UI updates
 **Graph lifecycle:** Init → Render → Update → Reset
 **Core events:** Node click, Search input, Reset button
 **Edit points:** see "Where to edit" section below


## Separation of Responsibilities

We divide the frontend into three layers to keep code maintainable and testable.

### 1. HTML (Structure)

The page structure is provided by the main template (index.html). Key sections:

* Header: title/description
* Graph container: area where the graph is rendered
* Side panel: details view for the selected mod
* Navigation controls: search, reset, filters
* Footer: auxiliary links and credits
* Typical files: index.html and templates under src/main/resources/templates/

Typical files: index.html and templates under src/main/resources/templates/

### 2. CSS (Presentation)

CSS handles layout, responsive behavior, and visuals:

* Layout: grid/flex to position graph and side panel
* Theme/colors: base palette and highlight colors for dependency states
* Typography and icons
* Responsive rules: e.g., side panel becomes bottom drawer on small screens

Typical files: styles.css and static CSS under src/main/resources/static/

### 3. JavaScript (Logic)

JS controls validation, rendering, and interactions:

* Data validation and error reporting
* Transform JSON into node/edge models for the graph library
* Initialize and configure the graph library (layout, zoom, styles)
* Event handling (click, search, reset) and side-panel population

Typical files: script.js, graphRenderer.js, eventHandlers.js under static JS folders.



## Data Flow

High-level path: JSON (mock or backend) → Validation → Graph Render → Interaction → UI updates

. JSON input: from `src/main/resources/mock/` (local) or backend API.
. Validation: check required/optional fields and reference integrity before rendering.
. Graph renderer: consumes validated model and creates nodes/edges for the chosen graph library.
. Interaction: user actions (click/search) update view and side panel.

Example JSON (minimal):

[source,json]
- - - -
{
    "mods": [
        {
            "id": "modA",
            "name": "Example Mod A",
            "dependencies": ["modB"],
            "version": "1.0.0",
            "description": "Optional"
        }
    ]
}
- - - 

Validation checklist (implemented or to implement):


* Root object with `mods` array
* Each mod has `id` (string) and `name` (string)
* `dependencies` (if present) is an array of strings
* Each dependency id refers to an existing mod id


Validation pseudocode:

[source,js]
- - - -
function validate(input) {
    if (!input || !Array.isArray(input.mods)) throw Error('Invalid payload');
    const ids = new Set(input.mods.map(m => m.id));
    input.mods.forEach(m => {
        if (!m.id || !m.name) throw Error('Missing id/name');
        if (m.dependencies) m.dependencies.forEach(d => { if (!ids.has(d)) throw Error('Unknown dependency'); });
    });
}
- - - 

How the renderer consumes JSON:

- Map each mod to a node object: { id, label: name, meta: {...} }
- Map each dependency to an edge: { from: mod.id, to: dependencyId }
- Load nodes/edges into the graph library API and apply styling/layout

How interactions update the graph/panel:

- Node Click: select node → highlight its dependencies/dependents → open side panel with metadata
- Search: find node(s) by id/name → center/zoom to node → temporary highlight
- Data update: re-validate JSON → compute diff → add/remove nodes and edges or re-render


## Graph Lifecycle

Stages the graph goes through:

. Init
     - Create container and initialize graph library
     - Set layout, zoom limits, styles, and event listeners

. Render
     - Parse validated JSON into nodes/edges
     - Populate the graph instance and run layout

. Update
     - Apply incremental changes (add/remove/update nodes/edges)
     - Optionally re-run layout or adjust camera

. Reset
     - Clear selections and highlights
     - Reset zoom/pan to default view


## Event Handling

List of main frontend events and their effects:

* Node Click
    ** Opens side panel with: name, version, description, dependency list, conflict/warning badges
    ** Highlights the node and its dependency edges
    ** Files: eventHandlers.js / graphRenderer.js

* Search Input
    ** Filters node list and finds matches
    ** Centers and zooms to selected node, pulses highlight
    ** Should be debounced to avoid heavy re-renders

* Reset Button
    ** Clears filters and selection
    ** Centers graph and resets zoom

* Hover
    ** Shows tooltip with quick info (name/version)

* Implementation tips:
** Use a single source of truth for selection state (controller or simple store)
** Animate camera moves for better UX when focusing nodes
** Debounce heavy inputs and async data loads


## Where to Edit / Developer Pointers

* Mock data: src/main/resources/mock/
* Static assets (HTML/CSS/JS): src/main/resources/static/
* Templates: src/main/resources/templates/
* Server-side endpoints: search for controllers under src/main/java/com/.../view or controller packages

If you're changing rendering behavior, look for the graph initialization code (search for `cytoscape`, `vis`, `d3`, or `graphRenderer`).


## Diagram (flow)

[source]
- - - -
flowchart TD

    A[JSON: mock / API] --> B[Validation]
    B --> C[Graph Renderer]
    C --> D[Graph UI]
    D --> E[User events]
    E --> F[Updates: highlight / detail panel / re-render]
    F --> D
    subgraph Controls
        S[Search]
        R[Reset]
    end
    E --> S
    E --> R
- - - 

==Notes:
* This README is the canonical project documentation for frontend architecture. The document will be mirrored/expanded into a Google Doc for team collaboration if needed.
* Keep this file up to date when new attributes, validation rules, or UI behaviors are introduced.






