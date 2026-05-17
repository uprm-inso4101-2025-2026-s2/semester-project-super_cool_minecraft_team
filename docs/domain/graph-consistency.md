# Graph Consistency and Conflict Validation

## Overview

This document defines the conceptual rules for ensuring consistency and identifying conflicts in a mod dependency graph. The goal is to determine when a modpack configuration becomes invalid due to contradictions, incompatibilities, or unsatisfiable constraints.

This analysis is based on Requirements Evaluation concepts such as consistency, conflict detection, and satisfiability.

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

This creates a direct contradiction.

---

### 2. Mutual Incompatibility in Required Mods
Two required mods are incompatible with each other.

**Example:**
- Mod A depends on Mod B  
- Mod A depends on Mod C  
- Mod B is incompatible with Mod C  

Both cannot coexist, making the configuration invalid.

---

### 3. Version Constraint Conflict
Dependency version requirements cannot be satisfied simultaneously.

**Example:**
- Mod A requires Mod B version ≥ 2.0  
- Mod C requires Mod B version ≤ 1.5  

No valid version satisfies both constraints.

---

### 4. Missing Required Dependency
A required dependency is not present in the configuration.

**Example:**
- Mod A depends on Mod B  
- Mod B is not included  

This results in an incomplete configuration.

---

## Consistency Rules

### Rule 1: No Contradictory Relationships
A mod cannot simultaneously depend on and be incompatible with the same mod.

---

### Rule 2: All Dependencies Must Be Satisfied
Every declared dependency must exist in the graph.

---

### Rule 3: Version Constraints Must Be Compatible
All version requirements for a given mod must overlap.

---

### Rule 4: Required Mods Must Be Compatible
All required dependencies must not conflict with each other.

---

## Conditions for Invalid Configurations

A mod dependency graph is considered invalid if any of the following conditions are met:

- A dependency is declared but not satisfied  
- A mod has conflicting relationships with another mod  
- Version constraints are unsatisfiable  
- Required dependencies are mutually incompatible  

---

## Role of Consistency

Consistency ensures that the dependency graph represents a valid and executable configuration. Without consistency:

- The system may fail to resolve dependencies  
- The modpack may not load correctly  
- Conflicts may cause runtime errors or instability  

Maintaining consistency allows for predictable and reliable behavior of the system.

---

## Conclusion

By defining conflict scenarios, consistency rules, and invalid conditions, we establish a clear conceptual framework for evaluating mod dependency graphs. This ensures that configurations are logically sound and aligned with requirements evaluation principles.