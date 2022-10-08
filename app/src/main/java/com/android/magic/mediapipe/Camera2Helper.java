package com.android.magic.mediapipe;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.mediapipe.components.CameraHelper;

import java.util.Collections;

import timber.log.Timber;

public class Camera2Helper extends CameraHelper {
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private final Context context;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private String cameraId;
    private Size imageDimension;
    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private SurfaceTexture outputSurface;
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Timber.e("onOpened");
            cameraDevice = camera;
            createCameraPreview();
            if (onCameraStartedListener != null) {
                onCameraStartedListener.onCameraStarted(outputSurface);
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            try {
                Timber.d(" Error on CameraDevice ");
                cameraDevice.close();
                cameraDevice = null;
            } catch (Exception e) {
                Timber.e("ERROR: " + e + " ER " + error);
                e.printStackTrace();
            }
        }
    };
    private CameraFacing cameraFacing;

    public Camera2Helper(Context context, SurfaceTexture surfaceTexture) {
        this.context = context;
        this.outputSurface = surfaceTexture;
    }

    @Override
    public void startCamera(Activity context, CameraFacing cameraFacing, @Nullable SurfaceTexture surfaceTexture) {
        this.cameraFacing = cameraFacing;
        closeCamera();
        startBackgroundThread();
        openCamera();

    }

    @Override
    public Size computeDisplaySizeFromViewSize(Size viewSize) {
        return new Size(viewSize.getWidth(), viewSize.getHeight());
    }

    @Override
    public boolean isCameraRotated() {
        return false;
    }

    public void closeCamera() {
        try {
            stopBackgroundThread();
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (Exception e) {
            Timber.d(e.toString());
        }

    }

    private void openCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraId == null) {
                cameraId = manager.getCameraIdList()[0];
            }
            if (cameraFacing == CameraFacing.BACK) {
                cameraId = manager.getCameraIdList()[0];
            } else {
                cameraId = manager.getCameraIdList()[1];
            }

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(manager.getCameraIdList()[0]);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                Timber.d("Permission issue");
                return;
            }

            Timber.d("Opening camera from manager %s", cameraId);

            manager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
            Timber.d(e.toString());
        }
    }

    protected void createCameraPreview() {
        try {
            Timber.d("Creating camera preview");
            outputSurface = (outputSurface == null) ? new CustomSurfaceTexture(0) : outputSurface;

            SurfaceTexture texture = outputSurface;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(context, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Timber.d(e.toString());
        }
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Timber.e("updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
