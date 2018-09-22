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

class MainActivity : AppCompatActivity(), MainView.ViewListener {

    private lateinit var mainView: MainView
    private val REQUEST_IMAGE_GET = 1
    private var planeHit: HitResult? = null
    private var floorEdge: HitResult? = null
    private var state = State()

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(R.layout.activity_ux)
        mainView = findViewById(R.id.main_view)
        mainView.listener = this
    }

    fun suggestPlacementPoint(wallPoint:HitResult, floorEdgePoint:HitResult) {
        val plane = wallPoint.trackable
        val wallPose = wallPoint.hitPose
        Log.d("Suggested placement", "wall point translation ${wallPose.extractTranslation()}")
        Log.d("Suggested placement", "wall point rotation ${wallPose.extractRotation()}")
        val distToWall = wallPoint.distance
        Log.d("Suggested placement", "distance to wall $distToWall")
        val distToFloorEdge = floorEdgePoint.distance
        Log.d("Suggested placement", "distance to floor edge $distToFloorEdge")
        val distBetweenPoints : Float = kotlin.math.sqrt((distToFloorEdge.pow(2)) - (distToWall.pow(2)))
        Log.d("Suggested placement", "distance between two $distBetweenPoints")
        //make a float array to feed into makeTranslation
//        when {
//            distBetweenPoints.equals(state.perfectHeight) -> //placementNode = getNode(wallPoint)
//                placeImage(wallPoint,plane as Plane)
              mainView.placeImage(plane as Plane, wallPose, (state.perfectHeight - distBetweenPoints).toFloat())
//            distBetweenPoints < state.perfectHeight -> {
//                //translate WallPoint up along the plane by state.perfectHeight - distBetweenPoints
//                //plane.createAnchor()
//            }
//            else -> {
//                //translate WallPoint down along the plane by distBetweenPoints - state.perfectHeight
//                //plane.createAnchor()
//            }
//        }
        Log.d("Suggested placement", "Done")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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

    override fun onTapArPlane(hitResult: HitResult, plane: Plane) {
        if (state.mode == State.MODE.SELF_PLACE) {
            mainView.placeImage(hitResult, plane)
        } else if (state.mode == State.MODE.SUGGEST_PLACE) {
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

    override fun onPickImage() {
        Log.d("Picker", "Picked")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }
    }

    /*
    Tap one indicates plane / stores it as hitResult.getTrackable()
    Tap two stores HitResult of where wall meets floor
    System calculates where perfectHeight intersects with detected plane up from wall tap, places new anchor
    Picture appears centred on new anchor
    */
    override fun setMode(mode: State.MODE) {
        if (mode == State.MODE.SUGGEST_PLACE) {
            Toast.makeText(this, "Hold phone parallel to the wall and tap on the wall", Toast.LENGTH_SHORT).show()
            state.mode = State.MODE.SUGGEST_PLACE
        } else {
            state.mode = State.MODE.SELF_PLACE
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.getSimpleName()
        private val MIN_OPENGL_VERSION = 3.1

        /**
         * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
         * on this device.
         *
         * Sceneform requires OpenGL 3.1 capabilities.
         *
         * Finishes the activity if Sceneform can not run
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

//fun FloatArray.toQ() = Quaternion(this[0], this[1], this[2], this[3])
//fun FloatArray.toV() = Vector3(this[0], this[1], this[2])
//operator fun Quaternion.times(other: Quaternion): Quaternion = multiply(this, other)