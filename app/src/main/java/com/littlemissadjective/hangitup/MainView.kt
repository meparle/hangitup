package com.littlemissadjective.hangitup

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

/**
 * View representing the entire app UI.
 *
 * <p>Receives updates from {@link MainActivity} and passes user interaction events back through the
 * {@link IMainView.ViewListener}, which in practice is always the activity.
 */
class MainView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IMainView {

    private lateinit var loadedRenderable: ViewRenderable
    private lateinit var imageView: ImageView
    private lateinit var arFragment: ArFragment
    private lateinit var _listener: IMainView.ViewListener

    override var listener: IMainView.ViewListener
        get() = _listener
        set(value) {_listener = value}

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<Button>(R.id.button_select).setOnClickListener { _: View ->  listener.onPickImage()  }
        findViewById<ToggleButton>(R.id.toggle_mode).setOnClickListener {v: View -> run { toggleMode(v) } }

        arFragment =
                (context as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent ->
            listener.onTapArPlane(hitResult, plane)
        }

        ViewRenderable.builder()
                .setView(context, R.layout.ar_test)
                .build()
                .thenAccept { renderable ->
                    Log.w("Load File", "loaded saved renderable")
                    imageView = renderable.view.findViewById(R.id.ux_fragment)
                    loadedRenderable = renderable
                }
                .exceptionally {
                    Log.w("Load File", "Unable to load saved renderable", it)
                    null
                }
    }

    private fun toggleMode(v: View) {
        val toggle = v as ToggleButton
        if (toggle.isChecked) {
            Log.d("Measure", "Started")
            listener.setMode(State.Mode.SUGGEST_PLACE)
        }
        else {
            Log.d("Place", "Started")
            listener.setMode(State.Mode.SELF_PLACE)
        }
    }

    override fun setImage(bitmap: Bitmap?) {
        imageView.setImageBitmap(bitmap)
    }

    override fun placeImage(hitResult: HitResult, plane: Plane) {
        val node = getNode(hitResult)
        node.renderable = loadedRenderable
        when (plane.type) {
            Plane.Type.HORIZONTAL_DOWNWARD_FACING -> {
            }
            Plane.Type.HORIZONTAL_UPWARD_FACING -> {
                node.localRotation = Quaternion.axisAngle(Vector3.right(), -90.0f)
            }
            Plane.Type.VERTICAL -> {
                node.setLookDirection(node.down,node.back)
            }
            null -> {}
        }
        Log.d("Look direction", "Changed to up")
        node.select()
    }

    override fun placeImage(plane: Plane, pose: Pose, translation: Float) {
        val translatedPose = pose.compose(Pose.makeTranslation(floatArrayOf(0f, 0f, translation * -1)))
        val node = getNode(plane, translatedPose)
        node.renderable = loadedRenderable
        when (plane.type) {
            Plane.Type.HORIZONTAL_DOWNWARD_FACING -> {
            }
            Plane.Type.HORIZONTAL_UPWARD_FACING -> {
                node.localRotation = Quaternion.axisAngle(Vector3.right(), -90.0f)
            }
            Plane.Type.VERTICAL -> {
                node.setLookDirection(node.down,node.back)
            }
            null -> {}
        }
        Log.d("Look direction", "Changed to up")
        node.select()
    }

    /**
     * Takes a HitResult and makes an AnchorNode out of it.
     * @return a TransformableNode which can be rotated that is a child of AnchorNode.
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

    private fun getNode(plane: Plane, pose: Pose): TransformableNode {
        val scene = arFragment.arSceneView.scene
        scene.children.filterIsInstance<AnchorNode>().forEach(scene::removeChild)

        val anchorNode = AnchorNode(plane.createAnchor(pose))
        scene.addChild(anchorNode)
        Log.d("Anchor", "Parent set")

        val point = TransformableNode(arFragment.transformationSystem)
        anchorNode.addChild(point)

        return point
    }
}