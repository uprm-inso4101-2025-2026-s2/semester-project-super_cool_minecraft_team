## Distinction from PTN in Issue #467
    The API fetcher sub-net modeled in #467 uses a Place/Transition Network (PTN) because it tracks weighted token flow, retry counts, and capacities (e.g., multiple pending requests, token accumulation for retries). That layer deals with quantitative state.

By contrast, the validation layer in DependencyResolverService.validate() operates on purely binary states:

A dependency is either missing or present.

A check is either complete or pending.

The overall result is either resolved or failed.

Therefore, this layer is modeled as a Condition/Event Network (CEN) where:

Each condition holds at most one mark (fulfilled / unfulfilled).

Events fire atomically when all pre-conditions are fulfilled and all post-conditions are simultaneously unfulfilled.

Firing flips pre‑conditions to unfulfilled and post‑conditions to fulfilled.

No weights, no capacities, no multi-token counts.

This binary, non‑quantitative structure makes CEN the correct and minimal formalism for the validation layer.



## Event Pre‑condition and Post‑condition Sets

Event	Pre‑conditions (all must be fulfilled)	Post‑conditions (all simultaneously unfulfilled before firing; become fulfilled after)

- E1: Check dependency in modRepository	C1 (Mod received), C9 (Dependency known in modRepository) C9 (flips back to unfulfilled – transient), C2 (Mandatory dependency identified as missing)

- E2: Invoke fetchVersionMetadata()	C1 (Mod received), C2 (Missing dependency)	C3 (API call completed)
- E3: Extract URL	C3 (API call completed)	C4 (URL extracted)
- E4: Build and cache DTO	C4 (URL extracted), C6 (Cache stored – initially unfulfilled)	C5 (DTO constructed), C6 (Cache stored – becomes fulfilled)
- E5: Run detectCircularDependency()	C5 (DTO constructed), C1 (Mod received)	C7 (Circular check completed), C10 (No circular dependency detected)
- E6: Construct ValidationResponse	C7 (Circular check completed), C8 (ValidationResponse returned – initially unfulfilled)	C8 (ValidationResponse returned – becomes fulfilled)

## Initial and Final Markings
Initial Marking (before any event fires)

Condition	State	Reason
C1	Fulfilled	Mod has been received by validator.
C9	Fulfilled	Dependency is known in modRepository (pre‑existing knowledge).
C2	Unfulfilled	No missing dependency identified yet.
C3	Unfulfilled	API not called.
C4	Unfulfilled	No URL extracted.
C5	Unfulfilled	No DTO built.
C6	Unfulfilled	Cache not stored.
C7	Unfulfilled	Circular check not run.
C10	Unfulfilled	Circular status unknown.
C8	Unfulfilled	Response not yet returned.
Initial marking set: {C1, C9} fulfilled. All others unfulfilled.

Final Marking (validation succeeded)
Condition	State
C1	Unfulfilled (consumed by E1/E2/E5)
C2	Unfulfilled (consumed after E2)
C3	Unfulfilled (consumed after E3)
C4	Unfulfilled (consumed after E4)
C5	Unfulfilled (consumed after E5)
C6	Fulfilled (cache stored, persists)
C7	Unfulfilled (consumed after E6)
C8	Fulfilled (response returned)
C9	Unfulfilled
C10	Fulfilled (no circular dependency)
Final marking set: {C6, C8, C10} fulfilled. All others unfulfilled.

5. Reachable Marking with No Event Activated & Final Not Fulfilled
Deadlock / stuck state (not final, but no event can fire):

Condition	State
C5	Fulfilled (DTO constructed)
C6	Unfulfilled (cache not stored — but E4 already fired once, C6 stays unfulfilled? Wait — E4 flips C6 to fulfilled. Let’s correct: This stuck state is after E4 but before E5 if C1 is already unfulfilled.)
Actually, by construction, the only reachable non‑final marking with no enabled event is:

C5 = Fulfilled (DTO built)

C6 = Fulfilled (cache stored)

C1 = Unfulfilled (mod already consumed earlier)

C7 = Unfulfilled

C8 = Unfulfilled

No event is activated because:

E5 requires C5 (OK) and C1 (fails — C1 is unfulfilled).

E6 requires C7 and C8 — both unfulfilled.

E1–E4 have their pre‑conditions unsatisfied (C3, C4 already consumed).

This is a dead state before circular check — it would require an external reset (new mod received → C1 fulfilled) to continue. In practice, the implementation would trigger a failure response, but in pure CEN terms, it’s a deadlock.