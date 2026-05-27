# Component-Based Architecture for Minecraft Dependency Graph Visualizer

## Overview
This document describes how the Minecraft Dependency Graph Visualizer in this repository is organized using a component-based architecture. The implementation separates responsibilities into three primary components:

1. Data Ingestion Component (Parser)
2. Logic & Processing Component (Graph Engine)
3. Presentation Component (User Interface)

This separation is visible in the project structure and in the request-to-render workflow from upload to visualization.

## Component 1: Data Ingestion Component (Parser)

### Responsibility
The Data Ingestion component is responsible for accepting modpack input and converting raw archive and metadata content into structured application data.

### Repository Implementation
- Upload entry point: ZipFileController (/api/modpack/zip) validates multipart input and ZIP extension.
- Parsing and orchestration service: ModpackParsingService provides parser-facing behavior.
- ZIP extraction and metadata parsing: ZipProcessingService handles archive extraction, walks files, reads fabric.mod.json, and maps relationship fields (depends, breaks, suggests, recommends, conflicts) into DTO and entity structures.

### Output Contract
This component outputs a normalized DTO (DTO) containing:
- discovered mods
- dependency and conflict edges
- missing dependencies
- resolved dependency identifiers

That DTO is stored in session (graphDto) and passed forward to graph processing.

## Component 2: Logic and Processing Component (Graph Engine)

### Responsibility
The Graph Engine transforms parsed domain data into graph-ready nodes and links and applies graph-domain rules for dependencies and conflicts.

### Repository Implementation
- Graph model: Graph, ModNode
- Graph adapter and service layer: GraphService
- Visualization bridge controller: GraphVisualizationController

### Processing Tasks
- Build an internal graph from parsed DTO data (new Graph(dto)).
- Generate stable node keys (modId@version) to avoid collisions.
- Map graph nodes into NodeDto and relationships into LinkDto.
- Normalize relationship output for client consumption (GraphResponseDto).
- Publish graph data into the view model and session for rendering.

### Structural Role
This component isolates graph-specific computation and mapping from both input parsing and frontend rendering concerns.

## Component 3: Presentation Component (User Interface)

### Responsibility
The UI component presents upload interactions and graph visualization, while remaining decoupled from archive parsing and graph construction internals.

### Repository Implementation
- Templates:
  - upload.html for user file selection and upload flow
  - graph_visualization.html for graph rendering workspace
- Frontend scripts:
  - upload.js submits ZIP files and redirects to /graph
  - graph_visualization.js validates render payload, initializes D3 visualization, and supports interaction (zoom, panel details, warnings)
  - dependency-resolution.js provides helper logic for dependency and dependent lookup in client-side graph data

### Presentation Duties
- Trigger server-side ingestion via HTTP upload.
- Render graph data exposed by the backend model.
- Handle user interaction and validation feedback without duplicating backend parsing logic.

## Structural Separation Across Components
The system follows a clear pipeline:

1. Ingestion (Parser): ZIP input is validated, extracted, and parsed into DTO and domain structures.
2. Processing (Graph Engine): DTO is transformed into graph nodes and links suitable for visualization.
3. Presentation (UI): the graph payload is rendered and interacted with through templates and JavaScript.

Each stage has a focused responsibility and communicates through explicit data contracts (DTOs and graph response models), instead of direct cross-layer coupling.

## Why This Improves Maintainability and Flexibility

### Maintainability Benefits
- Lower coupling: UI changes (layout, D3 behaviors) do not require parser rewrites.
- Focused testing: parsing logic, graph mapping logic, and UI behavior can be tested independently.
- Clear ownership boundaries: backend services and frontend scripts each have explicit scope.
- Easier debugging: failures can be localized to ingestion, processing, or rendering stages.

### Flexibility Benefits
- Parser extensibility: support for additional metadata formats can be added in ingestion services without redesigning the UI.
- Graph evolution: new relationship types or graph rules can be introduced in the graph engine while preserving upload and template flow.
- UI replacement readiness: alternative frontends (new JavaScript framework or API consumer) can reuse the same graph response contract.

## Conclusion
The repository already reflects a practical component-based architecture for dependency visualization: ingestion services parse and normalize modpack data, graph services convert that data into visualization-ready structures, and UI templates and scripts render interactive results. This separation directly supports long-term maintainability and future feature growth.
