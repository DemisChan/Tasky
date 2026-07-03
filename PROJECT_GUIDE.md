# Tasky — Project Guide

*A memory-refresh document covering what has been built, how it works, and where to pick up.*
*Last updated: July 2026 (codebase state as of the last commits in January 2026).*

---

## 1. What is Tasky?

Tasky is a **task/agenda management Android app** built as a learning project against the
[Tasky API](https://tasky.pl-coding.com/) (Philipp Lackner's backend). It is written in
**Kotlin** with **Jetpack Compose**, structured as a **multi-module Gradle project**, and uses
**Hilt** for dependency injection and **Retrofit/OkHttp** for networking.

### Where you left off (January 2026)

| Area | Status |
|---|---|
| Registration & Login (UI + API + validation feedback) | ✅ Done |
| Encrypted session/token persistence | ✅ Done |
| Splash screen with startup auth check | ✅ Done |
| Type-safe Compose navigation (nested auth/agenda graphs) | ✅ Done |
| Unit tests (repository + ViewModels) | ✅ Done |
| CI (GitHub Actions: lint + tests) and CodeQL | ✅ Done |
| Agenda feature | 🚧 Placeholder screen only ("Coming Soon") |
| Token refresh | ❌ Not implemented (refresh token is stored but unused) |
| Logout UI | ❌ Repository/API exist, nothing calls them from UI |

The last substantial work (PRs #6–#8) was: the splash screen + startup auth check,
centralizing build config in the root `build.gradle.kts`, creating the (still empty)
`features:agenda` module, and setting up CI/CodeQL.

---

## 2. Module map

```
Tasky
├── app                    # Entry point: MainActivity, MainViewModel, NavHost, theme
├── features
│   ├── auth               # Complete feature: data + domain + presentation (login/register)
│   └── agenda             # ⚠️ Shell only — has a build.gradle.kts but NO source code yet
└── core
    ├── domain/util        # Pure utilities: Result, Error, UiText
    └── data               # Networking (OkHttp/Retrofit DI), token storage, crypto
```

Dependency direction: `app` → `features:auth` → `core:data` → `core:domain:util`.
Type-safe project accessors are enabled (`projects.core.data` syntax) in `settings.gradle.kts`.

### `app`
- `MainActivity.kt` — splash screen handling, renders `TaskyNavHost` once auth check finishes.
- `MainViewModel.kt` — holds `MainState(isCheckingAuth, isLoggedIn)`; asks `TokenManager` if the stored token is valid.
- `ui/navigation/Routes.kt` + `TaskyNavHost.kt` — type-safe navigation graphs.
- `ui/theme/` — standard Material 3 theme scaffolding.

### `features:auth`
Classic three-layer feature module:
- `data/remote/` — `AuthApi` (Retrofit interface: `auth/login`, `auth/register`, `auth/logout`), DTOs, `AuthResponse`.
- `data/repository/DefaultAuthRepository.kt` — implements the domain interface; maps exceptions/HTTP codes to typed errors; saves the session on login.
- `domain/` — `AuthRepository` interface, `AuthError` sealed error types.
- `presentation/login/`, `presentation/register/` — Screen + ViewModel + Action + Event per screen.
- `presentation/agenda/AgendaScreen.kt` — ⚠️ the placeholder agenda screen currently lives **here**, not in `features:agenda` (see §8).
- `presentation/util/` — `ObserveAsEvents` (lifecycle-aware one-shot event collector), `toUiText` (error → string resource mapping).
- `di/AuthModule.kt` — provides `AuthApi` and `AuthRepository`.

### `core:domain:util`
Three small but load-bearing files used everywhere:
- `Result.kt` — generic `Result<D, E : Error>` with `map`, `onSuccess`, `onError`, `asEmptyDataResult`, and `typealias EmptyResult<E> = Result<Unit, E>`.
- `Error.kt` — marker interface all error enums implement.
- `UiText.kt` — `DynamicString` / `StringResource` wrapper so ViewModels never touch raw strings or `Context`.

### `core:data`
- `di/CoreDataModule.kt` — the network stack: `Json`, `CryptoManager`, `TokenManager`, both interceptors, `OkHttpClient`, `Retrofit`.
- `remote/ApiKeyInterceptor.kt` — adds `x-api-key` header from `BuildConfig.API_KEY` to every request.
- `remote/AuthTokenInterceptor.kt` — adds `Authorization: Bearer <accessToken>` if a session exists.
- `token/TokenManager.kt` — interface + `SessionData` model (access/refresh tokens, user id/name, expiry timestamp).
- `token/TokenStorage.kt` — `DataStoreTokenStorage`, the implementation (see §5).
- `security/CryptoManager.kt` — AES/GCM encryption backed by the Android Keystore.
- `local/EncryptedTokenData.kt` — the `@Serializable` DTO persisted to disk.

---

## 3. Tech stack (from `gradle/libs.versions.toml`)

- **Kotlin** 2.0.21, **AGP** 8.13.0, **KSP**; `compileSdk 36`, `minSdk 33`, JVM target 11
- **Jetpack Compose** (BOM 2024.09.00) + Material 3, **Navigation Compose** 2.8.5 (type-safe routes via `@Serializable`)
- **Hilt** 2.57.2 (+ `hilt-navigation-compose`)
- **Retrofit** 2.9 with the **kotlinx-serialization** converter, **OkHttp** 4.12 (+ logging interceptor)
- **DataStore Preferences** for persistence, **Android Keystore** (via custom `CryptoManager`) for encryption
- **Timber** for logging
- Tests: **JUnit 4**, **MockK**, **Turbine** (Flow testing), `kotlinx-coroutines-test`

---

## 4. How the key flows work

### 4.1 App startup & splash (`app` module)

1. `MainActivity` installs the AndroidX splash screen and keeps it visible while
   `MainViewModel.state.isCheckingAuth` is true.
2. `MainViewModel.init` calls `tokenManager.isTokenValid()` — this reads the stored session and
   checks the access-token expiry timestamp against `System.currentTimeMillis()` with a
   **5-minute safety buffer**.
3. When the check completes, the splash dismisses and `TaskyNavHost` renders with
   `isAuthenticated` deciding the start destination:
   - valid session → `AgendaGraphRoutes.AgendaGraph`
   - otherwise → `AuthGraphRoutes.AuthGraph` (starts at Login)

### 4.2 Navigation (`Routes.kt`, `TaskyNavHost.kt`)

Type-safe navigation with `@Serializable` route objects grouped in two nested graphs
(`AuthGraphRoutes`, `AgendaGraphRoutes`). On login success the whole auth graph is popped:

```kotlin
navController.navigate(AgendaGraphRoutes.AgendaGraph) {
    popUpTo(AuthGraphRoutes.AuthGraph) { inclusive = true }
}
```

so the user can't press "back" into the login screen.

### 4.3 Presentation pattern (MVI-flavoured MVVM)

Each screen follows the same shape — worth internalizing because the agenda feature should
copy it:

- **State**: a `data class` (`LoginUiState`, `RegisterUiState`) exposed as
  `var state by mutableStateOf(...)` with a `private set`.
- **Actions**: a sealed interface (`LoginAction`, `RegisterAction`) — the UI calls a single
  `viewModel.onAction(action)` entry point. Typing into a field also **clears the current error**.
- **One-shot events** (navigation, snackbars): a `Channel<Event>` exposed as
  `events = eventChannel.receiveAsFlow()`, collected in the UI with the custom
  `ObserveAsEvents` composable, which uses `repeatOnLifecycle(STARTED)` +
  `Dispatchers.Main.immediate` so events aren't dropped or replayed on rotation.

### 4.4 Error handling (the `Result`/`UiText` pipeline)

The flow of an error from the network to the screen:

1. `DefaultAuthRepository.safeApiCall` catches exceptions and maps them to typed errors:
   `HttpException` 400/401/409/5xx → `AuthError.Auth.*` / `AuthError.Network.SERVER_ERROR`,
   `SocketTimeoutException` → `TIMEOUT`, `IOException` → `NO_INTERNET`, anything else → `UNKNOWN`
   (`CancellationException` is correctly rethrown).
2. The repository returns `Result<T, AuthError>` (type aliases: `LoginResult`, `RegisterResult`, `LogoutResult`).
3. The ViewModel chains `.onSuccess { } .onError { }` and converts errors with
   `AuthError.toUiText()` → a `UiText.StringResource` pointing at `strings.xml`.
4. The composable resolves it via the `@Composable UiText.asString()` extension.

No raw exception messages or hardcoded strings ever reach the UI.

### 4.5 Networking (`CoreDataModule`)

One `OkHttpClient` with three interceptors, in order:
1. `ApiKeyInterceptor` — `x-api-key: BuildConfig.API_KEY` on every call.
2. `AuthTokenInterceptor` — `Authorization: Bearer <token>` when a session exists
   (uses `runBlocking` to bridge into the suspend `TokenManager` — standard for OkHttp interceptors).
3. `HttpLoggingInterceptor` at `Level.BODY`.

Retrofit is built against `BuildConfig.BASE_URL` with the kotlinx-serialization converter
(`Json { ignoreUnknownKeys = true }`).

### 4.6 Session persistence & encryption

On successful login, `DefaultAuthRepository` saves a `SessionData` (access + refresh token,
userId, username, expiry timestamp). `DataStoreTokenStorage.saveSession` then:

1. Serializes it to JSON (`EncryptedTokenData`).
2. Encrypts the bytes with `CryptoManager` — **AES/GCM/NoPadding** with a key named `"secret"`
   generated in and never leaving the **Android Keystore**. The output is
   `[4-byte IV length][IV][ciphertext]` so decryption can recover the IV.
3. Base64-encodes and stores it under one key (`session_data`) in DataStore (`session_prefs`).

`getSession()` reverses this and returns `null` on any decryption failure (fail-safe: user just
logs in again). This single-encrypted-blob design was a deliberate refactor (PR #5) away from
storing fields individually.

---

## 5. Building & running

1. The API key is resolved in the **root** `build.gradle.kts`:
   `System.getenv("API_KEY") ?: local.properties "apiKey"` — so locally add to `local.properties`:
   ```properties
   apiKey="your-tasky-api-key"
   ```
   (Note: the value is injected verbatim into `buildConfigField`, so it needs the quotes.)
2. The base URL (`https://tasky.pl-coding.com/`) is hardcoded in the same file and both land in
   `BuildConfig` of `core:data`, `features:auth`, and `features:agenda` — **debug build type only** (see §8).
3. Build/test: `./gradlew assembleDebug`, `./gradlew test`, `./gradlew lint`. JDK 17 is what CI uses.

---

## 6. Testing

All meaningful tests live in `features/auth/src/test/`:

- `DefaultAuthRepositoryTest.kt` — repository logic against a mocked `AuthApi` (MockK),
  incl. error-mapping cases.
- `LoginViewModelTest.kt` / `RegisterViewModelTest.kt` — state transitions and one-shot events,
  using **Turbine** for Flow assertions and `MainCoroutineRule` to swap the main dispatcher
  (the register test drives a hand-written `FakeAuthRepository` instead of MockK).

There's also an older `app/src/test/.../MainViewModelTest.kt` (plus a second copy of
`MainCoroutineRule` there). PR #4 ("Add repository tests and refactor ViewModel tests with
Turbine") established the Turbine pattern — follow it for new tests.

## 7. CI / GitHub

- `.github/workflows/ci.yml` — "Kotlin CI": on push/PR to `main`, JDK 17 (temurin, Gradle cache),
  runs `./gradlew lint` then `./gradlew test`. Requires the **`API_KEY` repository secret**
  (exported as `ORG_GRADLE_PROJECT_API_KEY`).
- `.github/workflows/codeql.yml` — CodeQL scanning (set up in PR #8).

### Development timeline (from PRs)

| PR | Branch | What it did |
|---|---|---|
| #1–2 | `featureAuth-Register` | Register screen; introduced the generic `Result` error handling |
| #3 | `featureAuth-Refactor` | Standardized errors with `UiText` + string resources |
| #4 | `featureAuth-Navigation` | Login ↔ Register navigation, type-safe nested graphs, single-shot events, DataStore token storage, Turbine tests, centralized network/token management in `core:data` |
| #5 | `featureTokenPersistance` | Repository pattern for tokens; consolidated session into one encrypted object |
| #6 | `splashScreen` | Splash screen + startup auth check; `MainState`; centralized build config; created `features:agenda` module |
| #8 | `DemisChan-CodeQL` | CodeQL + Kotlin CI workflow (JDK 17, lint, API key env var) |

---

## 8. Claude's observations (things I'd flag in a review)

Honest notes from reading the code — none are blockers, but several are worth fixing early:

1. **🔴 Passwords are logged in plaintext.** `RegisterViewModel.register()` logs
   `state.password` via Timber (`RegisterViewModel.kt:61`). Even in debug builds this is a bad
   habit and CodeQL may eventually flag it. Also, the OkHttp logging interceptor runs at
   `Level.BODY` unconditionally, so login/register request bodies (passwords) appear in Logcat.
   Fix: remove the password log line; gate the logging interceptor on `BuildConfig.DEBUG` and/or
   redact auth endpoints.
2. **`AgendaScreen` lives in the wrong module.** The placeholder is at
   `features/auth/.../presentation/agenda/AgendaScreen.kt`, while `features:agenda` contains only
   a `build.gradle.kts` and no source at all. First step when resuming: move the screen into
   `features:agenda` and add the module to `app`'s dependencies.
3. **No token refresh.** `SessionData.refreshToken` is stored but never used. When the access
   token expires (checked with a 5-min buffer at startup only), the user is forced to log in
   again — and a token that expires *mid-session* will just cause 401s, since nothing handles
   refresh at the OkHttp layer. Natural fix: an OkHttp `Authenticator` that calls the API's
   `/accessToken` refresh endpoint and updates the stored session.
4. **Logout is unreachable.** `AuthRepository.logout()` and `AuthApi.logout()` exist and clear
   the session, but no screen calls them. The agenda screen will need a logout menu item.
5. **`API_KEY`/`BASE_URL` only exist in the `debug` build type** (in `core:data`,
   `features:auth`, `features:agenda`). A `release` build will fail to compile because
   `BuildConfig.API_KEY` won't be generated. Move the `buildConfigField`s to `defaultConfig`.
6. **Naming drift in `core:data/token`.** The interface is `TokenManager` but the implementation
   file is `TokenStorage.kt` containing `DataStoreTokenStorage`, and the DI provider is called
   `providesSessionManager`. It really manages a *session*, not just a token — consider renaming
   to `SessionStorage`/`SessionManager` consistently.
7. **Duplication / dead code.** `MainCoroutineRule` exists twice (app + auth test source sets);
   `TokenManager.isAuthenticated()` duplicates the decrypt-and-check-expiry logic of
   `isTokenValid()` and is never called anywhere; `LoginAction.SignUpClicked` and
   `RegisterAction.LoginClicked` are empty no-op branches (navigation is passed as
   callbacks into the screens instead — pick one mechanism).
8. **Small nits:** `passwordVisible` is declared `var` inside otherwise-immutable state data
   classes (make it `val`; `copy()` is already used); unused version-catalog entries
   (`androidx-credentials`, `androidx-security-crypto` — you wrote your own `CryptoManager`
   instead, which is fine); Compose BOM 2024.09.00 is old relative to compileSdk 36 and worth
   bumping; the agenda module's build file has heavy dependencies (Retrofit, Hilt) it doesn't
   need yet.

Things that are genuinely **good** and worth keeping as patterns: the `Result`/`UiText`
pipeline, `safeApiCall`'s exception mapping (incl. rethrowing `CancellationException`), the
`Channel`-based one-shot events with `ObserveAsEvents`, the single-encrypted-blob session
storage, and the per-screen Action/State/Event structure.

---

## 9. Suggested next steps (in order)

1. **Quick fixes first** (small PRs, get back into the rhythm): stop logging passwords / gate the
   logging interceptor (§8.1), move `buildConfigField`s to `defaultConfig` (§8.5), move
   `AgendaScreen` into `features:agenda` (§8.2).
2. **Build the Agenda feature** in `features:agenda`, copying the auth module's structure:
   `data/remote` (AgendaApi: agenda items, tasks, events, reminders) → `domain` (models +
   repository interface) → `presentation` (AgendaScreen with State/Action/Event ViewModel).
3. **Token refresh** via an OkHttp `Authenticator` + the refresh endpoint, updating
   `DataStoreTokenStorage`.
4. **Logout UI** on the agenda screen (calls `AuthRepository.logout()`, navigates back to the
   auth graph).
5. Then: offline-first caching with Room, WorkManager sync — the usual Tasky roadmap.
