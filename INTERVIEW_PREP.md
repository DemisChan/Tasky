# Android Developer Interview Preparation Guide

**Prepared for:** Tasky Project Review & General Android Interviews
**Last Updated:** January 30, 2026

---

## Table of Contents

1. [Project-Specific Topics](#1-project-specific-topics)
   - [Architecture Patterns](#architecture-patterns)
   - [Dependency Injection with Hilt](#dependency-injection-with-hilt)
   - [Jetpack Compose](#jetpack-compose)
   - [Networking & Serialization](#networking--serialization)
   - [Coroutines & Flow](#coroutines--flow)
   - [Security & Encryption](#security--encryption)
   - [Navigation](#navigation)
   - [Testing](#testing)
2. [Android Fundamentals](#2-android-fundamentals)
3. [Kotlin Language Fundamentals](#3-kotlin-language-fundamentals)
4. [Common Interview Questions](#4-common-interview-questions)
5. [System Design Questions](#5-system-design-questions)
6. [Behavioral Questions](#6-behavioral-questions)

---

## 1. Project-Specific Topics

### Architecture Patterns

#### Clean Architecture

**What is it?**
- Separation of concerns into layers: Presentation → Domain → Data
- Dependency rule: Inner layers know nothing about outer layers
- Domain layer is framework-independent (pure Kotlin)

**Layers in Tasky:**

```
┌─────────────────────────────────────┐
│     Presentation Layer              │
│  (ViewModels, UI States, Screens)   │
│  Depends on: Domain                 │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│       Domain Layer                  │
│  (Repositories, Use Cases, Models)  │
│  Pure Kotlin - No Android deps      │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│        Data Layer                   │
│  (Repository Impl, API, DTOs)       │
│  Depends on: Domain                 │
└─────────────────────────────────────┘
```

**Benefits:**
- Testability: Domain logic can be tested without Android framework
- Flexibility: Easy to swap data sources (API → Database)
- Maintainability: Clear separation of responsibilities

**Interview Questions:**
- *"Why use Clean Architecture instead of MVVM alone?"*
  - MVVM is presentation pattern, Clean Architecture is full-stack architecture
  - Clean Architecture adds domain layer for business logic isolation
  - Better for complex apps with multiple data sources

- *"What's the difference between domain models and DTOs?"*
  - DTOs (Data Transfer Objects): Match API/database structure, may have extra/missing fields
  - Domain models: App's business representation, optimized for use cases
  - Example: `AuthResponse` (DTO) → `SessionData` (domain model)

#### Multi-Module Architecture

**Structure in Tasky:**
```
:app                    # Application entry point, DI setup
:features:auth          # Authentication feature
:features:agenda        # Agenda feature
:core:domain:util       # Shared domain utilities
:core:data              # Shared data layer
```

**Benefits:**
- Build time: Parallel builds, incremental compilation
- Separation: Clear feature boundaries
- Reusability: Core modules shared across features
- Team scalability: Teams can own specific modules

**Interview Questions:**
- *"When should you split into multiple modules?"*
  - Feature grows beyond ~20 files
  - Multiple developers working on same codebase
  - Want to enforce separation (e.g., prevent UI from accessing DB directly)
  - Build times exceed 30-60 seconds

#### MVI (Model-View-Intent)

**Pattern in Tasky:**
```kotlin
// State (Model)
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: AuthError? = null
)

// Intent (User Actions)
sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data object LoginClicked : LoginAction
}

// ViewModel
class LoginViewModel : ViewModel() {
    var state by mutableStateOf(LoginUiState())
        private set

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> state = state.copy(email = action.email)
            // ...
        }
    }
}
```

**Benefits:**
- Unidirectional data flow (predictable)
- Single source of truth (state)
- Immutable state (easier debugging)
- All actions explicit (easy to log/test)

**Interview Questions:**
- *"MVI vs MVVM?"*
  - MVVM: Multiple LiveData/StateFlow, bidirectional binding
  - MVI: Single state object, unidirectional flow
  - MVI better for complex state interactions

---

### Dependency Injection with Hilt

#### Core Concepts

**What is DI?**
- Pattern where objects receive dependencies instead of creating them
- Promotes loose coupling, testability

**Why Hilt over manual DI?**
- Compile-time verification
- Android lifecycle awareness
- Less boilerplate than Dagger
- Official Google recommendation

**Hilt Setup in Tasky:**

```kotlin
// 1. Application class
@HiltAndroidApp
class TaskyApplication : Application()

// 2. Activity
@AndroidEntryPoint
class MainActivity : ComponentActivity()

// 3. ViewModel
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel()

// 4. Module
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApi,
        tokenManager: TokenManager
    ): AuthRepository = DefaultAuthRepository(api, tokenManager)
}
```

**Scopes:**
- `@Singleton`: Lives for app lifetime
- `@ActivityScoped`: Lives for Activity lifetime
- `@ViewModelScoped`: Lives for ViewModel lifetime

**Interview Questions:**
- *"How does Hilt know which implementation to inject for an interface?"*
  - `@Binds` or `@Provides` in modules tell Hilt the binding
  - Example: `AuthRepository` interface → `DefaultAuthRepository` impl

- *"What happens if you forget @AndroidEntryPoint?"*
  - Compile error: "Hilt ViewModels must be used in a Hilt-enabled component"
  - Activity/Fragment must be annotated for injection to work

- *"How would you provide different implementations for debug/release?"*
  ```kotlin
  @Provides
  fun provideAuthRepository(
      @ApplicationContext context: Context
  ): AuthRepository {
      return if (BuildConfig.DEBUG) {
          FakeAuthRepository()
      } else {
          DefaultAuthRepository(/* ... */)
      }
  }
  ```

---

### Jetpack Compose

#### Fundamentals

**Composable Functions:**
```kotlin
@Composable
fun LoginScreen(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit
) {
    Column {
        TextField(
            value = state.email,
            onValueChange = { onAction(LoginAction.EmailChanged(it)) }
        )
        Button(onClick = { onAction(LoginAction.LoginClicked) }) {
            Text("Login")
        }
    }
}
```

**Key Concepts:**
- **Recomposition**: Function re-executes when state changes
- **State hoisting**: State lives in ViewModel, not in Composable
- **Side effects**: Use `LaunchedEffect`, `DisposableEffect`, etc.

**Compose State:**
```kotlin
// ViewModel
var state by mutableStateOf(LoginUiState())

// Composable
val state by viewModel.state.collectAsState()
```

**Interview Questions:**
- *"What's the difference between `remember` and `rememberSaveable`?"*
  - `remember`: Survives recomposition, NOT configuration changes
  - `rememberSaveable`: Survives both recomposition AND config changes (rotation)

- *"When should you use `LaunchedEffect` vs `SideEffect`?"*
  - `LaunchedEffect`: Suspend operations (API calls, delays)
  - `SideEffect`: Non-suspend side effects (analytics logging)
  - `DisposableEffect`: Cleanup needed (register/unregister listener)

- *"How do you optimize Compose performance?"*
  - Use `key()` in lists to help Compose track items
  - Avoid lambda allocations with `remember { {} }`
  - Use `derivedStateOf` for computed state
  - Stable parameters (immutable data classes)

**Compose vs Views:**

| Aspect | Views (XML) | Compose |
|--------|-------------|---------|
| Paradigm | Imperative | Declarative |
| Code location | XML + Kotlin | Kotlin only |
| Preview | Requires build | Instant @Preview |
| State management | Manual findViewById | Automatic recomposition |
| Performance | Faster initial | Faster updates |

---

### Networking & Serialization

#### Retrofit + OkHttp

**Setup in Tasky:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CoreDataModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor())
            .addInterceptor(AuthTokenInterceptor())
            .addInterceptor(HttpLoggingInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
```

**API Interface:**
```kotlin
interface AuthApi {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest)

    @GET("/logout")
    suspend fun logout()
}
```

**Interview Questions:**
- *"What's an interceptor? Give examples."*
  - Code that runs before/after HTTP requests
  - Examples: Adding headers, logging, authentication, error handling
  - Chain of responsibility pattern

- *"How do you handle 401 Unauthorized responses?"*
  ```kotlin
  class AuthTokenInterceptor @Inject constructor(
      private val tokenManager: TokenManager
  ) : Interceptor {
      override fun intercept(chain: Chain): Response {
          val request = chain.request().newBuilder()
              .addHeader("Authorization", "Bearer ${tokenManager.getToken()}")
              .build()

          val response = chain.proceed(request)

          if (response.code == 401) {
              // Token expired - refresh or logout
              tokenManager.clearSession()
          }

          return response
      }
  }
  ```

- *"Retrofit vs Ktor?"*
  - Retrofit: Java-based, mature ecosystem, annotation-based
  - Ktor: Kotlin-first, coroutines-native, more flexible
  - Retrofit better for REST APIs, Ktor better for custom protocols

#### Kotlinx Serialization

**Why over Gson/Moshi?**
- Kotlin-first (better type safety)
- Compile-time code generation (faster)
- Multiplatform support (KMP)
- Better null handling

**Usage:**
```kotlin
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val fullName: String,
    val accessTokenExpirationTimestamp: Long
)

// Custom names
@Serializable
data class User(
    @SerialName("user_id") val userId: String
)

// Enums
@Serializable
enum class AgendaItemType {
    @SerialName("EVENT") EVENT,
    @SerialName("TASK") TASK,
    @SerialName("REMINDER") REMINDER
}
```

---

### Coroutines & Flow

#### Coroutines Basics

**What are coroutines?**
- Lightweight threads for async programming
- Suspend functions: Can pause without blocking thread
- Structured concurrency: Parent-child relationship

**Scopes in Android:**
```kotlin
// ViewModel
viewModelScope.launch {
    // Cancelled when ViewModel cleared
}

// Lifecycle-aware
lifecycleScope.launch {
    // Cancelled when lifecycle destroyed
}

// Application-wide
GlobalScope.launch {
    // ⚠️ Avoid - never cancelled
}
```

**Dispatchers:**
- `Dispatchers.Main`: UI thread
- `Dispatchers.IO`: Network/disk operations (64 threads)
- `Dispatchers.Default`: CPU-intensive work (cores count)
- `Dispatchers.Unconfined`: Don't use (testing only)

**Example from Tasky:**
```kotlin
fun login(email: String, password: String) {
    viewModelScope.launch {
        state = state.copy(isLoading = true)

        repository.login(email, password)
            .onSuccess {
                // Navigate to next screen
            }
            .onError { error ->
                state = state.copy(error = error)
            }

        state = state.copy(isLoading = false)
    }
}
```

#### Flow

**What is Flow?**
- Asynchronous stream of values
- Cold stream (emits only when collected)
- Reactive programming for Kotlin

**StateFlow vs SharedFlow:**
```kotlin
// StateFlow - Always has value, replays last value
val isAuthenticated: StateFlow<Boolean>

// SharedFlow - May have no value, configurable replay
val events: SharedFlow<Event>
```

**Usage in Tasky:**
```kotlin
// TokenManager
fun isAuthenticated(): Flow<Boolean> = dataStore.data
    .map { prefs ->
        val encryptedToken = prefs[TOKEN_KEY]
        encryptedToken != null && isTokenValid()
    }

// Collecting in ViewModel
init {
    viewModelScope.launch {
        tokenManager.isAuthenticated()
            .collect { isAuth ->
                state = state.copy(isAuthenticated = isAuth)
            }
    }
}
```

**Interview Questions:**
- *"launch vs async?"*
  - `launch`: Fire and forget, returns Job
  - `async`: Returns Deferred<T>, call .await() to get result
  - Use `async` when you need return value

- *"What's structured concurrency?"*
  - Parent coroutine waits for children to complete
  - If parent cancelled, all children cancelled
  - Prevents coroutine leaks

- *"Flow vs LiveData?"*
  - Flow: Kotlin-first, more operators, cold stream, multiplatform
  - LiveData: Android-specific, lifecycle-aware, hot stream
  - Flow is newer, more powerful

---

### Security & Encryption

#### Token Storage in Tasky

**Architecture:**
```
TokenManager (interface)
    ↓
DataStoreTokenStorage (implementation)
    ↓ uses
CryptoManager (AES-256-GCM encryption)
    ↓ uses
Android Keystore (hardware-backed keys)
    ↓ stores in
DataStore Preferences (encrypted data)
```

**Encryption Implementation:**
```kotlin
class CryptoManager {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore")

    fun encrypt(data: ByteArray): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = getOrCreateKey()

        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv  // Random 12-byte IV
        val encryptedBytes = cipher.doFinal(data)

        return EncryptedData(
            ciphertext = encryptedBytes.toBase64(),
            iv = iv.toBase64()
        )
    }

    fun decrypt(encryptedData: EncryptedData): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = getOrCreateKey()

        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, encryptedData.iv))
        return cipher.doFinal(encryptedData.ciphertext)
    }
}
```

**Security Features:**
- **AES-256-GCM**: Authenticated encryption (confidentiality + integrity)
- **Android Keystore**: Hardware-backed keys (never exposed to app)
- **Random IV**: Different IV per encryption (prevents pattern analysis)
- **GCM Tag**: Detects tampering (authentication tag)

**Interview Questions:**
- *"Why not just use EncryptedSharedPreferences?"*
  - Tasky uses DataStore for modern coroutine-based API
  - Manual encryption gives more control
  - EncryptedSharedPreferences is fine for simpler apps

- *"What's the difference between encryption and hashing?"*
  - Encryption: Reversible (encrypt → decrypt)
  - Hashing: One-way (password → hash, can't reverse)
  - Use encryption for tokens, hashing for passwords

- *"How do you protect against man-in-the-middle attacks?"*
  - HTTPS/TLS: Encrypts network traffic
  - Certificate pinning: Validate server certificate
  - Don't trust user-installed certificates on device

**Token Expiration:**
```kotlin
fun isTokenValid(): Boolean {
    val expirationTime = tokenManager.getExpirationTimestamp()
    val currentTime = System.currentTimeMillis()
    val bufferTime = 5.minutes.inWholeMilliseconds

    return expirationTime - currentTime > bufferTime
}
```

---

### Navigation

#### Type-Safe Navigation with Navigation Compose

**Routes in Tasky:**
```kotlin
// Navigation graphs
@Serializable
data object AuthGraph

@Serializable
data object AgendaGraph

// Individual routes
sealed interface AuthRoute {
    @Serializable
    data object Login : AuthRoute

    @Serializable
    data object Register : AuthRoute
}

sealed interface AgendaRoute {
    @Serializable
    data object Agenda : AgendaRoute

    @Serializable
    data class EventDetail(val eventId: String) : AgendaRoute
}
```

**NavHost Implementation:**
```kotlin
@Composable
fun TaskyNavHost(isAuthenticated: Boolean) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) AgendaGraph else AuthGraph
    ) {
        navigation<AuthGraph>(startDestination = AuthRoute.Login) {
            composable<AuthRoute.Login> {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(AuthRoute.Register) },
                    onLoginSuccess = {
                        navController.navigate(AgendaGraph) {
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    }
                )
            }

            composable<AuthRoute.Register> {
                RegisterScreen(
                    onNavigateToLogin = { navController.navigateUp() }
                )
            }
        }

        navigation<AgendaGraph>(startDestination = AgendaRoute.Agenda) {
            composable<AgendaRoute.Agenda> {
                AgendaScreen(
                    onEventClick = { eventId ->
                        navController.navigate(AgendaRoute.EventDetail(eventId))
                    }
                )
            }
        }
    }
}
```

**Benefits:**
- Compile-time safety (no string routes)
- Type-safe arguments
- Refactoring-friendly (rename class → routes update automatically)

**Interview Questions:**
- *"How do you pass complex objects between screens?"*
  - Don't! Pass only IDs, fetch data in destination screen
  - Reason: Reduces coupling, prevents stale data
  - SavedStateHandle limited to primitives + parcelables

- *"What's the difference between popUpTo and popBackStack?"*
  - `popUpTo`: Remove destinations up to a specific route
  - `popBackStack()`: Remove top destination
  - `inclusive = true`: Also remove the destination you popped up to

---

### Testing

#### Unit Testing in Tasky

**Test Structure:**
```kotlin
@Test
fun `login with valid credentials saves session`() = runTest {
    // Given (Arrange)
    val email = "test@example.com"
    val password = "password123"
    val expectedResponse = AuthResponse(/* ... */)
    coEvery { api.login(any()) } returns expectedResponse

    // When (Act)
    val result = repository.login(email, password)

    // Then (Assert)
    assertThat(result).isInstanceOf<Result.Success<Unit>>()
    coVerify { tokenManager.saveSession(expectedResponse.toSessionData()) }
}
```

**MockK Usage:**
```kotlin
// Mock
val api: AuthApi = mockk()

// Stub
coEvery { api.login(any()) } returns AuthResponse(/* ... */)

// Verify
coVerify { tokenManager.saveSession(any()) }
coVerify(exactly = 1) { api.login(any()) }

// Relaxed mock (returns default values)
val relaxedMock: AuthApi = mockk(relaxed = true)
```

**Fake vs Mock:**

**Mock (LoginViewModelTest):**
```kotlin
class LoginViewModelTest {
    private val repository: AuthRepository = mockk()

    @Test
    fun `test login success`() {
        coEvery { repository.login(any(), any()) } returns Result.Success(Unit)
        // ...
    }
}
```

**Fake (RegisterViewModelTest):**
```kotlin
class FakeAuthRepository : AuthRepository {
    var shouldReturnError = false

    override suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): Result<Unit, AuthError> {
        return if (shouldReturnError) {
            Result.Error(AuthError.NetworkError)
        } else {
            Result.Success(Unit)
        }
    }
}
```

**When to use which?**
- **Mocks**: Test specific interactions, verify calls
- **Fakes**: Test behavior, more realistic, reusable across tests

**Coroutine Testing:**
```kotlin
@Test
fun `test coroutine delay`() = runTest {
    // Virtual time - instant execution
    delay(1000)  // Doesn't actually wait 1 second

    // Assert
    assertThat(result).isEqualTo(expected)
}
```

**Interview Questions:**
- *"What's the difference between unit, integration, and UI tests?"*
  - Unit: Single class in isolation (ViewModel)
  - Integration: Multiple classes together (Repository + API)
  - UI: User interactions (click button → verify navigation)

- *"How do you test ViewModels?"*
  - Mock/fake dependencies (repository, use cases)
  - Test state changes
  - Test actions dispatch correct events
  - Use `runTest` for coroutines

- *"What is TDD (Test-Driven Development)?"*
  - Write test first (fails)
  - Write minimal code to pass
  - Refactor
  - Benefits: Better design, 100% coverage, living documentation

---

## 2. Android Fundamentals

### Activity Lifecycle

```
onCreate() → onStart() → onResume() → [RUNNING]
                                          ↓
                                      onPause()
                                          ↓
                                      onStop()
                                          ↓
                                     onDestroy()
```

**Key Methods:**
- `onCreate()`: Initialize UI, set content view
- `onStart()`: Visible to user
- `onResume()`: Foreground, interactive
- `onPause()`: Losing focus (dialog appears)
- `onStop()`: No longer visible (another activity on top)
- `onDestroy()`: Activity finishing

**Configuration Changes:**
- Rotation, language change → Activity recreated
- Data lost unless saved in `onSaveInstanceState()`
- ViewModels survive configuration changes

**Interview Questions:**
- *"What happens if you start async work in onCreate() and user rotates?"*
  - Activity destroyed and recreated
  - Async work continues, may leak memory
  - Solution: Use ViewModel + viewModelScope

### Fragment Lifecycle

```
onAttach() → onCreate() → onCreateView() → onViewCreated()
    ↓
onStart() → onResume() → [RUNNING]
    ↓
onPause() → onStop() → onDestroyView() → onDestroy() → onDetach()
```

**Fragment vs Activity:**
- Fragments: Modular UI components, reusable
- Activities: Entry points, host fragments
- Modern approach: Single-Activity architecture with fragments/Compose

### Intents

**Explicit Intent:**
```kotlin
val intent = Intent(this, DetailActivity::class.java)
intent.putExtra("USER_ID", userId)
startActivity(intent)
```

**Implicit Intent:**
```kotlin
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
startActivity(intent)
```

**PendingIntent:**
- Used for notifications, alarms
- Intent wrapper for delayed execution
- Flags: `FLAG_IMMUTABLE`, `FLAG_UPDATE_CURRENT`

### Services

**Types:**
- **Foreground Service**: Visible notification (music player)
- **Background Service**: No UI (deprecated for long-running tasks)
- **Bound Service**: Client-server interface

**Modern Alternatives:**
- WorkManager for background tasks
- Foreground services for user-aware tasks

### Broadcast Receivers

**Register:**
```kotlin
// Manifest
<receiver android:name=".MyReceiver">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>

// Dynamic
registerReceiver(receiver, IntentFilter("MY_ACTION"))
```

**Use Cases:**
- System events (boot completed, battery low)
- App-to-app communication
- LocalBroadcastManager for internal events (deprecated - use LiveData/Flow)

### Content Providers

**Purpose:**
- Share data between apps
- Standard interface for CRUD operations

**Examples:**
- Contacts, Calendar, Media Store

**Modern Alternative:**
- Direct database access with Room (within app)
- Expose APIs for cross-app sharing

### Permissions

**Types:**
- **Normal**: Auto-granted (INTERNET, ACCESS_NETWORK_STATE)
- **Dangerous**: User approval (CAMERA, LOCATION, READ_CONTACTS)

**Request Runtime Permission:**
```kotlin
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        // Permission granted
    }
}

Button(onClick = { launcher.launch(android.Manifest.permission.CAMERA) }) {
    Text("Request Camera")
}
```

---

## 3. Kotlin Language Fundamentals

### Null Safety

```kotlin
var nullable: String? = null
var nonNull: String = "Hello"

// Safe call
val length = nullable?.length

// Elvis operator
val length = nullable?.length ?: 0

// Not-null assertion (avoid!)
val length = nullable!!.length  // Throws if null

// Safe cast
val str: String? = obj as? String
```

### Data Classes

```kotlin
data class User(val id: String, val name: String)

// Auto-generated:
// - equals() / hashCode()
// - toString()
// - copy()
// - componentN() for destructuring

val user = User("1", "John")
val updated = user.copy(name = "Jane")
val (id, name) = user  // Destructuring
```

### Sealed Classes

```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: String) : Result<Nothing>
}

// Exhaustive when
when (result) {
    is Result.Success -> println(result.data)
    is Result.Error -> println(result.error)
    // No else needed - compiler knows all cases
}
```

### Extension Functions

```kotlin
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

// Usage
val email = "test@example.com"
if (email.isValidEmail()) { /* ... */ }
```

### Higher-Order Functions

```kotlin
// Function as parameter
fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (item in this) {
        if (predicate(item)) result.add(item)
    }
    return result
}

// Usage
val numbers = listOf(1, 2, 3, 4, 5)
val evens = numbers.customFilter { it % 2 == 0 }
```

### Scope Functions

```kotlin
// let - transform object
val length = str?.let { it.length }

// run - execute block, return result
val result = run {
    val x = 5
    val y = 10
    x + y
}

// apply - configure object, return object
val user = User().apply {
    name = "John"
    age = 30
}

// also - side effects, return object
val list = mutableListOf<String>().also {
    println("Creating list")
}

// with - group operations
with(user) {
    println(name)
    println(age)
}
```

### Collections

```kotlin
// List (immutable)
val list = listOf(1, 2, 3)

// MutableList
val mutable = mutableListOf(1, 2, 3)
mutable.add(4)

// Map
val map = mapOf("key" to "value")

// Common operations
list.filter { it > 2 }
list.map { it * 2 }
list.forEach { println(it) }
list.groupBy { it % 2 }
list.sortedBy { it }
```

---

## 4. Common Interview Questions

### Memory & Performance

**Q: What causes memory leaks in Android?**

A: Common causes:
1. **Static references to Context/Activity**
   ```kotlin
   // ❌ BAD
   companion object {
       var activity: Activity? = null  // Leaks activity
   }

   // ✅ GOOD
   companion object {
       var appContext: Context? = null  // Application context OK
   }
   ```

2. **Inner classes holding references**
   ```kotlin
   // ❌ BAD - Inner class holds Activity reference
   inner class MyRunnable : Runnable {
       override fun run() { /* ... */ }
   }

   // ✅ GOOD - Static class with weak reference
   class MyRunnable(activity: Activity) : Runnable {
       private val weakRef = WeakReference(activity)
       override fun run() {
           weakRef.get()?.let { /* ... */ }
       }
   }
   ```

3. **Listeners not unregistered**
4. **Coroutines/RxJava not cancelled**

**Q: How do you detect memory leaks?**

A: Tools:
- **LeakCanary**: Auto-detects leaks in debug builds
- **Android Profiler**: Monitor memory usage
- **Heap Dump**: Analyze with MAT (Memory Analyzer Tool)

**Q: What is `onTrimMemory()`?**

A: Callback when system is low on memory. You should release caches:
```kotlin
override fun onTrimMemory(level: Int) {
    when (level) {
        TRIM_MEMORY_RUNNING_LOW -> imageCache.clear()
        TRIM_MEMORY_UI_HIDDEN -> releaseUI()
    }
}
```

### Data Persistence

**Q: Compare SharedPreferences, DataStore, Room, and Files.**

| Storage | Use Case | Max Size | Type Safety |
|---------|----------|----------|-------------|
| SharedPreferences | Key-value pairs | ~1 MB | No |
| DataStore | Key-value pairs | ~1 MB | Yes (Typed) |
| Room | Structured data, queries | Unlimited | Yes |
| Files | Large blobs (images) | Unlimited | No |

**Q: When would you use each?**

- **SharedPreferences/DataStore**: Settings, flags, tokens
- **Room**: User data, cached API responses, complex queries
- **Files**: Images, videos, documents

**Q: How does Room differ from SQLite?**

- Room is abstraction over SQLite
- Compile-time SQL verification
- LiveData/Flow integration
- Less boilerplate

### Threading

**Q: What's the difference between Thread, Handler, AsyncTask, and Coroutines?**

**Thread:**
```kotlin
Thread {
    // Background work
    runOnUiThread {
        // Update UI
    }
}.start()
```

**Handler + Looper:**
```kotlin
val handler = Handler(Looper.getMainLooper())
handler.post {
    // Runs on main thread
}
```

**AsyncTask (DEPRECATED):**
```kotlin
class MyTask : AsyncTask<Void, Int, String>() {
    override fun doInBackground(vararg params: Void?): String { }
    override fun onPostExecute(result: String?) { }
}
```

**Coroutines (MODERN):**
```kotlin
viewModelScope.launch {
    val result = withContext(Dispatchers.IO) {
        // Background work
    }
    // Update UI (automatically on Main)
}
```

**Q: What is the main thread / UI thread?**

- Thread that handles UI updates
- Only this thread can touch Views
- Long operations block UI (ANR - Application Not Responding)
- Use background threads for network/database

### RecyclerView

**Q: How does RecyclerView work?**

1. **LayoutManager**: Arranges items (Linear, Grid, Staggered)
2. **Adapter**: Binds data to ViewHolders
3. **ViewHolder**: Holds view references (avoids findViewById)
4. **ItemDecoration**: Dividers, spacing
5. **ItemAnimator**: Add/remove animations

**Q: What is the ViewHolder pattern?**

```kotlin
class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout (called once per view type)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind data (called on scroll)
        holder.textView.text = items[position]
    }
}
```

**Q: How do you optimize RecyclerView?**

- **ViewHolder pattern**: Cache view references
- **setHasFixedSize(true)**: If size doesn't change
- **DiffUtil**: Smart updates (only changed items)
- **Pagination**: Load data in chunks
- **RecycledViewPool**: Share ViewHolders across RecyclerViews

### LazyColumn (Compose Alternative)

```kotlin
LazyColumn {
    items(users) { user ->
        UserItem(user)
    }

    // Or with key for better recomposition
    items(users, key = { it.id }) { user ->
        UserItem(user)
    }
}
```

---

## 5. System Design Questions

### Design an Instagram-like feed

**Requirements:**
- Display posts (image + caption)
- Infinite scroll
- Like/comment
- Pull-to-refresh

**Architecture:**

```
┌─────────────────────────────────────┐
│         Presentation                │
│  FeedScreen → FeedViewModel         │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│           Domain                    │
│  GetFeedUseCase, LikePostUseCase    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│            Data                     │
│  FeedRepository → API + Database    │
└─────────────────────────────────────┘
```

**Key Decisions:**

1. **Pagination:**
   - Paging 3 library
   - Load 20 posts per page
   - Cache in Room database

2. **Images:**
   - Coil/Glide for loading
   - Cache on disk
   - Placeholder while loading

3. **Offline:**
   - Room database as source of truth
   - Sync when network available
   - WorkManager for background sync

4. **Real-time updates:**
   - WebSocket for new posts
   - Or polling every 30 seconds

**Code Sketch:**

```kotlin
@Pager(
    config = PagingConfig(pageSize = 20),
    pagingSourceFactory = { FeedPagingSource(api, database) }
)
val feedPager: Flow<PagingData<Post>>

@Composable
fun FeedScreen(viewModel: FeedViewModel) {
    val posts = viewModel.feedPager.collectAsLazyPagingItems()

    LazyColumn {
        items(posts) { post ->
            PostItem(post)
        }
    }
}
```

### Design offline-first note-taking app

**Requirements:**
- Create/edit/delete notes
- Works offline
- Sync when online
- Conflict resolution

**Architecture:**

```
Single Source of Truth: Room Database
    ↓
Repository watches database
    ↓
API syncs in background (WorkManager)
    ↓
Conflicts: Last-write-wins OR server timestamp
```

**Sync Strategy:**

```kotlin
class SyncWorker : CoroutineWorker() {
    override suspend fun doWork(): Result {
        // 1. Upload local changes
        val localChanges = database.getUnsynced()
        localChanges.forEach { note ->
            try {
                api.updateNote(note)
                database.markSynced(note.id)
            } catch (e: Exception) {
                // Retry later
            }
        }

        // 2. Download server changes
        val serverNotes = api.getNotesSince(lastSyncTime)
        database.insertAll(serverNotes)

        return Result.success()
    }
}
```

---

## 6. Behavioral Questions

### STAR Method

**Situation → Task → Action → Result**

Example:

**Q: Tell me about a time you optimized app performance.**

**S:** Our app had slow startup time (5+ seconds), users complained.

**T:** Reduce startup time to under 2 seconds.

**A:**
1. Profiled with Android Profiler, found heavy initialization on main thread
2. Moved network calls to background using coroutines
3. Lazy-loaded non-critical dependencies
4. Used baseline profiles for ART optimization

**R:** Startup time reduced to 1.2 seconds (76% improvement), Play Store rating increased from 3.8 to 4.3.

### Common Questions

**Q: How do you stay up-to-date with Android development?**

A:
- Official Android Blog & Now in Android newsletter
- Android Dev Summit & Google I/O
- Twitter: @AndroidDev, @JakeWharton, @chrisbanes
- Reddit: r/androiddev
- Blogs: ProAndroidDev, Medium

**Q: Describe a challenging bug you solved.**

A: (Use STAR method, focus on debugging process)

**Q: How do you handle code reviews?**

A:
- Constructive feedback, not criticism
- Ask questions: "Why did you choose this approach?"
- Suggest alternatives, don't demand changes
- Praise good code
- Focus on: correctness, performance, readability, security

**Q: What's your development process?**

A:
1. Understand requirements
2. Design architecture (Clean Architecture + MVI)
3. Write failing tests (TDD)
4. Implement feature
5. Refactor
6. Code review
7. Merge to main

---

## Key Takeaways

### What Makes a Strong Android Developer?

1. **Solid fundamentals**: Lifecycle, threading, memory management
2. **Modern tools**: Compose, Coroutines, Flow, Hilt
3. **Architecture knowledge**: Clean Architecture, MVVM/MVI
4. **Testing**: Unit tests, UI tests, integration tests
5. **Performance**: Memory leaks, rendering, battery
6. **Security**: Encryption, secure storage, HTTPS
7. **Soft skills**: Communication, code review, debugging

### Red Flags in Interviews

❌ "I don't write tests, QA handles that"
❌ "I use GlobalScope for all coroutines"
❌ "I store passwords in SharedPreferences"
❌ "I haven't learned Compose, XML is fine"
❌ "I don't know why it works, I copied from StackOverflow"

### Green Flags

✅ Explains trade-offs (e.g., Retrofit vs Ktor)
✅ Asks clarifying questions before jumping to code
✅ Discusses testing strategy
✅ Mentions recent Android changes (Compose, Material 3, etc.)
✅ Talks about performance/security proactively

---

## Resources

### Official Documentation
- [Android Developers](https://developer.android.com)
- [Kotlin Docs](https://kotlinlang.org/docs)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

### Courses
- [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course)
- [Advanced Android Development](https://developer.android.com/courses/advanced-training/overview)

### Books
- "Android Programming: The Big Nerd Ranch Guide"
- "Kotlin in Action" by Dmitry Jemerov
- "Clean Architecture" by Robert C. Martin

### YouTube Channels
- Android Developers
- Philipp Lackner
- Coding in Flow

### Practice
- LeetCode (Kotlin solutions)
- HackerRank
- Build side projects

---

**Good luck with your interviews! 🚀**
