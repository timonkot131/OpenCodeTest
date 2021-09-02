package com.example.opencodetest.custom_views

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import com.example.opencodetest.R
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.movies.MovieMetadata
import com.example.opencodetest.movies.networking.ConnectionError
import com.example.opencodetest.movies.networking.MovieError
import com.example.opencodetest.movies.networking.NotExisting
import com.example.opencodetest.utility.Res
import com.example.opencodetest.utility.ResError
import com.example.opencodetest.utility.ResOk
import kotlinx.android.synthetic.main.big_move_layout.view.*
import kotlinx.android.synthetic.main.small_movie_layout.view.*

class SmallMovieView(context: Context, val movie: Movie) : ConstraintLayout(context) {

    private val popUpMovieView = PopUpMovieView(context, movie)

    init {
        inflate(context, R.layout.small_movie_layout, this)
        smallMovieTitle.text = movie.name
        movie.getMetadata(::onMetadataGet)
    }

    private fun onMetadataGet(metadata: Res<MovieMetadata, MovieError>){
        when(metadata){
            is ResOk -> {
                //popup
                metadata.value.description.let( popUpMovieView.popupMovieDescription::setText)
                metadata.value.genres?.let{ popUpMovieView.popUpMovieGenre.text = arrayToCommaString(it) }
                metadata.value.directors?.let { popUpMovieView.popupMovieDirector.text = arrayToCommaString(it) }
                metadata.value.actors?.let { popUpMovieView.popupMovieActors.text = arrayToCommaString(it) }
                metadata.value.score?.let {
                    get5StarsBy10Rating(it, context).forEachIndexed { index, drawable ->
                        popUpMovieView.popupMovieStars.get(index).background = drawable
                    }
                }
                metadata.value.duration?.let{
                    popUpMovieView.popupMovieDuration.text = getDurationFromMinutes(it)
                }
                metadata.value.poster?.let{
                    popUpMovieView.popupMoviePoster.setImageBitmap(getBitmapFromBytes(it))
                }

                //smallMovie
                metadata.value.poster?.let {
                    smallMoviePoster.setImageBitmap(getBitmapFromBytes(it))
                }
                metadata.value.genres?.let { genres: Array<String> ->
                    metadata.value.yearOfCreate?.let { year ->
                        val genres = arrayToCommaString(genres)
                        smallMovieDescription.text = year.toString() + " · " + genres
                    }
                }
                metadata.value.duration?.let { smallMovieDescription.text = getDurationFromMinutes(it)}
                metadata.value.score?.let { score -> smallMovieScore10.text = score.toString()}
            }
            is ResError -> when(metadata.value) {
                is NotExisting -> {
                    popUpMovieView.popupMovieDescription.text = "Данных не обнаружено. Пробуем догрузить"
                    smallMovieDescription.text = "Данных нео бнаружено. Пробуем догрузить"
                }
                is ConnectionError -> {
                    popUpMovieView.popupMovieDescription.text = "При загрузке данных, возникла ошибка с соединением"
                    smallMovieDescription.text = "При загрузке данных, возникла ошибка с соединением"
                }
            }
        }
    }

    fun expand(){
        val popupWindow = PopupWindow(PopUpMovieView(context, movie))
        popupWindow.showAtLocation(parent as View, Gravity.BOTTOM, 10, 10)
    }

}