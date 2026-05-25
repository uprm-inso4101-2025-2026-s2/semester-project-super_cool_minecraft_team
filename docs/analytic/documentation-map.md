# Documentation Map: Minecraft Dependency Visualizer

## 1. Introduction and Objective
This document provides a formal mapping of the project's visual and technical assets to the **Software Engineering Triptych** (Domain, Requirements, Design). Its goal is to provide **traceability** between the initial project proposal and the current frontend implementation, serving as an **Analytic Document** for the system.

## 2. Stakeholder Perspectives
In accordance with the *Documents* lecture, this project addresses three primary perspectives:
* **The User (Modpack Creator):** Needs to identify conflicts and missing dependencies to stabilize a modpack environment.
* **The Developer (Team Member):** Needs to understand the data flow from JSON to the Graph UI as outlined in the `README.md`.
* **The Manager (Project Oversight):** Needs to verify that the "Core Events" (Search, Reset, Filter) align with the Milestone goals.

## 3. The Triptych Mapping

### 3.1 Domain Description
* **Definition:** Describing the existing entities in the Minecraft modding world before software is applied.
* **Project Entities:** * **Mods:** Individual software packages with unique identifiers and versions.
    * **Dependency Edges:** Relationships where Mod A requires Mod B to function.
    * **Conflict Edges:** Rules where Mod A and Mod C are mutually exclusive.
* **Source of Truth:** Section 3.1 of `Proposal.adoc` (Data Parsing and Graph Construction).

### 3.2 Requirements Prescription
* **Definition:** The "Software-to-be"; what the machine must do to satisfy stakeholders.
* **Project Features:**
    * **Sidebar Filtering:** Must allow users to toggle visibility for "Required," "Optional," and "Installed" mods (as seen in Sidebar Mockup).
    * **Graph Navigation:** Users must be able to search for specific nodes and center the view (as defined in `README.md` Core Events).
    * **Cycle Detection:** The system must identify circular dependencies (as specified in `Proposal.adoc` Section 3.2).
* **Source of Truth:** Section 3.2 of `Proposal.adoc` (Validation Walkthrough).

### 3.3 Software Design
* **Definition:** The architecture and internal structure that realizes the requirements.
* **Architecture Layers (from `README.md`):**
    * **HTML Layer:** Manages global structure (Header, Side panel, Graph container).
    * **CSS Layer:** Handles visual themes and color-coding for dependency states.
    * **JS/Renderer Layer:** Manages the "Graph Lifecycle" (Init → Render → Update).
* **Source of Truth:** Frontend Architecture Overview in `README.md`.

## 4. Verification and Theory Formation
Following the *Phases of Software Engineering* lecture, we verify that the **Software Design** implies the **Requirements Prescription** under the assumptions of the **Domain Description**.

| Phase | Asset | Verification Method |
| :--- | :--- | :--- |
| **Requirements** | Sidebar Design Mockup | Stakeholder review of filtering logic. |
| **Design** | Dependency Graph Mockup | Visual inspection of node-link hierarchy. |
| **Implementation** | `README.md` (Data Flow) | Unit testing of the JSON → Validation pipeline. |

## 5. Conclusion
By mapping our mockups and documentation to these formal phases, we ensure that every UI element (such as the "2D Interactive" view) serves a specific requirement derived from the Minecraft modding domain.