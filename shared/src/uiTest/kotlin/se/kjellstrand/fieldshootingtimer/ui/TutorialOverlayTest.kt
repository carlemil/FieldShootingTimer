package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Drives [TutorialOverlay] directly with plain Compose state — no flows. */
@OptIn(ExperimentalTestApi::class)
class TutorialOverlayTest {

    private class TutorialState {
        var step by mutableStateOf<Int?>(0)
        var dismissed = 0
    }

    private fun androidx.compose.ui.test.ComposeUiTest.setOverlayContent(state: TutorialState) {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                state.step?.let { stepIndex ->
                    TutorialOverlay(
                        stepIndex = stepIndex,
                        onNext = {
                            if (stepIndex == tutorialSteps.lastIndex) {
                                state.step = null
                                state.dismissed++
                            } else {
                                state.step = stepIndex + 1
                            }
                        },
                        onSkip = {
                            state.step = null
                            state.dismissed++
                        }
                    )
                }
            }
        }
    }

    @Test
    fun `next walks through every step and then dismisses`() = runComposeUiTest {
        val state = TutorialState()
        setOverlayContent(state)
        repeat(tutorialSteps.size) {
            onNodeWithTag(TUTORIAL_OVERLAY_TAG).assertExists()
            onNodeWithTag(TUTORIAL_NEXT_TAG).performClick()
        }
        onNodeWithTag(TUTORIAL_OVERLAY_TAG).assertDoesNotExist()
        assertEquals(1, state.dismissed)
    }

    @Test
    fun `skip dismisses immediately from the first step`() = runComposeUiTest {
        val state = TutorialState()
        setOverlayContent(state)
        onNodeWithTag(TUTORIAL_SKIP_TAG).performClick()
        onNodeWithTag(TUTORIAL_OVERLAY_TAG).assertDoesNotExist()
        assertEquals(1, state.dismissed)
        assertTrue(state.step == null)
    }
}
