# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project layout

Three modules:

- **`:shared`** — Kotlin Multiplatform + Compose Multiplatform library.
  All UI, domain logic, and the `TimerViewModel` live in `commonMain`. Android-only
  integrations (SoundPool, Vibrator, WindowManager, dynamic Material You colors)
  live in `androidMain` as `actual` implementations. iOS counterparts
  (AVAudioPlayer, UIImpactFeedbackGenerator, `idleTimerDisabled`) live in `iosMain`.
- **`:app`** — Android-only entrypoint. `MainActivity` (≈20 lines) hosts the
  shared `MainScreen()`. Owns the release-signing config and the Gradle Play
  Publisher plugin. Compose UI deps are transitive via `:shared` (`api{}`-exposed).
- **`iosApp/`** — SwiftUI Xcode project. Wraps `MainViewControllerKt.MainViewController()`
  (from `Shared.framework`) in a `UIViewControllerRepresentable`. The `project.pbxproj`
  is intentionally not committed; see `iosApp/README.md` for the one-time setup
  on a Mac.

## Build & Run

Android (Gradle wrapper; PowerShell calls bash wrapper via git-bash):

- Debug APK: `./gradlew :app:assembleDebug`
- Release AAB: `./gradlew :app:bundleRelease` (needs `keystore.properties` in project root)
- Install on device: `./gradlew :app:installDebug`
- Multiplatform unit tests (shared module, every target compilable on host):
  `./gradlew :shared:allTests`
- Android-side unit tests (incl. tests still in :app): `./gradlew :app:testDebugUnitTest`
- Android instrumented tests: `./gradlew :app:connectedDebugAndroidTest`
- Single unit test:
  `./gradlew :shared:testDebugUnitTest --tests "se.kjellstrand.fieldshootingtimer.ui.CommandTest"`

iOS (macOS only):

- Build the framework so Xcode can link it:
  `./gradlew :shared:embedAndSignAppleFrameworkForXcode`
- XCFramework artifact (release): `./gradlew :shared:assembleSharedXCFramework`
- Open `iosApp/iosApp.xcodeproj` (after first-time setup per `iosApp/README.md`)
  and build for an iOS 16+ simulator.

Release signing reads from `keystore.properties` (gitignored). The keystore file
`fst-release-key.jks` is in the project root.

The `/release` slash command (`.claude/commands/release.md`) currently only
ships Android (no iOS pipeline in v1). It still references the older
`appVersionCode`/`appVersionName` pattern from before the migration; double-check
before invoking.

## Architecture

Single-screen Compose app. One Activity, no navigation graph.

**State flow:** `TimerViewModel` (in `shared/commonMain/.../ui`) holds a
`MutableStateFlow<TimerUiState>` and exposes derived flows
(`currentTimeFlow`, `shootingDurationFlow`, `timerRunningStateFlow`,
`thumbValuesFlow`). `MainScreen` collects these and drives side effects.

**Timer loop:** runs inside `TimerViewModel.start()` via
`scope.launch { ... delay(tickMs) ... }`, anchored against
`kotlin.time.TimeSource.Monotonic` (wall-clock, multiplatform) so dropped
frames don't accumulate drift. Emits to `cueEventsFlow` (audio cues) and
`thumbCrossedFlow` (haptics) as the timer crosses each boundary. `MainScreen`
collects both flows and routes them to the platform `AudioPlayer` / `Haptics`.

**The `Command` enum (`ui/Command.kt`) is the heart of the domain model.**
Each entry bundles `audioPath: String?` (e.g. `"files/eld.mp3"`),
`stringRes: StringResource` (e.g. `Res.string.command_eld`), a `duration` in
seconds, and a `color`. The ordered `Command.entries` list with `duration >= 0`
defines the timer's sequence: `TenSecondsLeft (7s) → Ready (3s) → Fire
(configurable) → CeaseFire (3s) → UnloadWeapon (4s) → Visitation (2s)`.
`Load`, `AllReady`, and `Mark` have `duration = -1` and a `null` audioPath —
they're shown in the command list only. To add or reorder a command, edit
this enum; `segmentDurations`, `audioCues`, `range`, and the dial rendering
all derive from it.

**Fire duration is the only user-configurable segment.** `shootingDuration`
(default 5s) replaces `Command.Fire.duration` when building `segmentDurations`
and `audioCues` in the ViewModel. Everything else is fixed by the enum.

**Platform expects (`commonMain/.../platform/`):** Five abstraction points,
each with Android + iOS actuals.

| Expect | Android actual | iOS actual |
|---|---|---|
| `AudioPlayer` via `rememberAudioPlayer()` | SoundPool; loads via `Res.readBytes()` cached to `context.cacheDir` | `AVAudioPlayer` pool constructed from `NSData` |
| `Haptics` via `rememberHaptics()` | `VibrationEffect.createOneShot(300ms)` (O+) | `UIImpactFeedbackGenerator(.medium)` |
| `PlatformAudioPolicy` via `rememberPlatformAudioPolicy()` | reads `ringerMode` (NORMAL ⇒ play, !SILENT ⇒ vibrate) | always `true`/`true` (silent switch handled by `AVAudioSession(.ambient)` set in `iosAppApp.swift`) |
| `KeepScreenOn(enabled)` @Composable | toggles `Window.FLAG_KEEP_SCREEN_ON` via `DisposableEffect` | toggles `UIApplication.idleTimerDisabled` |
| `dynamicColorScheme(dark)` @Composable | `dynamic{Light,Dark}ColorScheme(ctx)` on Android 12+, else `null` | always `null` (falls back to static `Light/DarkColorScheme`) |

**Persistence (`commonMain/.../persistence/`):** `SettingsStore` interface +
`DataStoreSettingsStore` (multiplatform `datastore-preferences-core` +
`datastore-core-okio`). Persists `shootingDuration` and `thumbValues` across
process restarts. Android stores under `context.filesDir/settings.preferences_pb`;
iOS under `NSDocumentDirectory/settings.preferences_pb`.

**Dial rendering (`ui/DecoratedDial.kt`, `Dial.kt`, `DialHand.kt`)** is pure
multiplatform Canvas drawing. Text on badges is rendered via Compose's
`TextMeasurer` + `DrawScope.rotate { drawText(...) }` (no platform-specific
Paint usage). `thumbValues` (user-placed "ticks" on the slider) render both
as triangles on the ring and as numbered badges; their displayed time is
offset by `TenSecondsLeft.duration + Ready.duration` so users see time
relative to the start of the Fire phase. Per-second small ticks are drawn
for every integer second *except* on segment boundaries (avoids visual
clash with dividers).

**Portrait vs. landscape** are two sibling composables (`PortraitLayout`,
`LandscapeLayout` in `commonMain/.../ui`) selected by
`BoxWithConstraints { maxWidth > maxHeight }` in `MainScreen.kt`. They share
the `statelessSettingsComposable` so settings UI is identical in both.

## Resources

Live in `shared/src/commonMain/composeResources/`:

- `files/*.mp3` — 6 Swedish voice clips, one per audible Command.
- `values/strings.xml` — 10 of 11 strings (Swedish only). `app_name`
  stays in `:app/src/main/res/values/strings.xml` because the Android
  launcher reads it from there.
- `drawable/play_arrow.xml`, `stop.xml`, `skip_previous.xml` — the
  PlayButton icons (project-owned, not Material defaults).

Access via the generated `Res` object in package
`se.kjellstrand.fieldshootingtimer.resources` (configured in
`shared/build.gradle.kts`'s `compose.resources { packageOfResClass = ... }`).

## AGP 9 / Compose Multiplatform bypass

AGP 9.x's new built-in Kotlin support is incompatible with Compose
Multiplatform 1.9.x. As a temporary bypass, `gradle.properties` sets:

- `android.builtInKotlin=false`
- `android.newDsl=false`
- `kotlin.native.ignoreDisabledTargets=true`

Both AGP flags are slated for removal in AGP 10. When Compose MP adds AGP 9
support via the new `com.android.kotlin.multiplatform.library` plugin, remove
the bypass and switch `:shared` to the new plugin. `:app` explicitly applies
`kotlin-android` because that bypass disables AGP's auto-applied Kotlin
language plugin.

## Localization

Swedish-first. The app uses Swedish voice recordings in
`shared/src/commonMain/composeResources/files/*.mp3` and Swedish UI strings
in `shared/src/commonMain/composeResources/values/strings.xml`. No other
locales exist — don't assume English strings exist anywhere.
