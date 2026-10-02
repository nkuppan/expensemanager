package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Bottom-bar shape with a smooth, rounded notch ("cradle") in the top edge for a docked FAB —
 * the Material 2 BottomAppBar cutout, which Material 3 doesn't ship. Because it's the Surface's
 * shape, both the fill and the shadow follow the curve.
 *
 * The notch is an arc around the FAB, joined to the top edge on each side by a small "shoulder"
 * arc tangent to both, so there are no sharp corners where the bar dips into the cradle.
 *
 * @param fabDiameter size of the FAB sitting in the notch (56.dp for a standard FAB).
 * @param margin gap between the FAB and the notch edge.
 * @param shoulderRadius how gently the top edge curves into the notch; larger = softer.
 */
class DockedFabCutoutShape(
    private val fabDiameter: Dp = 56.dp,
    private val margin: Dp = 6.dp,
    private val shoulderRadius: Dp = 16.dp,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = with(density) {
        val notchRadius = (fabDiameter / 2 + margin).toPx()
        val shoulder = shoulderRadius.toPx()
        val cx = size.width / 2f

        // Shoulder circles sit just below the top edge (centre y = shoulder) and touch the
        // notch circle from outside, so their centres are notchRadius + shoulder from (cx, 0).
        val dx = sqrt((notchRadius + shoulder).let { it * it } - shoulder * shoulder)

        // Angles in degrees, screen coordinates (y grows downwards, positive sweep = clockwise).
        val shoulderSweep = toDegrees(atan2(-shoulder, dx)) + 90f // from straight up to contact
        val notchStartLeft = toDegrees(atan2(shoulder, -dx)) // contact point, lower-left
        val notchEndRight = toDegrees(atan2(shoulder, dx)) // contact point, lower-right

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(cx - dx, 0f)
            // Left shoulder: curve from the flat edge down towards the notch.
            arcTo(
                rect = Rect(cx - dx - shoulder, 0f, cx - dx + shoulder, 2 * shoulder),
                startAngleDegrees = -90f,
                sweepAngleDegrees = shoulderSweep,
                forceMoveTo = false,
            )
            // Notch: around the bottom of the FAB, left contact to right contact.
            arcTo(
                rect = Rect(cx - notchRadius, -notchRadius, cx + notchRadius, notchRadius),
                startAngleDegrees = notchStartLeft,
                sweepAngleDegrees = notchEndRight - notchStartLeft,
                forceMoveTo = false,
            )
            // Right shoulder: mirror of the left, back up to the flat edge.
            arcTo(
                rect = Rect(cx + dx - shoulder, 0f, cx + dx + shoulder, 2 * shoulder),
                startAngleDegrees = -90f - shoulderSweep,
                sweepAngleDegrees = shoulderSweep,
                forceMoveTo = false,
            )
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        Outline.Generic(path)
    }

    private fun toDegrees(radians: Float): Float = Math.toDegrees(radians.toDouble()).toFloat()
}
