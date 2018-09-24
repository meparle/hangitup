package com.littlemissadjective.hangitup

import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.littlemissadjective.hangitup.MainActivity.Companion.PERFECT_HEIGHT
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

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {

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

        // TODO: Maybe set up behaviors for the poses and planes too
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

        // TODO: Assert on the actual pose/plane if necessary
        verify(mockView).placeImage(any(), any(), eq((PERFECT_HEIGHT - 4f).toFloat()))
    }

    @Test
    fun saveFile() {
        //assuming a Uri, does fileCache() return a File
    }

    @Test
    fun isIntentFired() {
        //assuming you click on Select Image, is Intent fired
        val intent = Shadows.shadowOf(RuntimeEnvironment.application).nextStartedActivity
    }

    @Test
    fun modeChange() {
        //assuming you click on mode, does mode change (query State to find out)
    }
}
