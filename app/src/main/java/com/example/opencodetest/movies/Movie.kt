package com.example.opencodetest.movies

import com.example.opencodetest.movies.networking.MovieError
import com.example.opencodetest.utility.Res

interface Movie
{
    val name: String
    val getMetadata: ((Res<MovieMetadata, MovieError>) -> Unit) -> Unit
}

