# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project layout

Three modules:

- **`:shared`** — Kotlin Multiplatform + Compose Multiplatform library.
  All UI, domain logic, and the `TimerViewModel` live in `commonMain`. Android-only
  integrations (SoundPool, Vibrator, WindowManager, dynamic Material You colors)
  live in `androidMain` as `actual` implementations. iOS counterparts
  (AVAudioPlayer, UIImpactFeedbackGenerator, `idleTimerDisabled`) live in `iosMain`.
  A `jvm()` target exists purely for host-side testing — `jvmMain` holds no-op
  actuals (and an in-memory `SettingsStore`); nothing ships from it.
- **`:app`** — Android-only entrypoint. `MainActivity` (≈20 lines) hosts the
  shared `MainScreen()`. Owns the release-signing config and the Gradle Play
  Publisher plugin. Compose UI deps are transitive via `:shared` (`api{}`-exposed).
- **`iosApp/`** — SwiftUI Xcode project. Wraps `MainViewControllerKt.MainViewController()`
  (from `Shared.framework`) in a `UIViewControllerRepresentable`. The Xcode project
  is driven by a committed XcodeGen spec (`iosApp/project.yml`); the generated
  `iosApp.xcodeproj` is gitignored. Run `xcodegen generate` in `iosApp/` to
  (re)create it — see `iosApp/README.md`.

## Build & Run

Android (Gradle wrapper; PowerShell calls bash wrapper via git-bash). `:app`
declares a single product flavor `prod` (dimension `env`), so app variant tasks
are flavor-qualified (`prodDebug` / `prodRelease`). `assembleDebug` /
`assembleRelease` / `bundleRelease` exist as aggregate anchors, but there is no
flavor-less `installDebug`, `testDebugUnitTest`, or `connectedDebugAndroidTest`.

- Debug APK: `./gradlew :app:assembleProdDebug`
- Release AAB: `./gradlew :app:bundleProdRelease` (needs `keystore.properties` in project root)
- Install on device: `./gradlew :app:installProdDebug`
- Host verification (what CI's android job runs; all work on Windows):
  `./gradlew :shared:testDebugUnitTest :shared:jvmTest :app:testProdDebugUnitTest :app:assembleProdDebug`
- Single shared unit test:
  `./gradlew :shared:testDebugUnitTest --tests "se.kjellstrand.fieldshootingtimer.domain.CommandTest"`
- Don't rely on `:shared:allTests` for coverage claims: on a non-mac host the
  three iOS targets are silently skipped (`kotlin.native.ignoreDisabledTargets`),
  so it reports green while running only the Android-unit and jvm slices.

### Testing strategy

- **`commonTest`** — logic tests (`kotlin.test` + coroutines-test). Compiles and
  runs on every target: Android unit (`:shared:testDebugUnitTest`), jvm
  (`:shared:jvmTest`), and the iOS slices on macOS. All pure functions and the
  `TimerViewModel` state machine are covered here — put new logic tests here.
- **`uiTest` source set** — Compose UI tests via `runComposeUiTest`, shared
  between `jvmTest` (headless skiko, runs on Windows/Linux) and `iosTest`.
  Select nodes by the `testTag` constants defined next to each composable
  (`PLAY_BUTTON_TAG`, `TICKS_PLUS_TAG`, `SLIDER_THUMB_TAG`…), not by text or
  contentDescription. Keep this source set free of JVM-only APIs — it also
  compiles for iOS.
- **`jvmTest`** — jvm-only extras, e.g. `MainScreenSmokeTest`, which forces
  portrait/landscape via the desktop-only `runDesktopComposeUiTest(width, height)`.
  `MainScreen(timerViewModel)` is an internal seam so tests can seed a ViewModel.
- **No instrumented tests** (`app/src/androidTest` was deliberately removed —
  nothing needs a device; don't add emulator-only tests).

iOS (macOS only):

- Generate the Xcode project from the committed spec:
  `brew install xcodegen` (once), then `cd iosApp && xcodegen generate`.
- Open `iosApp/iosApp.xcodeproj` and build for an iOS 16+ simulator. Building
  auto-runs `:shared:embedAndSignAppleFrameworkForXcode` as a pre-build phase
  (single-arch, fast), so no manual framework step is needed.
- Run the shared logic tests on an iOS slice: `./gradlew :shared:iosSimulatorArm64Test`.
- XCFramework artifact (standalone, not used by the app build):
  `./gradlew :shared:assembleSharedXCFramework`.

Release signing reads from `keystore.properties` (gitignored). The keystore file
`fst-release-key.jks` is in the project root.

**Android release shipping.** The version is set by the `appVersionCode` /
`appVersionName` vals at the top of `app/build.gradle.kts`. The Gradle Play
Publisher plugin (`play { }` block in
`app/build.gradle.kts`) uploads an AAB to the Play "internal" track, reading
credentials from `play-account.json` (gitignored). Build the bundle with
`./gradlew :app:bundleProdRelease`.

**iOS release shipping.** Local fastlane pipeline under `iosApp/fastlane/` —
`bundle exec fastlane beta` archives + uploads to TestFlight, `... release`
uploads to the App Store (not submitted for review), `... metadata` pushes
Swedish ASC text + screenshots only, and `... select_build` attaches an
already-uploaded, processed build to the current App Store version (API only,
no binary/metadata upload). Per-developer signing lives in
`iosApp/Configuration/Signing.xcconfig` (gitignored, template alongside) and
ASC API credentials in `iosApp/fastlane/.env` (gitignored, template alongside).
`CFBundleVersion` is `$(CURRENT_PROJECT_VERSION)`; fastlane overrides it via
`xcargs` to `git rev-list --count HEAD` so every archive has a unique build
number. Marketing version (`CFBundleShortVersionString`) is bumped manually in
`iosApp/iosApp/Info.plist` — keep it aligned with `appVersionName` on Android.
App icons are regenerated from the single 1024×1024 source in
`AppIcon.appiconset/` via `bash iosApp/scripts/generate-app-icons.sh`.

## CI

`.github/workflows/ci.yml` runs on push to `main` and on PRs, with two jobs:

- **android** (`ubuntu-latest`): `:app:assembleProdDebug`, `:app:testProdDebugUnitTest`,
  `:shared:testDebugUnitTest`, and `:shared:jvmTest` (headless Compose UI tests).
- **ios** (`macos-15`): `:shared:iosSimulatorArm64Test` (the shared logic tests
  on the arm64 simulator slice), then `xcodegen generate` and an `xcodebuild`
  simulator build of the `iosApp` scheme.

Both jobs use **JDK 17** — the Gradle wrapper (9.3.1) and AGP (9.1.1) require it
to *run*. The `JvmTarget.JVM_11` / `JavaVersion.VERSION_11` settings in the build
scripts only set the output bytecode level, not the JVM that runs Gradle, so do
not downgrade CI to JDK 11. The iOS job caches `~/.konan` (the Kotlin/Native
toolchain) and pins `macos-15` (not `macos-latest`) so the simulator arch matches
the `iosSimulatorArm64` slice.

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

**The `Command` enum (`domain/Command.kt`) is the heart of the domain model.**
Each entry bundles `audioPath: String?` (e.g. `"files/eld.mp3"`),
`stringRes: StringResource` (e.g. `Res.string.command_eld`), a `duration` in
seconds, and a `color`. The ordered `Command.entries` list with `duration >= 0`
defines the timer's sequence: `TenSecondsLeft (7s) → Ready (3s) → Fire
(configurable) → CeaseFire (3s) → UnloadWeapon (4s) → Visitation (2s)`.
`Load`, `AllReady`, and `Mark` have `duration = -1` and a `null` audioPath —
they're shown in the command list only. To add or reorder a command, edit
this enum; everything else derives from it via the pure functions in
`domain/TimerPlan.kt` (`buildSegmentDurations`, `buildAudioCues` — cue times
are the cumulative segment boundaries — `buildRange`, crossing predicates),
all covered by `commonTest/domain/TimerPlanTest`.

**Fire duration is the only user-configurable segment.** `shootingDuration`
(default 5s) replaces `Command.Fire.duration` when building `segmentDurations`
and `audioCues`. Everything else is fixed by the enum.

**Platform expects (`commonMain/.../platform/`):** Six abstraction points,
each with Android + iOS actuals (plus no-op jvm stubs for host tests).

| Expect | Android actual | iOS actual |
|---|---|---|
| `AudioPlayer` via `rememberAudioPlayer()` | SoundPool; loads via `Res.readBytes()` cached to `context.cacheDir` | `AVAudioPlayer` pool constructed from `NSData` |
| `Haptics` via `rememberHaptics()` | `VibrationEffect.createOneShot(300ms)` (O+) | `UIImpactFeedbackGenerator(.medium)` |
| `PlatformAudioPolicy` via `rememberPlatformAudioPolicy()` | reads `ringerMode` (NORMAL ⇒ play, !SILENT ⇒ vibrate) | always `true`/`true` (silent switch handled by `AVAudioSession(.ambient)` set in `iosAppApp.swift`) |
| `KeepScreenOn(enabled)` @Composable | toggles `Window.FLAG_KEEP_SCREEN_ON` via `DisposableEffect` | toggles `UIApplication.idleTimerDisabled` |
| `dynamicColorScheme(dark)` @Composable | `dynamic{Light,Dark}ColorScheme(ctx)` on Android 12+, else `null` | always `null` (falls back to static `Light/DarkColorScheme`) |
| `Sharer` via `rememberSharer()` | `ACTION_SEND` `text/plain` chooser (`FLAG_ACTIVITY_NEW_TASK`) | `UIActivityViewController` presented from the topmost VC |

The `RadialMenu` (`ui/RadialMenu.kt`) is overlaid by `MainScreen` in the
`BoxWithConstraints` — top-end in portrait, top-start in landscape (so it
clears the right-hand settings column), fanning its items toward the screen
interior (`openTowardsStart`). Its items animate out on an arc with a
slightly underdamped spring, composed beneath the menu button so they hide
under it at rest. Items: share (the GitHub Pages landing page, `SHARE_URL`
in `MainScreen.kt`) and the competition/training mode toggle. The open
state is hoisted to `MainScreen`, which puts a 30% black scrim between the
app and the open menu — it swallows all presses and closes the menu on tap.

**Persistence (`commonMain/.../persistence/`):** `SettingsStore` interface +
`DataStoreSettingsStore` (multiplatform `datastore-preferences-core` +
`datastore-core-okio`). Persists `shootingDuration`, `thumbValues`, and
`timerMode` across process restarts. Android stores under `context.filesDir/settings.preferences_pb`;
iOS under `NSDocumentDirectory/settings.preferences_pb`.

**Dial rendering (`ui/DecoratedDial.kt`, `Dial.kt`, `DialHand.kt`)** is pure
multiplatform Canvas drawing. Text on badges is rendered via Compose's
`TextMeasurer` + `DrawScope.rotate { drawText(...) }` (no platform-specific
Paint usage). All geometry (sweep angles, per-second tick placement, polar
conversion, ring radii) lives as pure functions in `ui/DialGeometry.kt`,
covered by `DialGeometryTest`/`DialOverlayGeometryTest` — the composables are
thin draw loops over precomputed values, so change the geometry functions,
not the Canvas lambdas. `thumbValues` (user-placed "ticks" on the slider)
render both as blocks on the ring and as numbered badges; their displayed
time is offset by `TenSecondsLeft.duration + Ready.duration` so users see
time relative to the start of the Fire phase. Per-second small ticks are
drawn for every integer second *except* on segment boundaries (avoids visual
clash with dividers). The slider's value↔pixel mapping is the inverse pair
in `ui/SliderGeometry.kt`.

**The dial is the only place times and ticks are adjusted** (there are no
sliders), via `ui/DialGestureOverlay.kt` — a transparent gesture surface
`ShootTimer` stacks over the dial. One finger on the ring near a user tick
drags that tick; two fingers on the Fire (green) segment pinch the shooting
duration (range `SHOOT_TIME_MIN`/`MAX`, defined next to the overlay). The
gesture is hand-rolled (`awaitEachGesture`) because the stock detectors eat
touch slop before reporting a start position, and because a second finger
must be able to convert a started tick drag into a pinch. The touch→value
math (angle from center, the inverse of `tickAngle`, ring-band and wedge hit
tests, arc-px grab tolerance) is pure functions in `ui/DialGeometry.kt`,
covered by `DialDragGeometryTest`; end-to-end gestures are covered in
`uiTest/.../DialTicksDragTest` and `DialPinchTest`. Ticks are added/removed
with the `TicksAdjuster` +/- buttons in the dial's lower-left corner
(hosted by `TimerWithPlayButton`). All gesture paths share the ViewModel
contract: live updates during the gesture (`setThumbValues` /
`setShootingTime`), `roundThumbValues` on tick-drag release, everything
disabled unless the timer is `NotStarted`.

**Competition vs training mode (`domain/TimerMode.kt`).** Training runs the
sequence immediately and hides the `Load`/`AllReady` rows from the command
list. Competition prefixes the run with a 60s preparation countdown,
**modeled as `currentTime` running from -60 to 0** (constants in
`domain/TimerPlan.kt`) — this reuses the whole timer loop untouched: every
cue time is ≥ 0 so nothing fires until the countdown ends, and
stop/resume/reset just work. Renderers clamp: the dial hand coerces to ≥ 0;
while time is negative the play button shows `ceil(-currentTime)` as
countdown digits instead of the stop icon (still tappable to stop). The command-list highlight
(`ui/CommandHighlight.kt`, `highlightedCommand(...)`) returns `Load` before
the start and through most of the countdown, `AllReady` for the final 10s,
then follows the running segment. Covered by `TimerViewModelCountdownTest`
and `CommandHighlightTest`.

**Portrait vs. landscape** are two sibling composables (`PortraitLayout`,
`LandscapeLayout` in `commonMain/.../ui`) selected by
`BoxWithConstraints { maxWidth > maxHeight }` in `MainScreen.kt`. They share
the `statelessSettingsComposable` so settings UI is identical in both.

## Resources

Live in `shared/src/commonMain/composeResources/`:

- `files/*.mp3` — 6 Swedish voice clips, one per audible Command.
- `values/strings.xml` — all UI strings (Swedish only), except `app_name`,
  which stays in `:app/src/main/res/values/strings.xml` because the Android
  launcher reads it from there.
- `drawable/play_arrow.xml`, `stop.xml`, `skip_previous.xml`, `share.xml`,
  `menu.xml`, `competition.xml`, `training.xml` — the PlayButton/RadialMenu
  icons (project-owned, not Material defaults).

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

CI relies on these same `gradle.properties` flags (no extra CLI flags are passed
in `ci.yml`), so the bypass must stay until that migration lands.

## Localization

Swedish-first. The app uses Swedish voice recordings in
`shared/src/commonMain/composeResources/files/*.mp3` and Swedish UI strings
in `shared/src/commonMain/composeResources/values/strings.xml`. No other
locales exist — don't assume English strings exist anywhere.
