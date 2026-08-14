package se.kjellstrand.fieldshootingtimer.ui

/**
 * Value↔pixel mappings for [MultiThumbSlider]. The two functions are exact
 * inverses, so the drawn thumb position and the drag gesture's notion of a
 * thumb's position always agree.
 *
 * The track is inset by [endInsetPx] (half an integer-segment) at each end,
 * matching where the per-second markers are drawn.
 */

/** Where a thumb with [value] sits, in px from the slider's left edge. */
internal fun sliderValueToOffsetPx(
    value: Float,
    range: IntRange,
    trackWidthPx: Float,
    endInsetPx: Float
): Float =
    ((value - range.first) / (range.last - range.first)) * trackWidthPx + endInsetPx

/** Exact inverse of [sliderValueToOffsetPx]. */
internal fun sliderOffsetToValue(
    offsetPx: Float,
    range: IntRange,
    trackWidthPx: Float,
    endInsetPx: Float
): Float =
    ((offsetPx - endInsetPx) / trackWidthPx) * (range.last - range.first) + range.first
