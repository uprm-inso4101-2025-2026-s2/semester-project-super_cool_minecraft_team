# Graph Consistency and Conflict Validation

## Relation to Lecture Topic

This document applies Requirements Engineering concepts such as inconsistencies, conflicts, satisfiability, and dependency validation to Minecraft mod dependency graphs.

## Relation to Domain Facet Analysis

This document extends the graph domain definition by introducing validation rules that determine when a mod dependency graph becomes inconsistent, incomplete, or invalid during dependency resolution.

---

## Introduction

Minecraft modpacks often contain dozens or hundreds of interconnected mods that depend on specific loaders, APIs, and compatible versions. Dependency graphs represent these relationships, including required dependencies, optional integrations, and incompatibility rules.

This document defines the conditions under which a mod dependency graph is considered valid or invalid. It also describes common conflict scenarios experienced by players when assembling or launching modpacks.

---

## Consistency Rules

A dependency graph is considered consistent when:

### 1. Dependency Satisfaction

Every required dependency declared by a mod exists within the modpack configuration.

For example, Sodium requires Fabric API to function correctly on Fabric-based Minecraft instances.

### 2. Version Compatibility

All declared version constraints must be satisfiable simultaneously.

For example, mods targeting Minecraft 1.21 Fabric may become incompatible when paired with dependencies designed for Minecraft 1.20.4.

### 3. Non-Contradictory Relationships

A mod cannot simultaneously require and conflict with the same dependency.

Conflicting declarations create invalid dependency states that prevent successful dependency resolution.

---

## Conflict Scenarios

### 1. Dependency vs Conflict

Certain rendering optimization mods may require Fabric API while also conflicting with outdated rendering extensions installed in the same modpack.

For example, an outdated version of Indium may conflict with newer Sodium rendering implementations on Minecraft 1.21 Fabric.

→ This may result in startup crashes, rendering instability, or failed dependency validation.

---

### 2. Mutual Incompatibility

Some mods explicitly declare incompatibilities with one another because they modify the same internal Minecraft systems.

For example, OptiFine frequently conflicts with Fabric rendering and shader optimization mods.

→ These mods cannot reliably coexist in the same configuration.

---

### 3. Unsatisfiable Versions

Different mods may require incompatible versions of the same dependency.

For example:
- one mod requires Fabric API version 0.100+
- another mod only supports Fabric API versions below 0.95

→ No valid dependency configuration can satisfy both requirements simultaneously.

---

### 4. Missing Dependency

A required dependency may be absent from the modpack configuration.

For example, installing Sodium without the required Fabric Loader environment may prevent the game from launching correctly.

→ The dependency graph becomes incomplete and invalid.

---

## Invalid Configurations

A configuration is considered invalid if:

- a required dependency is missing
- a dependency violates incompatibility constraints
- version requirements cannot be satisfied
- required mods cannot coexist safely
- dependency cycles or contradictory relationships prevent stable resolution

---

## Role of Consistency

Consistency ensures that the dependency graph represents a stable and executable modpack configuration.

Without consistency:
- dependency resolution may fail
- Minecraft may crash during startup
- mods may load with missing functionality
- gameplay instability may occur
- players may experience corrupted or incomplete mod behavior

Consistency validation helps modpack creators identify incompatibilities before launching the game.

---

## Summary

This document defines:
- dependency consistency rules
- realistic mod conflict scenarios
- invalid configuration conditions
- validation principles for dependency graphs

These rules provide a conceptual foundation for validating Minecraft mod dependency graphs and improving modpack stability.