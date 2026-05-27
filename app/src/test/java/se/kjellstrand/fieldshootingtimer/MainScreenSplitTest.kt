package se.kjellstrand.fieldshootingtimer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MainScreenSplitTest {

    private val sourceRoots: List<File> = listOf(
        File("src/main/java/se/kjellstrand/fieldshootingtimer"),
        File("app/src/main/java/se/kjellstrand/fieldshootingtimer"),
        File("src/commonMain/kotlin/se/kjellstrand/fieldshootingtimer"),
        File("shared/src/commonMain/kotlin/se/kjellstrand/fieldshootingtimer"),
        File("../shared/src/commonMain/kotlin/se/kjellstrand/fieldshootingtimer"),
    ).filter { it.isDirectory }
        .ifEmpty { error("no source root found. cwd=${System.getProperty("user.dir")}") }

    private val sourceRoot: File = sourceRoots.first()

    private val manifestFile: File = run {
        val candidates = listOf(
            File("src/main/AndroidManifest.xml"),
            File("app/src/main/AndroidManifest.xml")
        )
        candidates.firstOrNull { it.isFile }
            ?: error("AndroidManifest.xml not found. cwd=${System.getProperty("user.dir")}")
    }

    private fun findFile(relative: String): File? =
        sourceRoots.map { File(it, relative) }.firstOrNull { it.isFile }

    private fun read(relative: String): String =
        findFile(relative)?.readText() ?: error("file not found: $relative")

    // --- Fixed behavior: the extracted files exist with the expected symbols ---

    @Test
    fun `MainActivity_kt exists and declares MainActivity class`() {
        val file = File(sourceRoot, "MainActivity.kt")
        assertTrue("MainActivity.kt must exist", file.isFile)
        assertTrue(
            "MainActivity.kt must declare 'class MainActivity'",
            file.readText().contains("class MainActivity")
        )
    }

    @Test
    fun `PortraitLayout_kt exists and declares PortraitLayout composable`() {
        val file = findFile("ui/PortraitLayout.kt")
            ?: error("ui/PortraitLayout.kt must exist in some source root")
        assertTrue(
            "must declare 'fun PortraitLayout('",
            file.readText().contains("fun PortraitLayout(")
        )
    }

    @Test
    fun `LandscapeLayout_kt exists and declares LandscapeLayout composable`() {
        val file = findFile("ui/LandscapeLayout.kt")
            ?: error("ui/LandscapeLayout.kt must exist in some source root")
        assertTrue(
            "must declare 'fun LandscapeLayout('",
            file.readText().contains("fun LandscapeLayout(")
        )
    }

    @Test
    fun `SettingsPanel_kt exists and declares SettingsPanel composable`() {
        val file = findFile("ui/SettingsPanel.kt")
            ?: error("ui/SettingsPanel.kt must exist in some source root")
        assertTrue(
            "must declare 'fun SettingsPanel('",
            file.readText().contains("fun SettingsPanel(")
        )
    }

    @Test
    fun `MainScreen_kt no longer hosts the Activity or legacy composables`() {
        val text = read("MainScreen.kt")
        assertFalse(
            "MainScreen.kt should not declare a ComponentActivity subclass anymore",
            text.contains(": ComponentActivity")
        )
        assertFalse("fun PortraitUI should be moved out", text.contains("fun PortraitUI("))
        assertFalse("fun LandscapeUI should be moved out", text.contains("fun LandscapeUI("))
        assertFalse(
            "fun Settings( should be moved out (renamed to SettingsPanel)",
            text.contains("fun Settings(")
        )
    }

    @Test
    fun `AndroidManifest references MainActivity not MainScreen`() {
        val manifest = manifestFile.readText()
        assertTrue(
            "AndroidManifest.xml must reference .MainActivity",
            manifest.contains(".MainActivity")
        )
        assertFalse(
            "AndroidManifest.xml must not still reference .MainScreen",
            manifest.contains("\".MainScreen\"")
        )
    }

    // --- Guard tests: root composable still lives in MainScreen.kt ---

    @Test
    fun `MainScreen_kt still declares the root MainScreen composable`() {
        assertTrue(
            "fun MainScreen( must remain as the root wiring composable",
            read("MainScreen.kt").contains("fun MainScreen(")
        )
    }

    @Test
    fun `root MainScreen composable carries the Composable annotation`() {
        val text = read("MainScreen.kt")
        // The @Composable annotation should appear in the file (for the root MainScreen fun).
        assertTrue(
            "MainScreen.kt must still have a @Composable annotation",
            text.contains("@Composable")
        )
    }
}
