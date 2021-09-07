package com.example.opencodetest.custom_views

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.Preference
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.core.view.get
import com.example.opencodetest.R
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.movies.MovieMetadata
import com.example.opencodetest.movies.networking.MovieError
import com.example.opencodetest.utility.Res
import com.example.opencodetest.utility.ResError
import com.example.opencodetest.utility.ResOk
import kotlinx.android.synthetic.main.big_search_movie_layout.view.*
import kotlinx.android.synthetic.main.small_movie_search_layout.view.*

class SmallMovieSearchView(context: Context, movie: Movie): RelativeLayout(context) {

    private var popUp = PopupWindow(context)
    private val bigMovieSearch = BigMovieSearchView(context, movie)

    init {
        inflate(context, R.layout.small_movie_search_layout, this)
        smallMovieSearchTitle.text = movie.name
        movie.getMetadata(::onMetadataGet)
        popUp.contentView = bigMovieSearch
        popUp.contentView.bigMovieSearchToolbar.navigationIcon = context.getDrawable(R.drawable.ic_baseline_arrow_back_ios_24)
    }

    fun expand(anchor: View, onAdd: (Movie) -> Unit): PopupWindow {
        (popUp.contentView as BigMovieSearchView).onAdd = onAdd
        popUp.width = WindowManager.LayoutParams.MATCH_PARENT
        popUp.height = WindowManager.LayoutParams.MATCH_PARENT
        popUp.contentView.bigMovieSearchToolbar.setNavigationOnClickListener {
            dismissPopup()
        }
        popUp.showAtLocation(anchor, Gravity.CENTER, 0, 0)

        return popUp
    }

    fun dismissPopup(){
        popUp.dismiss()
    }

    private fun onMetadataGet(metadata: Res<MovieMetadata, MovieError>){
        when(metadata){
            is ResOk -> {
                //bigMovieSearch
                metadata.value.poster?.let {
                    popUp.contentView.bigMovieSearchPoster.setImageBitmap(getBitmapFromBytes(it))
                }

                metadata.value.score?.let {
                    get5StarsBy10Rating(it, context).forEachIndexed { index, drawable ->
                        popUp.contentView.bigMovieSearchStars.get(index).background = drawable
                    }
                }

                metadata.value.description?.let {
                    popUp.contentView.bigMovieSearchDescription.text = "Описание: " + it
                }

                metadata.value.actors?.let {
                    val actors = arrayToCommaString(it)
                    popUp.contentView.bigMovieSearchActors.text = "Aктеры: " + actors
                }

                metadata.value.directors?.let {
                    val director = arrayToCommaString(it)
                    popUp.contentView.bigMovieSearchDirector.text = "Директор: " + director
                }

                metadata.value.genres?.let {
                    val genres = arrayToCommaString(it)
                    popUp.contentView.bigMovieSearchGenre.text = "Жанр: " + genres
                }

                metadata.value.duration?.let {
                    popUp.contentView.bigMovieSearchDuration.text = "Длительность: " + getDurationFromMinutes(it)
                }

                //smallMovieSearch
                metadata.value.poster?.let{
                    val image = getBitmapFromBytes(it)
                    smallMovieSearchPoster.setImageBitmap(image)
                }
                metadata.value.yearOfCreate?.let{
                    smallMovieSearchYear.text = it.toString()

                }
            }
            is ResError -> popUp.contentView.bigMovieSearchError.text = "Оффлайн режим"

        }
    }

}