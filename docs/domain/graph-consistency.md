# Graph Consistency and Conflict Validation

## Overview

This document applies Requirements Engineering concepts such as inconsistencies, conflicts, satisfiability, and dependency validation to Minecraft mod dependency graphs.

Minecraft modpacks often contain dozens or hundreds of interconnected mods that depend on specific loaders, APIs, and compatible versions. Dependency graphs represent these relationships, including required dependencies, optional integrations, and incompatibility rules.

This document defines the conditions under which a mod dependency graph is considered valid or invalid. It also describes common conflict scenarios experienced by players when assembling or launching modpacks.

---

## Relation to Domain Facet Analysis

This document extends the graph domain definition by introducing validation rules that determine when a mod dependency graph becomes inconsistent, incomplete, or invalid during dependency resolution.

It defines the conceptual rules for ensuring consistency and identifying conflicts in a mod dependency graph. The goal is to determine when a modpack configuration becomes invalid due to contradictions, incompatibilities, or unsatisfiable constraints.

This analysis is based on Requirements Engineering concepts such as consistency, conflict detection, and satisfiability.

---

## Key Concepts

### Consistency

A dependency graph is considered consistent when no contradictions exist between its nodes and relationships.

### Conflict

A conflict occurs when two or more relationships cannot be satisfied simultaneously.

### Satisfiability

A configuration is satisfiable if all dependency constraints can be fulfilled without violating any rules.

---

## Conflict Scenarios

### 1. Dependency vs Incompatibility Conflict

A mod declares a dependency on another mod that it is also incompatible with.

**Example:**
- Mod A depends on Mod B
- Mod A is incompatible with Mod B

This creates a direct contradiction and results in an invalid dependency configuration.

---

### 2. Mutual Incompatibility in Required Mods

Two required mods are incompatible with each other.

**Example:**
- Mod A depends on Mod B
- Mod A depends on Mod C
- Mod B is incompatible with Mod C

Both dependencies cannot coexist, making the configuration invalid.

For example, some rendering optimization mods may conflict because they modify the same internal Minecraft rendering systems.

---

### 3. Version Constraint Conflict

Dependency version requirements cannot be satisfied simultaneously.

**Example:**
- Mod A requires Mod B version ≥ 2.0
- Mod C requires Mod B version ≤ 1.5

No valid version satisfies both constraints.

For example, one mod may require Fabric API version 0.100+ while another only supports versions below 0.95.

---

### 4. Missing Required Dependency

A required dependency is not present in the configuration.

**Example:**
- Mod A depends on Mod B
- Mod B is not included

This results in an incomplete dependency graph.

For example, installing Sodium without the required Fabric Loader environment may prevent Minecraft from launching correctly.

---

## Consistency Rules

### Rule 1: No Contradictory Relationships

A mod cannot simultaneously depend on and be incompatible with the same mod.

---

### Rule 2: All Dependencies Must Be Satisfied

Every declared dependency must exist within the graph.

---

### Rule 3: Version Constraints Must Be Compatible

All version requirements for a given dependency must overlap.

---

### Rule 4: Required Mods Must Be Compatible

All required dependencies must not conflict with each other.

---

## Conditions for Invalid Configurations

A mod dependency graph is considered invalid if any of the following conditions are met:

- A required dependency is missing
- A dependency violates incompatibility constraints
- Version requirements cannot be satisfied
- Required dependencies are mutually incompatible
- Dependency cycles or contradictory relationships prevent stable resolution

---

## Role of Consistency

Consistency ensures that the dependency graph represents a stable and executable modpack configuration.

Without consistency:
- dependency resolution may fail
- Minecraft may crash during startup
- mods may load with missing functionality
- gameplay instability may occur
- players may experience corrupted or incomplete mod behavior

Maintaining consistency allows for predictable and reliable behavior of the system. Consistency validation also helps modpack creators identify incompatibilities before launching the game.

---

## Conclusion

By defining conflict scenarios, consistency rules, and invalid configuration conditions, this document establishes a clear conceptual framework for evaluating Minecraft mod dependency graphs.

These validation principles help ensure that configurations are logically sound, executable, and aligned with Requirements Engineering principles.