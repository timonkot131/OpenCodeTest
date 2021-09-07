package com.example.opencodetest.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.repositories.ReactiveMovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StartViewModel(app: Application) : AndroidViewModel(app) {

    private val reactiveRepository = ReactiveMovieRepository(app.applicationContext)

    private val scope = MainScope()

    private var supervisorJob = SupervisorJob()

    val movies: MutableLiveData<List<Movie>> by lazy {
        MutableLiveData<List<Movie>>()
    }

    fun getMovies() {
        scope.launch(Dispatchers.IO + supervisorJob) {
            val fetchedMovies = reactiveRepository.fetchMovies()
            scope.launch(Dispatchers.Main) { movies.value = fetchedMovies }
        }
    }

    fun removeMovie(movie: Movie) {
        scope.launch(Dispatchers.IO + supervisorJob) {
            reactiveRepository.removeMovie(movie)
            getMovies()
        }
    }
}