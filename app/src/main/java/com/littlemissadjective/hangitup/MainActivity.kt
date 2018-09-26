package com.littlemissadjective.hangitup

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import java.io.File
import kotlin.math.pow

/**
 * Main application activity.
 *
 * <p>Displays a calibration image, a button to select an image from the phone's gallery, and a
 * button to toggle the placement mode. The two placement modes are self place, where the image is
 * placed by tapping on plane, and suggest place, where the image is automatically placed in a
 * recommended position.
 */
class MainActivity : AppCompatActivity(), IMainView.ViewListener {

    private var planeHit: HitResult? = null
    private var floorEdge: HitResult? = null
    private var state = State()
    /** Main app UI. Can be set by tests; otherwise instantiated in {@link #onCreate}. */
    lateinit var mainView: IMainView

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(R.layout.activity_ux)
        if (!this::mainView.isInitialized) {
            mainView = findViewById<MainView>(R.id.main_view)
            mainView.listener = this
        }
    }

    private fun suggestPlacementPoint(wallPoint: HitResult, floorEdgePoint: HitResult) {
        val plane = wallPoint.trackable
        val wallPose = wallPoint.hitPose
        Log.d("Suggested placement", "wall point translation ${wallPose.extractTranslation()}")
        Log.d("Suggested placement", "wall point rotation ${wallPose.extractRotation()}")
        val distToWall = wallPoint.distance
        Log.d("Suggested placement", "distance to wall $distToWall")
        val distToFloorEdge = floorEdgePoint.distance
        Log.d("Suggested placement", "distance to floor edge $distToFloorEdge")
        val distBetweenPoints: Float = kotlin.math.sqrt((distToFloorEdge.pow(2)) - (distToWall.pow(2)))
        Log.d("Suggested placement", "distance between two $distBetweenPoints")
        mainView.placeImage(plane as Plane, wallPose, (PERFECT_HEIGHT - distBetweenPoints).toFloat())
        Log.d("Suggested placement", "Done")
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            val savedFile = fileCache(data!!.data)
            mainView.setImage(BitmapFactory.decodeFile(savedFile.absolutePath))
            Log.w("Bitmap", "Loaded")
        }
    }

    private fun fileCache(image: Uri): File {
        val f3 = this.cacheDir.resolve("images")
        f3.mkdirs()
        val file = f3.resolve("imageTemp.png")
        try {
            this.contentResolver.openInputStream(image).use { ins ->
                file.outputStream().use { outs ->
                    ins.copyTo(outs)
                }
            }
            Log.d("Bitmap", "Saved")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    /**
     * Called when the user taps a point on the AR plane.
     *
     * <p>In {@code SELF_PLACE} mode, this tap places the image in that location.
     *
     * <p>In {@code SUGGEST_PLACE} mode, if this is the first tap, the hit result is recorded and no
     * other action is taken. If this is the second tap, the two locations are used to determine the
     * optimal image placement, which is suggested to the user.
     *
     * <p>The first tap is expected to be on the wall directly opposite the phone and the second tap
     * is expected to be on the intersection of wall and floor.
     *
     * @param hitResult represents the location of the tap event in the AR scene
     * @param plane the plane of the tap event
     */
    override fun onTapArPlane(hitResult: HitResult, plane: Plane) {
        if (state.mode == State.Mode.SELF_PLACE) {
            mainView.placeImage(hitResult, plane)
        } else if (state.mode == State.Mode.SUGGEST_PLACE) {
            if (planeHit == null) {
                planeHit = hitResult
                Toast.makeText(this, "Rotate phone and tap on where the floor meets the wall", Toast.LENGTH_SHORT).show()
            } else if (floorEdge == null) {
                floorEdge = hitResult
                suggestPlacementPoint(planeHit!!, floorEdge!!)
                planeHit = null
                floorEdge = null
            }
        }
    }

    /**
     * Called when the user clicks the image selection button. Launches an intent to choose an image
     * from the device.
     */
    override fun onPickImage() {
        Log.d("Picker", "Picked")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }
    }

    /**
     * Sets the app to one of two modes:
     *
     * <ul>
     *   <li>{@code SUGGEST_PLACE}: The app expects two taps on the AR scene, the first on the wall
     *       directly opposite the phone, and the second on the intersection of the wall and floor.
     *       From these the optimal picture placement is suggested.
     *   <li>{@code SELF_PLACE}: The user can tap to place the image directly on the wall.
     * </ul>
     */
    override fun setMode(mode: State.Mode) {
        if (mode == State.Mode.SUGGEST_PLACE) {
            Toast.makeText(this, "Hold phone parallel to the wall and tap on the wall", Toast.LENGTH_SHORT).show()
            state.mode = State.Mode.SUGGEST_PLACE
        } else {
            state.mode = State.Mode.SELF_PLACE
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val MIN_OPENGL_VERSION = 3.1
        const val REQUEST_IMAGE_GET = 1
        const val PERFECT_HEIGHT = 1.4478 // meters

        /**
         * Checks if the device can support Sceneform.
         *
         * Finishes the activity with an error message if the device does not support Sceneform;
         * otherwise returns true.
         */
        fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
            val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                    .deviceConfigurationInfo
                    .glEsVersion
            if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                Log.e(TAG, "Sceneform requires OpenGL ES 3.1 later")
                Toast.makeText(activity, "Sceneform requires OpenGL ES 3.1 or later", Toast.LENGTH_LONG)
                        .show()
                activity.finish()
                return false
            }
            return true
        }
    }
}