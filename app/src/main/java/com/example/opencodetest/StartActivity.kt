package com.example.opencodetest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels

import com.example.opencodetest.adapters.MovieGridAdapter
import com.example.opencodetest.custom_views.SmallMovieView
import com.example.opencodetest.database.AppDatabase
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.utility.observe
import com.example.opencodetest.viewmodels.StartViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {
    val scope = MainScope()

    private val startViewModel: StartViewModel by viewModels()

    private val movieGridAdapter = MovieGridAdapter(this, ::onSmallMovieClick)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = AppDatabase.getDatabase(this)
        movieGrid.numColumns = 2
        startViewModel.movies.observe(this, ::updateMovies)
        movieGrid.adapter = movieGridAdapter
    }

    private fun updateMovies(movies: List<Movie>){
        movieGridAdapter.setMovies(movies)
    }

    private fun onSmallMovieClick(smallMovie: SmallMovieView) {
        smallMovie.expand()
    }

    override fun onResume() {
        super.onResume()
        startViewModel.getMovies()
    }
}