package com.jhoonpark.app.photoviewer.domain.model

import java.util.UUID

data class PhotoDataModel(
    val uuid: UUID,
    val albumId: Int,
    val id: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String
)
