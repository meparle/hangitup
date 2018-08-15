package com.littlemissadjective.hangitup

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.hardware.Camera
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Button
import android.widget.Toast

class CameraPreviewActivity : AppCompatActivity() {

    private val TAG = "CameraPreviewActivity"
    private val CAMERA_ID = 0

    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Open an instance of the first camera and retrieve its info.
        camera = getCameraInstance(CAMERA_ID)
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(CAMERA_ID, cameraInfo)

        if (camera == null) {
            // Camera is not available, display error message.
//            setContentView(R.layout.activity_camera_unavailable)
        } else {
            setContentView(R.layout.activity_camera_preview)

            // Get the rotation of the screen to adjust the preview image accordingly.
            val displayRotation = windowManager.defaultDisplay.rotation

            // Create the Preview view and set it as the content of this Activity.
            val cameraPreview = CameraPreview(this, null,
                    0, camera, cameraInfo, displayRotation)
            findViewById<FrameLayout>(R.id.camera_preview).addView(cameraPreview)
        }
        val button = findViewById<Button>(R.id.button_select)
        button.setOnClickListener {
            Toast.makeText(this@CameraPreviewActivity,"Hi",Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onPause() {
        super.onPause()
        // Stop camera access
        releaseCamera()
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private fun getCameraInstance(cameraId: Int): Camera? {
        var c: Camera? = null
        try {
            c = Camera.open(cameraId) // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "Camera $cameraId is not available: ${e.message}")
        }

        return c // returns null if camera is unavailable
    }

    private fun releaseCamera() {
        camera?.release()
        camera = null
    }

}
//TODO: remove deprecated Camera in favour of camera2