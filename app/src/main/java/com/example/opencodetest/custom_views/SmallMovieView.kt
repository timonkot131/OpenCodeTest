package com.example.opencodetest.custom_views

import android.content.Context
import android.view.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SmallMovieView(context: Context, val movie: Movie, onClose: () -> Unit) : ConstraintLayout(context) {

    private var popupWindow = PopupWindow(context)
    private val popUpMovieView = PopUpMovieView(context, movie)

    init {
        popupWindow.setBackgroundDrawable(context.getDrawable(R.drawable.empty))
        inflate(context, R.layout.small_movie_layout, this)
        popupWindow.contentView = popUpMovieView
        smallMovieTitle.text = movie.name
        movie.getMetadata(::onMetadataGet)
        popupWindow.contentView.popupMovieCloseButton.setOnClickListener { onClose() }
        popupWindow.contentView.popupMovieBackground.setOnClickListener { onClose() }
    }

    private fun onMetadataGet(metadata: Res<MovieMetadata, MovieError>){
        when(metadata){
            is ResOk -> {
                //popup
                metadata.value.description.let( popupWindow.contentView.popupMovieDescription::setText)
                metadata.value.genres?.let{ popupWindow.contentView.popUpMovieGenre.text = arrayToCommaString(it) }
                metadata.value.directors?.let { popupWindow.contentView.popupMovieDirector.text = arrayToCommaString(it) }
                metadata.value.actors?.let { popupWindow.contentView.popupMovieActors.text = arrayToCommaString(it) }
                metadata.value.score?.let {
                    get5StarsBy10Rating(it, context).forEachIndexed { index, drawable ->
                        popupWindow.contentView.popupMovieStars.get(index).background = drawable
                    }
                }
                metadata.value.duration?.let{
                    popupWindow.contentView.popupMovieDuration.text = getDurationFromMinutes(it)
                }
                metadata.value.poster?.let{
                    popupWindow.contentView.popupMoviePoster.setImageBitmap(getBitmapFromBytes(it))
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
                metadata.value.duration?.let { smallMovieDuration.text = getDurationFromMinutes(it)}
                metadata.value.score?.let { score -> smallMovieScore10.text = score.toString()}
            }
            is ResError -> when(metadata.value) {
                is ConnectionError -> popupWindow.contentView.popupMovieError.text = "Оффлайн версия"
            }
        }
    }

    fun expand(anchor: View, onRemove: (Movie) -> Unit): PopupWindow {
        (popupWindow.contentView as PopUpMovieView).onRemove = onRemove
        popupWindow.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindow.height = WindowManager.LayoutParams.MATCH_PARENT

        popupWindow.showAtLocation(anchor, Gravity.CENTER, 0, 0)
        return popupWindow
    }

    fun dismissPopup(){
        popupWindow.dismiss()
    }

}