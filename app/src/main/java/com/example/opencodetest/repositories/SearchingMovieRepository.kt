package com.example.opencodetest.repositories

import android.content.Context
import com.example.opencodetest.database.AppDatabase
import com.example.opencodetest.database.entities.DatabaseMovie
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.movies.MovieMetadata
import com.example.opencodetest.movies.networking.DownloadingMovie
import com.example.opencodetest.movies.networking.MovieError
import com.example.opencodetest.movies.networking.MovieSource
import com.example.opencodetest.themoviedb.TheMovieDbSource
import com.example.opencodetest.utility.Res
import com.example.opencodetest.utility.ResOk
import com.example.opencodetest.utility.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SearchingMovieRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val source: MovieSource = TheMovieDbSource(context)
    private val movieDao = database.movieDao()

    private val scope = MainScope()

    private class AwaitingMovie(
        var downloadedMetadata: MovieMetadata?,
        override val name: String,
        override val getMetadata: ((Res<MovieMetadata, MovieError>) -> Unit) -> Unit
    ) : Movie

    fun addMovie(movie: Movie) {
        if(movie !is AwaitingMovie) {
            throw IllegalArgumentException("Don't implement Movie interface. You must put return value of searchMovie method instead.")
        }
        movieDao.addMovie(DatabaseMovie(0, movie.name, movie.downloadedMetadata))
    }

    private fun awaitMapper(downloadingMovie: DownloadingMovie): Movie {
        var downloadedMeta: MovieMetadata? = null
        return AwaitingMovie(
            downloadedMetadata = downloadedMeta,

            getMetadata = {
                scope.launch(Dispatchers.IO) {
                    val meta = downloadingMovie.metadata.await()
                    when (meta) {
                        is ResOk -> downloadedMeta = meta.value
                        else -> Unit
                    }
                    scope.launch(Dispatchers.Main) { it(meta) }
                }
            },

            name = downloadingMovie.name
        )
    }

    fun searchMovies(movieName: String): Res<List<Movie>, MovieError> =
        source.searchMovies(movieName).map { it.map(::awaitMapper) }


}