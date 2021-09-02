package com.example.opencodetest.movies.networking

sealed class MovieError
class ConnectionError(message: String) : MovieError()
class BadResponse(message: String) : MovieError()
class NotExisting(): MovieError()