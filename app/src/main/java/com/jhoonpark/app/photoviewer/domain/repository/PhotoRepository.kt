package com.jhoonpark.app.photoviewer.domain.repository

import android.util.Log
import com.jhoonpark.app.photoviewer.domain.model.PhotoDataModel
import com.jhoonpark.app.photoviewer.data.network.PhotoService
import java.util.UUID
import javax.inject.Inject

class PhotoRepository @Inject constructor(private val photoService: PhotoService) {

    suspend fun getPhotosList(): Result<List<PhotoDataModel>> {
        return try {
            val photoDataList = photoService.fetchPhotoData().map { entity ->
                PhotoDataModel(
                    uuid = UUID.randomUUID(),
                    albumId = entity.albumId,
                    id = entity.id,
                    title = entity.title,
                    url = entity.url,
                    thumbnailUrl = entity.thumbnailUrl
                )
            }
            Result.success(photoDataList)
        } catch(e: Exception) {
            Log.e(TAG, "Failed to fetch photo data: $e")
            Result.failure(e)
        }
    }

    companion object {
        const val TAG = "PhotoRepository"
    }
}