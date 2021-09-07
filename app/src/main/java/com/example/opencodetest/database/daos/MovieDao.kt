package com.example.opencodetest.database.daos

import androidx.room.*
import com.example.opencodetest.database.entities.DatabaseMovie

@Dao
interface MovieDao {
    @Query("SELECT * FROM databasemovie")
    fun getMovies(): Array<DatabaseMovie>

    @Delete
    fun removeMovie(movie: DatabaseMovie)

    @Insert
    fun addMovie(movie: DatabaseMovie)

    @Update
    fun updateMovie(movie: DatabaseMovie)

    @Query("SELECT * FROM databasemovie WHERE id = :id")
    fun getMovie(id: Int): DatabaseMovie
}