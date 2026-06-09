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
  enabled; no background audio mode (intentional). `CFBundleVersion` is
  `$(CURRENT_PROJECT_VERSION)` so fastlane can stamp a unique build number
  on every archive (see "Releasing" below).
- `iosApp/iosApp/Assets.xcassets/` — AppIcon (regenerated from
  `AppIcon-1024.png` by `scripts/generate-app-icons.sh`), `LaunchLogo`
  image set, and `LaunchBackground` color set referenced by
  `UILaunchScreen` in Info.plist.
- `iosApp/iosApp/PrivacyInfo.xcprivacy` — declares no tracking, no data
  collection, plus conservative `NSPrivacyAccessedAPI` reasons for the
  file-timestamp / disk-space / UserDefaults / boot-time categories that
  the shared `datastore-core-okio` persistence layer may touch.
- `iosApp/Configuration/Signing.xcconfig.template` — copy to
  `Signing.xcconfig` (gitignored) with your real `DEVELOPMENT_TEAM` and
  `PROVISIONING_PROFILE_SPECIFIER`.
- `iosApp/fastlane/` — `Fastfile` lanes for TestFlight (`beta`),
  App Store (`release`), and metadata-only (`metadata`) uploads.
  `metadata/sv-SE/` holds Swedish-first ASC text under git;
  `screenshots/sv-SE/` is where 6.7" iPhone screenshots go.

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

## Releasing

The iOS release pipeline runs **locally from a Mac** (not from CI). CI keeps its
simulator-build-only role. One-time setup on a Mac with Xcode 16+:

```sh
brew install fastlane                                # one-time

cd iosApp
cp Configuration/Signing.xcconfig.template Configuration/Signing.xcconfig
$EDITOR Configuration/Signing.xcconfig               # set DEVELOPMENT_TEAM

cp fastlane/.env.template fastlane/.env
$EDITOR fastlane/.env                                # fill in ASC API key + Apple ID
```

The ASC API key (`ASC_KEY_ID`, `ASC_ISSUER_ID`, `.p8` path in `ASC_KEY_PATH`)
is generated at [appstoreconnect.apple.com/access/api](https://appstoreconnect.apple.com/access/api).
Apple only lets you download the `.p8` once — store it somewhere safe
(`~/.appstoreconnect/` with `chmod 700` is the convention used here).

You do **not** need to pre-create the Apple Distribution certificate or App
Store provisioning profile — the `beta`/`release` lanes call fastlane's
`get_certificates` and `get_provisioning_profile` actions on first run, which
create both via the ASC API key and install them in your login keychain.

Then:

```sh
fastlane beta        # archive + upload to TestFlight
fastlane release     # archive + upload to App Store (not submitted for review)
fastlane metadata    # push sv-SE ASC text + screenshots without a binary
```

`fastlane beta` stamps `CFBundleVersion` with `git rev-list --count HEAD` so
every archive has a unique build number. Marketing version (`CFBundleShortVersionString`)
is bumped manually in `iosApp/iosApp/Info.plist` for each release — keep it
aligned with the Android `appVersionName` in `app/build.gradle.kts`.

### Regenerating app icons

If you change `AppIcon-1024.png`, regenerate the full size set:

```sh
bash iosApp/scripts/generate-app-icons.sh
```

The script only requires the macOS-builtin `sips` — no extra tooling. It also
regenerates the `LaunchLogo` image set used by the launch screen.

## Out of scope (intentional)

- `fastlane match` for centralized cert/profile storage in a private git repo.
- Localizations of App Store metadata beyond Swedish (the app itself is Swedish-first).
- CI-driven TestFlight upload (feasible — `macos-15` runner + ASC API key as a
  GitHub secret — but not configured here).

CI does build and test iOS — see `.github/workflows/ci.yml` (the `ios` job runs
the shared unit tests on a simulator target and `xcodebuild`s the app).
