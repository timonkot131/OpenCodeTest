package com.example.opencodetest.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.repositories.ReactiveMovieRepository
import com.example.opencodetest.repositories.SearchingMovieRepository
import com.example.opencodetest.utility.ResOk
import com.example.opencodetest.utility.map
import kotlinx.coroutines.*

class MovieSearchViewModel(app: Application) : AndroidViewModel(app) {

    private val searchingRepository = SearchingMovieRepository(app.applicationContext)
    private val reactiveRepository = ReactiveMovieRepository(app.applicationContext)

    private val scope = MainScope()

    private var searchingJob: Job? = null
    private var supervisorJob = SupervisorJob()

    val movies: MutableLiveData<List<Movie>> by lazy {
        MutableLiveData<List<Movie>>()
    }

    private var searchString: String = ""

    fun searchMovies(searchString: String) {
        searchingJob?.cancel()
        this.searchString = searchString
        if (searchString.isBlank() || searchString.isEmpty()) {
            movies.value = listOf()
            return
        }
        searchingJob = scope.launch(Dispatchers.IO + supervisorJob) {
            val result = searchingRepository.searchMovies(searchString).map {
                val current = reactiveRepository.fetchMovies().map { it.name }
                it.filter { movie -> !current.contains(movie.name) }
            }

            if (isActive) {
                scope.launch(Dispatchers.Main) {
                    if (result is ResOk) movies.value = result.value
                }
            }
        }
    }

    fun addMovie(movie: Movie) {
        scope.launch(Dispatchers.IO) {
            searchingRepository.addMovie(movie)
            scope.launch(Dispatchers.Main) {
                movies.value = movies.value?.filter { it.name != movie.name }
            }
        }
    }
}