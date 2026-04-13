# Graph Domain Construction and Representation

## Relation to Lecture Topic
This document applies Domain Engineering concepts to the construction of the mod dependency graph. Domain engineering focuses on identifying and modeling the entities, functions, and relationships that exist within a system’s domain.

The graph represents these domain elements by modeling mods as entities (nodes) and dependencies or incompatibilities as relationships (edges).

## Relation to Domain Facet Analysis
This document contributes to the domain model by defining how domain elements are represented in the graph structure. It establishes the foundational representation of mods and their relationships before additional analysis such as validation or dependency resolution is performed.

---

## Introduction
The mod dependency graph models the structure of relationships between mods within a modpack configuration.

Each mod is represented as a node in the graph, while relationships such as dependencies or conflicts are represented as edges.

This document defines how domain elements are translated into graph components and how the graph structure represents the system domain.

---

## Graph Domain Elements

### 1. Mods (Nodes)
Mods are the primary entities in the domain. Each mod is represented as a node in the graph.

A mod node may contain information such as:
- mod identifier
- version
- metadata related to compatibility

### 2. Dependencies (Edges)
Dependencies represent relationships where one mod requires another mod to function.

In the graph:
- a directed edge connects the dependent mod to the required mod.

Example: Mod A → Mod B (Mod A depends on Mod B)

### 3. Incompatibilities (Edges)
Incompatibility relationships represent mods that cannot coexist.

In the graph:
- an edge represents a conflict relationship between two mods.

Example: Mod A ↔ Mod B (Mod A conflicts with Mod B)

---

## Graph Construction Process

### 1. Node Creation
Each mod detected in the configuration is converted into a graph node.

### 2. Relationship Extraction
Dependency and incompatibility information is extracted from mod metadata.

### 3. Edge Construction
Edges are created to represent dependencies and incompatibilities between mods.

### 4. Graph Assembly
All nodes and edges are combined to form the dependency graph representing the modpack domain.

---

## Role of Graph Construction
Graph construction provides the foundation for later system operations, including:
- dependency validation
- conflict detection
- dependency resolution
- configuration analysis

Without a properly constructed graph:
- relationships between mods cannot be analyzed
- dependency resolution becomes unreliable
- conflicts cannot be detected effectively

---

## Summary
This document defines:
- the domain entities (mods)
- the relationships between entities
- the process for constructing the dependency graph

These elements provide the conceptual foundation for modeling the mod dependency domain.
