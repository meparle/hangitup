package com.littlemissadjective.hangitup

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.hardware.Camera
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Button
import android.widget.Toast
import android.provider.MediaStore



class CameraPreviewActivity : AppCompatActivity() {

    private val TAG = "CameraPreviewActivity"
    private val CAMERA_ID = 0
    private val REQUEST_IMAGE_GET = 1

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
    }

    public override fun onPause() {
        super.onPause()
        // Stop camera access

    }

/**    public override fun onResume() {
        super.onResume()
        // Restart camera access
        setContentView(R.layout.activity_camera_preview)
        val cameraPreview = CameraPreview(this, null,
                0, camera, Camera.CameraInfo())
        findViewById<FrameLayout>(R.id.camera_preview).addView(cameraPreview)
    }
*/
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

    fun pickImage(v: View) {
        Log.w("Picker","Picked")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
            val drawable = BitmapDrawable(this.resources, bitmap)
            // TODO: Add Sceneform, then convert Drawable to Renderable (sceneform).
            Log.w("Bitmap","Loaded")
        }
    }

}
//TODO: remove deprecated Camera in favour of camera2