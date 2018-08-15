package com.littlemissadjective.hangitup

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

class CameraPreview @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        private val camera: Camera? = null,
        private val cameraInfo: Camera.CameraInfo? = null,
        private val displayOrientation: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    init {

        // Do not initialise if no camera has been set
        if (camera != null && cameraInfo != null) {
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            holder.addCallback(this@CameraPreview)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            camera?.run {
                setPreviewDisplay(holder)
                startPreview()
            }
            Log.d(TAG, "Camera preview started.")
        } catch (e: IOException) {
            Log.d(TAG, "Error setting camera preview: ${e.message}")
        }

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (holder.surface == null) {
            // preview surface does not exist
            Log.d(TAG, "Preview surface does not exist")
            return
        }
        Log.d(TAG, "Preview stopped.")

        fun Camera.CameraInfo.calculatePreviewOrientation(rotation: Int): Int {
            val degrees = when (rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }

            return if (facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                // compensate the mirror
                360 - ((orientation + degrees) % 360) % 360
            } else {
                // back-facing
                (orientation - degrees + 360) % 360
            }
        }

        camera?.run {
            // stop preview before making changes
            stopPreview()
            cameraInfo?.let {
                setDisplayOrientation(it.calculatePreviewOrientation(displayOrientation))
            }
            setPreviewDisplay(holder)
            startPreview()
            Log.d(TAG, "Camera preview started.")
        }
    }

    companion object {
        private val TAG = "CameraPreview"
    }
}