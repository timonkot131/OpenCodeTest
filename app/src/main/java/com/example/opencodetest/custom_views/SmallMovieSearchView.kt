package com.example.opencodetest.custom_views

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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

    private val popUp = PopupWindow(context)
    private val bigMovieSearch = BigMovieSearchView(context, movie)
    val params = LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

    init {
        inflate(context, R.layout.small_movie_search_layout, this)
        smallMovieSearchTitle.text = movie.name
        movie.getMetadata(::onMetadataGet)
        bigMovieSearch.layoutParams = params
        popUp.contentView = bigMovieSearch
    }

    fun expand(onAdd: (Movie) -> Unit): PopupWindow {
        (popUp.contentView as BigMovieSearchView).onAdd = onAdd
        popUp.showAtLocation(parent as View, Gravity.CENTER, 0, 0)
        return popUp
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
                    popUp.contentView.bigMovieSearchDuration.text = "Длительность: " + it
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
            is ResError -> {
                smallMovieSearchYear.text = "Не удалось получить данные"
                popUp.contentView.bigMovieSearchDescription.text = "Не удалось прогрузить остальные данные"
            }
        }
    }

}