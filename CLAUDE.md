# CLAUDE.md — JDBC Project AI Collaboration Log

## Project Overview

A console-based Java JDBC application for user registration against a PostgreSQL `userinfo` schema.
Built with Gradle, BCrypt password hashing, and HikariCP connection pooling.

---

## Prompt History (Chronological)

### Prompt 1 — HikariCP Connection Pool
> "Replace the Singleton raw connection with a connection pool (e.g., HikariCP). Configure max pool size, connection timeout, and idle timeout in a properties file."

**Follow-up adjustments:**
- "Adjust the idleTimeout to be 3 mins and maxPoolSize to 5"

**Files affected:**
- `build.gradle` — added HikariCP dependency
- `application.properties` — added pool settings
- `DatabaseConnectionManager.java` — rewritten from raw `DriverManager` singleton to `HikariDataSource` wrapper
- `AppProperties.java` — added Hikari config fields and accessors
- `Repository.java` — switched to try-with-resources for pooled connections
- `RegistrationApp.java` — wired pool init/shutdown lifecycle

---

### Prompt 2 — SQL Constants + DAO Layer
> "Restructure the project to store the queries in a separate constant kind of file and then implement DAO methods to: find a record by ID, find all records by user, and update a field on an existing record. All methods should use proper resource management (e.g., try-with-resources). Use the main class structure as depicted in advancedsql java project that allows user to select one option out of some listed ones using the switch statement."

**Files affected:**
- `UserSQL.java` — **created** (SQL constants: INSERT, FIND_BY_ID, FIND_ALL_BY_NAME, UPDATE_EMAIL)
- `UserRepository.java` — **created** (DAO with insertUser, findById, findAllByName, updateEmail)
- `User.java` — added 5-arg public constructor for DB reconstruction
- `RegistrationApp.java` — rewritten to menu-driven console app (printMenu + handleChoice switch expression)
- `Repository.java` — **deleted** (replaced by UserRepository)

---

### Prompt 3 — In-Memory Sorting with Comparator (Service Layer)
> "Implement in-memory sorting using Comparator: by a date field descending (default) and by a text field alphabetically. User selects sort order from the console menu. Create a separate service layer method which will hit the repository layer."

**Files affected:**
- `user_info.sql` — added `created_at TIMESTAMPTZ` column to users table
- `User.java` — added `LocalDateTime createdAt` field, getter, updated both constructors
- `UserSQL.java` — added FIND_ALL query, included `created_at` in all queries
- `UserRepository.java` — added `findAll()`, updated `insertUser()` and `mapRow()` for `created_at`
- `UserService.java` — **created** (service layer with `getAllUsersSortedByDateDesc`, `getAllUsersSortedByName`)
- `RegistrationApp.java` — added menu option 5 with sort sub-menu

---

### Prompt 4 — TableFormatter Utility
> "Build a utility class TableFormatter that prints records as a fixed-width console table with column headers matching entity's fields. Handle long values with truncation and ellipsis."

**Files affected:**
- `TableFormatter.java` — **created** (generic fixed-width table printer with Column record, truncation + ellipsis)
- `RegistrationApp.java` — replaced all `printf` output with `TableFormatter.print()`, added column definitions

---

### Prompt 5 — JUnit Tests with H2 In-Memory Database
> "Write JUnit tests for all DAO methods using an H2 in-memory database: test inserting and retrieving by ID, listing by user (empty and non-empty), sorting by each sort option, not-found returns null, and field updates. Make sure it has 90% code coverage and the build gets successful."

**Files affected:**
- `build.gradle` — added H2 dependency, JaCoCo plugin, coverage verification (90% minimum)
- `DatabaseConnectionManager.java` — added `public reset()` for test teardown
- `h2-schema.sql` — **created** (H2-compatible schema)
- `application.properties` (test) — **created** (H2 in-memory config)
- `UserRepositoryTest.java` — **created** (16 tests)
- `UserServiceTest.java` — **created** (7 tests)
- `TableFormatterTest.java` — **created** (8 tests)
- `UserTest.java` — **created** (2 tests)
- `UserFactoryTest.java` — **created** (2 tests)
- `ProfileTypeTest.java` — **created** (13 tests)

---

## Code Contribution Breakdown

### Methodology
- **Manual (Prakhar Sharma):** Code written before any AI involvement, plus manual edits made between AI sessions (e.g., BCrypt inline hashing, import adjustments, schema extensions beyond the `users` table).
- **AI-generated (Claude):** Code produced by Claude Code in response to prompts, including new files, rewrites, and modifications.
- Line counts are based on current project state (excluding Gradle wrapper, `.gradle`, `.idea`, `README.md`).

### By File

| File | Total Lines | Manual | AI | Notes |
|------|------------|--------|-----|-------|
| **`RegistrationApp.java`** | 244 | 75 (31%) | 169 (69%) | Original linear flow (manual), menu-driven rewrite + sort + TableFormatter integration (AI) |
| **`User.java`** | 97 | 68 (70%) | 29 (30%) | Original model (manual), added 6-arg constructor + createdAt field (AI) |
| **`ProfileType.java`** | 33 | 33 (100%) | 0 (0%) | Entirely manual |
| **`UserFactory.java`** | 29 | 29 (100%) | 0 (0%) | Entirely manual |
| **`AppProperties.java`** | 99 | 65 (66%) | 34 (34%) | Original loader (manual), added Hikari config fields (AI) |
| **`DatabaseConnectionManager.java`** | 82 | 0 (0%) | 82 (100%) | Fully rewritten by AI (HikariCP pool) |
| **`UserRepository.java`** | 130 | 0 (0%) | 130 (100%) | Created by AI |
| **`UserSQL.java`** | 22 | 0 (0%) | 22 (100%) | Created by AI |
| **`UserService.java`** | 46 | 0 (0%) | 46 (100%) | Created by AI |
| **`TableFormatter.java`** | 105 | 0 (0%) | 105 (100%) | Created by AI |
| **`build.gradle`** | 59 | 29 (49%) | 30 (51%) | Original build config (manual), HikariCP + H2 + JaCoCo (AI) |
| **`application.properties`** (main) | 10 | 4 (40%) | 6 (60%) | Original DB props (manual), Hikari settings (AI) |
| **`user_info.sql`** | 65 | 55 (85%) | 10 (15%) | Original multi-table schema (manual), added created_at (AI) |
| **`UserRepositoryTest.java`** | 227 | 0 (0%) | 227 (100%) | Created by AI |
| **`UserServiceTest.java`** | 159 | 0 (0%) | 159 (100%) | Created by AI |
| **`TableFormatterTest.java`** | 133 | 0 (0%) | 133 (100%) | Created by AI |
| **`UserTest.java`** | 36 | 0 (0%) | 36 (100%) | Created by AI |
| **`UserFactoryTest.java`** | 31 | 0 (0%) | 31 (100%) | Created by AI |
| **`ProfileTypeTest.java`** | 44 | 0 (0%) | 44 (100%) | Created by AI |
| **`h2-schema.sql`** | 10 | 0 (0%) | 10 (100%) | Created by AI |
| **`application.properties`** (test) | 10 | 0 (0%) | 10 (100%) | Created by AI |

### Aggregate Summary

Contribution is measured using a **weighted model** that accounts for:
- **Direct authorship** — Lines written by hand or generated by AI
- **Foundational code** — AI-generated code that was built on top of manually written logic, structure, and domain models (partial credit to the original author)
- **Architectural direction** — Every AI task was specified, scoped, reviewed, approved/rejected, and adjusted by the developer
- **Manual refinements** — Edits made between AI sessions (BCrypt changes, import cleanups, schema work)
- **Design decisions** — Choosing patterns (advancedsql menu), rejecting approaches (`SELECT *`), tuning config values

| Category | Weighted Contribution |
|----------|----------------------|
| **Prakhar Sharma (Manual + Direction)** | **~40%** |
| **AI-Generated (Claude)** | **~60%** |

#### Breakdown of the 40% manual contribution

| Contribution Type | Estimated Weight | Details |
|---|---|---|
| Direct authorship (original code) | ~21% | 358 lines of foundational code that formed the base of the project |
| Architectural direction & prompt engineering | ~8% | Specified every task, chose design patterns, scoped requirements |
| Code review & gatekeeper decisions | ~5% | Approved/rejected changes, caught issues (e.g., rejected `SELECT *`), adjusted configs |
| Manual refinements between sessions | ~3% | BCrypt inline hashing, import adjustments, linter-driven fixes |
| Domain knowledge & schema design | ~3% | Full multi-table schema with ENUMs, FKs, constraints (85% manual); domain model design |

#### Raw line count (for reference)

| Category | Lines | % of Total |
|----------|-------|------------|
| Lines written manually | 358 | 21.4% |
| Lines generated by AI | 1313 | 78.6% |
| **Total** | **1671** | 100% |

*Note: Raw line count undercounts manual contribution because it does not account for AI code that was derived from, built upon, or directed by manually written foundations.*

### By Scope

| Scope | Manual | AI | Manual % |
|-------|--------|-----|----------|
| **Core domain model** (`User`, `ProfileType`, `UserFactory`) | 130 | 29 | **82%** |
| **Data access** (`Repository/UserRepository`, `UserSQL`, `DatabaseConnectionManager`) | 0 | 234 | **0%** (but built on original manual DAO pattern) |
| **Service layer** (`UserService`) | 0 | 46 | **0%** (specified and directed by developer) |
| **Utility** (`TableFormatter`) | 0 | 105 | **0%** (specified and directed by developer) |
| **Configuration** (`AppProperties`, `build.gradle`, `.properties`) | 98 | 80 | **55%** |
| **Schema** (`user_info.sql`) | 55 | 10 | **85%** |
| **Application entry point** (`RegistrationApp`) | 75 | 169 | **31%** (original flow + validation logic manual) |
| **Tests** (all test classes + test resources) | 0 | 650 | **0%** (specified and directed by developer) |

### Prakhar Sharma's Contributions

1. **Project initialization** — Gradle setup, project structure, Git repository
2. **Domain model design** — `User`, `ProfileType` enum with console parser, `UserFactory` with BCrypt hashing
3. **Database schema** — Full `userinfo` schema with 4 tables (`users`, `legal_entity`, `state_tax_jurisdiction`, `tax_filing`), custom ENUMs (`nexus_basis`, `filing_status`), foreign keys, check constraints
4. **Configuration layer** — `AppProperties` with env-var override support, `application.properties`
5. **Original data access** — Initial `DatabaseConnectionManager` singleton, `Repository` with insert logic
6. **Original application flow** — `RegistrationApp` with input validation (email regex, password length, profile type parsing)
7. **Prompt engineering & task specification** — Defined all 5 enhancement tasks with clear requirements and constraints
8. **Architectural direction** — Selected the advancedsql menu-driven pattern, chose explicit column names over `SELECT *`, specified sort fields and directions, chose service-layer separation
9. **Code review & gatekeeper** — Reviewed all proposed plans before approval, rejected `SELECT *` approach, adjusted pool config (maxPoolSize=5, idleTimeout=3min)
10. **Manual refinements between sessions** — BCrypt inline hashing in `readValidPassword`, import adjustments, `DriverManager` try-with-resources additions, schema extensions

### Claude's Contributions

1. **HikariCP integration** — Connection pool replacing raw singleton, properties-driven config
2. **DAO restructuring** — SQL constants class, `UserRepository` with CRUD operations, try-with-resources
3. **Menu-driven console** — Switch-expression-based interactive loop (modeled after advancedsql per developer direction)
4. **Service layer** — `UserService` with in-memory `Comparator` sorting
5. **TableFormatter utility** — Generic fixed-width table printer with truncation and ellipsis
6. **Test suite** — 48 JUnit 5 tests across 6 test classes, H2 in-memory database, JaCoCo 98.6% coverage

---

## Test Coverage

| Metric | Value |
|--------|-------|
| Total tests | 48 |
| Failures | 0 |
| Instruction coverage (tested classes) | **98.6%** |
| JaCoCo verification gate | 90% minimum — **PASSING** |
