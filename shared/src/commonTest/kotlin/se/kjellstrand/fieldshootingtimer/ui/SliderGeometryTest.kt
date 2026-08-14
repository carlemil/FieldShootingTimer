package se.kjellstrand.fieldshootingtimer.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class SliderGeometryTest {

    // Layout as computed by MultiThumbSlider for maxWidth=700, range 11..17:
    // segmentWidth = 700 / 7 = 100, endInset = 50, trackWidth = 600.
    private val range = 11..17
    private val trackWidth = 600f
    private val endInset = 50f

    @Test
    fun `value maps to px with range endpoints half a segment from the edges`() {
        assertEquals(50f, sliderValueToOffsetPx(11f, range, trackWidth, endInset))
        assertEquals(650f, sliderValueToOffsetPx(17f, range, trackWidth, endInset))
        assertEquals(350f, sliderValueToOffsetPx(14f, range, trackWidth, endInset))
    }

    @Test
    fun `offset maps back to value at the same landmarks`() {
        assertEquals(11f, sliderOffsetToValue(50f, range, trackWidth, endInset))
        assertEquals(17f, sliderOffsetToValue(650f, range, trackWidth, endInset))
        assertEquals(14f, sliderOffsetToValue(350f, range, trackWidth, endInset))
    }

    @Test
    fun `render and drag share one mapping - round trip is identity`() {
        for (v in listOf(11f, 11.3f, 12.5f, 14f, 16.9f, 17f)) {
            val roundTrip = sliderOffsetToValue(
                sliderValueToOffsetPx(v, range, trackWidth, endInset),
                range, trackWidth, endInset
            )
            assertEquals(v, roundTrip, 1e-4f, "round trip should be identity for $v")
        }
    }

    @Test
    fun `round trip is identity for other ranges and widths too`() {
        val cases = listOf(
            Triple(1..27, 940f, 18f),
            Triple(11..12, 300f, 75f),
            Triple(4..12, 512f, 28.4f)
        )
        for ((r, track, inset) in cases) {
            for (v in listOf(r.first.toFloat(), (r.first + r.last) / 2f, r.last.toFloat())) {
                val roundTrip = sliderOffsetToValue(
                    sliderValueToOffsetPx(v, r, track, inset), r, track, inset
                )
                assertEquals(v, roundTrip, 1e-3f, "round trip should be identity for $v in $r")
            }
        }
    }
}
