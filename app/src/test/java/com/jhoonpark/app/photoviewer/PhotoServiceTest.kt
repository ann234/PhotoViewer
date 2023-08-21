package com.jhoonpark.app.photoviewer

import com.jhoonpark.app.photoviewer.data.entity.PhotoDataEntity
import com.jhoonpark.app.photoviewer.data.network.PhotoService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class PhotoServiceTest {

    @Test
    fun `fetchPhotoData는 서버 API 요청에 성공하면 PhotoData의 List를 반환합니다`() {
        val testRawData = """
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
            }
        ]
    """.trimIndent()

        val mockBody: ResponseBody = mock()
        `when`(mockBody.string()).thenReturn(testRawData)

        val mockResponse: Response = mock()
        `when`(mockResponse.body).thenReturn(mockBody)

        val mockCall: Call = mock()
        `when`(mockCall.execute()).thenReturn(mockResponse)

        val mockClient: OkHttpClient = mock()
        `when`(mockClient.newCall(any())).thenReturn(mockCall)

        val photoService = PhotoService(mockClient)
        val result = runBlocking {
            photoService.fetchPhotoData()
        }
        val expected = Json.decodeFromString<List<PhotoDataEntity>>(testRawData)
        assert(result == expected)
    }

    @Test
    fun `fetchPhotoData는 서버 API 요청중 coroutine이 중단되면 요청(okhttp3 Call)을 취소합니다`() {
        val mockResponse: Response = mock()
        val mockCall: Call = mock()
        `when`(mockCall.execute()).thenAnswer {
            Thread.sleep(10000L)
            mockResponse
        }

        val mockClient: OkHttpClient = mock()
        `when`(mockClient.newCall(any())).thenReturn(mockCall)

        runBlocking {
            val photoService = PhotoService(mockClient)
            try {
                photoService.fetchPhotoData()
            } catch(_: Exception) { }

            verify(mockCall).cancel()
        }
    }
}