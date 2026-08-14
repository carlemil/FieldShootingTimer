package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class MultiThumbSliderTest {

    @Test
    fun `renders one drag handle per thumb value`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                MultiThumbSlider(
                    thumbValues = listOf(12f, 14f, 16f),
                    onHorizontalDragSetThumbValues = {},
                    range = 11..17
                )
            }
        }
        onNodeWithTag("${SLIDER_THUMB_TAG}0").assertIsDisplayed()
        onNodeWithTag("${SLIDER_THUMB_TAG}1").assertIsDisplayed()
        onNodeWithTag("${SLIDER_THUMB_TAG}2").assertIsDisplayed()
    }

    @Test
    fun `disabled slider exposes no drag handles`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                MultiThumbSlider(
                    thumbValues = listOf(12f),
                    onHorizontalDragSetThumbValues = {},
                    range = 11..17,
                    enabled = false
                )
            }
        }
        onNodeWithTag("${SLIDER_THUMB_TAG}0").assertDoesNotExist()
    }
}
