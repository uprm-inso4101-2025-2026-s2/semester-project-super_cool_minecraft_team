= Team B: Domain Facets for the Missing Dependencies Feature
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
*Document Type:* Lecture Topic Task — Domain Engineering, Domain Facets +
*Lecture Topic:* Domain Engineering and Domain Facets +
*Related Issues:* #648, #649 (requirements refactoring) +
*Related Classes:* `DependencyResolverService`, `DependencyApiClient`,
`DependencyParser`, `DependencyCacheService` +
*Related DTOs:* `ResolvedDependencyDto`, `ValidationResponse` +
*Related Files:* `missing-dependencies.html`, `missing-dependencies.css`

=== Purpose

The Domain Engineering lecture defines domain facets as generic perspectives
used to structure the exploration of a domain. Each facet focuses on a
specific aspect: intrinsics, support technology, management and organization,
rules and regulations, scripts, and human behavior. Business procedures are
examined as long-winded behaviors that cut across the other facets and help
analyze them.

This document applies all seven facets to Team B's Missing Dependencies
feature. Each facet entry is concrete and tied to observable properties of
the domain, the codebase, or the user-facing interface. The document is not
a restatement of requirements. It is a characterization of the domain the
feature inhabits, written so that a developer encountering the feature for
the first time can understand what exists independently of the system, what
the system implements, and what constraints govern both.

---

== 1. Intrinsics

The lecture defines intrinsics as the entities without which the domain no
longer exists. For a hospital the intrinsics are beds, doctors, and patients.
Remove any one of them and the hospital domain ceases to be a hospital domain.

For Team B's Missing Dependencies feature the intrinsics are as follows.

A mod::
A self-contained software artifact distributed as a `.jar` file. A mod
declares its own identifier, its target Minecraft version, its target loader
(Fabric, Forge, etc.), and a list of other mods it depends on. Without mods
there is no dependency problem and no domain. In the codebase a mod is
represented by the `Mod` entity, which carries an `id`, a `version`, and a
`getDepends()` list.

A dependency declaration::
A typed relationship between two mods. One mod (the dependent) declares that
it requires another mod (the dependency) in order to function. The declaration
carries a mandatory flag: if mandatory is true and the dependency is absent,
the modpack is structurally invalid. In the codebase this relationship is
expressed as entries in `Mod.getDepends()`. Without dependency declarations
there is nothing to resolve and the feature has no subject matter.

A modpack::
A finite collection of mods assembled by a user for a specific Minecraft
version and loader combination. The modpack is the unit of analysis. Without
a modpack there is no collection to validate and no context for determining
which dependencies are present or absent.

A mod repository::
An external, queryable source of mod metadata and download artifacts. The
Modrinth repository (`https://api.modrinth.com/v2`) is the repository used
by this feature. The repository is what makes automated resolution possible:
without it the system can detect missing dependencies but cannot tell the
user where to obtain them. `DependencyApiClient.fetchVersionMetadata()` is
the sole point of contact with this repository.

A missing dependency::
A mod that has been declared as a mandatory dependency by at least one mod
in the modpack but is not itself present in the modpack. This is the central
entity the feature exists to handle. The `missingDependencies` list inside
`DependencyResolverService.validate()` accumulates these. Without the concept
of a missing dependency the feature has no purpose.

A resolved dependency::
A missing dependency for which the system has located a download URL in the
external repository. Represented as a `ResolvedDependencyDto` containing the
mod ID, a list of links, and a preferred direct download URL. A missing
dependency becomes resolved when `DependencyParser.buildDto()` produces a
non-fallback URL. The distinction between resolved and unresolved is surfaced
in `missing-dependencies.html` via the `status-resolved` and
`status-unresolved` badge classes.

---

== 2. Support Technology

The lecture defines support technology as the means of implementation at
various technology levels. Support technologies implement certain phenomena
and can be modeled to express quality requirements such as accuracy,
reliability, fault tolerance, availability, and accessibility.

Spring Boot (`@Service`, `@Component`)::
The dependency injection framework that wires `DependencyApiClient`,
`DependencyParser`, and `DependencyCacheService` into
`DependencyResolverService`. Spring Boot's container manages the lifecycle
of all four classes. If Spring fails to inject any one of them, the entire
resolution pipeline fails at startup rather than at runtime, which is the
desired fault detection behavior.

`RestTemplate`::
The HTTP client used by `DependencyApiClient` to communicate with the
Modrinth API. `RestTemplate` is a synchronous blocking client. Each call
to `fetchVersionMetadata()` blocks the calling thread until the API responds
or throws an exception. This is a technology-level constraint: the current
implementation cannot resolve multiple dependencies in parallel without
replacing `RestTemplate` with a non-blocking alternative. The PTN document
(`team-b-dependency-resolution-ptn.adoc`) models the concurrency implications
of this constraint.

`ConcurrentHashMap` in `DependencyCacheService`::
The in-memory data structure backing the cache. `ConcurrentHashMap` provides
thread-safe reads and writes without external locking. It is the correct
technology choice for a cache accessed from multiple request threads in a
Spring Boot application. The 30-minute TTL is implemented via lazy eviction:
expired entries are removed on the next `get()` call for that key rather than
by a background thread.

`ObjectMapper` (Jackson) in `DependencyParser`::
The JSON deserialization engine used to traverse the Modrinth API response
tree. `ObjectMapper.readTree()` produces a `JsonNode` graph. The parser
navigates to `rootArray[0].files[n].url` where `n` is the primary file index.
If `ObjectMapper` cannot parse the response (malformed JSON), the exception
is caught and `extractPreferredUrl()` returns null, triggering the browse-link
fallback in `buildDto()`.

Thymeleaf templating (`missing-dependencies.html`)::
The server-side rendering technology that produces the Missing Dependencies
page. Thymeleaf binds `${loader}` from the model to the install guide panel
at render time. The rest of the page state (dependency list, banners,
loading indicator) is managed by the JavaScript module
`frontend-missing-dependencies.js` after page load, not by Thymeleaf.

---

== 3. Management and Organization

The lecture defines management and organization as the relations between
resources and decisions concerning those resources: acquisition, scheduling,
allocation, and activation. It concerns orchestration of people and decisions,
lines of command, and reporting structures.

Within Team B's Missing Dependencies feature, management and organization
manifest in the following ways.

Resource acquisition and disposal via the cache::
`DependencyCacheService` is the resource manager for resolved dependency
metadata. `put()` acquires a cache entry. `invalidate()` disposes of a
single entry. `invalidateAll()` disposes of all entries. The TTL of 30
minutes encodes an operational decision: metadata fetched from Modrinth is
treated as sufficiently stable for half an hour. This decision was made
implicitly by the implementation and is not currently stated as a
requirement (see Finding I-1 in `team-b-requirements-validation-report.adoc`).

Allocation of resolution work to the service layer::
`DependencyResolverService.validate()` is the allocation point. It iterates
over `mod.getDepends()` and for each missing mandatory dependency calls
`resolveExternalMetadata()`, which in turn delegates to `DependencyApiClient`,
`DependencyParser`, and `DependencyCacheService` in sequence. The coordinator
does not perform any of the work itself; it allocates each concern to the
appropriate service. This is the organizational principle that the refactoring
task (which produced the four-class architecture) was designed to enforce.

Team-level responsibility boundary::
Team B owns the Missing Dependencies feature end-to-end: from the
`DependencyResolverService` on the backend to the `missing-dependencies.html`
page on the frontend. Team A owns the graph visualization. Team C owns client
storage decisions. The boundary is enforced organizationally through issue
labeling (`Team B`) and milestone assignment, not through code-level
architectural barriers.

---

== 4. Rules and Regulations

The lecture defines a rule as a text in the domain that governs how people
or equipment are expected to behave, and a regulation as a text that governs
remedial action when a rule has not been followed.

The following rules and regulations govern Team B's domain.

Rule R-1: Loader compatibility::
A mod built for Fabric must not be installed in a Forge modpack, and vice
versa. This rule exists in the mod ecosystem itself, not in the system. The
system surfaces it as advisory text in the Warnings panel of
`missing-dependencies.html`. The system does not enforce it programmatically:
it does not verify that a resolved download link points to the correct loader
variant. The loader parameter passed to `DependencyApiClient.fetchVersionMetadata()`
is currently hardcoded to `"fabric"` in `DependencyResolverService.resolveExternalMetadata()`,
which partially enforces R-1 by filtering Modrinth results, but does not
validate the parentContext loader against the hardcoded value.

Rule R-2: Minecraft version compatibility::
A mod built for Minecraft 1.20.1 must not be installed in a 1.19.4 modpack.
This rule is partially enforced by passing `parentContext.getVersion()` as the
`mcVersion` parameter to `fetchVersionMetadata()`. If `parentContext.getVersion()`
returns null the fallback is `"1.20.1"`, which is a default and not a
validated value.

Rule R-3: Mandatory dependency presence::
A modpack is structurally invalid if any mod in the pack declares a mandatory
dependency that is not present. This rule is the core domain rule that the
entire feature enforces. It is checked in `DependencyResolverService.validate()`
via `dep.isMandatory()` and `modRepository.findById(dep.getId()).isEmpty()`.

Regulation REG-1: Unresolvable dependency action::
When a mandatory dependency cannot be resolved (either because the API returns
an empty result set or because the API is unreachable), the user is presented
with a fallback browse link and an Unresolved badge. This is the remedial
action the system provides. DR-12 in the Requirements Validation Report
further distinguishes the regulation for the case where the mod is genuinely
absent from the repository versus the case where the API call failed.

---

== 5. Scripts

The lecture defines scripts as workflows or behaviors within the domain that
are subject to description and negotiation at the domain level. Scripts are
the recurring, structured sequences of actions that stakeholders follow.
Unlike business processes (which are long-winded behaviors that cross many
facets), scripts are localized and repeatable.

The following scripts operate in Team B's domain.

Script S-1: Modpack validation cycle::
A user assembles a modpack, uploads it, receives the Missing Dependencies
report, installs the listed mods, and re-uploads. This cycle repeats until
the report shows no missing dependencies. Each iteration of the cycle is a
single execution of `DependencyResolverService.validate()` followed by a
render of `missing-dependencies.html`. The script is driven by the user and
terminates when the `empty-state` div becomes visible.

Script S-2: Individual dependency resolution::
For each entry in the `dependency-list` div, the user opens the link, selects
the correct file for their Minecraft version and loader, downloads the `.jar`,
and places it in the `mods` folder. This script is performed outside the
system. The system's contribution is providing the correct link (direct
download URL from Modrinth where available, browse page otherwise) so that
the user can locate the correct file with minimal navigation.

Script S-3: Cache-hit resolution::
When `DependencyResolverService.resolveExternalMetadata()` is called for a
mod ID that already has a non-expired entry in `DependencyCacheService`,
`cacheService.get()` returns the cached `ResolvedDependencyDto` immediately
and the API client, parser, and cache-write steps are all skipped. This
script runs entirely within the system and is invisible to the user, but it
is a distinct recurring behavioral sequence that the system executes
frequently during bulk validation.

---

== 6. Human Behavior

The lecture defines human behavior as the qualities of interaction in the
domain: dutiful, forgetful, sloppy, criminal, and so on. Human behavior
describes how the people in the domain actually behave, as distinct from how
rules say they should behave.

The following human behaviors are relevant to Team B's domain.

Forgetful behavior: omitting dependencies from the modpack::
The Modpack Builder (Proposal Section 2.3.2, Persona 1) assembles a modpack
by adding mods one at a time. Dependency chains are not always visible or
documented. A builder routinely adds a content mod without adding its
required library mod, not because they are sloppy but because the dependency
is implicit and easy to overlook. This behavior is the primary trigger for
the entire Missing Dependencies feature. The `detected-missing-banner` in
`missing-dependencies.html` and the `dependency-list` div exist specifically
to surface what the builder forgot.

Sloppy behavior: installing the wrong file variant::
When presented with a Modrinth download page, a user may install a file for
the wrong Minecraft version or the wrong loader. The Warnings panel in
`missing-dependencies.html` addresses this directly with three bullet points.
The system cannot prevent this behavior; it can only advise against it. The
loader filter in `DependencyApiClient.fetchVersionMetadata()` reduces the
probability by returning only compatible file variants, but the final
selection is made by the user on the Modrinth page.

Dutiful behavior: re-running analysis after installing::
The install guide panel in `missing-dependencies.html` ends with the
instruction "After installing the missing mods, re-run analysis." A dutiful
user follows this instruction and uploads the corrected modpack, allowing the
system to confirm that all mandatory dependencies are now present. The
`back-to-graph-button` and the `btn-primary` upload button are the UI
affordances that support this behavior.

Impatient behavior: abandoning the page before analysis completes::
The `loading-state` div is shown while `frontend-missing-dependencies.js`
fetches the dependency list. An impatient user may navigate away before the
list renders, particularly if the API calls in `DependencyApiClient` are slow
due to network latency. The system has no mechanism to retain partial results
across navigation. This behavior is currently unmitigated.

---

== 7. Business Procedures

The lecture defines business procedures as long-winded behaviors that are
regularly performed and relevant to the domain. They help analyze the other
facets because they reveal how the facets interact in practice.

The following business procedures are relevant to Team B's domain.

Procedure BP-1: Modpack build and release cycle::
A Modpack Builder selects a set of content mods for a target Minecraft
version and loader, assembles them into a ZIP, uploads for analysis, resolves
any reported missing dependencies, re-analyzes until the report is clean, and
publishes the modpack for others to use. This procedure crosses all six other
facets: it involves the intrinsic entities (mods, dependencies, modpack), uses
the support technology (Modrinth repository, Spring Boot backend), follows
rules R-1 through R-3, executes scripts S-1 and S-2, and exhibits both
dutiful and forgetful human behaviors. The entire Missing Dependencies feature
exists to support this procedure.

Procedure BP-2: Dependency metadata maintenance::
The Modrinth repository periodically adds new mod versions, deprecates old
ones, and changes file availability. `DependencyCacheService` holds metadata
for 30 minutes per entry. When a user uploads a modpack shortly after a
repository change, the cache may return stale data. The organization facet
(specifically DR-CACHE-1 from the Requirements Validation Report) governs
when the cache must be invalidated to prevent stale results from interfering
with this procedure.

Procedure BP-3: Server modpack consistency verification::
A Server Organizer (Proposal Section 2.3.2, Persona 3) periodically verifies
that all player clients are running the same set of mods at compatible
versions. This procedure involves uploading the server modpack for analysis
and checking that no missing dependencies are reported. The system supports
this procedure through the same validation flow as BP-1. The distinction is
that the Server Organizer is more sensitive to false negatives (a missing dep
that the system fails to report) than to false positives, because an
undetected incompatibility causes crashes for all players on the server rather
than for one individual user.

---

== 8. Relationship to Other Team B Documents

[cols="2,3"]
|===
|Document |Facets addressed

|`team-b-requirements-validation-report.adoc`
|Rules and regulations (R-1 through R-3, REG-1 correspond to DR-1 through
DR-12 in that report). Human behavior (Findings C-1, C-2 arise from
forgetful and impatient behavior that the requirements did not fully cover).

|`team-b-dependency-validation-cen.adoc`
|Intrinsics (the CEN conditions map directly to the intrinsic entities:
mod received, dependency missing, response available, DTO constructed).
Scripts (Script S-3, the cache-hit path, corresponds to the early-exit
branch before E2 in the CEN).

|`team-b-dependency-resolution-ptn.adoc`
|Support technology (P-SLOT models the concurrency constraint imposed by
`RestTemplate` being synchronous). Management and organization (the slot pool
capacity N=3 is an allocation decision modeled as a PTN capacity constraint).

|===

---

== References

* Domain Engineering and Domain Facets lecture, Marko Schütz-Schmuck,
  University of Puerto Rico at Mayagüez, October 14, 2020
* Project Proposal — Section 2.3 Domain Requirements
* Project Proposal — Section 2.3.2 Personas
* Project Proposal — Section 2.7 Domain Events
* `team-b-requirements-validation-report.adoc`
* `team-b-dependency-validation-cen.adoc`
* `team-b-dependency-resolution-ptn.adoc`
* `DependencyResolverService.java` — `service/resolver/`
* `DependencyApiClient.java` — `service/client/`
* `DependencyParser.java` — `service/parser/`
* `DependencyCacheService.java` — `service/cache/`
* `missing-dependencies.html` — `src/main/resources/templates/`
* `missing-dependencies.css` — `src/main/resources/static/CSS_Files/`

---