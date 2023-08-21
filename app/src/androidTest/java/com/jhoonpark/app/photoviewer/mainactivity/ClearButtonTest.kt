package com.jhoonpark.app.photoviewer.mainactivity

import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
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
import org.hamcrest.Description
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ClearButtonTest {
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

    //  ClearButton을 클릭하는 경우
    //  1. RecyclerView의 아이템이 모두 제거된다.
    //  2. ReadButton을 비활성화 한다.
    //  3. N초 뒤, ReadButton을 활성화 한다.
    @Test
    fun whenPressClearButtonClearAllRecyclerView() = runTest {
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
            .perform(click())

        //  ClearButton 클릭 후
        onView(withId(R.id.button_clear))
            .perform(click())
        //  삭제가 종료될 때까지 대기
        //  TODO: 삭제가 너무 오래 걸릴 경우 테스트를 중지하는 기능 필요할까?
        while(mainViewModel.mainState == MainViewModel.MainState.CLEARING) {
            withContext(Dispatchers.Default) { delay(100L) }
        }
        //  RecyclerView에 남은 데이터 개수 확인
        onView(withId(R.id.rvPhotoView))
            .check(matches(withItemCount(0)))

        //  ClearButton 클릭 후 ReadButton은 비활성화 되어야 한다.
        onView(withId(R.id.button_read))
            .check(matches(not(isEnabled())))

        //  N초 뒤, ReadButton은 다시 활성화 되어야 한다.
        withContext(Dispatchers.Default) { delay(MainViewModel.RESTORE_READBUTTON_TIME_MILLS) }
        onView(withId(R.id.button_read))
            .check(matches(isEnabled()))
    }

    //  데이터를 불러와 업데이트하는 작업 중 ClearButton을 클릭하는 경우, 작업을 중단한다.
    @Test
    fun whenClearButtonPressedCancelFetchDataJob() = runTest {
        val dummyPhotoModelList = List(100) { index ->
            PhotoDataModel(
                uuid = UUID.randomUUID(),
                albumId = index,
                id = index,
                title = "Test Title $index",
                url = "https://test.com",
                thumbnailUrl = "https://test.com",
            )
        }
        //  ReadButton을 누를 경우 위 데이터를 불러와 RecyclerView에 업데이트하는 작업을 수행하겠지만,
        //  업데이트되기 전 ClearButton을 누른다면 작업이 취소될 것이다.
        val readTime = 2500L
        coEvery { mockPhotoRepository.getPhotosList() } coAnswers {
            //  취소 시간을 주기 위해 임의로 딜레이 부여
            delay(readTime)
            Result.success(dummyPhotoModelList)
        }

        //  ReadButton을 클릭 후 취소를 위해 바로 ClearButton을 클릭
        onView(withId(R.id.button_read))
            .perform(click())
        onView(withId(R.id.button_clear))
            .perform(click())

        //  충분히 데이터 업데이트가 완료될 수 있는 시간까지 대기
        withContext(Dispatchers.Default) { delay(readTime + 1000L) }

        //  RecyclerView에 데이터가 없다면, ClearButton이 데이터 업데이트 작업을 중단한 것으로 판단한다.
        onView(withId(R.id.rvPhotoView))
            .check(matches(withItemCount(0)))
    }

    // 아이템 수를 검사하는 커스텀 매처 함수.
    private fun withItemCount(expectedCount: Int): TypeSafeMatcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("RecyclerView with item count: $expectedCount")
            }

            override fun matchesSafely(view: View): Boolean {
                return view is RecyclerView && view.adapter?.itemCount == expectedCount
            }
        }
    }
}