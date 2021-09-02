package com.example.opencodetest.movies

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

class MovieMetadata (
    val yearOfCreate: Int?,
    val directors: Array<String>?,
    val genres: Array<String>?,
    val actors: Array<String>?,
    val duration: Int?,
    val score: Float?,
    val description: String?,
    val poster: ByteArray?
)