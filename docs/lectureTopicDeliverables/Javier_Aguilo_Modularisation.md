# Lecture Topic Task: Object Modularisation Report
**Author:** Javier Aguiló  
**Role:** Backend team leader  
**Associated Issue:** # 712  

---

## 1. Objective & Context
The objective of this individual task is to apply the formal principles of **Object Modularisation** from the course lecture to Team C's Minecraft Mod Dependency subsystem. Specifically, this implementation targets the architectural design of the backend structures that support our dependency page to enforce 
**High Cohesion**, **Low Coupling**, and **Information Hiding** without disrupting the existing codebase.

In a community-driven ecosystem like Minecraft modding, external data structures (like the Modrinth API schemas) and local infrastructure (like storage directories) change frequently. 
Documenting and defining distinct modular boundaries ensures these infrastructure shifts do not ripple through and break our core system validation logic.

---

## 2. Module Architectural Specifications

To isolate concerns, the subsystem is conceptually and structurally modularized into two distinct operational boundaries, completely hiding implementation details behind public abstractions.

### Module A: Core Dependency Resolver (`IDependencyResolver`)
* **Core Responsibility:** Pure algorithmic evaluation of the mod dependency graph and version conflict detection.
* **Cohesion Classification:** **High Cohesion (Functional Cohesion).** This module performs exactly one single, well-defined operational task. 
It contains no network protocol operations, file system drivers, or user interface configurations.
* **Encapsulated Data (Private & Hidden):**
  * `private Map<String, List<Object>> resolutionCache` -> Stores temporary local resolution paths to optimize performance.
  * `private DirectedGraph modGraph` -> The structural model of the active dependency tree.

#### Public Interface Boundary Description
```text
Interface: IDependencyResolver
Method Signature: resolve(List<String> modIds) -> DependencyGraph