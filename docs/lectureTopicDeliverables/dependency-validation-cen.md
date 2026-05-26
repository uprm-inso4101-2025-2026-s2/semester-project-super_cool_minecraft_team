= Team B: Dependency Validation Flow as a Condition/Event Network (CEN)
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
*Document Type:* Lecture Topic Task — Petri Nets, Condition/Event Networks +
*Related Issue:* #467 (PTN model for the API fetcher sub-net) +
*Related Classes:* `DependencyResolverService`, `DependencyApiClient`,
`DependencyParser`, `DependencyCacheService` +
*Related Files:* `missing-dependencies.html`

=== Purpose

This document models the dependency validation flow in
`DependencyResolverService.validate()` as a Condition/Event Network (CEN),
as defined in the Petri Nets lecture. It defines every condition, every event,
and the pre-condition and post-condition sets that govern when each event may
fire. It identifies the initial and final markings, records two distinct
reachable intermediate markings, performs a deadlock analysis, and documents
how this CEN model relates to and differs from the PTN model produced in
issue #467.

---

== 1. CEN Definitions

=== 1.1 Conditions

A condition in a CEN is a binary state: it is either fulfilled (marked) or
unfulfilled (unmarked). The following conditions cover the full execution of
`DependencyResolverService.validate()` for a single dependency.

[cols="1,2,3"]
|===
|ID |Label |Mapped to codebase

|C1
|Mod received
|`validate(Mod mod)` has been called; `mod` and `mod.getDepends()` are
available in scope.

|C2
|Mandatory dependency identified as missing
|`modRepository.findById(dep.getId())` returned empty and `dep.isMandatory()`
is true.

|C3
|API call completed
|`DependencyApiClient.fetchVersionMetadata()` has returned (either a JSON
string or null).

|C4
|Preferred URL extracted
|`DependencyParser.extractPreferredUrl()` has returned a non-null URL string
or null (fallback path).

|C5
|DTO constructed
|`DependencyParser.buildDto()` has produced a `ResolvedDependencyDto` instance.

|C6
|Result stored in cache
|`DependencyCacheService.put(dep.getId(), dto)` has been called.

|C7
|Dependency present or not mandatory
|`modRepository.findById(dep.getId())` returned a non-empty result, or
`dep.isMandatory()` is false.

|C8
|Circular dependency check complete
|`detectCircularDependency()` has finished traversing the dependency graph for
the root mod.

|C9
|`ValidationResponse` constructed (final state)
|`new ValidationResponse(missingDependencies, circularDependencies,
resolvedDependencies)` has been returned to the caller.

|===

=== 1.2 Events

An event in a CEN fires when all its pre-conditions are fulfilled and all its
post-conditions are simultaneously unfulfilled. Firing unfulfills every
pre-condition and fulfills every post-condition atomically.

[cols="1,2,3,3"]
|===
|ID |Label |Pre-conditions |Post-conditions

|E1
|Check dependency presence in `modRepository`
|{C1}
|{C2} xor {C7} (exactly one is fulfilled depending on the repository result)

|E2
|Invoke `DependencyApiClient.fetchVersionMetadata()`
|{C2}
|{C3}

|E3
|Extract URL via `DependencyParser.extractPreferredUrl()`
|{C3}
|{C4}

|E4
|Build DTO and store in cache via `DependencyParser.buildDto()` and
`DependencyCacheService.put()`
|{C4}
|{C5, C6}

|E5
|Skip resolution (dependency present or not mandatory)
|{C7}
|{C8} (the dep contributes nothing to the missing list; circular check
can proceed immediately)

|E6
|Run `detectCircularDependency()`
|{C5, C6}
|{C8}

|E7
|Construct and return `ValidationResponse`
|{C8}
|{C9}

|===

*Note on E1:* In the CEN formalism, a single event produces exactly one set
of post-conditions. E1 has two mutually exclusive outcomes. This is modeled
as two separate post-condition paths: the resolution path (E1 fulfills C2)
and the skip path (E1 fulfills C7). In the diagram these are shown as two
arcs out of E1 with a dashed arc indicating the skip branch.

---

== 2. Pre-condition and Post-condition Sets (Explicit Verification)

The CEN rules require that:

. No condition appears as both a pre-condition and a post-condition of the
  same event.
. No event appears in the pre- or post-condition set of another event (arcs
  connect only conditions to events or events to conditions).

Verification against each event:

* *E1* pre: {C1}; post: {C2} or {C7}. C1 is in neither post-set.
  No condition appears on both sides. Rule satisfied.
* *E2* pre: {C2}; post: {C3}. C2 and C3 are disjoint. Rule satisfied.
* *E3* pre: {C3}; post: {C4}. C3 and C4 are disjoint. Rule satisfied.
* *E4* pre: {C4}; post: {C5, C6}. C4 is in neither post-set. Rule satisfied.
* *E5* pre: {C7}; post: {C8}. C7 and C8 are disjoint. Rule satisfied.
* *E6* pre: {C5, C6}; post: {C8}. No overlap. Rule satisfied.
* *E7* pre: {C8}; post: {C9}. C8 and C9 are disjoint. Rule satisfied.

No arc in the diagram connects two conditions or two events directly.
All arcs run condition → event or event → condition. CEN structural rules
are satisfied.

---

== 3. Markings

=== 3.1 Initial Marking

M0 = {C1}

Only C1 is fulfilled. The mod has been passed to `validate()` but no
processing has started. E1 is the only activated event (all its
pre-conditions are fulfilled and all its post-conditions are unfulfilled).

=== 3.2 Reachable Marking 1: Resolution path in progress

M1 = {C2}

E1 has fired on the resolution branch: C1 is unfulfilled, C2 is fulfilled.
E2 is the only activated event. The system is waiting to call
`DependencyApiClient.fetchVersionMetadata()`.

=== 3.3 Reachable Marking 2: DTO built, awaiting circular check

M2 = {C5, C6}

E4 has fired: C4 is unfulfilled, C5 and C6 are both fulfilled. E6 is the
only activated event. The `ResolvedDependencyDto` is in memory and the cache
has been written. `detectCircularDependency()` has not yet run.

=== 3.4 Final Marking

MF = {C9}

E7 has fired: C8 is unfulfilled, C9 is fulfilled. `ValidationResponse` has
been returned. No event is activated. The net has reached its terminal state.

---

== 4. Deadlock Analysis

A deadlock in a CEN is a reachable marking in which no event is activated
and the final condition is not yet fulfilled.

The resolution path (C1 → E1 → C2 → E2 → C3 → E3 → C4 → E4 → {C5, C6} →
E6 → C8 → E7 → C9) and the skip path (C7 → E5 → C8 → E7 → C9) both
terminate at C9. There is no reachable marking on either path in which the
chain is broken under normal execution.

One latent deadlock exists and corresponds directly to Finding R-3 in the
Requirements Validation Report (`team-b-requirements-validation-report.adoc`):
if `DependencyApiClient.fetchVersionMetadata()` returns null (API
unreachable) and `DependencyParser.extractPreferredUrl()` also returns null,
the system currently falls back silently in `buildDto()` and continues. In
CEN terms this is not a structural deadlock because the fallback ensures C4
is fulfilled regardless. However, if the fallback behavior were removed (as
it could be, given that it is not stated as a requirement anywhere in the
Proposal), the fulfillment of C4 would depend on a null check with no
specified outcome, and the net could arrive at a marking where C3 is
fulfilled but E3 cannot fire because there is no defined post-condition for
a null URL. This is the condition identified in Finding C-2 and addressed by
DR-11 in the Requirements Validation Report.

No other deadlock-producing marking is reachable under the current
implementation.

---

== 5. Distinction from the PTN Model in Issue #467

Issue #467 modeled the API fetcher sub-net (places P8 to P9) as a
Place/Transition Network. That model was the correct choice for the fetch
layer because: places in that sub-net hold counts of in-flight requests,
arrows carry weights reflecting how many tokens are consumed per retry
attempt, and parallel fork/join behavior at T7 and P9 requires a formalism
that can represent more than one token in a place simultaneously.

None of those properties apply to the validation layer modeled in this
document. Each dependency is either missing or present (binary, not counted).
Each check either has or has not been performed (binary). There is no
parallel fan-out within `validate()` and no weighted consumption of
resources. The activation condition for every event in this net reduces to
a simple boolean: are all pre-conditions marked and all post-conditions
unmarked? CEN is the minimal formalism that captures this layer correctly.
Using a PTN here would introduce weights of 1 on every arc and capacity of
infinity on every place, which adds no information and obscures the binary
nature of the conditions.

The two models are complementary: the PTN in #467 documents how the fetcher
manages resources and concurrency within a single resolution step; this CEN
documents how the validation coordinator sequences those steps and decides
which path to take.

---

== References

* Petri Nets lecture, Marko Schütz-Schmuck, University of Puerto Rico at
  Mayagüez, October 15, 2020
* Issue #467 — Mod Dependency API Fetcher modelled as a Petri Net (Team B,
  Milestone 2)
* `team-b-requirements-validation-report.adoc` (Team B, Milestone 2/3)
* `DependencyResolverService.java` — `service/resolver/`
* `DependencyApiClient.java` — `service/client/`
* `DependencyParser.java` — `service/parser/`
* `DependencyCacheService.java` — `service/cache/`

---
