package com.littlemissadjective.hangitup

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Instrumented test of {@link MainActivity}. */
@RunWith(AndroidJUnit4::class)
class MainActivityUITest {

    @Rule @JvmField
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun packageName() {
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.littlemissadjective.hangitup", appContext.packageName)
    }

    @Test
    fun changeMode() {
        onView(withId(R.id.toggle_mode))
                .check(matches(withText("Suggest Place Mode")))
                .perform(click())
                .check(matches(withText("Self Place Mode")))
    }
} 
