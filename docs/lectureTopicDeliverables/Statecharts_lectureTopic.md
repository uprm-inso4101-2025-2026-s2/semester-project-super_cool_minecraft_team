# Lecture Topic Task: Frontend Lifecycle Statechart Specification
**Author:** Javier Aguilo 
**Role:**  Backend team leader
**Associated Issue:** # 714  

---

## 1. Objective & Context
The objective of this individual task is to apply the **Statecharts** framework from the course lecture to model the formal behavioral lifecycles of the frontend dependency graph UI component.

According to our frontend `README.md`, the UI moves dynamically through distinct computational stages as users interact with nodes, use search parameters, or load mock configurations. Creating a formal statechart eliminates race conditions—ensuring events like "Node Click" or "Search Input" only fire when the system has securely transitioned into a stable state.

---

## 2. State Machine Decomposition

The lifecycle of our dependency visualization canvas is modeled via 4 main execution states.

### Operational States
1. `UNINITIALIZED [Initial State]`: The container element in `index.html` is allocated, but the graph engine library (e.g., Cytoscape/D3) has not yet reserved memory or mapped event listeners.
2. `IDLE_EMPTY`: The library is fully configured, zoom boundaries are clamped, and the system is awaiting a validated JSON data stream.
3. `ACTIVE_VISUALIZED`: Validated mod entities are rendered on screen as visible nodes and interactive dependency edges.
4. `NODE_SELECTED [Sub-state / Focus state]`: A single node is actively targeted. Upstream dependencies are visually highlighted while irrelevant metadata is hidden in the side panel view.

---

## 3. State Transition Matrix

The table below maps the exact mathematical reactive properties of our frontend graph interactions:

| Source State | Event Trigger | Guard Condition / Action | Target Next State |
| :--- | :--- | :--- | :--- |
| `UNINITIALIZED` | `DOMContentReady` | None / Execute UI library initialization script | `IDLE_EMPTY` |
| `IDLE_EMPTY` | `onDataLoadSuccess` | `[if validate(input) == true]` / Map inputs to elements and display | `ACTIVE_VISUALIZED` |
| `IDLE_EMPTY` | `onDataLoadSuccess` | `[if validate(input) == false]` / Alert UI validation error | `IDLE_EMPTY` |
| `ACTIVE_VISUALIZED`| `onNodeClick` | None / Open side info panel & dim non-adjacent edges | `NODE_SELECTED` |
| `NODE_SELECTED` | `onResetClick` | None / Clear side panel metadata & restore default pan/zoom | `ACTIVE_VISUALIZED` |
| `ACTIVE_VISUALIZED`| `onSearchSubmit` | `[if matchFound == true]` / Animate camera focus onto node location | `NODE_SELECTED` |

---

## 4. Formal Transition Guard Implementation
A crucial element of the Statechart lecture is the use of conditional **Guards** to govern valid runtime pathways. In our system, this is explicitly handled by our validation routine before moving from data staging to visual layout projection:

```javascript
// Sample execution block showcasing the Statechart Transition Guard in script.js
function handleDataIngestionEvent(rawJsonPayload) {
    try {
        // Guard Condition Assertion
        validate(rawJsonPayload); 
        
        // Action on Successful Guard evaluation: Move state to ACTIVE_VISUALIZED
        graphRenderer.render(rawJsonPayload);
        this.transitionTo('ACTIVE_VISUALIZED');
    } catch (error) {
        // Fallback Action on Guard failure: Enforce state loop to IDLE_EMPTY
        uiController.displayErrorBadge(error.message);
        this.transitionTo('IDLE_EMPTY');
    }
}