package com.littlemissadjective.hangitup

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import java.security.Permission

const val REQUEST_IMAGE_GET = 1
const val PERMISSION_REQUEST = 0

private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)

class MainActivity : AppCompatActivity() {

    private lateinit var layout: View

    // Set to true ensures requestInstall() triggers installation if necessary.

    private var mUserRequestedInstall = true

    /**
     * You must disclose the use of ARCore, and how it collects and processes data.
     * This can be done by displaying a prominent link to the site "How Google uses data when
     * you use our partners' sites or apps", (located at www.google.com/policies/privacy/partners/)
     */

    override fun onResume() {
        super.onResume()
        showCameraPreview()
        var mSession: Session? = null
        try {
            if (mSession == null) {
                when(ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED ->
                        mSession = Session(this)
                        // Success.
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED ->
                        // Ensures next invocation of requestInstall() will either return
                        // INSTALLED or throw an exception.
                        mUserRequestedInstall = false
                    else -> mUserRequestedInstall = false
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            // Display an appropriate message to the user and return gracefully.
            return
//        } catch (...) {  // current catch statements
//            ...
//            return;  // mSession is still null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        layout = findViewById(R.id.main_layout)

//        findViewById<Button>(R.id.button_open_camera).setOnClickListener { showCameraPreview() }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (grantResults.filter { result -> result == PackageManager.PERMISSION_GRANTED }.size
                == PERMISSIONS.size) {
            startCamera()
        }



//        if (requestCode == PERMISSION_REQUEST) {
//            // Request for camera permission.
//            if (grantResults.size == PERMISSIONS.size && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission has been granted. Start camera preview Activity.
////                layout.showSnackbar(R.string.camera_permission_granted, Snackbar.LENGTH_SHORT)
//                startCamera()
//            } else {
////                // Permission request was denied.
////                layout.showSnackbar(R.string.camera_permission_denied, Snackbar.LENGTH_SHORT)
//            }
//        }
    }
//
    private fun showCameraPreview() {
        for (i in 0..PERMISSIONS.size-1) {
            if (checkSelfPermissionCompat(PERMISSIONS[i]) !=
                PackageManager.PERMISSION_GRANTED) {
                // Permission is missing and must be requested.
                requestAllPermission()
            } else {
                //layout.showSnackbar(R.string.camera_permission_available, Snackbar.LENGTH_SHORT)
                startCamera()
            }
        }
    }
//
//    /**
//     * Requests the [android.Manifest.permission.CAMERA] permission.
//     * If an additional rationale should be displayed, the user has to launch the request from
//     * a SnackBar that includes additional information.
//     */
    private fun requestAllPermission() {
//        // Permission has not been granted and must be requested.
//        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.CAMERA)) {
//            // Provide an additional rationale to the user if the permission was not granted
//            // and the user would benefit from additional context for the use of the permission.
//            // Display a SnackBar with a button to request the missing permission.
//            layout.showSnackbar(R.string.camera_access_required,
//                    Snackbar.LENGTH_INDEFINITE, R.string.ok) {
//                requestPermissionsCompat(arrayOf(Manifest.permission.CAMERA),
//                        PERMISSION_REQUEST_CAMERA)
//            }
//
//        } else {
//            layout.showSnackbar(R.string.camera_permission_not_available, Snackbar.LENGTH_SHORT)
//
//            // Request the permission. The result will be received in onRequestPermissionResult().
        requestPermissions(PERMISSIONS, PERMISSION_REQUEST)
//        }
    }
//
    private fun startCamera() {
        val intent = Intent(this, CameraPreviewActivity::class.java)
        startActivity(intent)
    }

    fun AppCompatActivity.checkSelfPermissionCompat(permission: String) =
            ActivityCompat.checkSelfPermission(this, permission)

    fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

    fun AppCompatActivity.requestPermissionsCompat(permissionsArray: Array<String>,
                                                   requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
    }

}
//given photo within app (asset), place on scene
//TODO: write Gallery intent (remove read external storage permission?) ACTION_GET_CONTENT