import XCTest

// Drives the app to the states shown in the App Store screenshots and saves
// raw PNGs to /tmp/shots/ (simulator processes write straight to the host
// filesystem). Runs headless:
//   xcodebuild test -scheme iosApp -destination id=<udid> \
//     -only-testing:iosAppUITests/ScreenshotTests/<method>
// Timings assume the seeded settings: training mode, 5 s fire time, so the
// command sequence is 0-7 TenSecondsLeft, 7-10 Ready, 10-15 Fire,
// 15-18 CeaseFire, 18-22 Unload, 22-24 Visitation.
final class ScreenshotTests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        try FileManager.default.createDirectory(
            atPath: "/tmp/shots", withIntermediateDirectories: true)
    }

    func testLandscapeRunScreenshots() throws {
        let app = XCUIApplication()
        app.launch()
        sleep(3)
        XCUIDevice.shared.orientation = .landscapeLeft
        sleep(3)
        save("ip_03_landscape")
        tapPlay(app)
        sleep(13); save("ip_04_landscape")  // ELD!
        sleep(4);  save("ip_05_landscape")  // ELD UPPHÖR!
        sleep(4);  save("ip_06_landscape")  // PATRON UR! PROPPA VAPEN!
        sleep(2);  save("ip_07_landscape")  // VISITATION!
    }

    func testPortraitRunScreenshots() throws {
        let app = XCUIApplication()
        app.launch()
        sleep(3)
        XCUIDevice.shared.orientation = .portrait
        sleep(2)
        save("ipad_01_idle")
        let menu = app.descendants(matching: .any)["RadialMenuButton"].firstMatch
        if menu.waitForExistence(timeout: 3) {
            menu.tap()
            sleep(2)
            save("ipad_02_menu")
            app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.85)).tap()
            sleep(2)
        }
        tapPlay(app)
        sleep(13); save("ipad_03_eld")
        sleep(4);  save("ipad_04_eldupphor")
        sleep(4);  save("ipad_05_patronur")
        sleep(2);  save("ipad_06_visitation")
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
