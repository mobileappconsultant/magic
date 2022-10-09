package com.android.magic.mediapipe

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import com.google.mediapipe.components.CameraHelper
import com.google.mediapipe.components.ExternalTextureConverter
import com.google.mediapipe.components.FrameProcessor
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList
import com.google.mediapipe.framework.AndroidAssetUtil
import com.google.mediapipe.framework.Packet
import com.google.mediapipe.framework.PacketGetter
import com.google.mediapipe.framework.ProtoUtil
import com.google.mediapipe.glutil.EglManager
import com.google.protobuf.InvalidProtocolBufferException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoseEstimation @Inject constructor() : MediaPipe {
    private val binaryGraph: String = "pose_tracking_gpu.binarypb"
    private val inputVideoStream: String = "input_video"
    private val outputVideoStream: String = "throttled_input_video"
    private val outputLandmarksStream: String = "pose_landmarks"
    private val cameraFacing: CameraHelper.CameraFacing = CameraHelper.CameraFacing.BACK
    private val shouldFlipFramesVertically: Boolean = true
    private val mediaPipeJni: String = "mediapipe_jni"
    private val eglManager: EglManager = EglManager(null)
    lateinit var onLandmarkCallback: (LandmarkProto.NormalizedLandmarkList) -> Unit

    private lateinit var previewDisplayView: SurfaceView
    private lateinit var processor: FrameProcessor
    private lateinit var cameraHelper: Camera2Helper
    private lateinit var converter: ExternalTextureConverter
    private var previewFrameTexture: SurfaceTexture? = null

    private var isGLContextSet = false

    companion object {
        private const val TAG = "MediaPipePoseDetect"
    }

    init {
        System.loadLibrary(mediaPipeJni)
        try {
            System.loadLibrary("opencv_java3")
        } catch (e: UnsatisfiedLinkError) {
            // Some example apps (e.g. template matching) require OpenCV 4.
            System.loadLibrary("opencv_java4")
        }
    }

    /**
     * "Add a callback function that will be called when new landmarks are added."
     *
     * The function takes a single parameter, which is a function that takes a single parameter, which
     * is a LandmarkProto.NormalizedLandmarkList
     *
     * @param onNewLandmarks A callback function that will be called when new landmarks are detected.
     */
    override fun addLandmarkCallback(onNewLandmarks: (LandmarkProto.NormalizedLandmarkList) -> Unit) {
        onLandmarkCallback = onNewLandmarks
    }

    /**
     * Initialize the camera and the processor
     *
     * @param viewGroup The view group that the camera preview will be added to.
     * @param context The context of the application.
     */
    override fun init(viewGroup: ViewGroup, context: Context) {
        cameraHelper = Camera2Helper(
            context,
            CustomSurfaceTexture(65),
        )

        previewDisplayView = SurfaceView(context)

        if (previewDisplayView.parent != null) {
            (previewDisplayView.parent as ViewGroup).removeView(previewDisplayView)
        }
        viewGroup.addView(previewDisplayView)

        previewDisplayView.visibility = View.INVISIBLE

        previewDisplayView.holder.addCallback(
            object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    processor.videoSurfaceOutput.setSurface(holder.surface)
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    onPreviewDisplaySurfaceChanged(holder, format, width, height)
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    processor.videoSurfaceOutput.setSurface(null)
                }
            })

        AndroidAssetUtil.initializeNativeAssetManager(context)
        initProcessor(context)

        onResume()
    }

    /**
     * TODO
     *
     * @param context Context
     */
    override fun initProcessor(context: Context) {
        processor = FrameProcessor(
            context,
            eglManager.nativeContext,
            binaryGraph,
            inputVideoStream,
            outputVideoStream,
        )

        ProtoUtil.registerTypeName(
            LandmarkProto.NormalizedLandmarkList::class.java,
            "mediapipe.NormalizedLandmarkList"
        )

        processor.videoSurfaceOutput.setFlipY(shouldFlipFramesVertically)

        processor.addPacketCallback(outputLandmarksStream) { packet: Packet ->
            try {
                val poseLandmarks = PacketGetter.getProto(
                    packet,
                    NormalizedLandmarkList::class.java
                )

                onLandmarkCallback(poseLandmarks)
            } catch (e: InvalidProtocolBufferException) {
                Log.e(TAG, "Couldn't Exception received - ${e.message}")
                return@addPacketCallback
            }
        }
    }

    override fun onResume() {
        converter = ExternalTextureConverter(eglManager.context, 2)
        converter.setFlipY(shouldFlipFramesVertically)
        converter.setConsumer(processor)
    }

    override fun onPause() {
        converter.close()
        // Hide preview display until we re-open the camera again.
        previewDisplayView.visibility = View.GONE
    }

    override fun onClose() {
        processor.close()
        converter.close()
        eglManager.release()
        (previewDisplayView.parent as ViewGroup).removeView(previewDisplayView)
        previewFrameTexture?.release()
        System.gc()
    }

    /**
     * It starts the camera and sets the preview frame texture
     *
     * @param context The context of the calling Activity.
     */
    override fun startCamera(context: Context) {
        cameraHelper.setOnCameraStartedListener { surfaceTexture: SurfaceTexture? ->
            previewFrameTexture =
                surfaceTexture ?: throw IllegalStateException("surfaceTexture is null")
            // Make the display view visible to start showing the preview. This triggers the
            // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
            previewDisplayView.visibility = View.VISIBLE
            Log.d(TAG, "Camera Started")
        }
        cameraHelper.startCamera(context as Activity, cameraFacing, null)
    }

    /**
     * When the surface view's size changes,
     * the converter is reconfigured to match the new size
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The pixel format of the camera-preview frames that will be fed to this
     * SurfaceView.
     * @param width The width of the SurfaceView.
     * @param height The height of the SurfaceView.
     */
    override fun onPreviewDisplaySurfaceChanged(
        holder: SurfaceHolder?,
        format: Int,
        width: Int,
        height: Int
    ) {

        val viewSize = computeViewSize(width, height)
        val displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize)
        val isCameraRotated = cameraHelper.isCameraRotated

        if (displaySize == null) return

        // Connect the converter to the camera-preview frames as its input (via
        // previewFrameTexture), and configure the output width and height as the computed
        // display size.
        previewFrameTexture?.let {
            if (isGLContextSet) {
                converter.setSurfaceTexture(
                    previewFrameTexture,
                    if (isCameraRotated) displaySize.height else displaySize.width,
                    if (isCameraRotated) displaySize.width else displaySize.height
                )
            } else {
                isGLContextSet = true

                converter.setSurfaceTextureAndAttachToGLContext(
                    previewFrameTexture,
                    if (isCameraRotated) displaySize.height else displaySize.width,
                    if (isCameraRotated) displaySize.width else displaySize.height
                )
            }
        }
    }

    override fun computeViewSize(width: Int, height: Int): Size {
        return Size(width, height)
    }
}
