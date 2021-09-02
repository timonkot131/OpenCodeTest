package com.example.opencodetest.movies.networking

import com.example.opencodetest.utility.Res

interface MovieSource {
    fun searchMovies(movie: String): Res<List<DownloadingMovie>, MovieError>
    fun getDetails(movie: String): Res<DownloadingMovie, MovieError>
}