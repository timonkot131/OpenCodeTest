package com.example.opencodetest.movies.networking

import com.example.opencodetest.movies.MovieMetadata
import com.example.opencodetest.utility.Res
import kotlinx.coroutines.Deferred

class DownloadingMovie(
    val name: String,
    val metadata: Deferred<Res<MovieMetadata, MovieError>>
)