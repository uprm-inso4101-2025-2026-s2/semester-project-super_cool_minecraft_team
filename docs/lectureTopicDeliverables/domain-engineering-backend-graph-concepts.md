# Domain Engineering Backend Graph Concepts

## Objective
Explain the domain concepts and domain facets that justify the backend graph model used for mod dependency analysis (`Graph`, `ModNode`, `DTO`).

## Domain intrinsics
The domain exists because mods have:
- **Identity + version** (mods are released in versions)
- **Declared relationships** in metadata:
    - **Dependencies** (“requires”)
    - **Conflicts/Incompatibilities** (“cannot co-exist”)
- A **modpack/configuration** (a set of mod instances to analyze)

## Domain concepts mapped to the code
- **Mod Instance (entity)** → `ModNode`
    - `modID` + `modVersion` represent a specific installed mod release.
- **Dependency (relation)** → `ModNode.dependencies: Set<String>`
- **Conflict/Incompatibility (relation)** → `ModNode.conflicts: Set<String>`

In `Graph(DTO dto)`, dependencies/conflicts are normalized into key strings like `id@version`, so relations point to specific mod instances.

## Why `modId@version` is the identity rule
A mod name alone is not stable in the domain (multiple versions; compatibility depends on version).  
So the backend uses `modId@version` as a unique identifier to store nodes (`Map<String, ModNode>`) and to reference targets in `dependencies`/`conflicts`.

## Domain facets tied to modeling choices
- **Intrinsics:** nodes + typed relations exist because mods, versions, dependencies, and incompatibilities exist in the domain.
- **Support technology:** `HashMap` enables fast lookup by identity; `HashSet` avoids duplicate edges and speeds up “contains” checks.
- **Rules/scripts:** deleting a node should remove references to it from other nodes (no dangling dependencies/conflicts).

## DTO as the boundary object (interface facet)
`DTO` packages graph-related data for other layers (e.g., frontend or service layer):
- `mods`: the mod instances being represented
- `edges`: explicit edge list if needed by visualization
- `missingDependencies` / `resolvedDependencies`: results of resolution that the graph model supports