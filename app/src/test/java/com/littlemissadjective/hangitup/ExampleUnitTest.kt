package com.littlemissadjective.hangitup

import com.google.ar.core.HitResult
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {

    //use Test View instead of real one
    //interface with all methods, have view implement it
    //function to MainActivity to do tests with test View

    val mockHR1 = mock(HitResult::class.java)
    val mockHR2 = mock(HitResult::class.java)
    val mockView = mock(MainView::class.java)

    @Test
    fun suggestedPlacementPoint() {
        //feed in two sides of a triangle, get out the third side, is translation correct

        assertEquals(4, 2 + 2)
    }

    @Test
    fun saveFile() {
        //assuming a Uri, does fileCache() return a File
    }

    @Test
    fun isIntentFired() {
        //assuming you click on Select Image, is Intent fired
    }

    @Test
    fun modeChange() {
        //assuming you click on mode, does mode change (query State to find out)
    }
}
