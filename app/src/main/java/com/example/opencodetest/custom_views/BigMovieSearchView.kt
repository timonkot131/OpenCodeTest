package com.example.opencodetest.custom_views

import android.content.Context
import android.widget.Button
import android.widget.RelativeLayout
import com.example.opencodetest.R
import com.example.opencodetest.movies.Movie
import kotlinx.android.synthetic.main.big_search_movie_layout.view.*

class BigMovieSearchView(context: Context, movie: Movie, var onAdd: ((Movie) -> Unit)? = null) :
    RelativeLayout(context) {

    init {
        inflate(context, R.layout.big_search_movie_layout, this)
        bigMovieSearchTitle.text = movie.name
        bigMovieSearchAddToFavourites.setOnClickListener {
            it as Button
            it.isPressed = true
            it.isActivated = false
            onAdd?.invoke(movie)
        }
    }

}