# Requirements Facets — Missing Dependencies Page

## 1. BPR — How the user's work changes

**Intrinsics** (the things we work with)
- Before: mods, dependencies, versions — all loose files
- After: the system creates objects like `ResolvedDependencyDto` and `MissingDependencyDto` to organize what's resolved and what's missing

**Support Technology** (the tools)
- Before: user opens Modrinth in browser and searches manually
- After: `DependencyLookupService`, `ModrinthServiceWrapper`, and `DependencyLookupCache` search automatically

**Management and Organization** (how work is organized)
- Before: each user tracked missing deps on their own
- After: the system centralizes everything, user just checks results

**Rules and Regulations** (the rules)
- Before: user manually checks loader (Fabric/Forge) and MC version compatibility
- After: the system filters automatically with `DependencyApiClient`

**Human Behavior** (how the person acts)
- Before: user could forget deps or pick the wrong version
- After: user follows the guide and re-runs analysis if needed

**Scripts** (the steps)
- Before: open browser → search mod → download → put in folder (repeat for each mod)
- After: upload ZIP → analyze → resolve → show results → click download

---

## 2. Domain Requirements — What the system knows

**Projection** (what stays and what goes)
The system takes the whole mod world and only keeps what matters:
- **Keeps**: dependency ID, name, required version, loader, MC version
- **Throws away**: mod file contents, descriptions, modpack internal structure
- Code: `MissingDependenciesResponse` (fields `missingDependencies[]`, `resolvedDependencies[]`, `analysisHasPartialResults`)

**Determination** (same input = same output)
The system is deterministic:
- Same mod + same version + same loader = same result every time
- The cache (`DependencyLookupCache`) ensures this while the TTL hasn't expired
- Code: `DependencyResolverService.resolveExternalMetadata()` (lines 104-108)

**Instantiation** (generic → specific)
The abstract "external mod repository" becomes something real:
- **Modrinth API v2** (`https://api.modrinth.com/v2`)
- Code: `ModrinthServiceWrapper.java`

**Extension** (the system adds new ideas)
The system creates something that doesn't exist in the real world:
- **Partial Results**: In real life a dependency is either found or not. The system adds a middle state: "some resolved, some didn't" with the `analysisHasPartialResults` flag
- Code: `frontend-missing-dependencies.js:35-37` shows the `#partial-results-banner`

**Fitting** (frontend and backend agree)
- Backend sends data with `MissingDependenciesResponse` via `GET /api/missing-dependencies`
- Frontend builds a `resolvedById` Map (`frontend-missing-dependencies.js:28-33`) to match resolved deps with missing ones using the same `id`

---

## 3. Interface Requirements — How they talk to each other

**Shared Phenomena** (what user and system share)
The `MissingDependenciesResponse` DTO is the common language:
- `missingDependencies[]` — what's missing (id, name, version, loader, MC)
- `resolvedDependencies[]` — what's resolved (id, name, links, preferred)
- `analysisHasPartialResults` — whether only some were resolved

**Shared Data Initialization** (how data is created)
- When user uploads a ZIP, `ZipFileController` parses it and stores info in `HttpSession`
- When user visits `/missing-dependencies`, `MissingDependenciesController` reads from session
- Code: `MissingDependenciesController.java:24-25`

**Shared Data Refreshment** (how data is updated)
- Every time user visits the page, data is fetched again
- The cache refreshes when TTL expires
- Code: `backend-missing-dependencies.js:1-23`

**Computational Data and Control** (page states)
The page goes through these states:

| State | What the user sees |
|-------|-------------------|
| Loading | A spinner while data loads |
| Results | Cards for each dependency |
| Search | Type and cards filter in real-time |
| Error | Error message if something fails |
| Empty | "No missing dependencies" message |
| Partial | Yellow warning: "only some were resolved" |

**Man-Machine Dialogue** (user does → system responds)

| User does | System responds |
|-----------|----------------|
| Visits page | Shows loading, fetches data, shows cards |
| Types in search box | Filters cards in real-time (`frontend-missing-dependencies.js:150-162`) |
| Presses "Clear" | Shows all cards again (`frontend-missing-dependencies.js:165-176`) |
| Clicks "View Download Link" | Redirects safely with `/r?u=...` |

**Man-Machine Physiological Interface** (what it looks like)
- Cards show name, version, status (Resolved/Not found), and download button
- States have colors: normal, red for error, banner for partial results

**Machine-Machine Dialogue** (how machines talk)
- **Backend → Modrinth API**: HTTP calls with `HttpClient` in `ModrinthServiceWrapper`
- **Cache**: `DependencyLookupCache` saves results so we don't call the API twice
- **Session**: `HttpSession` keeps data between pages

---

## 4. Machine Requirements — What the machine needs

### Performance

| What | How fast | Where |
|------|----------|-------|
| Resolve one dependency | Less than 2 seconds | `DependencyApiClient.fetchVersionMetadata()` with timeout |
| Filter on the page | Instant (client-side) | JavaScript, no server call |
| Load the page | Less than 1 second | Thymeleaf renders static template, JS fetches data after |

### Dependability

| Attribute | What it means | How we do it |
|-----------|--------------|--------------|
| **Availability** | Works even if Modrinth is slow | Cache (`DependencyLookupCache`) saves results |
| **Reliability** | Doesn't crash if API fails | Shows error instead of crashing (`frontend-missing-dependencies.js:84-87`) |
| **Safety** | Safe links | `RedirectController` only allows trusted domains |
| **Robustness** | Handles unexpected errors | If no session, returns error message (`MissingDependenciesController.java:28-32`) |
| **Fault tolerance** | Works even if some deps fail | `analysisHasPartialResults` shows what did work |

### Maintenance
- **Adaptive**: adding more repositories (not just Modrinth) needs new wrappers
- **Corrective**: fixing bugs in `DependencyResolverService` or the frontend
- **Perfective**: improving search, adding better filters
- **Preventive**: cache TTL prevents stale data

### Platform (where it runs)
- **Development**: Spring Boot 4.0.3, Java, vanilla JavaScript
- **Execution**: Spring Boot with embedded Tomcat
- **Demonstration**: local dev server

### Documentation Requirements
- `missing-deps-contract.md` — what the API returns
- `QA-Checklist-missing-dependencies.md` — how to test
- `docs/integration-dependency-download-flow.md` — how everything works together

---

