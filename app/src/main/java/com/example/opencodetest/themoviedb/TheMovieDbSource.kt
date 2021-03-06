package com.example.opencodetest.themoviedb

import android.content.Context
import android.util.Base64
import com.example.opencodetest.R
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
        val posterPath: String?,
        val description: String?,
        val year: Int?,
        val title: String,
        val id: Int,
        val score: Float?
    )

    data class MovieDetails(val runtime: Int?, val genres: Array<String>)

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
                val date = try {obj.getString("release_date") } catch (e: Throwable){ null }?.split("-")?.get(0)?.toIntOrNull()
                val description = try { obj.getString("overview") } catch (e: Throwable) { null }
                val poster: String? = obj.getString("poster_path")
                val score = try { obj.getDouble("vote_average") } catch (e: Throwable) { null }?.toFloat()
                yield(SearchResult(poster, description, date, title, id, score))
            }
        }.toList().toTypedArray()
    }

    private fun getDetailsFromString(string: String): MovieDetails {
        val root = JSONObject(string)
        val runtime = try { root.getInt("runtime") } catch(e: Throwable) { null }
        val genres = try { root.getJSONArray("genres") } catch (e: Throwable) { null }
        val genreNames = sequence {
            repeat(genres?.length() ?:0) {
                val obj = genres!!.getJSONObject(it)
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

    private fun getImage(imagePath: String?): Res<ByteArray?, MovieError> =
        try {
            if (imagePath == null)
                // ???? ?????????? ???????? ?????? ??????????????????, ???????? ?????????????????????? bytearray, ?????? option ??????.
                // ???????????? ??????, ???? ???????????? ????????????. Nullable ?? ??????????????, ?????? ?????? Option ???????????????????? ?? ??????????????????
                ResOk(null)
            else
            (URL("https://image.tmdb.org/t/p/w500" + imagePath).openConnection() as HttpURLConnection).run {
                connect()
                if (isSuccessful(responseCode)) {
                    ResOk(inputStream.readBytes())
                } else {
                    if (responseCode == 404)
                        ResOk(null)
                    else{
                        ResError(BadResponse(""))
                    }
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
                    getImage(res.posterPath).bindSus { bm -> //?????????????????? ?????????????????? ?? isActive ????????????????. ???????? ?????????????? ??????????????????, ???????????????????? ?????? ????????????????
                        getMovieCredits(res.id).bindSus { credits ->
                            getMovieDetails(res.id).mapSus { details ->
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
                searchRes.bindSus { res ->
                    getImage(res.posterPath).bindSus { bm ->
                        getMovieCredits(res.id).bindSus { credits ->
                            getMovieDetails(res.id).mapSus { details ->
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