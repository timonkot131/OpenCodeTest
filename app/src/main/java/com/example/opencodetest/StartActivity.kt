package com.example.opencodetest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.viewModels

import com.example.opencodetest.adapters.MovieGridAdapter
import com.example.opencodetest.custom_views.BigMovieSearchView
import com.example.opencodetest.custom_views.SmallMovieView
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.utility.observe
import com.example.opencodetest.viewmodels.StartViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.small_movie_search_layout.view.*
import kotlinx.coroutines.MainScope

class StartActivity : AppCompatActivity() {
    val scope = MainScope()

    private val startViewModel: StartViewModel by viewModels()

    private val movieGridAdapter = MovieGridAdapter(this, ::onSmallMovieClick)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startViewModel.movies.observe(this, ::updateMovies)
        startMovieGrid.numColumns = 2
        startMovieGrid.adapter = movieGridAdapter

        setSupportActionBar(startToolbar)
        supportActionBar!!.run {
            setDisplayShowTitleEnabled(true)
            title = "Movie Browser"
        }

        startToolbar.setBackgroundColor(resources.getColor(R.color.navigation_background))
        startToolbar.navigationIcon = getDrawable(R.drawable.ic_baseline_account_circle_24)
        startToolbar.setNavigationOnClickListener {
            startAccountSlider.animateOpen()
        }

        /*
        val popUp = PopupWindow(this)
        val bigMovieSearch = layoutInflater.inflate(R.layout.big_move_layout, null)
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )

            bigMovieSearch.layoutParams = params
            popUp.contentView = bigMovieSearch
        popUp.showAtLocation(startRoot, Gravity.NO_GRAVITY, 0,0)*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.start_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId){
            R.id.app_bar_search ->
            {
                startActivity(Intent(this, MovieSearch::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    private fun updateMovies(movies: List<Movie>){
        movieGridAdapter.setMovies(movies)
    }

    private fun onSmallMovieClick(smallMovie: SmallMovieView) {
        smallMovie.expand(startRoot, startViewModel::removeMovie)
    }

    override fun onResume() {
        super.onResume()
        startViewModel.getMovies()
    }
}