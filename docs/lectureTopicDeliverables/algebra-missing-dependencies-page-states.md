# Abstract Algebra for Missing Dependencies Page States

An **algebra** is defined as (A, Ω):
- **A** (carrier set) — the elements/values in the system
- **Ω** (operations) — functions that take elements from A and produce elements from A (closed)

A concrete example: integers with (+, -, *). An abstract example: stacks with (push, pop, top).

This document defines an algebra for the Missing Dependencies page states, then maps each element and operation to the actual source code.

## Carrier Set A

A = `PageState ∪ DependencyData ∪ Query ∪ ErrorMessage`

### PageState

| Element | Meaning |
|---------|---------|
| `Idle` | Page rendered, before `DOMContentLoaded` |
| `Loading` | Fetching `/api/missing-dependencies` |
| `Results` | Dependencies loaded and rendered as cards |
| `PartialResults` | Some deps resolved, `#partial-results-banner` visible |
| `Empty` | No missing dependencies, `#empty-state` visible |
| `Error` | Request failed, `#error-state` visible |
| `NoAnalysis` | No DTO in session |
| `Filtered` | Search active, some cards hidden |

### DependencyData
API response with `missingDependencies[]`, `resolvedDependencies[]`, `analysisHasPartialResults`

### Query
Search string typed by user

### ErrorMessage
Error text from failed request

## Operations Ω

| Operation | Input → Output | Code |
|-----------|---------------|------|
| `init` | `() → Loading` | `DOMContentLoaded` → `missingDependenciesPage()` |
| `displayResults` | `(Loading, DependencyData) → Results` | `dependencyList.appendChild(createDependencyCard())` |
| `displayPartial` | `(Loading, DependencyData) → PartialResults` | `partialResultsBanner.hidden = false` |
| `displayEmpty` | `(Loading) → Empty` | `emptyState.hidden = false` |
| `displayNoAnalysis` | `(Loading) → NoAnalysis` | Backend: `payload.put("error", ...)` |
| `displayError` | `(Loading, ErrorMessage) → Error` | `errorState.textContent = error.message` |
| `search` | `(Results \| PartialResults, Query) → Filtered` | `searchInput.addEventListener("input", ...)` |
| `clearSearch` | `(Filtered) → Results` | `clearButton.addEventListener("click", ...)` |
| `navigateAway` | `(PageState) → Idle` | User clicks navigation links |

## Valid State Flow

The algebra only allows certain sequences. These are the valid paths the page can follow:

```
Idle → Loading → Results → Filtered → Results
               → PartialResults → Filtered → Results
               → Empty
               → NoAnalysis
               → Error
Error → (user leaves page) → Idle
```

Invalid sequences (not allowed by the algebra):
- `search(Empty, q)` — can't search when there's nothing to search
- `displayResults(Results, d)` — can't show results twice
- `retry` is not currently defined since there's no retry button yet
