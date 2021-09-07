package com.example.opencodetest.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.opencodetest.movies.MovieMetadata

@Entity
class DatabaseMovie (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    @Embedded val metadata: MovieMetadata?
)