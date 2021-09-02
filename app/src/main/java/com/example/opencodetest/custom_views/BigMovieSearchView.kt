package com.example.opencodetest.custom_views

import android.content.Context
import android.widget.RelativeLayout
import androidx.core.view.get
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.movies.MovieMetadata
import com.example.opencodetest.movies.networking.MovieError
import com.example.opencodetest.movies.networking.NotExisting
import com.example.opencodetest.utility.Res
import com.example.opencodetest.utility.ResError
import com.example.opencodetest.utility.ResOk
import kotlinx.android.synthetic.main.big_search_movie_layout.view.*
import kotlinx.android.synthetic.main.small_movie_search_layout.view.*

class BigMovieSearchView(context: Context, movie: Movie): RelativeLayout(context) {


    init {
        smallMovieSearchTitle.text = movie.name
    }



}