package se.kjellstrand.fieldshootingtimer.ui

/**
 * Value↔pixel mappings for [MultiThumbSlider].
 *
 * NOTE: the render mapping ([sliderValueToOffsetPx]) and the drag pair
 * ([sliderDragValueToOffsetPx]/[sliderDragOffsetToValue]) are NOT inverses of
 * each other — render insets the track by half a segment at each end, drag
 * normalizes over the full width. This mismatch is pinned by
 * SliderGeometryTest and fixed in the follow-up commit.
 */

/** Where a thumb with [value] is drawn, in px from the slider's left edge. */
internal fun sliderValueToOffsetPx(
    value: Float,
    range: IntRange,
    trackWidthPx: Float,
    endInsetPx: Float
): Float =
    ((value - range.first) / (range.last - range.first)) * trackWidthPx + endInsetPx

/** The drag gesture's notion of a thumb's current pixel offset. */
internal fun sliderDragValueToOffsetPx(
    value: Float,
    range: IntRange,
    fullWidthPx: Float
): Float =
    ((value - range.first) / (range.last - range.first)) * fullWidthPx

/** The drag gesture's mapping from a pixel offset back to a value. */
internal fun sliderDragOffsetToValue(
    offsetPx: Float,
    range: IntRange,
    fullWidthPx: Float
): Float =
    (offsetPx / fullWidthPx) * (range.last - range.first) + range.first
