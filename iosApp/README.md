# iosApp — Field Shooting Timer iOS host

A minimal SwiftUI app that embeds the Compose Multiplatform UI exposed by
`:shared` as `Shared.xcframework`.

## What's committed

- `iosApp/iosApp/iosAppApp.swift` — `@main` SwiftUI app. Configures
  `AVAudioSession(.ambient)` once at launch so the silent switch silences
  audio (matching Android's `RINGER_MODE_SILENT` behavior).
- `iosApp/iosApp/ContentView.swift` — wraps `MainViewControllerKt.MainViewController()`
  from the shared framework in a `UIViewControllerRepresentable`.
- `iosApp/iosApp/Info.plist` — portrait + both landscape orientations
  enabled; no background audio mode (intentional).

- `iosApp/project.yml` — the [XcodeGen](https://github.com/yonaskolb/XcodeGen)
  spec. Running `xcodegen generate` produces `iosApp.xcodeproj` deterministically.

## What's NOT committed

- The Xcode project file (`iosApp.xcodeproj`). Its `project.pbxproj` is fragile
  to hand-author and noisy across Xcode versions, so it is **generated** from
  `project.yml` and gitignored. Regenerate it with `xcodegen generate`.

## First-time setup on a Mac

```sh
brew install xcodegen          # one-time
cd iosApp && xcodegen generate # produces iosApp.xcodeproj (gitignored)
open iosApp.xcodeproj           # then build for an iOS 16+ simulator
```

Building the app automatically (re)builds the shared framework — `project.yml`
adds a pre-build Run Script phase that calls
`./gradlew :shared:embedAndSignAppleFrameworkForXcode`. To build the framework
manually first (e.g. for a headless `xcodebuild`):

```sh
./gradlew :shared:embedAndSignAppleFrameworkForXcode
# or, for a standalone fat artifact (not used by the app build):
./gradlew :shared:assembleSharedXCFramework
```

## How the framework gets linked

`project.yml` wires the static `Shared` framework into the app without any
manual Xcode clicks:

- a **pre-build Run Script** phase runs
  `./gradlew :shared:embedAndSignAppleFrameworkForXcode` (it builds only the arch
  Xcode is currently targeting and writes it under
  `iosApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`);
- **`FRAMEWORK_SEARCH_PATHS`** points at that directory;
- **`OTHER_LDFLAGS`** adds `-framework Shared`. The framework is *static*, so it
  is linked, not embedded (no Copy Frameworks phase).

## Manual acceptance criteria

- App launches; the dial renders.
- Voice cues play with the phone unmuted; silent switch silences them.
- Haptics fire at thumb crossings on a real device (silent switch is ignored
  for haptics, by design).
- Screen does not auto-lock while the timer is running.
- Rotating the device switches between portrait and landscape layouts.

## Out of scope for v1

- App Store / TestFlight pipeline (no fastlane / Xcode Cloud yet).
- App icon asset catalog (a simulator build works without one).
- Localized iOS app name beyond `CFBundleDisplayName` in Info.plist.

CI does build and test iOS — see `.github/workflows/ci.yml` (the `ios` job runs
the shared unit tests on a simulator target and `xcodebuild`s the app).
