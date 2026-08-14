package se.kjellstrand.fieldshootingtimer.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.abs

/**
 * Characterization tests pinning the CURRENT slider mappings, including the
 * render/drag mismatch, before the behavior-changing fix lands.
 */
class SliderGeometryTest {

    // Layout as computed by MultiThumbSlider for maxWidth=700, range 11..17:
    // segmentWidth = 700 / 7 = 100, endInset = 50, trackWidth = 600.
    private val range = 11..17
    private val fullWidth = 700f
    private val trackWidth = 600f
    private val endInset = 50f

    @Test
    fun `render mapping places range endpoints half a segment from the edges`() {
        assertEquals(50f, sliderValueToOffsetPx(11f, range, trackWidth, endInset))
        assertEquals(650f, sliderValueToOffsetPx(17f, range, trackWidth, endInset))
        assertEquals(350f, sliderValueToOffsetPx(14f, range, trackWidth, endInset))
    }

    @Test
    fun `drag mapping spans the full width`() {
        assertEquals(0f, sliderDragValueToOffsetPx(11f, range, fullWidth))
        assertEquals(700f, sliderDragValueToOffsetPx(17f, range, fullWidth))
        assertEquals(11f, sliderDragOffsetToValue(0f, range, fullWidth))
        assertEquals(17f, sliderDragOffsetToValue(700f, range, fullWidth))
    }

    @Test
    fun `drag pair is self-inverse`() {
        for (v in listOf(11f, 12.5f, 14f, 16.9f, 17f)) {
            val roundTrip =
                sliderDragOffsetToValue(sliderDragValueToOffsetPx(v, range, fullWidth), range, fullWidth)
            assertEquals(v, roundTrip, 1e-4f)
        }
    }

    @Test
    fun `render and drag mappings disagree - pinned mismatch`() {
        // For value 14 (center) render says 350px, drag says 350px — they only
        // agree at the exact center. At the endpoints they differ by the
        // endInset (50px), i.e. the drawn thumb and the drag origin diverge.
        val renderAt11 = sliderValueToOffsetPx(11f, range, trackWidth, endInset)
        val dragAt11 = sliderDragValueToOffsetPx(11f, range, fullWidth)
        assertTrue(
            abs(renderAt11 - dragAt11) == 50f,
            "expected the known 50px mismatch at the range start, got render=$renderAt11 drag=$dragAt11"
        )
    }
}
