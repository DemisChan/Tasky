# Project TODO List

Priority order within each section. Sources: code reviews of `featureAgenda`, `tasky-api.pdf`, and the old TODO.
Once Linear is set up, these become tickets and this file shrinks to a pointer.

## 1. Bugs & Review Findings (fix first)

- [ ] **`createEvent` skips the shared upload flow** (`DefaultAgendaRepository.kt`)
  - It uses the old inline upload: a failed upload becomes `UNKNOWN`, photos are matched by index instead of `photoKey`, and `confirmUpload` is never called.
  - Fix: call `uploadPhotosAndConfirm` the same way `updateEvent` does.
- [ ] **`updateEvent` hardcodes `isGoing = true` and `deletedPhotoKeys = emptyList()`**
  - An attendee editing an event is marked as going, and photos cannot be deleted.
  - Fix: add both as parameters of `AgendaRepository.updateEvent`.
- [ ] **Debug HTTP logging leaks secrets** (`CoreDataModule.kt`)
  - `redactHeader("Api-Key")` is the wrong name; the header is `x-api-key`. `Level.BODY` also logs passwords and tokens from `/auth/*`.
  - Fix: `redactHeader("x-api-key")`, and use `BASIC` or skip `/auth` bodies.
- [ ] **Add tests for the upload paths**: failed upload on create, failed upload on update, successful upload confirms the server's `uploadKey`s.
- [ ] **Register button enabled flag** (`RegisterScreen.kt:140`, `TODO(Add enabled flag to button)`): disable "GET STARTED" until the form is valid.

## 2. API Contract Mismatches (only matter against a real API)

- [ ] Timestamps: the doc uses ISO 8601 (`2024-01-15T14:00:00Z`); the code sends and parses epoch millis (`toEpochMillisString`, `toLocalDateTime`).
- [ ] DTOs require fields the doc's responses omit (`updatedAt` on task/reminder responses, `remindAt` on `GET /event/{id}`): make them nullable.
- [ ] Token refresh: implement `POST /auth/refresh` with an OkHttp `Authenticator`; the refresh token is stored but unused.
- [ ] Logout: `POST /auth/logout` needs `{ "refreshToken": ... }` in the body; `AuthApi.logout()` sends none. Wire it to the agenda screen's logout action.
- [ ] Handle `429 Too Many Requests` (limits: 50/min, 1000/hour) with a new `AgendaError` case.
- [ ] Last-write-wins: honour `updatedAt` conflict responses on `PUT`.

## 3. Features (Agenda epic)

- [ ] Agenda screen UI: day selector, list of events/tasks/reminders.
- [ ] Create/edit screens for event, task and reminder.
- [ ] Attendees: invite by email (`checkAttendeeExists`), remove self (`removeAttendee`).
- [ ] Photo picker + `PhotoCompressor` wired into create/update event (the `coil` and `accompanist.permissions` dependencies are already added).
- [ ] Offline-first: Room as source of truth, `syncAgenda` for deletions, WorkManager sync.
- [ ] Reminders/notifications at `remindAt`.

## 4. Technical Debt & Refactoring

- [ ] **Refactor Gradle scripts with convention plugins**
  - **Problem:** `core/data`, `features/auth`, `features/agenda` and `app` repeat `plugins`, `android {}` and shared dependencies. A single `buildConfigField` change already had to be made in three files and one missed copy broke the release build.
  - **Solution:** a `build-logic` module with convention plugins (e.g. `tasky.android.library`, `tasky.android.feature`), plus version-catalog bundles for the shared dependencies. Put the `API_KEY` / `BASE_URL` / `USE_FAKE_BACKEND` handling in the plugin.
  - **Do after** the bugs in section 1 and before adding new feature modules.
  - **Affected files:** `core/data`, `features/auth`, `features/agenda`, `app` build scripts.
- [ ] Move `UiText` out of `core:domain:util` (it imports `android.content.Context` and `androidx.annotation`).
- [ ] Remove unused dependencies after the convention-plugin work: `androidx.credentials` in `features/auth`, and catalog entries `androidx-security-crypto` and `androidx-credentials*`.
- [ ] Rename `TokenManager` / `DataStoreTokenStorage` / `providesSessionManager` consistently (it manages a session, not just a token); delete the unused `isAuthenticated()` Flow.
- [ ] Remove duplicates: `MainCoroutineRule` (app and auth tests), `EMAIL_REGEX` and fake latency constant in the two in-memory repositories.
- [ ] Replace wildcard imports in `DefaultAgendaRepository.kt` with explicit ones.
- [ ] Bump the Compose BOM (2024.09.00).
- [ ] Empty no-op actions: `LoginAction.SignUpClicked`, `RegisterAction.LoginClicked` (navigation is passed as callbacks; pick one mechanism).

## 5. Fake Backend Improvements (optional)

- [ ] `removeAttendee` should remove the current user from the event.
- [ ] Show photos: keep bytes in memory and expose a `data:` URI instead of `url = ""`.
- [ ] Use the logged-in user's id as the seeded event host instead of `"fake-host"`.
