package se.kjellstrand.fieldshootingtimer.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PreviewNamingConventionTest {

    private val sourceRoot: File = run {
        val candidates = listOf(
            File("src/main/java/se/kjellstrand/fieldshootingtimer"),
            File("app/src/main/java/se/kjellstrand/fieldshootingtimer")
        )
        candidates.firstOrNull { it.isDirectory }
            ?: error("source root not found. cwd=${System.getProperty("user.dir")}")
    }

    private data class AnnotatedFn(val file: String, val name: String, val isPreview: Boolean)

    // Matches one or more annotations (each optionally with (...) args) followed by `fun Name`.
    // Group 1: the annotation block. Group 2: the function name.
    private val annotatedFunRegex = Regex("""((?:@\w+(?:\([^)]*\))?\s*)+)fun\s+(\w+)""")

    private fun scan(): List<AnnotatedFn> {
        val results = mutableListOf<AnnotatedFn>()
        sourceRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            annotatedFunRegex.findAll(file.readText()).forEach { match ->
                val annotations = match.groupValues[1]
                val name = match.groupValues[2]
                val hasComposable = annotations.contains("@Composable")
                val hasPreview = annotations.contains("@Preview")
                if (hasComposable || hasPreview) {
                    results += AnnotatedFn(file.name, name, hasPreview)
                }
            }
        }
        return results
    }

    // --- Fixed behavior (should FAIL before rename, PASS after) ---

    @Test
    fun `every Preview function name ends with Preview`() {
        val previews = scan().filter { it.isPreview }
        assertTrue("expected to find @Preview functions in the codebase", previews.isNotEmpty())

        val violators = previews.filterNot { it.name.endsWith("Preview") }
        assertTrue(
            "@Preview functions missing 'Preview' suffix: $violators",
            violators.isEmpty()
        )
    }

    @Test
    fun `every Preview function name starts with a sibling composable name`() {
        val all = scan()
        val previews = all.filter { it.isPreview }
        val composablesByFile = all.filterNot { it.isPreview }.groupBy { it.file }

        val violators = previews.filter { preview ->
            val siblings = composablesByFile[preview.file].orEmpty().map { it.name }
            // Allow previews whose name equals a sibling (legacy collision) to also be flagged;
            // a conforming preview is <Sibling>[...]Preview.
            siblings.none { sibling ->
                preview.name != sibling && preview.name.startsWith(sibling)
            }
        }
        assertTrue(
            "@Preview functions not starting with a sibling composable's name: $violators",
            violators.isEmpty()
        )
    }
}
