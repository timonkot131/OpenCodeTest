package com.example.opencodetest.adapters

import android.content.Context
import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ListAdapter
import com.example.opencodetest.custom_views.SmallMovieView
import com.example.opencodetest.movies.Movie

class MovieGridAdapter(
    private val context: Context,
    private val onClick: (Int, SmallMovieView) -> Unit,
    private val onClose: () -> Unit,
    private var movies: MutableList<Movie> = mutableListOf()
) : ListAdapter {


    fun setMovies(movies: List<Movie>) {
        this.movies = movies.toMutableList()
    }

    fun removeMovie(pos: Int) {
        movies.removeAt(pos)
    }

    override fun isEmpty(): Boolean = movies.isEmpty()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val smallMovieView = SmallMovieView(context, movies[position], onClose)
        smallMovieView.setOnClickListener { onClick(position, it as SmallMovieView) }
        return smallMovieView
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {}

    override fun getItemViewType(position: Int) = Adapter.IGNORE_ITEM_VIEW_TYPE

    override fun getItem(position: Int) = movies[position]

    override fun getViewTypeCount() = 1

    override fun isEnabled(position: Int) = true

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun areAllItemsEnabled(): Boolean = true

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {}

    override fun getCount(): Int = movies.count()
}