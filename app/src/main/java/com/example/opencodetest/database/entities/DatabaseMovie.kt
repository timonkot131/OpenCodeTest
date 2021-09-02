package com.example.opencodetest.database.entities

import androidx.room.*
import com.example.opencodetest.movies.MovieMetadata
import org.jetbrains.annotations.PropertyKey

@Entity
class DatabaseMovie (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    @Embedded val metadata: MovieMetadata?
)