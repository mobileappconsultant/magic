package com.android.magic.mediapipe

import android.content.Context
import android.util.Size
import android.view.SurfaceHolder
import android.view.ViewGroup
import com.google.mediapipe.formats.proto.LandmarkProto

/**
 * API to control Instance of MediaPipe
 */
interface MediaPipe {
    fun addLandmarkCallback(onNewLandmarks: (LandmarkProto.NormalizedLandmarkList) -> Unit)

    fun init(viewGroup: ViewGroup, context: Context)

    fun initProcessor(context: Context)

    fun startCamera(context: Context)

    fun onResume()

    fun onPause()

    fun onClose()

    fun onPreviewDisplaySurfaceChanged(
        holder: SurfaceHolder?,
        format: Int,
        width: Int,
        height: Int
    )

    fun computeViewSize(width: Int, height: Int): Size
}
