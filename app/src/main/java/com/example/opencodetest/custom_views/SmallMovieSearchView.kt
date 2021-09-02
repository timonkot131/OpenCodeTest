package com.example.opencodetest.custom_views

import android.content.Context
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

    private val bigMovieSearch = BigMovieSearchView(context,movie)

    init {
        inflate(context, R.layout.small_movie_search_layout, this)
        smallMovieSearchTitle.text = movie.name
        movie.getMetadata(::onMetadataGet)
    }

    fun expand(){

    }

    private fun onMetadataGet(metadata: Res<MovieMetadata, MovieError>){
        when(metadata){
            is ResOk -> {
                //bigMovieSearch
                metadata.value.poster?.let {
                    bigMovieSearch.bigMovieSearchPoster.setImageBitmap(getBitmapFromBytes(it))
                }

                metadata.value.score?.let {
                    get5StarsBy10Rating(it, context).forEachIndexed { index, drawable ->
                        bigMovieSearch.bigMovieSearchStars.get(index).background = drawable
                    }
                }

                metadata.value.description?.let {
                    bigMovieSearch.bigMovieSearchDescription.text = "Описание: " + it
                }

                metadata.value.actors?.let {
                    val actors = arrayToCommaString(it)
                    bigMovieSearch.bigMovieSearchActors.text = "Aктеры: " + actors
                }

                metadata.value.directors?.let {
                    val director = arrayToCommaString(it)
                    bigMovieSearch.bigMovieSearchDirector.text = "Директор: " + director
                }

                metadata.value.genres?.let {
                    val genres = arrayToCommaString(it)
                    bigMovieSearch.bigMovieSearchGenre.text = "Жанр: " + genres
                }

                metadata.value.duration?.let {
                    bigMovieSearch.bigMovieSearchDuration.text = "Длительность: " + it
                }

                //smallMovieSearch
                metadata.value.poster?.let{
                    val image = getBitmapFromBytes(it)
                    smallMovieSearchPoster.setImageBitmap(image)
                }
                metadata.value.yearOfCreate?.let(smallMovieSearchYear::setText)
            }
            is ResError -> {
                smallMovieSearchYear.text = "Не удалось получить данные"
                bigMovieSearchDescription.text = "Не удалось прогрузить остальные данные"
            }
        }
    }

}