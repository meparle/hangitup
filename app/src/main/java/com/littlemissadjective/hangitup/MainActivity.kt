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
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import android.widget.ToggleButton
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion.axisAngle
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.io.File
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    enum class MODE {
        SELF_PLACE, SUGGEST_PLACE
    }

    private lateinit var arFragment: ArFragment
    private lateinit var imageView: ImageView
    private lateinit var loadedRenderable: ViewRenderable
    private lateinit var placementNode: TransformableNode
    private var currentMode = MODE.SELF_PLACE
    private val REQUEST_IMAGE_GET = 1
    private var planeHit: HitResult? = null
    private var floorEdge: HitResult? = null

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(R.layout.activity_ux)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment

        ViewRenderable.builder()
                .setView(this, R.layout.ar_test)
                .build()
                .thenAccept { renderable ->
                    Log.w("Load File", "loaded saved renderable")
                    imageView = renderable.view.findViewById<ImageView>(R.id.ux_fragment)
                    loadedRenderable = renderable
                }
                .exceptionally {
                    Log.w("Load File", "Unable to load saved renderable", it)
                    null
                }

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->

            if (currentMode == MODE.SELF_PLACE) {
                placeImage(hitResult, plane)
            } else if (currentMode == MODE.SUGGEST_PLACE) {
                if (planeHit == null) {
                    planeHit = hitResult
                    Toast.makeText(MainActivity@ this, "Rotate phone and tap on where the floor meets the wall", Toast.LENGTH_SHORT).show()
                } else if (floorEdge == null) {
                    floorEdge = hitResult
                    suggestPlacementPoint(planeHit!!, floorEdge!!)
                    planeHit = null
                    floorEdge = null
                }

            }
        }
    }

    fun suggestPlacementPoint(wallPoint:HitResult, floorEdgePoint:HitResult) {
        val plane = wallPoint.trackable
        val distToWall = wallPoint.distance
        Log.d("Suggested placement", "distance to wall $distToWall")
        val distToFloorEdge = floorEdgePoint.distance
        Log.d("Suggested placement", "distance to floor edge $distToFloorEdge")
        val distBetweenPoints : Float = kotlin.math.sqrt((distToFloorEdge.pow(2)) - (distToWall.pow(2)))
        Log.d("Suggested placement", "distance between two $distBetweenPoints")
//        when {
//            distBetweenPoints.equals(1.4478) -> //placementNode = getNode(wallPoint)
                placeImage(wallPoint,plane as Plane)
//
//            distBetweenPoints < 1.4478 -> {
//                //translate WallPoint up along the plane by 1.4478 - distBetweenPoints
//                //plane.createAnchor()
//            }
//            else -> {
//                //translate WallPoint down along the plane by distBetweenPoints - 1.4478
//                //plane.createAnchor()
//            }
//        }
        Log.d("Suggested placement", "Done")
        //TODO: finish maths
    }

    fun placeImage(hitResult: HitResult, plane: Plane) {
        val node = getNode(hitResult)
        node.renderable = loadedRenderable
        when (plane.type) {
            Plane.Type.HORIZONTAL_DOWNWARD_FACING -> {
            }
            Plane.Type.HORIZONTAL_UPWARD_FACING -> {
                node.localRotation = axisAngle(Vector3.right(), -90.0f)
            }
            Plane.Type.VERTICAL -> {
                //val planeNormal = plane.centerPose.yAxis.toV();
                //val upQuat = Quaternion.lookRotation(planeNormal, Vector3.up()).inverted()
                node.setLookDirection(node.down,node.back)
                //node.scaleController.
            }
        }
        Log.d("Look direction", "Changed to up")
        //node.localScale = node.localScale.scaled(0.1f) //appears to do nothing!
        node.select()
    }

    /*
    Takes a hitResult, makes an AnchorNode out of it
    gives a TransformableNode which can be rotated that is a child of AnchorNode
     */
    private fun getNode(hitResult: HitResult): TransformableNode {
        val scene = arFragment.arSceneView.scene
        scene.children.filterIsInstance<AnchorNode>().forEach(scene::removeChild)

        val anchorNode = AnchorNode(hitResult.createAnchor())
        scene.addChild(anchorNode)
        Log.d("Anchor", "Parent set")

        val point = TransformableNode(arFragment.transformationSystem)
        anchorNode.addChild(point)

        return point
    }

    fun pickImage(v: View) {
        Log.d("Picker", "Picked")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            val savedFile = fileCache(data!!.data)
            imageView.setImageBitmap(BitmapFactory.decodeFile(savedFile.absolutePath))
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

    fun measureUp(v: View) {
        val toggle = v as ToggleButton
        if (toggle.isChecked) {
            Log.d("Measure", "Started")
            Toast.makeText(MainActivity@ this, "Hold phone parallel to the wall and tap on the wall", Toast.LENGTH_SHORT).show()
            currentMode = MODE.SUGGEST_PLACE
            //tell user to tap the vertical plane they want to place the item on (collision), then tap where floor hits wall
            //place anchor / image node 57 inches above where tapped on plane

            //tap one indicates plane / stores it as val using hitTest(float,float) or hitTest(MotionEvent) .getTrackable()
            //tap two places node (or anchor?), system calculates where 57 inches up intersects with detected plane, places new anchor
            //detaches old anchor and places picture centred on new anchor
        }
        else {
            Log.d("Place", "Started")
            currentMode = MODE.SELF_PLACE
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
//TODO: maths to centre image 57 inches (1.4478) above
//TODO: model-view-presenter
//TODO: tests
//fun FloatArray.toQ() = Quaternion(this[0], this[1], this[2], this[3])
//fun FloatArray.toV() = Vector3(this[0], this[1], this[2])
//operator fun Quaternion.times(other: Quaternion): Quaternion = multiply(this, other)