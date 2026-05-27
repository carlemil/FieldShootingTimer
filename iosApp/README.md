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

## What's NOT committed

- The Xcode project file (`iosApp.xcodeproj/project.pbxproj`). Its format
  is fragile to hand-author across Xcode versions, so we create it once
  on a Mac (instructions below).

## First-time setup on a Mac

1. Build the shared framework so it exists for Xcode to link:
   ```
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   # or for ad-hoc framework only:
   ./gradlew :shared:assembleSharedXCFramework
   ```
2. In Xcode, **File → New → Project → iOS → App**. Settings:
   - Product Name: `iosApp`
   - Bundle Identifier: `se.kjellstrand.fieldshootingtimer`
   - Interface: SwiftUI
   - Language: Swift
   - Deployment Target: iOS 16.0
3. Save the project into this `iosApp/` directory. Delete the auto-generated
   `iosAppApp.swift`, `ContentView.swift`, and `Info.plist` from Xcode
   and **drag the committed copies in their place**.
4. **Add the Kotlin framework**:
   - In project Build Phases, add a new **Run Script Build Phase** *before*
     the "Compile Sources" phase with:
     ```sh
     cd "$SRCROOT/.."
     ./gradlew :shared:embedAndSignAppleFrameworkForXcode
     ```
   - In Build Settings, set **Framework Search Paths** to
     `$(SRCROOT)/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`.
   - Add **`-framework Shared`** to **Other Linker Flags**.
5. Build & Run on the iOS 16+ simulator.

## Manual acceptance criteria

- App launches; the dial renders.
- Voice cues play with the phone unmuted; silent switch silences them.
- Haptics fire at thumb crossings on a real device (silent switch is ignored
  for haptics, by design).
- Screen does not auto-lock while the timer is running.
- Rotating the device switches between portrait and landscape layouts.

## Out of scope for v1

- App Store / TestFlight pipeline (no fastlane / Xcode Cloud yet).
- iOS-only CI workflow.
- Localized iOS app name beyond `CFBundleDisplayName` in Info.plist.
