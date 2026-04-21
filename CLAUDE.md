# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

Android app (Kotlin, Jetpack Compose). Gradle wrapper is committed; use `./gradlew` (bash) — the PowerShell environment still invokes the bash wrapper via git-bash.

- Build debug APK: `./gradlew :app:assembleDebug`
- Build release AAB: `./gradlew :app:bundleRelease` (requires `keystore.properties` in project root; see `app/build.gradle.kts` signing config)
- Install on device: `./gradlew :app:installDebug`
- Unit tests: `./gradlew :app:testDebugUnitTest`
- Instrumented tests: `./gradlew :app:connectedDebugAndroidTest`
- Single unit test: `./gradlew :app:testDebugUnitTest --tests "se.kjellstrand.fieldshootingtimer.ExampleUnitTest.addition_isCorrect"`

Release signing reads from `keystore.properties` (gitignored). The keystore file `fst-release-key.jks` is in the project root.

The `/release` slash command (`.claude/commands/release.md`) describes a full Play Store publishing flow, but note it assumes a `val appVersionCode`/`val appVersionName` pattern and `bundleProdRelease`/`publishProdReleaseBundle` Gradle tasks that **do not currently exist** in `app/build.gradle.kts` — the build file uses `versionCode`/`versionName` directly inside `defaultConfig` and has no product flavors or Gradle Play Publisher plugin. Before running that flow, the prerequisites in Step 1 of the release doc must be applied.

## Architecture

Single-activity Compose app. One screen, no navigation graph.

**State flow (all state lives in `TimerViewModel`):**
`TimerUiState` is held in a single `MutableStateFlow`, exposed as derived flows (`currentTimeFlow`, `shootingDurationFlow`, `timerRunningStateFlow`, `thumbValuesFlow`). The `MainScreen` composable collects these and drives side effects.

**Timer loop:** `MainScreen.kt` runs a `LaunchedEffect(timerRunningState)` that, while `Running`, uses `withFrameMillis` to advance `currentTime` every frame. On each tick it delegates to `AudioManager.playAudioCue` (plays sounds whose cue time has passed and hasn't been played yet, tracked via `playedAudioIndices`), and a separate `LaunchedEffect` fires one-shot vibrations when `currentTime` crosses user-set `thumbValues`.

**The `Command` enum (`ui/Command.kt`) is the heart of the domain model.** Each entry bundles an audio resource, a string resource, a duration in seconds, and a color. The ordered `Command.entries` list with `duration >= 0` defines the timer's sequence: `TenSecondsLeft (7s) → Ready (3s) → Fire (configurable) → CeaseFire (3s) → UnloadWeapon (4s) → Visitation (2s)`. `Load`, `AllReady`, and `Mark` have `duration = -1` and are shown in the command list only; they don't drive the timer. To add or reorder a command, edit this enum — `segmentDurations`, `audioCues`, `range`, and the dial rendering all derive from it.

**Fire duration is the only user-configurable segment.** `shootingDuration` (default 5s) replaces `Command.Fire.duration` when building `segmentDurations` and `audioCues` in `MainScreen`. Everything else is fixed by the enum.

**Audio/vibration respect ringer mode.** `AudioManager.playSound` plays only in `RINGER_MODE_NORMAL`; `MainScreen` skips vibration in `RINGER_MODE_SILENT`. This is intentional (see commit `e201487`).

**Dial rendering (`ui/DecoratedDial.kt`, `Dial.kt`, `DialHand.kt`)** is custom Canvas drawing. Segments are laid out around a circle with a configurable `gapAngleDegrees` at the bottom. `thumbValues` (user-placed "ticks" on the slider) are rendered both as triangles on the ring and as numbered badges; their displayed time is offset by `TenSecondsLeft.duration + Ready.duration` so users see time relative to the start of the Fire phase. Per-second small ticks are drawn for every integer second *except* on segment boundaries (avoids visual clash with dividers).

**Portrait vs. landscape** are two sibling composables (`PortraitUI`, `LandscapeUI`) in `MainScreen.kt`, selected by `LocalConfiguration.current.orientation`. They share a `statelessSettingsComposable` so settings UI is identical in both.

## Localization

App is Swedish-first. Strings are in Swedish (`values/strings.xml`), audio cues are Swedish voice recordings in `res/raw/*.mp3`. There are no other locale folders — don't assume English strings exist.
