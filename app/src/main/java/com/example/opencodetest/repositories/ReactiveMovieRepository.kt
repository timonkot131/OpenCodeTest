package com.example.opencodetest.repositories

import android.content.Context
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.movies.networking.MovieError
import com.example.opencodetest.movies.networking.MovieSource
import com.example.opencodetest.themoviedb.TheMovieDbSource
import com.example.opencodetest.database.*
import com.example.opencodetest.database.entities.DatabaseMovie
import com.example.opencodetest.movies.MovieMetadata
import com.example.opencodetest.movies.networking.NotExisting
import com.example.opencodetest.utility.*
import kotlinx.coroutines.*

class ReactiveMovieRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val source: MovieSource = TheMovieDbSource(context)
    private val movieDao = database.movieDao()

    private val scope = MainScope()

    private class ReactiveMovie(
        val databaseMovie: DatabaseMovie,
        override val name: String,
        override val getMetadata: ((Res<MovieMetadata, MovieError>) -> Unit) -> Unit
    ) : Movie

    //В начале, когда только UI запросит данные, выстреливает данными из базы, а потом когда прогрузится актуальная информация, выстреливает еще раз.
    private fun makePseudoReactive(
        callback: (Res<MovieMetadata, MovieError>) -> Unit,
        deffered: Deferred<Res<MovieMetadata, MovieError>>
    ) {

        scope.launch(Dispatchers.IO) {
            val awaited = deffered.await()
            scope.launch(Dispatchers.Main) { callback(awaited) }
        }
    }

    private fun cacheDownloaded(
        dbMovie: DatabaseMovie,
        downloadingMeta: Deferred<Res<MovieMetadata, MovieError>>
    ) {
        scope.launch(Dispatchers.IO) {
            val meta = downloadingMeta.await()
            if (meta is ResOk) {
                movieDao.updateMovie(DatabaseMovie(dbMovie.id, dbMovie.name, meta.value))
            }
        }
    }

    private fun handleField(
        cb: (Res<MovieMetadata, MovieError>) -> Unit,
        dbMovie: DatabaseMovie,
        deferred: Deferred<Res<MovieMetadata, MovieError>>
    ) {
        makePseudoReactive(cb, deferred)
        cacheDownloaded(dbMovie, deferred)
    }

    private fun dbMovieToReactiveMapper(dbMovie: DatabaseMovie): Movie =
        ReactiveMovie(
            name = dbMovie.name,

            getMetadata = {
                val databaseResult =
                    if (dbMovie.metadata == null)
                        ResError<MovieMetadata, MovieError>(NotExisting())
                    else
                        ResOk<MovieMetadata, MovieError>(dbMovie.metadata)

                it(databaseResult)

                scope.launch(Dispatchers.IO) {
                    val reactiveMetadata =
                        when (val x = source.getDetails(dbMovie.name)) { // разворачивает результат
                            // Если запрос произошел в целом нормально, то получает джобу с результатом метаданных
                            is ResOk -> x.value.metadata
                            // если у нас ошибка, достает ее и заворачивает, так как если бы это выглядело сверху, тем самым мы продожаем цепочку с сохраненной ошибкой
                            is ResError -> scope.async { ResError<MovieMetadata, MovieError>(x.value) }
                        }
                    handleField(it, dbMovie, reactiveMetadata)
                }
            },

            databaseMovie = dbMovie
        )

    fun fetchMovies(): List<Movie> {
        val dbMovies = movieDao.getMovies()
        return dbMovies.map(::dbMovieToReactiveMapper)
    }

    fun removeMovie(movie: Movie) {
        if (movie !is ReactiveMovie) {
            throw IllegalArgumentException("Don't implement Movie interface. You must put return value of fetchMovies method instead.")
        }
        movieDao.removeMovie(movie.databaseMovie)
    }
}