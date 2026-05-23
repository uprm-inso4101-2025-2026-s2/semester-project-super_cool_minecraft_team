# Non-Functional (Machine) Requirements for Backend Graph Data API

## Overview

This document specifies the non-functional (machine) requirements for the Backend Graph Data API that serves data to the Team A dependency graph feature. The requirements are written to ensure objective, testable, and maintainable standards, drawing from the domain analysis and engineering practices documented in the project’s architecture.

## Scope

These requirements apply to all endpoints and processes involved in providing graph data (i.e., mods and their dependency relationships) from backend (Java/Spring) to the frontend, including interactions with external mod repositories (Modrinth, CurseForge).

---

## 1. Performance

**Requirement 1.1 – Response Time:**  
- The backend API *shall* return graph data responses to the frontend within **2 seconds** for standard requests involving 100 mods or fewer, under normal operating conditions.

**Requirement 1.2 – Degradation Handling:**  
- Under heavy load (e.g., large requests or degraded external services), the backend API *may* increase response time up to **8 seconds**, and *shall* queue or defer further requests to maintain system stability (see Proposal.adoc, "Degradation Requirements").

---

## 2. Reliability & Availability

**Requirement 2.1 – Uptime:**  
- The backend API *must* maintain an availability of **99% uptime** during scheduled operational hours throughout the project timeline.

**Requirement 2.2 – Error Handling:**  
- The backend API *shall* implement structured error reporting for failed dependency resolutions, including HTTP status codes and machine-readable error messages (JSON).

**Requirement 2.3 – Recovery:**  
- The system *shall* provide logging and monitoring for failed/slow dependency lookups and support retriable operations for transient errors accessing external APIs.

---

## 3. Security

**Requirement 3.1 – Input Validation:**  
- The backend API *must* validate all JSON input payloads for correct structure (i.e., each mod must have a valid "id" and "name", dependencies reference valid mod IDs, etc.) and *must* reject malformed or tampered requests with a detailed 400-series error response.

**Requirement 3.2 – Information Disclosure:**  
- The backend API *shall not* expose sensitive server or backend implementation details in error responses (i.e., exception messages and stack traces must not be returned to the client).

**Requirement 3.3 – Access Control:**  
- If user/project context is supported in the future, the backend API *shall* enforce access rules such that graph data is only available to authorized users or roles.

---

## 4. Resource Usage

**Requirement 4.1 – Resource Efficiency:**  
- Backend memory and CPU usage *must* remain within reasonable operational limits while processing and building dependency graphs. The system *shall* implement monitoring/logging for performance bottlenecks and abnormal resource consumption.

---

## 5. Testability

**Requirement 5.1 – Testable Statements:**  
- All above requirements are written using "must" or "shall" and are measurable through functional/system tests, log reviews, and performance monitoring.

---

## References

- [`Proposal.adoc`: Section on Backend API Layer responsibilities](https://github.com/uprm-inso4101-2025-2026-s2/semester-project-super_cool_minecraft_team/blob/main/Proposal.adoc)
- [`README.md`: Data Flow and Validation Checklist](https://github.com/uprm-inso4101-2025-2026-s2/semester-project-super_cool_minecraft_team/blob/main/README.md)
- API Example: [`backend-missing-dependencies.js`](https://github.com/uprm-inso4101-2025-2026-s2/semester-project-super_cool_minecraft_team/blob/main/src/main/resources/static/MissingDependencies/backend-missing-dependencies.js)

---
