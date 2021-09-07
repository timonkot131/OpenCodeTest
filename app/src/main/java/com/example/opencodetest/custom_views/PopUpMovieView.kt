package com.example.opencodetest.custom_views

import android.content.Context
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.opencodetest.R
import com.example.opencodetest.movies.Movie
import kotlinx.android.synthetic.main.big_move_layout.view.*

class PopUpMovieView(context: Context, movie: Movie, var onRemove: ((Movie) -> Unit)? = null) :
    ConstraintLayout(context) {

    init {
        inflate(context, R.layout.big_move_layout, this)
        popupMovieTitle.text = movie.name

        popupMovieDeleteButton.setOnClickListener {
            it as ImageView
            it.isActivated = false
            it.isPressed = true
            onRemove?.invoke(movie)
        }
    }


}