package com.android.magic.ui.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import com.android.magic.ui.theme.colorWhite
import com.android.magic.ui.theme.colorBlue
import com.android.magic.ui.theme.colorGray
import com.google.mediapipe.formats.proto.LandmarkProto
import com.android.magic.mediapipe.Point
import com.android.magic.mediapipe.PoseMarker

enum class LIMB {
    CENTER_SHOULDERS,
    CENTER_HIPS,
    LEFT_HUMERUS,
    LEFT_FOREARM,
    LEFT_SIDE,
    LEFT_FEMUR,
    LEFT_TIBIA,
    RIGHT_HUMERUS,
    RIGHT_FOREARM,
    RIGHT_SIDE,
    RIGHT_FEMUR,
    RIGHT_TIBIA,
}
/** Draw the detected pose in preview.  */
@Composable
fun SkeletonOverlay(
    landmarks: List<LandmarkProto.NormalizedLandmark>,
    options: SkeletonOptions = SkeletonOptions(),
) {

    val colorMap: MutableMap<LIMB, Array<Color>> = mutableMapOf()
    val stopMap: MutableMap<LIMB, FloatArray> = mutableMapOf()

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val jointColorMap = mutableMapOf<Int, Color>()

        fun drawJoint(color: Color, center: Offset, radius: Float) {
            this.drawCircle(
                color = color,
                radius = radius,
                style = Stroke(width = options.STROKE_WIDTH),
                center = center,
            )
        }

        // Go through each limb
        options.limbs.forEach { (limb, joints) ->

            // Store the start and end point of each limb
            val start = landmarks[joints[0]]
            val end = landmarks[joints[1]]

            // Get the width and height we are drawing the skeleton on
            val viewWidth = this.size.width
//            val viewWidth = Constants.HD_WIDTH
            val viewHeight = this.size.height
//            val viewHeight = Constants.HD_HEIGHT

            val xStart = start.x * viewWidth
            val xEnd = end.x * viewWidth

            val yStart = start.y * viewHeight
            val yEnd = end.y * viewHeight

            val startCircleRadius = 12f

            val endCircleRadius = 12f

            val length: Double = Math.hypot((xStart - xEnd).toDouble(), (yStart - yEnd).toDouble())
            val startPoint = pointOnLine(Point(xEnd, yEnd), Point(xStart, yStart), length, length - startCircleRadius)
            val endPoint = pointOnLine(Point(xStart, yStart), Point(xEnd, yEnd), length, length - endCircleRadius)

            val stops = stopMap[limb]
            val colors = colorMap[limb]

            val colorStops = colors?.zip(stops!!.toTypedArray()) { color, stop ->
                Pair(stop, color)
            }?.toTypedArray()

            // For each of the joints we are using store the colours
            for (i in joints.indices) {
                val color = if ((colorMap[limb]?.size ?: 0) > i) colorMap[limb]!![i] else colorWhite
                val storedColor = jointColorMap[joints[i]]
                val radius = if (i == 0) startCircleRadius else endCircleRadius

                if (storedColor != null) {
                    if (storedColor == colorWhite) {
                        jointColorMap[joints[i]] = color
                        drawJoint(
                            color = color,
                            center = Offset(
                                if (i == 0) xStart else xEnd,
                                if (i == 0) yStart else yEnd,
                            ),
                            radius,
                        )
                    } else if (storedColor == colorBlue && color == colorGray) {
                        jointColorMap[joints[i]] = color
                        drawJoint(
                            color = color,
                            center = Offset(
                                if (i == 0) xStart else xEnd,
                                if (i == 0) yStart else yEnd,
                            ),
                            radius,
                        )
                    }
                } else {
                    jointColorMap[joints[i]] = color
                    drawJoint(
                        color = color,
                        center = Offset(
                            if (i == 0) xStart else xEnd,
                            if (i == 0) yStart else yEnd,
                        ),
                        radius,
                    )
                }
            }

            if (colorStops.isNullOrEmpty() || colorStops.size <= 1) {
                drawLine(
                    color = colorWhite,
                    strokeWidth = options.STROKE_WIDTH,
                    start = Offset(startPoint.x, startPoint.y),
                    end = Offset(endPoint.x, endPoint.y),
                    cap = StrokeCap.Round
                )
            } else {
                val brush = Brush.linearGradient(
                    colorStops = colorStops,
                    start = Offset(startPoint.x, startPoint.y),
                    end = Offset(endPoint.x, endPoint.y),
                    tileMode = TileMode.Clamp
                )

                brush.let {
                    this.drawLine(
                        brush = it,
                        strokeWidth = options.STROKE_WIDTH,
                        start = Offset(startPoint.x, startPoint.y),
                        end = Offset(endPoint.x, endPoint.y),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

fun pointOnLine(start: Point, end: Point, length: Double, distance: Double): Point {
    val ratio: Double = distance / length
    val x: Double = ratio * end.x + (1.0 - ratio) * start.x
    val y: Double = ratio * end.y + (1.0 - ratio) * start.y
    return Point(x.toFloat(), y.toFloat())
}

// Options are created and stored prior only once
class SkeletonOptions {
    val STROKE_WIDTH = 5.0f

    val limbs: Map<LIMB, List<Int>> = mapOf(
        // / CENTER
        Pair(LIMB.CENTER_SHOULDERS, listOf(PoseMarker.LEFT_SHOULDER.ordinal, PoseMarker.RIGHT_SHOULDER.ordinal)),
        Pair(LIMB.CENTER_HIPS, listOf(PoseMarker.LEFT_HIP.ordinal, PoseMarker.RIGHT_HIP.ordinal)),

        // / LEFT
        Pair(LIMB.LEFT_HUMERUS, listOf(PoseMarker.LEFT_SHOULDER.ordinal, PoseMarker.LEFT_ELBOW.ordinal)),
        Pair(LIMB.LEFT_FOREARM, listOf(PoseMarker.LEFT_ELBOW.ordinal, PoseMarker.LEFT_WRIST.ordinal)),
        Pair(LIMB.LEFT_SIDE, listOf(PoseMarker.LEFT_SHOULDER.ordinal, PoseMarker.LEFT_HIP.ordinal)),
        Pair(LIMB.LEFT_FEMUR, listOf(PoseMarker.LEFT_HIP.ordinal, PoseMarker.LEFT_KNEE.ordinal)),
        Pair(LIMB.LEFT_TIBIA, listOf(PoseMarker.LEFT_KNEE.ordinal, PoseMarker.LEFT_ANKLE.ordinal)),

        // / RIGHT
        Pair(LIMB.RIGHT_HUMERUS, listOf(PoseMarker.RIGHT_SHOULDER.ordinal, PoseMarker.RIGHT_ELBOW.ordinal)),
        Pair(LIMB.RIGHT_FOREARM, listOf(PoseMarker.RIGHT_ELBOW.ordinal, PoseMarker.RIGHT_WRIST.ordinal)),
        Pair(LIMB.RIGHT_SIDE, listOf(PoseMarker.RIGHT_SHOULDER.ordinal, PoseMarker.RIGHT_HIP.ordinal)),
        Pair(LIMB.RIGHT_FEMUR, listOf(PoseMarker.RIGHT_HIP.ordinal, PoseMarker.RIGHT_KNEE.ordinal)),
        Pair(LIMB.RIGHT_TIBIA, listOf(PoseMarker.RIGHT_KNEE.ordinal, PoseMarker.RIGHT_ANKLE.ordinal)),
    )
}
