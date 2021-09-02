package com.example.opencodetest.custom_views

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import com.example.opencodetest.R
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.movies.MovieMetadata
import com.example.opencodetest.movies.networking.BadResponse
import com.example.opencodetest.movies.networking.ConnectionError
import com.example.opencodetest.movies.networking.MovieError
import com.example.opencodetest.movies.networking.NotExisting
import com.example.opencodetest.utility.Res
import com.example.opencodetest.utility.ResError
import com.example.opencodetest.utility.ResOk
import kotlinx.android.synthetic.main.big_move_layout.view.*

class PopUpMovieView(context: Context, movie: Movie) : ConstraintLayout(context) {

    init {
        inflate(context, R.layout.big_move_layout, this)
        popupMovieTitle.text = movie.name
    }


}