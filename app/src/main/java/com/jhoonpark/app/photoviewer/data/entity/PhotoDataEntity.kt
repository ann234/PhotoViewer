package com.jhoonpark.app.photoviewer.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class PhotoDataEntity(
    val albumId: Int,
    val id: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String
)
