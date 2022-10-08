package com.android.magic.ui.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.android.magic.ui.theme.colorWhite
import com.google.mediapipe.formats.proto.LandmarkProto
import com.android.magic.mediapipe.Point
import com.android.magic.mediapipe.PoseMarker

enum class BodyPart {
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
    LEFT_EYE,
    RIGHT_EYE,
    NOSE,
    MOUTH
}
/** Draw the detected pose in preview.  */
@Composable
fun SkeletonOverlay(
    landmarks: List<LandmarkProto.NormalizedLandmark>,
    options: SkeletonOptions = SkeletonOptions(),
) {
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
        options.limbs.forEach { (_, joints) ->

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

            val radius = if (joints[0] != joints[1]) 4f else 12f

            val length: Double = Math.hypot((xStart - xEnd).toDouble(), (yStart - yEnd).toDouble())
            val startPoint = pointOnLine(Point(xEnd, yEnd), Point(xStart, yStart), length, length - radius)
            val endPoint = pointOnLine(Point(xStart, yStart), Point(xEnd, yEnd), length, length - radius)

            // For each of the joints we are using store the colours
            for (i in joints.indices) {
                val color = colorWhite
                drawJoint(
                    color = color,
                    center = Offset(
                        if (i == 0) xStart else xEnd,
                        if (i == 0) yStart else yEnd,
                    ),
                    radius,
                )
            }

            if (joints[0] != joints[1]) {
                drawLine(
                    color = colorWhite,
                    strokeWidth = options.STROKE_WIDTH,
                    start = Offset(startPoint.x, startPoint.y),
                    end = Offset(endPoint.x, endPoint.y),
                    cap = StrokeCap.Round
                )
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

    val limbs: Map<BodyPart, List<Int>> = mapOf(
        // / CENTER
        Pair(BodyPart.CENTER_SHOULDERS, listOf(PoseMarker.LEFT_SHOULDER.ordinal, PoseMarker.RIGHT_SHOULDER.ordinal)),
        Pair(BodyPart.CENTER_HIPS, listOf(PoseMarker.LEFT_HIP.ordinal, PoseMarker.RIGHT_HIP.ordinal)),

        // / LEFT
        Pair(BodyPart.LEFT_HUMERUS, listOf(PoseMarker.LEFT_SHOULDER.ordinal, PoseMarker.LEFT_ELBOW.ordinal)),
        Pair(BodyPart.LEFT_FOREARM, listOf(PoseMarker.LEFT_ELBOW.ordinal, PoseMarker.LEFT_WRIST.ordinal)),
        Pair(BodyPart.LEFT_SIDE, listOf(PoseMarker.LEFT_SHOULDER.ordinal, PoseMarker.LEFT_HIP.ordinal)),
        Pair(BodyPart.LEFT_FEMUR, listOf(PoseMarker.LEFT_HIP.ordinal, PoseMarker.LEFT_KNEE.ordinal)),
        Pair(BodyPart.LEFT_TIBIA, listOf(PoseMarker.LEFT_KNEE.ordinal, PoseMarker.LEFT_ANKLE.ordinal)),

        // / RIGHT
        Pair(BodyPart.RIGHT_HUMERUS, listOf(PoseMarker.RIGHT_SHOULDER.ordinal, PoseMarker.RIGHT_ELBOW.ordinal)),
        Pair(BodyPart.RIGHT_FOREARM, listOf(PoseMarker.RIGHT_ELBOW.ordinal, PoseMarker.RIGHT_WRIST.ordinal)),
        Pair(BodyPart.RIGHT_SIDE, listOf(PoseMarker.RIGHT_SHOULDER.ordinal, PoseMarker.RIGHT_HIP.ordinal)),
        Pair(BodyPart.RIGHT_FEMUR, listOf(PoseMarker.RIGHT_HIP.ordinal, PoseMarker.RIGHT_KNEE.ordinal)),
        Pair(BodyPart.RIGHT_TIBIA, listOf(PoseMarker.RIGHT_KNEE.ordinal, PoseMarker.RIGHT_ANKLE.ordinal)),
        Pair(BodyPart.LEFT_EYE, listOf(PoseMarker.LEFT_EYE.ordinal, PoseMarker.RIGHT_EYE.ordinal)),
        Pair(BodyPart.NOSE, listOf(PoseMarker.NOSE.ordinal, PoseMarker.NOSE.ordinal)),
        Pair(BodyPart.MOUTH, listOf(PoseMarker.LEFT_MOUTH.ordinal, PoseMarker.RIGHT_MOUTH.ordinal)),

    )
}
