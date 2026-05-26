# 📝 [Lecture Topic Task] Software Design Principles in Dependency Resolution Architecture
INSO 4101 — Lecture Topic Task | Victor E. Ravelo Santana 

### 🎯 Objective
Analyze how software design principles are applied in the dependency lookup subsystem to improve modularity, maintainability, scalability, and separation of responsibilities.

---

## Introduction
Software design focuses on organizing backend systems into modular and maintainable components. Good software architecture improves readability, debugging, scalability, and long-term maintainability.

The Minecraft modpack dependency lookup subsystem demonstrates several important software design principles through the interaction of:
- `DependencyLookupService`
- `DependencyLookupCache`
- `ModrinthServiceWrapper`

---

## Main Software Design Principles

### Separation of Concerns
Each backend component focuses on a specific responsibility.

- `DependencyLookupService` manages workflow coordination
- `DependencyLookupCache` handles cache operations
- `ModrinthServiceWrapper` manages API communication

This prevents tightly coupled backend logic.

---

### Single Responsibility Principle
Each class has one main responsibility.

#### DependencyLookupService
- Controls dependency resolution flow
- Coordinates backend operations

#### DependencyLookupCache
- Stores dependency data
- Retrieves cached dependencies

#### ModrinthServiceWrapper
- Sends API requests
- Handles API responses and failures

---

### Abstraction
Abstraction hides complex implementation details.

Examples:
- `DependencyLookupService` does not manage low-level HTTP requests
- `ModrinthServiceWrapper` abstracts API communication
- `DependencyLookupCache` abstracts cache storage operations

---

### Modularity
The subsystem is divided into independent modules.

Benefits include:
- Easier debugging
- Better testing
- Improved scalability
- Cleaner code organization

---

## Dependency Lookup Workflow

### Step 1: Request Received
`DependencyLookupService` receives a dependency request.

### Step 2: Validation
The request is validated.

### Step 3: Cache Lookup
The system checks `DependencyLookupCache`.

#### Cache Hit
- Dependency returned immediately
- API request avoided

#### Cache Miss
- Request forwarded to `ModrinthServiceWrapper`

### Step 4: External API Lookup
The backend contacts Modrinth.

### Step 5: Dependency Resolution
If successful:
- Dependency returned
- Dependency cached

If unsuccessful:
- Lookup fails safely
- Cache remains unchanged

---

## Maintainability Benefits

### Easier Debugging
Problems can be isolated to individual components.

### Easier Testing
Each service can be tested independently.

### Easier Future Expansion
Developers can:
- Replace APIs
- Upgrade cache systems
- Add new features
- Improve scalability

without rewriting the entire backend.

---

## Example of Poor Software Design
If all logic existed inside one large backend class:
- Debugging would become difficult
- Code duplication would increase
- Scalability would decrease
- Maintenance would become harder

This tightly coupled architecture would reduce backend quality.

---

## Scalability Benefits
The current design supports:
- Parallel dependency requests
- Independent service upgrades
- Flexible backend expansion
- Improved performance optimization

---

## Conclusion
The dependency lookup subsystem demonstrates important software design principles including modularity, abstraction, separation of concerns, and the single responsibility principle. The interaction between `DependencyLookupService`, `DependencyLookupCache`, and `ModrinthServiceWrapper` creates a backend architecture that is easier to maintain, debug, scale, and extend.

