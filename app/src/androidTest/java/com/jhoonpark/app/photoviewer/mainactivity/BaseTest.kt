package com.jhoonpark.app.photoviewer.mainactivity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhoonpark.app.photoviewer.R
import com.jhoonpark.app.photoviewer.presentation.view.activities.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class BaseTest {
    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun init() {
        hiltRule.inject()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun testButtonsAllExist() {
        // Read 버튼이 존재하는지 확인
        onView(withId(R.id.button_read))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.button_read)))

        // Clear 버튼이 존재하는지 확인
        onView(withId(R.id.button_clear))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.button_clear)))

        // Sort 버튼이 존재하는지 확인
        onView(withId(R.id.button_sort))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.button_sort)))
    }
}