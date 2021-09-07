package com.example.opencodetest

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import com.example.opencodetest.custom_views.SmallMovieSearchView
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.utility.observe
import com.example.opencodetest.viewmodels.MovieSearchViewModel
import kotlinx.android.synthetic.main.activity_movie_search.*

class MovieSearch : AppCompatActivity() {

    private val movieSearchViewModel: MovieSearchViewModel by viewModels()
    private var popupOwner: SmallMovieSearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_search)

        movieSearchViewModel.movies.observe(this, ::onMovieUpdate)

        movieSearchSearchingText.addTextChangedListener {
            movieSearchProgressBar.isIndeterminate = true
            movieSearchViewModel.searchMovies(it.toString())
        }

        movieSearchCancel.setOnClickListener {
            movieSearchSearchingText.text.clear()
        }

        setSupportActionBar(movieSearchToolbar)
        movieSearchToolbar.setBackgroundColor(resources.getColor(R.color.navigation_background))
        movieSearchToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        movieSearchToolbar.setNavigationOnClickListener {
            finish()
        }


        supportActionBar!!.run {
            setDisplayShowTitleEnabled(true)
            title = "Поиск фильмов"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        popupOwner?.dismissPopup()
    }

    override fun onBackPressed() {
        if (popupOwner != null) {
            popupOwner!!.dismissPopup()
            popupOwner = null
        } else {
            finish()
        }
    }

    private fun onMovieUpdate(movies: List<Movie>) {
        movieSearchMovieLayout.removeAllViews()
        movieSearchProgressBar.isIndeterminate = false

        for (movie in movies) {
            movieSearchMovieLayout.addView(SmallMovieSearchView(this, movie))
        }

        movieSearchMovieLayout.children.forEach { movie ->
            movie as SmallMovieSearchView

            movie.setOnClickListener {
                // при всплытии окна, записывает событие, если пользователь нажмет на кнопку добавить.
                popupOwner = movie
                movie.expand(searchRoot) {
                    movieSearchViewModel.addMovie(it)
                    popupOwner?.dismissPopup()
                    popupOwner = null
                }
            }

        }
    }


}