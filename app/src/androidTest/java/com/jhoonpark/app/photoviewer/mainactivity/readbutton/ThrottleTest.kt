package com.jhoonpark.app.photoviewer.mainactivity.readbutton

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhoonpark.app.photoviewer.R
import com.jhoonpark.app.photoviewer.domain.repository.PhotoRepository
import com.jhoonpark.app.photoviewer.presentation.view.activities.MainActivity
import com.jhoonpark.app.photoviewer.presentation.viewmodel.MainViewModel
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkClass
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ThrottleTest {
    @BindValue
    val mockPhotoRepository = mockkClass(PhotoRepository::class, relaxed = true, relaxUnitFun = true)

    @BindValue
    lateinit var mainViewModel: MainViewModel

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun init() {
        mainViewModel = MainViewModel(mockPhotoRepository)
        hiltRule.inject()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    //  ReadButton의 throttle이 정상 작동하는지
    @Test
    fun testThrottleInReadButton() = runTest {
        coEvery { mockPhotoRepository.getPhotosList() } returns Result.success(emptyList())

        val numOfClick = 5
        onView(withId(R.id.button_read)).run {
            for(i in 0 until numOfClick) {
                perform(click())
            }
        }

        //  ReadButton을 클릭하면 MainViewModel.updatePhotoList()를 수행한다
        //  하지만 ReadButton에는 throttle이 적용돼있어, 첫 클릭 후 0.5초간은 버튼을 클릭해도 업데이트가 수행되지 않도록 제어하고 있다.
        //  따라서 클릭을 여러번 수행할 경우 최소 클릭한 횟수보다 1회 적게 updatePhotoList가 호출될 것이다.
        val expectedNumOfUpdate = numOfClick - 1
        coVerify(atMost = expectedNumOfUpdate) {
            mockPhotoRepository.getPhotosList()
        }
    }
}