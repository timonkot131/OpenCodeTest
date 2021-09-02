package com.example.opencodetest.themoviedb

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.opencodetest.R
import com.example.opencodetest.movies.networking.DownloadingMovie
import com.example.opencodetest.movies.MovieMetadata
import com.example.opencodetest.movies.networking.*
import com.example.opencodetest.utility.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class TheMovieDbSource(context: Context) : MovieSource {

    val scope = MainScope()

    val magicWord = decodeMagicWord(context.getString(R.string.magicWord))

    private fun decodeMagicWord(word: String) =
        Base64.decode(word, Base64.DEFAULT).toString(Charsets.UTF_8)

    private fun isSuccessful(code: Int) = code in 200..299

    data class SearchResult(
        val posterPath: String,
        val description: String,
        val year: Int,
        val title: String,
        val id: Int,
        val score: Float
    )

    data class MovieDetails(val runtime: Int, val genres: Array<String>)

    data class Credits(val actors: Array<String>, val directors: Array<String>)

    private fun getSearchResultsFromString(string: String): Array<SearchResult> {
        val root = JSONObject(string)
        val results = root.getJSONArray("results")

        if (results.length() == 0) return arrayOf()

        return sequence {
            repeat(results.length()) {
                val obj = results.getJSONObject(it)
                val id = obj.getInt("id")
                val title = obj.getString("title")
                val date = obj.getString("release_date").split("-")[0].toInt()
                val description = obj.getString("overview")
                val poster = obj.getString("poster_path")
                val score = obj.getDouble("vote_average").toFloat()
                yield(SearchResult(poster, description, date, title, id, score))
            }
        }.toList().toTypedArray()
    }

    private fun getDetailsFromString(string: String): MovieDetails {
        val root = JSONObject(string)
        val runtime = root.getInt("runtime")
        val genres = root.getJSONArray("genres")
        val genreNames = sequence {
            repeat(genres.length()) {
                val obj = genres.getJSONObject(it)
                yield(obj.getString("name"))
            }
        }.toList().toTypedArray()

        return MovieDetails(runtime, genreNames)
    }

    private fun getCreditsFromString(string: String): Credits {
        val root = JSONObject(string)
        val cast = root.getJSONArray("cast")
        val crew = root.getJSONArray("crew")

        val actors = sequence {
            repeat(cast.length()) {
                val obj = cast.getJSONObject(it)
                yield(obj.getString("name"))
            }
        }.toList().toTypedArray()

        val directors = sequence {
            repeat(crew.length()) {
                val obj = crew.getJSONObject(it)
                if (obj.getString("job") == "Director")
                    yield(obj.getString("name"))
            }
        }.toList().toTypedArray()

        return Credits(actors, directors)
    }

    private fun getImage(imagePath: String): Res<ByteArray, MovieError> =
        try {
            (URL("https://image.tmdb.org/t/p/w500" + imagePath).openConnection() as HttpURLConnection).run {
                connect()
                if (isSuccessful(responseCode)) {
                    ResOk(inputStream.readBytes())
                } else {
                    throw Throwable(errorStream.toString())
                    ResError(BadResponse(""))
                }
            }
        } catch (e: IOException) {
            ResError(ConnectionError(""))
        }


    private fun searchMovie(movie: String): Res<Array<SearchResult>, MovieError> =
        try {
            (URL("https://api.themoviedb.org/3/search/movie?api_key=" + magicWord + "&query=" + movie).openConnection() as HttpURLConnection).run {
                connect()
                if (isSuccessful(responseCode)) {
                    val result = getSearchResultsFromString(inputStream.bufferedReader().readText())
                    ResOk(result)
                } else {
                    throw Throwable(errorStream.toString())
                    ResError(BadResponse(""))
                }
            }
        } catch (e: IOException) {
            ResError(ConnectionError(""))
        }

    private fun getMovieCredits(movieId: Int): Res<Credits, MovieError> =
        try {
            (URL("https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + magicWord).openConnection() as HttpURLConnection).run {
                connect()
                if (isSuccessful(responseCode)) {
                    val result = getCreditsFromString(inputStream.bufferedReader().readText())
                    ResOk(result)
                } else {
                    throw Throwable(errorStream.toString())
                    ResError(BadResponse(""))
                }
            }
        } catch (e: IOException) {
            ResError(ConnectionError(""))
        }

    private fun getMovieDetails(movieId: Int): Res<MovieDetails, MovieError> =
        try {
            (URL("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + magicWord).openConnection() as HttpURLConnection).run {
                connect()
                if (isSuccessful(responseCode)) {
                    val result = getDetailsFromString(inputStream.bufferedReader().readText())
                    ResOk(result)
                } else {
                    throw Throwable(errorStream.toString())
                    ResError(BadResponse(""))
                }
            }
        } catch (e: IOException) {
            ResError(ConnectionError(""))
        }

    override fun searchMovies(movie: String): Res<List<DownloadingMovie>, MovieError> =
        searchMovie(movie).map { searchResults ->
            searchResults.map { res ->
                val title = res.title

                val metadata = scope.async(Dispatchers.IO) {
                    getImage(res.posterPath).bind { bm ->
                        getMovieCredits(res.id).bind { credits ->
                            getMovieDetails(res.id).map { details ->
                                MovieMetadata (
                                   res.year,
                                   credits.directors,
                                   details.genres,
                                   credits.actors,
                                   details.runtime,
                                   res.score,
                                   res.description,
                                   bm
                               )
                            }
                        }
                    }
                }
                return@map DownloadingMovie(title, metadata)
            }
        }

    override fun getDetails(movie: String): Res<DownloadingMovie, MovieError> {
        val searchRes = searchMovie(movie).map { it[0] }
        val title = searchRes.map { it.title }
        val metadata =
            scope.async(Dispatchers.IO) {
                searchRes.bind { res ->
                    getImage(res.posterPath).bind { bm ->
                        getMovieCredits(res.id).bind { credits ->
                            getMovieDetails(res.id).map { details ->
                                MovieMetadata(
                                    res.year,
                                    credits.directors,
                                    details.genres,
                                    credits.actors,
                                    details.runtime,
                                    res.score,
                                    res.description,
                                    bm
                                )
                            }
                        }
                    }
                }
            }
        return title.map { t ->
            DownloadingMovie(t, metadata)
        }
    }

}