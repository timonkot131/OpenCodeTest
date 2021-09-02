package com.example.opencodetest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.widget.SlidingDrawer
import androidx.lifecycle.ViewModel

import com.example.opencodetest.viewmodels.StartViewModel

class MovieSearch : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_search)

    }
}