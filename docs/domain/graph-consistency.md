# Graph Consistency and Conflict Validation

## Relation to Lecture Topic

This document applies Requirements Engineering concepts such as inconsistencies, conflicts, and satisfiability to the mod dependency graph.

## Relation to Domain Facet Analysis

This document extends the graph domain definition by introducing validation rules that determine when a dependency graph becomes inconsistent or invalid.

---

## Introduction

The mod dependency graph represents relationships between mods, including dependencies and incompatibilities.

This document defines the conditions under which a graph is considered valid or invalid.

---

## Consistency Rules

A dependency graph is consistent when:

### 1. Dependency Satisfaction
Every required dependency must exist in the graph.

### 2. Version Compatibility
All version constraints must be satisfiable.

### 3. Non-Contradictory Relationships
A mod cannot both depend on and conflict with the same mod.

---

## Conflict Scenarios

### 1. Dependency vs Conflict
Mod A depends on Mod B, but also declares Mod B as incompatible.

→ This creates a logical contradiction.

---

### 2. Mutual Incompatibility
Mod A conflicts with Mod B, and Mod B conflicts with Mod A.

→ They cannot coexist in the same configuration.

---

### 3. Unsatisfiable Versions
Different mods require incompatible versions of the same dependency.

→ No valid configuration exists.

---

### 4. Missing Dependency
A required dependency is not present.

→ The configuration is incomplete.

---

## Invalid Configurations

A configuration is invalid if:

- A required dependency is missing
- A dependency conflicts with an incompatibility rule
- Version constraints cannot be satisfied
- Required mods cannot coexist

---

## Role of Consistency

Consistency ensures that the graph represents a valid modpack.

Without consistency:
- dependency resolution fails
- conflicts occur
- the system becomes unreliable

---

## Summary

This document defines:
- consistency rules
- conflict scenarios
- invalid configuration conditions

These rules provide a conceptual foundation for validating mod dependency graphs.