package com.jhoonpark.app.photoviewer.mainactivity.readbutton

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhoonpark.app.photoviewer.R
import com.jhoonpark.app.photoviewer.domain.model.PhotoDataModel
import com.jhoonpark.app.photoviewer.domain.repository.PhotoRepository
import com.jhoonpark.app.photoviewer.presentation.view.activities.MainActivity
import com.jhoonpark.app.photoviewer.presentation.viewmodel.MainViewModel
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class RecyclerViewUpdateTest {
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

    //  ReadButton을 클릭해서 새로운 PhotoList를 불러온 경우 RecyclerView의 list 뒤에 추가되는가
    @Test
    fun testReadButtonAddsNewPhotoListToRecyclerView() = runTest {
        //  RecyclerView에 기본 더미 데이터 100를 추가
        val basePhotoList = List(100) { index ->
            PhotoDataModel(
                uuid = UUID.randomUUID(),
                albumId = index,
                id = index,
                title = "Test Title $index",
                url = "https://test.com",
                thumbnailUrl = "https://test.com",
            )
        }
        coEvery { mockPhotoRepository.getPhotosList() } returns Result.success(basePhotoList)
        onView(withId(R.id.button_read))
            .perform(ViewActions.click())

        //  ReadButton의 throttling에 걸리지 않도록 delay
        withContext(Dispatchers.Default) {
            delay(1000L)
        }

        //  데이터가 올바르게 추가됐는지 테스트하는 용도의 데이터를 추가
        val additionPhotoList = List(20) {
            val index = basePhotoList.size + it
            PhotoDataModel(
                uuid = UUID.randomUUID(),
                albumId = index,
                id = index,
                title = "Target Title $index",
                url = "https://test.com",
                thumbnailUrl = "https://test.com",
            )
        }
        coEvery { mockPhotoRepository.getPhotosList() } returns Result.success(additionPhotoList)
        onView(withId(R.id.button_read))
            .perform(ViewActions.click())

        // 데이터 추가 후 RecyclerView가 보여지고 있는지 확인
        onView(withId(R.id.rvPhotoView))
            .check(matches(isDisplayed()))

        //  추가한 데이터가 RecyclerView에 순서에 맞게 들어갔는지 확인
        for(i in additionPhotoList.indices) {
            val index = basePhotoList.size + i
            onView(withId(R.id.rvPhotoView))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(index))

            onView(withText("Target Title $index"))
                .check(matches(isDisplayed()))
        }
    }
}