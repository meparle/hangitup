package com.littlemissadjective.hangitup

import android.graphics.Bitmap
import android.view.View
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose

/** Interface for the main app UI, split out from the implementation to facilitate testing. */
interface IMainView {
    var listener: IMainView.ViewListener

    /** Listener to receive user interaction callbacks. */
    interface ViewListener {
        /** Called when the user taps a point in the AR scene. */
        fun onTapArPlane(hitResult: HitResult, plane: Plane)
        /** Called when the user clicks the image picker button. */
        fun onPickImage()
        /** Called when the user clicks the mode toggle. */
        fun setMode(mode: State.Mode)
    }

    /** Sets the image to be placed. */
    fun setImage(bitmap: Bitmap?)

    /** Places the current image on the plane at the point represented by {@code hitResult}. */
    fun placeImage(hitResult: HitResult, plane: Plane)

    /**
     * Places the current image on the plane with the given pose, translated vertically by {@code
     * translation}.
     */
    fun placeImage(plane: Plane, pose: Pose, translation: Float)
}