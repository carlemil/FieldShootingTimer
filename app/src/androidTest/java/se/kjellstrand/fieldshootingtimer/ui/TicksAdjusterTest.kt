package se.kjellstrand.fieldshootingtimer.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TicksAdjusterTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    /**
     * Hosts TicksAdjuster wired to a state-backed thumb list. The +/-
     * callbacks mutate the list using the same semantics the
     * TimerViewModel uses (addNewThumbValue caps at range capacity;
     * dropLastThumbValue removes the tail).
     */
    private fun render(
        range: IntRange = 4..18,
        enabled: Boolean = true,
        initial: List<Float> = emptyList()
    ): () -> List<Float> {
        var thumbs by mutableStateOf(initial)
        composeTestRule.setContent {
            Column {
                TicksAdjuster(
                    thumbValues = thumbs,
                    range = range,
                    enabled = enabled,
                    setThumbValuesMinusOne = {
                        thumbs = thumbs.dropLast(1)
                    },
                    setThumbValuesPlusOne = {
                        if (thumbs.size < (range.last - range.first)) {
                            // Mirror TimerViewModel.findNextFreeThumbSpot's
                            // start-from-center behavior loosely; exact value
                            // is not asserted here, only count and tail.
                            val center = (range.first + range.last) / 2
                            val candidate = (0..(range.last - range.first))
                                .firstOrNull { offset ->
                                    val v = (center + offset)
                                    v in range && thumbs.none { it.toInt() == v }
                                }
                                ?: center
                            thumbs = thumbs + candidate.toFloat()
                        }
                    },
                    onHorizontalDragSetThumbValues = { thumbs = it },
                    onHorizontalDragRoundThumbValues = {}
                )
            }
        }
        return { thumbs }
    }

    @Test
    fun ticksAdjuster_plusButton_addsThumb() {
        val read = render()

        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, read().size)
    }

    @Test
    fun ticksAdjuster_plusButton_doesNotExceedCapacity() {
        val range = 4..6 // capacity = range.last - range.first = 2
        val read = render(range = range)

        repeat(5) {
            composeTestRule.onNodeWithText("+").performClick()
            composeTestRule.waitForIdle()
        }

        assertEquals(2, read().size)
    }

    @Test
    fun ticksAdjuster_minusButton_removesLastThumb() {
        val read = render(initial = listOf(8f, 9f, 10f))

        composeTestRule.onNodeWithText("-").performClick()
        composeTestRule.waitForIdle()

        val current = read()
        assertEquals(2, current.size)
        assertEquals(listOf(8f, 9f), current)
    }

    @Test
    fun ticksAdjuster_minusButton_isNoopWhenEmpty() {
        val read = render()

        composeTestRule.onNodeWithText("-").performClick()
        composeTestRule.waitForIdle()

        assertEquals(emptyList<Float>(), read())
    }

    @Test
    fun ticksAdjuster_plusButton_disabled_doesNotAddThumb() {
        val read = render(enabled = false)

        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.waitForIdle()

        assertEquals(emptyList<Float>(), read())
    }
}
