package com.jhoonpark.app.photoviewer.mainactivity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhoonpark.app.photoviewer.R
import com.jhoonpark.app.photoviewer.data.entity.PhotoDataEntity
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
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SortButtonTest {
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

    //  SortButton을 클릭하는 경우 RecyclerView의 아이템이 Title의 사전 순서대로 오름차순 정렬된다.
    @Test
    fun testSortButtonSortRecyclerViewItemsByTitle() = runTest {
        val dummyModels = run {
            val dummyEntities = Json.decodeFromString<List<PhotoDataEntity>>(dummyData)
            dummyEntities.map {
                PhotoDataModel(
                    uuid = UUID.randomUUID(),
                    albumId = it.albumId,
                    id = it.id,
                    title = it.title,
                    url = it.url,
                    thumbnailUrl = it.thumbnailUrl
                )
            }
        }
        coEvery { mockPhotoRepository.getPhotosList() } returns Result.success(dummyModels)
        onView(withId(R.id.button_read))
            .perform(click())

        //  추가한 데이터 순서 그대로 RecyclerView에 들어갔는지 확인
        for(index in dummyModels.indices) {
            onView(withId(R.id.rvPhotoView))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(index))

            onView(withText(dummyModels[index].title))
                .check(matches(isDisplayed()))
        }

        //  SortButton 클릭 후
        onView(withId(R.id.button_sort))
            .perform(click())
        //  정렬이 종료될 때까지 대기
        //  TODO: 정렬이 너무 오래걸릴 경우 테스트를 중지하는 기능 필요할까?
        while(mainViewModel.mainState == MainViewModel.MainState.SORTING) {
            withContext(Dispatchers.Default) { delay(100L) }
        }

        //  Title의 사전 순서 오름차순대로 RecyclerView에 들어갔는지 확인
        val sortedModels = dummyModels.sortedBy {
            it.title
        }
        for(index in dummyModels.indices) {
            onView(withId(R.id.rvPhotoView))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(index))

            onView(withText(sortedModels[index].title))
                .check(matches(isDisplayed()))
        }
    }

    private val dummyData = """
        [
            {
                "albumId": 1,
                "id": 1,
                "title": "accusamus beatae ad facilis cum similique qui sunt",
                "url": "https://via.placeholder.com/600/92c952",
                "thumbnailUrl": "https://via.placeholder.com/150/92c952"
            },
            {
                "albumId": 1,
                "id": 2,
                "title": "reprehenderit est deserunt velit ipsam",
                "url": "https://via.placeholder.com/600/771796",
                "thumbnailUrl": "https://via.placeholder.com/150/771796"
            },
            {
                "albumId": 1,
                "id": 3,
                "title": "officia porro iure quia iusto qui ipsa ut modi",
                "url": "https://via.placeholder.com/600/24f355",
                "thumbnailUrl": "https://via.placeholder.com/150/24f355"
            },
            {
                "albumId": 1,
                "id": 4,
                "title": "culpa odio esse rerum omnis laboriosam voluptate repudiandae",
                "url": "https://via.placeholder.com/600/d32776",
                "thumbnailUrl": "https://via.placeholder.com/150/d32776"
            },
            {
                "albumId": 1,
                "id": 5,
                "title": "natus nisi omnis corporis facere molestiae rerum in",
                "url": "https://via.placeholder.com/600/f66b97",
                "thumbnailUrl": "https://via.placeholder.com/150/f66b97"
            },
            {
                "albumId": 1,
                "id": 6,
                "title": "accusamus ea aliquid et amet sequi nemo",
                "url": "https://via.placeholder.com/600/56a8c2",
                "thumbnailUrl": "https://via.placeholder.com/150/56a8c2"
            }
        ]
    """.trimIndent()
}