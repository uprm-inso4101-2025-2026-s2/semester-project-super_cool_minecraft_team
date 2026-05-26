= Team B: Concurrent Dependency Resolution Modelled as a Place/Transition Network (PTN)
Team B
ICOM4009 | INSO4101 | Introduction to Software Engineering
University of Puerto Rico at Mayagüez
:doctype: article
:toc:
:toclevels: 3

---

== Overview

*Feature:* Missing Dependencies +
*Team:* Team B +
*Document Type:* Lecture Topic Task — Petri Nets, Place/Transition Networks +
*Related Issues:* #467 (PTN for the API fetcher sub-net), CEN document
(`team-b-dependency-validation-cen.adoc`) +
*Related Classes:* `DependencyResolverService`, `DependencyApiClient`,
`DependencyParser`, `DependencyCacheService` +
*Related DTOs:* `ResolvedDependencyDto`

=== Purpose

This document models concurrent dependency resolution across Team B's
four-class service layer as a Place/Transition Network (PTN), as defined
in the Petri Nets lecture. It defines all places with their initial token
counts and capacities, all transitions with their input and output arc
weights, the initial marking, two distinct blocked markings illustrating
the effect of the API slot capacity constraint, and a written description
mapping each place and transition to its corresponding class and method.

---

== 1. Motivation: Why PTN for This Layer

The CEN document (`team-b-dependency-validation-cen.adoc`) modeled the
validation coordinator in `DependencyResolverService.validate()` as a
Condition/Event Network because that layer operates on binary states:
each dependency is either missing or present, and each check either has
or has not been performed.

This document models a different concern: what happens when multiple
missing dependencies are resolved concurrently, sharing a single pool of
outbound API call slots. This is a resource-sharing problem with
multiplicity. Places must hold counts (how many deps are pending, how many
JSON responses are in flight, how many DTOs have been written to the
cache). Transitions consume and produce multiple tokens per firing when
multiple pipelines are active. Arc weights encode how many tokens are
consumed or produced per transition firing. These are all properties
that require PTN, not CEN.

The slot pool place (P-SLOT) is the central shared resource. Its capacity
is set to N=3, modeling the practical limit of simultaneous outbound
Modrinth API calls before throttling risk increases. Every `dispatch fetch`
transition consumes one token from P-SLOT and returns it upon completion,
ensuring that at most N resolution pipelines are in the fetch phase
simultaneously regardless of how many dependencies are pending.

---

== 2. Places

A place in a PTN can hold multiple tokens. The initial token count
reflects the system state at the start of a resolution cycle for a
modpack with two missing mandatory dependencies: sodium (`AANobbMI`) and
lithium (`svB5oeCS`).

[cols="1,2,2,1,1"]
|===
|ID |Label |Mapped to codebase |Initial tokens |Capacity

|P-SLOT
|API slot pool
|`DependencyApiClient.fetchVersionMetadata()` — each token represents one
available outbound API call slot. Consumed by dispatch transitions,
returned on completion.
|3
|3

|PA1
|Pipeline A: pending resolution
|One entry in the `missingDependencies` list inside `validate()` for
sodium (`AANobbMI`). Token present when the dep has been identified as
missing and mandatory but resolution has not started.
|1
|inf

|PA2
|Pipeline A: raw JSON response in flight
|Return value of `DependencyApiClient.fetchVersionMetadata("AANobbMI",
"fabric", "1.20.1")`. Token present when the JSON string has been
received and is awaiting parsing.
|0
|inf

|PA3
|Pipeline A: preferred URL ready
|Return value of `DependencyParser.extractPreferredUrl(json, "AANobbMI")`.
Token present when the URL string has been extracted (or null fallback
determined) and is awaiting DTO construction.
|0
|inf

|PA4
|Pipeline A: DTO in memory
|`ResolvedDependencyDto` instance produced by `DependencyParser.buildDto()`.
Token present when the DTO has been constructed and is awaiting cache write.
|0
|inf

|PA5
|Pipeline A: cached
|`DependencyCacheService.put("AANobbMI", dto)` has been called. Token
present when the result is durably held in the cache map.
|0
|inf

|PB1
|Pipeline B: pending resolution
|One entry in the `missingDependencies` list for lithium (`svB5oeCS`).
Same semantics as PA1 for the second pipeline.
|1
|inf

|PB2
|Pipeline B: raw JSON response in flight
|Return value of `DependencyApiClient.fetchVersionMetadata("svB5oeCS",
"fabric", "1.20.1")`.
|0
|inf

|PB3
|Pipeline B: preferred URL ready
|Return value of `DependencyParser.extractPreferredUrl(json, "svB5oeCS")`.
|0
|inf

|PB4
|Pipeline B: DTO in memory
|`ResolvedDependencyDto` for lithium, produced by `DependencyParser.buildDto()`.
|0
|inf

|PB5
|Pipeline B: cached
|`DependencyCacheService.put("svB5oeCS", dto)` has been called.
|0
|inf

|P-RES
|Resolved pool
|The `resolvedDependencies` list accumulated inside `validate()`. Each
token represents one fully resolved and cached `ResolvedDependencyDto`
ready to be included in the `ValidationResponse`.
|0
|inf

|===

---

== 3. Transitions

A PTN transition fires when all input places have sufficient tokens
(at least equal to the arc weight) and all output places have sufficient
remaining capacity (current tokens plus arc weight does not exceed the
place capacity). Firing removes tokens from input places and delivers
tokens to output places according to arc weights.

[cols="1,2,2,2,2"]
|===
|ID |Label |Method called |Input arcs (place, weight) |Output arcs (place, weight)

|TA1
|Dispatch API fetch for sodium
|`DependencyApiClient.fetchVersionMetadata("AANobbMI", "fabric", "1.20.1")`
|(PA1, 1), (P-SLOT, 1)
|(PA2, 1), (P-SLOT, 1)*

|TA2
|Extract preferred URL for sodium
|`DependencyParser.extractPreferredUrl(json, "AANobbMI")`
|(PA2, 1)
|(PA3, 1)

|TA3
|Build DTO for sodium
|`DependencyParser.buildDto(dep, preferredUrl)`
|(PA3, 1)
|(PA4, 1)

|TA4
|Write sodium DTO to cache
|`DependencyCacheService.put("AANobbMI", dto)`
|(PA4, 1)
|(PA5, 1)

|TB1
|Dispatch API fetch for lithium
|`DependencyApiClient.fetchVersionMetadata("svB5oeCS", "fabric", "1.20.1")`
|(PB1, 1), (P-SLOT, 1)
|(PB2, 1), (P-SLOT, 1)*

|TB2
|Extract preferred URL for lithium
|`DependencyParser.extractPreferredUrl(json, "svB5oeCS")`
|(PB2, 1)
|(PB3, 1)

|TB3
|Build DTO for lithium
|`DependencyParser.buildDto(dep, preferredUrl)`
|(PB3, 1)
|(PB4, 1)

|TB4
|Write lithium DTO to cache
|`DependencyCacheService.put("svB5oeCS", dto)`
|(PB4, 1)
|(PB5, 1)

|===

*The slot return arc (P-SLOT, 1) on TA1 and TB1 fires when the API call
completes and control returns from `fetchVersionMetadata()`. In the model
this is represented as the dispatch transition having both an input arc
from P-SLOT (consuming 1 slot on start) and an output arc back to P-SLOT
(returning 1 slot on completion). See Section 4 for the blocked marking
that demonstrates what happens when all 3 slots are occupied.

In addition, once PA5 and PB5 are both marked (both pipelines cached),
a final aggregation transition fires to move both tokens into P-RES,
representing the accumulation of the `resolvedDependencies` list:

[cols="1,2,2,2,2"]
|===
|ID |Label |Method called |Input arcs |Output arcs

|T-AGG
|Aggregate resolved DTOs into response list
|`resolvedDependencies.add(dto)` inside `validate()` loop; then
`new ValidationResponse(...)` at loop end
|(PA5, 1), (PB5, 1)
|(P-RES, 2)

|===

---

== 4. Markings

=== 4.1 Initial Marking

M0 = { P-SLOT: 3, PA1: 1, PB1: 1, all others: 0 }

Both pipelines have one pending dependency. Three API slots are available.
TA1 and TB1 are both activated: PA1 has 1 token (weight 1 satisfied),
P-SLOT has 3 tokens (weight 1 satisfied), PA2 has 0 tokens and remaining
capacity is infinite (output capacity satisfied). Both transitions can
fire concurrently. This is the normal operating state at the start of a
multi-dependency resolution cycle.

=== 4.2 Blocked Marking: API slot pool exhausted

Suppose a modpack upload triggers resolution of 4 missing dependencies
simultaneously (sodium, lithium, fabric-api, cloth-config). The initial
marking would be:

M-BLOCK = { P-SLOT: 3, PA1: 1, PB1: 1, PC1: 1, PD1: 1, all others: 0 }

After TA1, TB1, and TC1 have each consumed one slot from P-SLOT:

M-BLOCK-2 = { P-SLOT: 0, PA2: 1, PB2: 1, PC2: 1, PD1: 1, all others: 0 }

TD1 (the dispatch transition for cloth-config) is blocked: its input arc
from P-SLOT requires 1 token, but P-SLOT holds 0 tokens. The capacity
constraint is active. The fourth pipeline cannot enter the fetch phase
until one of TA1, TB1, or TC1 completes and returns its slot token to
P-SLOT. This is the PTN encoding of the API throttle behavior described
in the consistency finding I-2 of the Requirements Validation Report.

=== 4.3 Final Marking

MF = { P-RES: 2, all others: 0 }

Both pipelines have completed. PA5 and PB5 each held 1 token; T-AGG fired
consuming both and producing 2 tokens in P-RES. The `resolvedDependencies`
list in `validate()` contains two `ResolvedDependencyDto` instances. A
`ValidationResponse` can now be constructed and returned.

---

== 5. Arc Weights and Capacity Summary

[cols="2,1,1,1"]
|===
|Arc |Weight |Source/target capacity |Notes

|PA1 → TA1
|1
|inf
|Default weight; one dep per pipeline

|P-SLOT → TA1
|1
|3
|Slot consumed on fetch start; capacity enforces max N concurrent fetches

|TA1 → PA2
|1
|inf
|Default

|TA1 → P-SLOT
|1
|3
|Slot returned on fetch completion

|PA2 → TA2 through PA5
|1 each
|inf each
|All parsing and cache arcs are weight 1

|PA5, PB5 → T-AGG
|1 each
|inf
|Both pipelines must complete before aggregation fires

|T-AGG → P-RES
|2
|inf
|Produces 2 tokens (one per resolved dep) in the shared result pool

|===

All arcs not listed explicitly carry the default weight of 1 and connect
to places with infinite capacity.

---

== 6. Relationship to Other Team B PTN and CEN Documents

This document is the second of three Petri Net models produced by Team B.

Issue #467 modeled the API fetcher sub-net (P8 to P9) as a PTN focused
on the internal structure of a single `fetchVersionMetadata()` call:
the cache-hit fast path, the Modrinth API fetch path, and the error/retry
path. That model treats the fetch operation as a black box from the
outside and details its internals. The transitions TA1 and TB1 in this
document correspond to the entry point of that sub-net; the P-SLOT
mechanism here is the resource-level complement to the guard conditions
documented in #467.

The CEN document models the validation coordinator layer in
`DependencyResolverService.validate()` using binary conditions. This PTN
document models the resource layer beneath that coordinator: how many
tokens (deps, responses, DTOs) are in flight at once and how the shared
slot pool gates concurrency. The two models are complementary at different
levels of abstraction.

The three models together cover the full resolution stack:

[cols="1,2,2"]
|===
|Document |Formalism |Layer covered

|Issue #467
|PTN
|Internal fetch sub-net: cache hit, API call, retry, error path for a
single dependency

|`team-b-dependency-validation-cen.adoc`
|CEN
|Validation coordinator: binary state transitions for one dependency from
mod received to `ValidationResponse` returned

|This document
|PTN
|Concurrent resource layer: token flow across multiple simultaneous
pipelines, API slot pool capacity constraint, aggregation into the resolved
pool

|===

---

== References

* Petri Nets lecture, Marko Schütz-Schmuck, University of Puerto Rico at
  Mayagüez, October 15, 2020
* Issue #467 — Mod Dependency API Fetcher modelled as a Petri Net
  (Team B, Milestone 2)
* `team-b-dependency-validation-cen.adoc` (Team B, Milestone 2)
* `team-b-requirements-validation-report.adoc` — Finding I-2 (retry vs.
  graceful degradation under throttle conditions)
* `DependencyResolverService.java` — `service/resolver/`
* `DependencyApiClient.java` — `service/client/`
* `DependencyParser.java` — `service/parser/`
* `DependencyCacheService.java` — `service/cache/`

---