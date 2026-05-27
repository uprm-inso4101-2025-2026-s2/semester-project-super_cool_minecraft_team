# Software Quality Attributes for the Minecraft Dependency Graph Tool

## Overview
The Minecraft Dependency Graph tool is designed to help users upload a modpack, inspect dependency relationships, identify conflicts or missing dependencies, and navigate a graph-based view of mod compatibility.

From a Non-Functional Requirements perspective, three quality attributes are especially important for this project:

1. Usability
2. Performance
3. Maintainability

These attributes are not only academic requirements. They directly influence whether the tool can be used effectively by real players and modpack maintainers, and whether the team can evolve the system over time.

## Usability

### What Usability Means
Usability is the degree to which users can learn, understand, and operate the system efficiently and with low friction.

### How It Applies to This Project
For this tool, usability means a user should be able to:
- upload a modpack ZIP without confusion
- move through the graph and quickly understand dependency relationships
- find important nodes using search
- recover orientation using reset and zoom controls
- notice data quality problems (for example missing dependencies) through clear visual warnings

### Practical Examples in This Repository
- The upload flow validates ZIP files and provides immediate success/failure feedback before redirecting to graph view.
- The graph screen provides direct interaction controls, including zoom in, zoom out, reset view, and a search box.
- The graph UI includes toggles such as optional-mod visibility and keeps that preference in local storage for continuity between sessions.
- Client-side validation checks graph payload quality (nodes, links, relationship values) and surfaces errors/warnings in an in-page banner instead of silently failing.

### Why It Matters
If users cannot interpret the graph quickly, the tool fails its purpose even when algorithms are correct. Usability improves trust, lowers onboarding effort, and makes dependency analysis practical for day-to-day modpack maintenance.

## Performance

### What Performance Means
Performance is how responsively and efficiently the system processes data and serves interactions under realistic workloads.

### How It Applies to This Project
For the Minecraft Dependency Graph tool, performance has two critical dimensions:
- backend processing of uploaded archives and dependency lookups
- frontend rendering and interaction with graph data

### Practical Examples in This Repository
- Backend ZIP handling and parsing are isolated in services dedicated to extraction and metadata processing.
- Dependency lookup uses a TTL-based cache to reduce repeated external requests and avoid unnecessary recomputation.
- Frontend processing validates input before full render and applies visibility filtering (for example optional links) to reduce visual and cognitive load.
- The graph interface supports focused navigation (search, zoom, reset), which helps users work effectively even as graph complexity increases.

### Large Modpack Context
As modpacks become larger, rendering and dependency traversal costs grow. Prioritizing performance keeps interactions responsive and prevents the tool from feeling unreliable under high-node/high-edge scenarios.

### Why It Matters
Performance directly affects user confidence. Slow feedback during upload, parsing, or graph interaction makes troubleshooting harder and can discourage adoption. In practice, responsive behavior is essential for real-world use with non-trivial modpacks.

## Maintainability

### What Maintainability Means
Maintainability is the ease with which the software can be understood, corrected, extended, tested, and evolved.

### How It Applies to This Project
This tool spans multiple concerns: upload and parsing, graph/domain transformation, dependency resolution, and UI rendering. Maintainability requires clear separation so teams can change one part without destabilizing others.

### Practical Examples in This Repository
- The project separates controllers, services, DTOs, graph-domain classes, templates, and static scripts.
- Parsing and graph transformation are handled in service and graph layers, while rendering concerns remain in template and JavaScript files.
- Distinct components for lookup, caching, and visualization reduce coupling and improve targeted debugging.
- Existing structure supports incremental extension, such as introducing additional loader formats (for example NeoForge) with minimal disruption to graph UI contracts.

### Why It Matters
Course projects often evolve rapidly. Good maintainability allows the team to implement new features, fix edge cases, and improve architecture without large regressions. It also supports handoff between teammates and future cohorts.

## Why These Quality Attributes Matter
Usability, performance, and maintainability reinforce each other:

- Better usability ensures users can act on dependency and conflict information quickly.
- Better performance ensures those interactions remain fast enough to be practical.
- Better maintainability ensures the tool can keep improving as requirements and ecosystems change.

Together, these priorities improve both immediate value and long-term sustainability:
- immediate value: clearer dependency diagnostics and smoother graph exploration
- long-term sustainability: easier adaptation to new formats, larger modpacks, and future enhancements

For this Minecraft Dependency Graph tool, treating these three attributes as first-class requirements is essential to project success, not optional polish.
