package se.kjellstrand.fieldshootingtimer.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CallbackSignatureTest {

    private val sourceRoots: List<File> = listOf(
        File("src/main/java/se/kjellstrand/fieldshootingtimer"),
        File("app/src/main/java/se/kjellstrand/fieldshootingtimer"),
        File("src/commonMain/kotlin/se/kjellstrand/fieldshootingtimer"),
        File("shared/src/commonMain/kotlin/se/kjellstrand/fieldshootingtimer"),
        File("../shared/src/commonMain/kotlin/se/kjellstrand/fieldshootingtimer"),
    ).filter { it.isDirectory }
        .ifEmpty { error("no source root found. cwd=${System.getProperty("user.dir")}") }

    private fun read(relative: String): String =
        sourceRoots.map { File(it, relative) }.firstOrNull { it.exists() }?.readText()
            ?: error("file not found in any source root: $relative")

    private val stateLambdaRegex = Regex("""State<\([^)]*\) -> [^>]+>""")

    // --- Fixed behavior: callback parameters are plain lambdas, not State<lambda> ---

    @Test
    fun `MultiThumbSlider has no State-wrapped callback parameters`() {
        val offenders = stateLambdaRegex.findAll(read("ui/MultiThumbSlider.kt"))
            .map { it.value }.toList()
        assertTrue(
            "MultiThumbSlider still uses State-wrapped lambdas: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `TicksAdjuster has no State-wrapped callback parameters`() {
        val offenders = stateLambdaRegex.findAll(read("ui/TicksAdjuster.kt"))
            .map { it.value }.toList()
        assertTrue(
            "TicksAdjuster still uses State-wrapped lambdas: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `ShootTimeAdjuster has no State-wrapped callback parameters`() {
        val offenders = stateLambdaRegex.findAll(read("ui/ShootTimeAdjuster.kt"))
            .map { it.value }.toList()
        assertTrue(
            "ShootTimeAdjuster still uses State-wrapped lambdas: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `MainScreen has no rememberUpdatedState usages`() {
        val text = read("MainScreen.kt")
        val count = Regex("""\brememberUpdatedState\b""").findAll(text).count()
        assertTrue(
            "MainScreen still has $count rememberUpdatedState reference(s) — plain lambdas should suffice",
            count == 0
        )
    }

    // --- Guard tests: target composables still exist ---

    @Test
    fun `target composable functions still exist`() {
        assertTrue(
            "MultiThumbSlider fun missing",
            read("ui/MultiThumbSlider.kt").contains("fun MultiThumbSlider(")
        )
        assertTrue(
            "TicksAdjuster fun missing",
            read("ui/TicksAdjuster.kt").contains("fun TicksAdjuster(")
        )
        assertTrue(
            "ShootTimeAdjuster fun missing",
            read("ui/ShootTimeAdjuster.kt").contains("fun ShootTimeAdjuster(")
        )
    }
}
