package com.example.opencodetest

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.opencodetest.adapters.MovieGridAdapter
import com.example.opencodetest.custom_views.SmallMovieView
import com.example.opencodetest.database.AppDatabase
import com.example.opencodetest.database.entities.DatabaseMovie
import com.example.opencodetest.movies.Movie
import com.example.opencodetest.utility.observe
import com.example.opencodetest.viewmodels.StartViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {

    private val startViewModel: StartViewModel by viewModels()
    private val movieGridAdapter = MovieGridAdapter(this, ::onSmallMovieClick, ::onPopupClose)
    private var popupOwner: SmallMovieView? = null
    val scope = MainScope()

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


        val prefs = getPreferences(MODE_PRIVATE)
        if (!prefs.contains(FIRST_TIME)) {
            val db = AppDatabase.getDatabase(this)
            scope.launch(Dispatchers.IO) {
                db.movieDao().addMovie(DatabaseMovie(0, "A Dog's Journey", null))
                db.movieDao().addMovie(DatabaseMovie(0, "Blade Runner 2049", null))
                db.movieDao().addMovie(DatabaseMovie(0, "Yes, God, Yes", null))
                db.movieDao().addMovie(DatabaseMovie(0, "Drive", null))
            }
            prefs.edit().putBoolean(FIRST_TIME, true).apply()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.start_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.app_bar_search -> {
                startActivity(Intent(this, MovieSearch::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    override fun onBackPressed() {
        if (popupOwner != null) {
            popupOwner!!.dismissPopup() // при ребаинде убирает попап с экрана
            popupOwner = null
        } else {
            finish()
        }
    }

    private fun onPopupClose() {
        popupOwner?.dismissPopup()
        popupOwner = null
    }

    private fun updateMovies(movies: List<Movie>) {
        movieGridAdapter.setMovies(movies)
        startMovieGrid.invalidateViews()
    }

    private fun onSmallMovieClick(index: Int, smallMovie: SmallMovieView) {
        if (startAccountSlider.isOpened)
            return

        this.popupOwner = smallMovie
        smallMovie.expand(startRoot) {
            movieGridAdapter.removeMovie(index)
            popupOwner?.dismissPopup()
            startViewModel.removeMovie(it)
            startMovieGrid.invalidateViews()
        }
    }

    override fun onResume() {
        startViewModel.getMovies()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        popupOwner?.dismissPopup()
    }

    companion object {
        const val FIRST_TIME = "firstTime"
    }

}