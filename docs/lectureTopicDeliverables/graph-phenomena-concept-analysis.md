## Phenomena and Concepts in the Mod Dependency Domain

## Relation to Lecture Topic

This document applies Phenomena and Concepts analysis to the mod dependency domain.

Phenomena and Concepts distinguishes between observable instances within a domain (phenomena) and abstractions used to classify and reason about those instances (concepts).

Within the mod dependency domain, individual mods, dependency declarations, and incompatibility declarations are phenomena, while categories such as mods, dependencies, incompatibilities, and dependency graphs are concepts used to model the domain.

## Relation to Domain Facet Analysis

This document contributes to domain analysis by identifying and classifying the phenomena and concepts that exist within the Minecraft mod dependency domain.

These classifications provide a foundation for later domain modeling activities such as graph construction, dependency validation, and conflict analysis.

## Introduction

The Minecraft mod dependency domain contains various entities, relationships, events, and behaviors that must be understood before constructing a dependency graph.

Phenomena and Concepts analysis helps identify these elements and organize them according to their level of abstraction.

This document describes the primary phenomena and concepts present in the mod dependency domain.

## Domain Phenomena

1. Individual Mods
Specific mods installed within a modpack are phenomena because they are concrete instances that exist within the domain.

Examples include:
> Fabric API
> Sodium
> JEI

Each mod has observable properties such as:
> identifier
> version
> dependency metadata

2. Dependency Declarations
A dependency declaration between two specific mods is a phenomenon.

Examples include:
> Sodium requires Fabric API
> Mod A requires Mod B

These declarations can be observed directly within mod metadata.

3. Incompatibility Declarations
An incompatibility declaration between specific mods is also a phenomenon.

Examples include:
> Mod A conflicts with Mod B
> Mod C conflicts with Mod D

## Domain Concepts

1. Mods
The concept of a mod represents the general category of software modifications that extend Minecraft functionality.

Individual mods are phenomena belonging to this concept.

2. Dependencies
Dependencies are conceptual relationships that describe requirements between mods.

Specific dependency declarations are instances of this concept.

3. Incompatibilities
Incompatibilities represent the concept of conflict relationships between mods.

Specific conflicts are phenomena that belong to this category.

4. Dependency Graph
The dependency graph is an abstract concept used to represent relationships between mods and analyze their interactions.

## Role of Phenomena and Concepts Analysis

Phenomena and Concepts analysis helps identify:
> relevant domain entities
> relevant relationships
> domain abstractions
> observable domain instances

Without these distinctions, it becomes difficult to organize and model the dependency domain consistently.

## Summary

This document defines:

> domain phenomena such as individual mods and dependency declarations
> domain concepts such as mods, dependencies, incompatibilities, and dependency graphs

These elements provide a conceptual foundation for understanding the Minecraft mod dependency domain.
