package com.littlemissadjective.hangitup

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
class MainActivity : AppCompatActivity() {

    private var arFragment: ArFragment? = null
    private var andyRenderable: ViewRenderable? = null
    private var loadedRenderable: ViewRenderable? = null
    private var isPlaced = false
    private val REQUEST_IMAGE_GET = 1

    override// CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(R.layout.activity_ux)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
//        ModelRenderable.builder()
//                .setSource(this, R.raw.redpanda)
//                .build()
//                .thenAccept { renderable -> andyRenderable = renderable }
//                .exceptionally { throwable ->
//                    val toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
//                    toast.setGravity(Gravity.CENTER, 0, 0)
//                    toast.show()
//                    null
//                }



        ViewRenderable.builder()
                .setView(this, R.layout.ar_test)
                //.setSizer {  }
                .build()
                .thenAccept { renderable -> andyRenderable = renderable }
                .exceptionally {
                    val toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    null
                }


        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->

            //            if (!isPlaced) {
            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
//                val node : BaseTransformableNode? = null
            anchorNode.setParent(arFragment!!.arSceneView.scene)
//                node!!.setParent(anchorNode)
            Log.w("Anchor","Parent set")

            // Create the transformable andy and add it to the anchor.
            val point = TransformableNode(arFragment!!.transformationSystem)
            point.renderable = andyRenderable

//                if (plane.type == Plane.Type.VERTICAL) {
            val yAxis = plane.getCenterPose().getYAxis();
            val planeNormal = Vector3(yAxis[0], yAxis[1], yAxis[2]);
            val upQuat = Quaternion.lookRotation(planeNormal,Vector3.up()).inverted()
            point.setWorldRotation(upQuat)
//                    var anchorUp = anchorNode.left
//                    point.setLookDirection(anchorUp)
            Log.w("Look direction","Changed to up")
//                }
//                var copyRenderable : ViewRenderable = andyRenderable!!.makeCopy()
//                copyRenderable.setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
//                var rotation1 : Quaternion = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 90.0f)
//                node.setWorldRotation(rotation1)
//                point.renderable = copyRenderable
//                point.setParent(node)

            point.setParent(anchorNode)
            //point.scaleController.
            point.select()
//                isPlaced = true
//                Log.w("Anchor","Placed")
//            }

        }
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
//            val drawable = BitmapDrawable(this.resources, bitmap)
            fileCache(bitmap)
            Log.w("Bitmap","Loaded")
            ViewRenderable.builder()
                    .setView(this, R.layout.ar_testload)
                    .build()
                    .thenAccept { renderable -> loadedRenderable = renderable } //need to put in saved location here
                    .exceptionally {
                        Log.w("Load File","Unable to load saved renderable")
                        null
                    }
        }
    }

    private fun fileCache(image : Bitmap) {
        val f3 = File(Environment.getExternalStorageDirectory().toString() + "/images/");
        if(!f3.exists())
            f3.mkdirs()
        val outStream : OutputStream
        val file = File(Environment.getExternalStorageDirectory().toString() + "/images/"+".png");
        try {
            outStream = FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 85, outStream);
            outStream.close();
            Log.w("Bitmap","Saved")
        } catch (e : Exception) {
            e.printStackTrace();
        }
    }

    companion object {
        private val TAG = MainActivity::class.java!!.getSimpleName()
        private val MIN_OPENGL_VERSION = 3.1

        /**
         * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
         * on this device.
         *
         *
         * Sceneform requires Android N on the device as well as OpenGL 3.1 capabilities.
         *
         *
         * Finishes the activity if Sceneform can not run
         */
        fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
            if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
                Log.e(TAG, "Sceneform requires Android N or later")
                Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
                activity.finish()
                return false
            }
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
//TODO: scale image down/up, improve rotation