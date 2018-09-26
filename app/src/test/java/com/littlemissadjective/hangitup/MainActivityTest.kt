package com.littlemissadjective.hangitup

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.littlemissadjective.hangitup.MainActivity.Companion.PERFECT_HEIGHT
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowContentResolver
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/** Unit test for {@link MainActivity}. */
@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    private val mockHR1 = mock(HitResult::class.java)
    private val mockHR2 = mock(HitResult::class.java)
    private val mockPlane = mock(Plane::class.java)
    private val mockView = mock(IMainView::class.java)
    private lateinit var activity: MainActivity

    /** Used in place of Mockito.any for non-nullable parameters. */
    private fun <T> any(): T {
        fun <T> uninitialized(): T = null as T
        Mockito.any<T>()
        return uninitialized()
    }

    @Before
    fun setUp() {
        val activityController = Robolectric.buildActivity(MainActivity::class.java)
        activity = activityController.get()
        activity.mainView = mockView
        activityController.setup()
    }

    @Test
    fun suggestedPlacementPoint() {
        // User taps mode toggle
        activity.setMode(State.Mode.SUGGEST_PLACE)

        Mockito.`when`(mockHR1.hitPose).thenReturn(mock(Pose::class.java))
        Mockito.`when`(mockHR1.trackable).thenReturn(mock(Plane::class.java))
        Mockito.`when`(mockHR2.hitPose).thenReturn(mock(Pose::class.java))
        Mockito.`when`(mockHR2.trackable).thenReturn(mock(Plane::class.java))

        // Sides of a right triangle, where the other side is 4f (Pythagoras)
        Mockito.`when`(mockHR1.distance).thenReturn(3f)
        Mockito.`when`(mockHR2.distance).thenReturn(5f)

        // User taps on wall and then on intersection of floor and wall
        activity.onTapArPlane(mockHR1, mockPlane)
        activity.onTapArPlane(mockHR2, mockPlane)

        verify(mockView).placeImage(any(), any(), eq((PERFECT_HEIGHT - 4f).toFloat()))
    }

    @Test
    fun selfPlace() {
        activity.setMode(State.Mode.SELF_PLACE)
        activity.onTapArPlane(mockHR1, mockPlane)
        verify(mockView).placeImage(any(), any())
    }

    @Test
    fun isIntentFired() {
        // Simulate click image picker button
        activity.onPickImage()
        val actualIntent = Shadows.shadowOf(RuntimeEnvironment.application).nextStartedActivity
        assertEquals(Intent.ACTION_GET_CONTENT, actualIntent.action)
        assertEquals("image/*", actualIntent.type)
    }

    @Test
    fun isImageSet() {
        val shadowContentResolver : ShadowContentResolver = shadowOf(RuntimeEnvironment.application.contentResolver)
        val uri = Uri.parse("file://foo.png")
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        shadowContentResolver.registerInputStream(uri, inputStream)
        val dataIntent = Intent()
        dataIntent.data = uri
        activity.onActivityResult(MainActivity.REQUEST_IMAGE_GET, AppCompatActivity.RESULT_OK, dataIntent)
        verify(mockView).setImage(any())
    }

}
