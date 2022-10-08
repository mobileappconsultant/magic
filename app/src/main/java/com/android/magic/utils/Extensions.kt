package com.android.magic.utils

import com.android.magic.mediapipe.PoseMarker
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark

fun List<NormalizedLandmark>?.extractData(): String {
    if (this == null) {
        return "No Live Feed Available"
    }
    val stringBuilder = StringBuilder()
    forEachIndexed { index, landmark ->
        stringBuilder.append(
            "${PoseMarker.fromOrdinal(index)} \n" +
                "Presence: ${landmark.presence.toPercent()}\n" +
                "x: ${landmark.x}\n" +
                "y: ${landmark.y}\n" +
                "z: ${landmark.z}\n"
        )
        stringBuilder.append("\n")
    }
    return stringBuilder.toString()
}

fun Float.toPercent() = "${times(100)}%"
