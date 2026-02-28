# Project TODO List

## Technical Debt & Refactoring

- [ ] **Refactor Gradle Scripts with Convention Plugins**
  - **Problem:** Significant duplication exists across module-level `build.gradle.kts` files (`plugins`, `android` config, `dependencies`). This makes maintenance difficult and error-prone.
  - **Solution:** Create a `build-logic` module to house custom "convention plugins" (e.g., `tasky.android.feature`) that encapsulate all the shared build logic.
  - **Goal:** Drastically simplify module build scripts to only declare what is unique to them. This improves maintainability and creates a single source of truth for dependencies and build configurations.
  - **Affected Files:** `features/auth/build.gradle.kts`, `features/agenda/build.gradle.kts`, and eventually `app/build.gradle.kts`.
