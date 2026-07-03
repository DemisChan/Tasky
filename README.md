# Tasky

An offline-first task management app for Android — events, tasks, and reminders with secure session persistence, built on a multi-module Clean Architecture foundation.

> Built as a portfolio project through the PL mentorship programme, implementing the Tasky V2 specification (real REST backend, offline sync, notifications).

<!-- TODO: add screenshots. A row of 3 (Login, Agenda, Event Detail) reads best on GitHub. -->
<!-- ![Login](docs/login.png) ![Agenda](docs/agenda.png) ![Detail](docs/detail.png) -->

---

## Tech Stack

| Layer | Choices |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose, Material 3 |
| DI | Hilt |
| Networking | Retrofit, OkHttp, Kotlinx Serialization |
| Persistence | DataStore (Preferences), Android Keystore |
| Async | Coroutines, Flow, Channels |
| Navigation | Navigation Compose (type-safe, serialized routes) |
| Logging | Timber |
| Testing | JUnit4, MockK, Turbine, Coroutines Test |

---

## Architecture

Feature-based multi-module structure following Clean Architecture. Each feature is split into `data` / `domain` / `presentation` / `di` layers, depending inward on shared `core` modules.

```
:app                  Application, MainActivity, MainViewModel, navigation host
:features:auth        Login & Register
:features:agenda      Daily agenda (in progress)
:core:domain:util     Result<D, E>, Error abstraction, extension functions
:core:data            Token storage, encryption, session, centralised networking
```

**Presentation pattern (MVI-lite):** each screen holds a single `UiState`, exposes one `onAction(action)` entry point, and emits one-time navigation events through a `Channel` consumed as a `Flow`. UI state uses Compose's `mutableStateOf`; transient events use Channels so they aren't replayed on recomposition.

**Error handling:** a custom sealed `Result<out D, out E : Error>` replaces exception propagation. Repositories return typed results; ViewModels branch on domain error enums via `onSuccess` / `onError` / `map` extensions rather than try/catch.

---

## Key Features

### Secure session persistence
- Access + refresh tokens, user metadata, and expiry stored via **DataStore** with a manual **AES-256-GCM** encryption layer (`CryptoManager`), keyed by the **Android Keystore** (hardware-backed where available).
- Chosen over `EncryptedSharedPreferences` (deprecated, synchronous, no reactive stream) for an async, Flow-based, key-managed approach.
- Tokens never cross into the presentation layer — the repository owns persistence as a side effect, so login returns `Result<Unit, AuthError>`.
- `AuthTokenInterceptor` attaches `Authorization: Bearer <token>` to every request. Interceptor chain: `ApiKey → AuthToken → Logging`.
- Token validity checked against expiry with a 5-minute safety buffer.

### Auth-aware launch
- Uses the **Android 12+ SplashScreen API** (no custom splash composable). `MainViewModel` resolves auth state on startup; the splash is held via `setKeepOnScreenCondition` until the encrypted read + expiry check completes, then routes to the Agenda or Auth graph.

### Type-safe navigation
- Serializable route objects and nested graphs; screens receive callbacks rather than the `NavController`. Auth backstack cleared on successful login.

---

## Getting Started

### Prerequisites
- Android Studio (latest stable)
- JDK 11
- An API key for the Tasky backend

### Configuration
The API key is loaded from `local.properties`, which is git-ignored. Create it in the project root:

```properties
apiKey=YOUR_API_KEY_HERE
```

<!-- TODO: confirm the exact property name and any BASE_URL override the build expects -->

### Build & Run

```bash
./gradlew build                     # build all modules
./gradlew app:assembleDebug         # debug APK
./gradlew test                      # all unit tests
./gradlew features:auth:testDebugUnitTest   # single-module tests
./gradlew check                     # tests + lint
```

---

## Testing

Unit tests live under each module's `src/test/`. The auth feature deliberately demonstrates **two isolation styles** side by side — MockK mocks (`LoginViewModelTest`) and hand-rolled fakes (`RegisterViewModelTest`) — as a comparison exercise. Channel-based events are verified with Turbine.

<!-- TODO: update counts once verified against the current branch -->
Current coverage: auth ViewModels, repository token-saving, and startup auth state. Encryption round-trip and interceptor tests are not yet written.

---

## Roadmap

- [x] Auth (login / register) with typed error handling
- [x] Encrypted token persistence & session management
- [x] Auth-aware splash routing
- [ ] Agenda screen — colour-coded items, ascending order, live time needle, per-item context menu, task toggle *(in progress)*
- [ ] Real agenda repository against `GET /agenda`
- [ ] Event / Task / Reminder detail screens (role-based event access)
- [ ] Offline-first: Room cache, queued sync, `POST /syncAgenda`, last-write-wins
- [ ] Scheduled notifications (reboot-persistent, cleared on delete/logout)
- [ ] 401 handling & token refresh

---

## Project Notes

This project prioritises production-shaped decisions over shortcuts: centralised networking, a data-layer boundary that tokens never escape, platform-native splash handling, and explicit error modelling. Several trade-offs (DataStore vs EncryptedSharedPreferences, Channels vs StateFlow for events, fakes vs mocks) were made deliberately and are documented in `CLAUDE.md`.

<!-- TODO: choose a licence. If unsure, MIT is the common default for portfolio repos. -->
## License

<!-- e.g. This project is licensed under the MIT License — see LICENSE for details. -->
