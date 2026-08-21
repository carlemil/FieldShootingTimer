import XCTest

// Drives the app to the states shown in the App Store screenshots and saves
// raw PNGs to /tmp/shots/ (simulator processes write straight to the host
// filesystem). Runs headless:
//   xcodebuild test -scheme iosApp -destination id=<udid> \
//     -only-testing:iosAppUITests/ScreenshotTests/<method>
// The landscape PNGs come out in the device's native portrait frame with the
// content rotated, so rotate them before they go in fastlane/screenshots/:
//   sips -r 270 /tmp/shots/0[456]_landscape_*.png
// Timings assume a fresh install: training mode, 5 s fire time, so the
// sequence is 0-7 TenSecondsLeft, 7-10 Ready, 10-15 Fire, 15-18 CeaseFire,
// 18-21 silent pause, 21-25 UnloadWeapon (training ends there).
final class ScreenshotTests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        try FileManager.default.createDirectory(
            atPath: "/tmp/shots", withIntermediateDirectories: true)
    }

    func testPortraitScreenshots() throws {
        let app = XCUIApplication()
        app.launch()
        sleep(3)
        XCUIDevice.shared.orientation = .portrait
        sleep(2)
        dismissTutorialIfPresent(app)
        save("01_portrait_idle")

        tapItem(app, "RadialMenuButton")
        sleep(2)
        save("02_menu")

        // Toggle dark mode (the menu stays open), close, shoot, restore.
        tapItem(app, "RadialMenuItemTheme")
        sleep(1)
        closeMenu(app)
        sleep(1)
        save("03_portrait_dark")
        tapItem(app, "RadialMenuButton")
        sleep(1)
        tapItem(app, "RadialMenuItemTheme")
        closeMenu(app)
        sleep(1)
    }

    func testLandscapeRunScreenshots() throws {
        let app = XCUIApplication()
        app.launch()
        sleep(3)
        dismissTutorialIfPresent(app)
        // Seed two partid flags so the landscape run shows them too.
        tapItem(app, "RadialMenuButton")
        sleep(1)
        tapItem(app, "RadialMenuItemAddTick")
        tapItem(app, "RadialMenuItemAddTick")
        closeMenu(app)
        sleep(1)
        XCUIDevice.shared.orientation = .landscapeLeft
        sleep(3)
        tapPlay(app)
        sleep(13); save("04_landscape_eld")        // ELD! (10-15)
        sleep(4);  save("05_landscape_eldupphor")  // ELD UPPHÖR! (15-18)
        sleep(5);  save("06_landscape_patronur")   // PATRON UR! (21-25)
    }

    private func dismissTutorialIfPresent(_ app: XCUIApplication) {
        let skip = app.descendants(matching: .any)["TutorialSkip"].firstMatch
        if skip.waitForExistence(timeout: 3) {
            skip.tap()
            sleep(1)
        }
    }

    private func tapItem(_ app: XCUIApplication, _ tag: String) {
        let item = app.descendants(matching: .any)[tag].firstMatch
        if item.waitForExistence(timeout: 3) {
            item.tap()
        }
    }

    private func closeMenu(_ app: XCUIApplication) {
        // Tap the scrim well below the fanned-out items.
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.9)).tap()
    }

    private func tapPlay(_ app: XCUIApplication) {
        let play = app.descendants(matching: .any)["PlayButton"].firstMatch
        if play.waitForExistence(timeout: 3) {
            play.tap()
        } else {
            // Fallback: the play button's rough spot in the landscape layout.
            app.coordinate(withNormalizedOffset: CGVector(dx: 0.135, dy: 0.44)).tap()
        }
    }

    private func save(_ name: String) {
        let png = XCUIScreen.main.screenshot().pngRepresentation
        try? png.write(to: URL(fileURLWithPath: "/tmp/shots/\(name).png"))
    }
}
