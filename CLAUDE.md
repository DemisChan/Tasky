# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Current Progress & Next Steps

### Current Task: Ready for PR Review ✅

**Branch**: `splashScreen`
**Status**: Complete - Ready to merge

All implementation and testing complete for splash screen feature using Android 12+ Splash Screen API.

### Recently Completed

#### 1. Type-Safe Navigation (Branch: featureAuth-Navigation)
**What was done:**
- Implemented type-safe navigation using Kotlin Serialization and Navigation Compose
- Created `Routes.kt` with serializable route objects: `AuthGraph`, `AgendaGraph`, `AuthRoute.Login`, `AuthRoute.Register`, `AgendaRoute.Agenda`
- Built `TaskyNavHost.kt` with nested navigation graphs
- Connected Login ↔ Register navigation
- Added navigation from Login to Agenda on successful authentication (clears auth backstack)

**Files modified:**
- `app/src/main/java/com/dmd/tasky/ui/navigation/Routes.kt` (new)
- `app/src/main/java/com/dmd/tasky/ui/navigation/TaskyNavHost.kt` (new, replaces old NavGraph.kt)
- `app/build.gradle.kts` (added kotlinx-serialization plugin and navigation dependencies)

#### 2. Test Suite Improvements
**What was done:**
- Fixed bug in `RegisterViewModelTest.kt` where generic error test was checking for wrong error type
- Added event emission tests to `LoginViewModelTest.kt` for Success and Error events
- Added "typing email should clear error" test to `LoginViewModelTest.kt`
- Uncommented and fixed validation tests in `RegisterViewModelTest.kt`
- Started `DefaultAuthRepositoryTest.kt` with basic login/register success and error tests

**Key findings from test analysis:**
- Discovered that Login API returns `AuthResponse` with `accessToken`, `refreshToken`, `userId`, `username`, and `accessTokenExpirationTimestamp`
- Register API returns `Unit` (no token - user must login after registration)
- Current implementation only returns `accessToken` string from repository, discarding other critical data
- LoginViewModel has TODO comment on line 61: `// TODO: Save token, navigate to next screen`
- Both ViewModels support dual testing approaches: Fakes (RegisterViewModel) and Mocks (LoginViewModel) for learning purposes

### Current Task: Token Persistence & Session Management ✅ COMPLETE

**Branch**: `featureTokenPersistance`
**Status**: Implementation complete - integrated with splash screen for auth state navigation

#### What Was Implemented

##### Phase 1-2: Token Storage Infrastructure & Repository Integration ✅

**Created**: `:core:data` module with complete token management system

**Files created:**
- `core/data/src/main/java/com/dmd/tasky/core/data/token/TokenManager.kt` - Interface for session management
- `core/data/src/main/java/com/dmd/tasky/core/data/token/DataStoreTokenStorage.kt` - Implementation using DataStore + Encryption
- `core/data/src/main/java/com/dmd/tasky/core/data/security/CryptoManager.kt` - AES-256-GCM encryption handler
- `core/data/src/main/java/com/dmd/tasky/core/data/remote/AuthTokenInterceptor.kt` - OkHttp interceptor for Bearer tokens
- `core/data/src/main/java/com/dmd/tasky/core/data/di/CoreDataModule.kt` - Hilt DI configuration
- `core/data/src/main/java/com/dmd/tasky/core/data/local/EncryptedTokenData.kt` - Data class for serialization

**Key Features Implemented:**
- ✅ `TokenManager` interface with complete CRUD operations
- ✅ `SessionData` stores: accessToken, refreshToken, userId, username, accessTokenExpirationTimestamp
- ✅ AES-256-GCM encryption using Android Keystore (hardware-backed keys)
- ✅ DataStore Preferences for persistent storage (with manual encryption layer)
- ✅ Token expiration checking with 5-minute safety buffer
- ✅ Flow-based reactive authentication state via `isAuthenticated()`
- ✅ Base64 encoding for encrypted data
- ✅ Comprehensive error handling and logging

**Security Implementation:**
- **Encryption**: AES-256-GCM with 256-bit keys
- **Key Storage**: Android Keystore (secure hardware element)
- **IV Generation**: Random 12-byte IV per encryption operation
- **Authentication Tag**: GCM provides built-in integrity verification
- **No Plain Text Logging**: Tokens never logged in production builds

**Repository Integration** (`DefaultAuthRepository.kt`):
- ✅ Lines 27-35: Saves complete session via `TokenManager` after successful login
- ✅ Changed return type from `Result<String, AuthError>` to `Result<Unit, AuthError>`
- ✅ Lines 97-111: `logout()` clears tokens via `TokenManager.clearSession()`
- ✅ Proper error handling with HTTP status code mapping

**Network Configuration** (`CoreDataModule.kt`):
- ✅ Centralized OkHttp and Retrofit configuration in `:core:data`
- ✅ Interceptor chain: ApiKeyInterceptor → AuthTokenInterceptor → HttpLoggingInterceptor
- ✅ All API requests automatically include authentication tokens

**Dependencies Added:**
```gradle
// gradle/libs.versions.toml
securityCrypto = "1.1.0-alpha06"
datastore = "1.1.1"

// core/data/build.gradle.kts
implementation(libs.androidx.security.crypto)
implementation(libs.androidx.datastore.preferences)
implementation(libs.okhttp)
implementation(libs.okhttp.logging.interceptor)
implementation(libs.retrofit)
implementation(libs.retrofit.converter.kotlinx.serialization)
```

##### Phase 3: Token Requests & Expiration ✅

**Status**: COMPLETE
- ✅ `AuthTokenInterceptor` adds Bearer tokens to all API requests
- ✅ `logout()` method clears tokens
- ✅ Token expiration validation with safety buffer
- ⚠️ 401 response handling not yet implemented (future enhancement)

#### Breaking Changes Made

1. **LoginResult Type Change**:
   - FROM: `Result<String, AuthError>` (returned token string)
   - TO: `Result<Unit, AuthError>` (token saved internally)
   - Impact: ViewModels no longer receive token directly

2. **Repository Responsibility Shift**:
   - Repository now handles token storage (separation of concerns)
   - ViewModels only handle UI state and navigation
   - Tokens never exposed to presentation layer

#### Auth State Navigation Integration

**Implementation**: Auth state checking integrated with Splash Screen (see section 4 below)
- ✅ `MainViewModel` checks `TokenManager.isTokenValid()` on app startup
- ✅ `TaskyNavHost` receives `isAuthenticated` parameter and routes accordingly
- ✅ Splash screen keeps visible while auth state is being checked
- ✅ Automatic navigation to AgendaGraph (authenticated) or AuthGraph (not authenticated)

#### Architecture Decisions Made

**Why DataStore + CryptoManager (Not EncryptedSharedPreferences)?**
- ✅ Modern, coroutine-based API
- ✅ Flow-based reactive state
- ✅ Manual encryption gives control over key management
- ✅ Consistent with async architecture

**Why Changed LoginResult from String to Unit?**
- ✅ Separation of concerns (repository handles storage)
- ✅ Prevents token exposure in ViewModels
- ✅ Centralizes token management logic
- ✅ Easier to add features like token refresh

**Why Centralize Networking in :core:data?**
- ✅ Single source of truth for OkHttpClient
- ✅ Interceptors applied consistently
- ✅ Easier to add global error handling
- ✅ Follows Clean Architecture principles

#### Session Flow

```
Login Screen
    ↓
Enter credentials
    ↓
Repository.login() → API call → AuthResponse
    ↓                              ↓
TokenManager.saveSession() ←───────┘
    ↓
Encrypt with CryptoManager
    ↓
Store in DataStore
    ↓
Return Success(Unit)
    ↓
Navigate to AgendaScreen
    ↓
App Restart
    ↓
Splash Screen shows (Android 12+ API)
    ↓
MainViewModel checks TokenManager.isTokenValid()
    ↓
Read from DataStore → Decrypt → Check expiration
    ↓
MainViewModel.isAuthenticated updates (true/false)
    ↓
Splash screen dismisses
    ↓
TaskyNavHost receives isAuthenticated parameter
    ↓
If valid: Navigate to AgendaGraph ✅
If expired: Navigate to AuthGraph
```

#### Testing Status
- ✅ `DefaultAuthRepositoryTest` - Verifies token saving on login (4 tests)
- ✅ `RegisterViewModelTest` - Uses fake repository pattern (8 tests)
- ✅ `LoginViewModelTest` - Fixed type mismatch issue (4 tests)
- ✅ `MainViewModelTest` - Tests auth state initialization (3 tests)
- ❌ Token encryption/decryption - No dedicated tests
- ❌ AuthTokenInterceptor - No tests

**Total Test Coverage**: 19 unit tests across 4 test files - ALL PASSING ✅

## Project Overview

Tasky is an Android task management application built with Kotlin and Jetpack Compose, following a modular multi-module architecture with Clean Architecture principles.

**Base package**: `com.dmd.tasky`

**Key Technologies**:
- Kotlin 2.0.21 with Compose 2.0.21
- Jetpack Compose for UI
- Hilt for dependency injection
- Retrofit + OkHttp + Kotlinx Serialization for networking
- Timber for logging
- MockK + Kotlinx Coroutines Test for testing

## Module Architecture

The project follows a feature-based modular architecture:

```
:app                    - Main application module, hosts MainActivity and TaskyApplication
:features:auth          - Authentication feature (login, register)
:core:domain:util       - Shared domain utilities (Result, Error)
:core:data              - Shared data layer (token storage, session management)
```

**Core Modules:**
- `:core:domain:util` - Domain-level abstractions (Result, Error interfaces)
- `:core:data` - Data persistence, token storage, session management (uses DataStore with AES-256-GCM encryption)

Each feature module follows Clean Architecture layers:
- `data/` - Repository implementations, API clients, DTOs, interceptors
- `domain/` - Repository interfaces, domain models, business logic
- `presentation/` - ViewModels, UI states, actions, Compose screens
- `di/` - Hilt modules for dependency injection

**Token Storage Architecture:**
- `TokenManager` interface defines contract for secure session management
- `DataStoreTokenStorage` implementation uses DataStore with manual AES-256-GCM encryption
- Encryption handled by `CryptoManager` using Android Keystore (hardware-backed keys)
- Stores: accessToken, refreshToken, userId, username, accessTokenExpirationTimestamp
- Provides Flow-based authentication state with automatic expiration checking (5-minute safety buffer)

## Common Commands

### Build & Assemble
```bash
./gradlew build                    # Build all modules
./gradlew app:assembleDebug        # Build debug APK
./gradlew app:assembleRelease      # Build release APK
./gradlew clean                    # Clean build artifacts
```

### Testing
```bash
./gradlew test                           # Run all unit tests
./gradlew testDebugUnitTest              # Run debug unit tests
./gradlew features:auth:testDebugUnitTest # Run tests for specific module
./gradlew connectedAndroidTest           # Run instrumented tests on connected devices
./gradlew connectedDebugAndroidTest      # Run debug instrumented tests
```

### Code Quality
```bash
./gradlew lint                     # Run lint on default variant
./gradlew lintDebug                # Run lint on debug variant
./gradlew lintFix                  # Apply safe lint suggestions
./gradlew check                    # Run all checks (tests + lint)
```

## Architecture Patterns

### Result/Error Handling

The codebase uses a custom `Result` sealed interface for error handling:

```kotlin
// Located in :core:domain:util
sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D)
    data class Error<out E: Error>(val error: E)
}
```

Feature modules define domain-specific errors (e.g., `AuthError`) that implement the `Error` interface from `:core:domain:util`.

**Extension functions**: `map()`, `onSuccess()`, `onError()`, `asEmptyDataResult()`

**Usage pattern**: Repository methods return `Result` types, ViewModels handle success/error cases using extension functions.

### Dependency Injection

Hilt is used throughout with the following setup:
- Application class: `TaskyApplication` annotated with `@HiltAndroidApp`
- MainActivity: Annotated with `@AndroidEntryPoint`
- ViewModels: Annotated with `@HiltViewModel` with `@Inject` constructor
- DI modules: Feature modules provide their own Hilt modules in `di/` package (e.g., `AuthModule`)

### Presentation Layer Pattern

Features use MVI-style architecture:
- **ViewModel**: Holds `state` (UI state data class) and `onAction(action: Action)` method
- **Action**: Sealed interface defining all user interactions
- **UiState**: Data class containing all screen state
- **Screen**: Composable that observes ViewModel state and dispatches actions

Example from auth feature:
- `LoginViewModel` with `LoginUiState` and `LoginAction`
- `LoginScreen` Composable

### API Configuration

The auth feature uses `BuildConfig` for API configuration:
- Base URL: Defined in `features:auth/build.gradle.kts` as `BuildConfig.BASE_URL`
- API Key: Loaded from `local.properties` file as `apiKey` property
- Interceptor: `ApiKeyInterceptor` adds API key to requests

**Note**: The `local.properties` file is git-ignored and must be created locally with the `apiKey` property.

### Networking

Retrofit setup in `AuthModule`:
- Uses Kotlinx Serialization for JSON parsing
- OkHttp with logging interceptor (body level logging)
- Custom `ApiKeyInterceptor` for authentication
- DTOs in `data/remote/dto/` package
- API interface in `data/remote/` package

### Navigation

The app uses type-safe navigation with Navigation Compose and Kotlin Serialization:

**Setup:**
- Routes defined in `app/src/main/java/com/dmd/tasky/ui/navigation/Routes.kt`
- All route objects are `@Serializable` data objects
- Navigation graphs: `AuthGraph` and `AgendaGraph`
- Individual routes: `AuthRoute.Login`, `AuthRoute.Register`, `AgendaRoute.Agenda`

**Pattern:**
```kotlin
// Navigation graphs (top-level destinations)
@Serializable
data object AuthGraph

// Route sealed interfaces for type-safety
sealed interface AuthRoute {
    @Serializable
    data object Login : AuthRoute
}
```

**Usage in NavHost:**
- `TaskyNavHost` in `app/src/main/java/com/dmd/tasky/ui/navigation/TaskyNavHost.kt`
- Uses nested navigation with `navigation<Graph>()` composable
- Screens receive navigation callbacks (e.g., `onNavigateToLogin`, `onLoginSuccess`)
- Clearing backstacks: Use `popUpTo(Graph) { inclusive = true }` for login→agenda flow

**Dependencies:**
- `androidx.navigation:navigation-compose` (in app module)
- `kotlinx-serialization-json` (in app module)
- Kotlin Serialization plugin enabled in `app/build.gradle.kts`

#### 3. Repository Refactoring - `safeApiCall` Pattern ✅ COMPLETE

**Branch**: `featureTokenPersistance` (merged)
**Date**: November 29, 2024

**What was done:**
- Implemented `safeApiCall` helper function to eliminate code duplication
- Refactored all repository methods (`login`, `register`, `logout`) to use DRY pattern
- Added contextual logging with operation names for better debugging
- Used `.onSuccess` for declarative side effects (session management)

**Implementation:**
- File: `features/auth/src/main/java/com/dmd/tasky/features/auth/data/repository/DefaultAuthRepository.kt`
- Helper function: `safeApiCall(operation: String, apiCall: suspend () -> T)` (lines 65-91)
- Centralized error handling for all HTTP exceptions, timeouts, and network errors
- Reduced from ~120 lines to 92 lines (28 lines saved)

**Pattern used:**
```kotlin
override suspend fun logout(): LogoutResult {
    return safeApiCall("logout") { api.logout() }
        .onSuccess { tokenManager.clearSession() }
}
```

**Benefits achieved:**
- ✅ DRY principle - error handling in one place
- ✅ Declarative side effects with `.onSuccess`
- ✅ Contextual logging ("HTTP Error during login: Code=401")
- ✅ Easier to maintain and test
- ✅ All tests updated and passing

#### 4. Splash Screen Implementation ✅ COMPLETE (Updated after PR Review)

**Branch**: `splashScreen`
**Date**: November 30, 2024
**PR Review Fix**: January 2025

**Architecture Decision:**
- Using Android 12+ Splash Screen API instead of custom splash screen/view
- Auth state check handled in `MainViewModel` (not dedicated SplashViewModel)
- Simpler implementation, leverages platform capabilities

**What was implemented:**

**Files created:**
- `app/src/main/java/com/dmd/tasky/MainViewModel.kt` - Manages auth state on app startup
- `app/src/test/java/com/dmd/tasky/MainViewModelTest.kt` - 4 unit tests, all passing

**Files modified:**
- `app/src/main/java/com/dmd/tasky/MainActivity.kt` - Integrated Splash Screen API
- `app/src/main/java/com/dmd/tasky/ui/navigation/TaskyNavHost.kt` - Accepts `isAuthenticated` parameter
- `app/src/main/java/com/dmd/tasky/ui/navigation/Routes.kt` - Removed unused Splash route
- `app/build.gradle.kts` - Added `androidx.core:core-splashscreen` dependency

**Implementation Details (CORRECTED after PR review):**

`MainViewModel.kt`:
```kotlin
data class MainState(
    val isCheckingAuth: Boolean = true,   // Splash screen condition
    val isLoggedIn: Boolean = false       // Navigation decision
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {
    var state by mutableStateOf(MainState())
        private set

    init {
        viewModelScope.launch {
            val isValid = tokenManager.isTokenValid()
            state = state.copy(
                isCheckingAuth = false,
                isLoggedIn = isValid
            )
        }
    }
}
```

`MainActivity.kt`:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Keep splash visible while CHECKING auth (not while unauthenticated!)
    installSplashScreen().setKeepOnScreenCondition {
        viewModel.state.isCheckingAuth
    }

    setContent {
        if (!viewModel.state.isCheckingAuth) {
            TaskyNavHost(isAuthenticated = viewModel.state.isLoggedIn)
        }
    }
}
```

**Flow (CORRECTED):**
```
App Launch
    ↓
isCheckingAuth = true, isLoggedIn = false
    ↓
Splash stays visible (isCheckingAuth = true)
    ↓
Token check completes
    ↓
isCheckingAuth = false, isLoggedIn = true/false
    ↓
Splash dismisses (isCheckingAuth = false)
    ↓
TaskyNavHost renders → AgendaGraph OR AuthGraph
```

**Test Coverage:**
- `MainViewModelTest.kt` - 4 tests
  1. ✅ MainState defaults have correct values
  2. ✅ Token valid → isLoggedIn = true, isCheckingAuth = false
  3. ✅ Token invalid → isLoggedIn = false, isCheckingAuth = false
  4. ✅ Verifies token check called exactly once

**Advantages of this approach:**
- ✅ Leverages Android platform Splash Screen API
- ✅ No custom splash screen composable needed
- ✅ Simple, maintainable code
- ✅ Automatic splash screen animations and transitions
- ✅ Works with system theme and branding

**⚠️ LESSON LEARNED - Splash Screen Anti-Pattern:**

The original implementation had a bug caught in PR review:

```kotlin
// ❌ WRONG - Splash stays forever for logged-out users!
installSplashScreen().setKeepOnScreenCondition {
    !viewModel.isAuthenticated  // FALSE for logged-out users = splash never dismisses
}
```

**The problem:** Using authentication STATUS as splash condition means:
- Logged-in users: `isAuthenticated = true` → `!true = false` → splash dismisses ✓
- Logged-out users: `isAuthenticated = false` → `!false = true` → splash NEVER dismisses ✗

**The fix:** Separate "checking" from "result":
- `isCheckingAuth` - Are we still checking? (splash condition)
- `isLoggedIn` - What was the result? (navigation decision)

**Key insight:** Splash screen should dismiss when the CHECK COMPLETES, not based on the CHECK RESULT.

## Development Workflow

### Adding a New Feature Module

1. Create module structure in `settings.gradle.kts`
2. Follow the standard layer structure: `data/`, `domain/`, `presentation/`, `di/`
3. Create Hilt module for dependencies
4. Depend on `:core:domain:util` for shared utilities
5. Add feature module dependency to `:app` module

### Testing Guidelines

- Unit tests: Located in `src/test/` directory
- Use MockK for mocking
- Use `kotlinx-coroutines-test` for testing coroutines
- ViewModels should have corresponding test files (e.g., `RegisterViewModelTest`)
- Test files follow `*Test.kt` naming convention

### Code Style

- Package structure follows reverse domain: `com.dmd.tasky`
- Feature-specific packages include feature name: `com.dmd.tasky.features.auth`
- Use Timber for logging (configured in `TaskyApplication`)
- Timber only plants `DebugTree` in debug builds

## Common Pitfalls & Lessons Learned

### 1. Splash Screen: Separate "Checking" from "Result"

**Anti-pattern:**
```kotlin
// ❌ WRONG - condition based on auth RESULT
installSplashScreen().setKeepOnScreenCondition {
    !viewModel.isAuthenticated
}
```

**Correct pattern:**
```kotlin
// ✅ CORRECT - condition based on check COMPLETION
data class MainState(
    val isCheckingAuth: Boolean = true,  // For splash
    val isLoggedIn: Boolean = false      // For navigation
)

installSplashScreen().setKeepOnScreenCondition {
    viewModel.state.isCheckingAuth
}
```

**Why:** Splash should dismiss when async operation COMPLETES, not based on RESULT.

### 2. Composition vs Inheritance for Domain Models

When domain models share many properties but differ in few, prefer composition:

**Anti-pattern (inheritance):**
```kotlin
sealed interface AgendaItem {
    data class Event(override val id, override val title, ..., val attendees) : AgendaItem
    data class Task(override val id, override val title, ..., val isDone) : AgendaItem
}
// Problem: Repeated "override val" for every common property
```

**Correct pattern (composition):**
```kotlin
data class AgendaItem(
    val id: String,
    val title: String,
    // ...common properties defined ONCE
    val details: AgendaItemDetails
)

sealed interface AgendaItemDetails {
    data class Event(val to: LocalDateTime, val attendees: List<Attendee>) : AgendaItemDetails
    data class Task(val isDone: Boolean) : AgendaItemDetails
    data object Reminder : AgendaItemDetails  // No unique properties
}
```

**Why:**
- Common properties defined once
- `data object` elegantly expresses "no unique properties"
- Sorting/filtering works directly: `items.sortedBy { it.time }`