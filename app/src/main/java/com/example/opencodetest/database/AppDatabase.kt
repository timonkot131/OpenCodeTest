package com.example.opencodetest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.opencodetest.database.AppDatabase.Companion.DB_VERSION
import com.example.opencodetest.database.converters.StringArrayConverter
import com.example.opencodetest.database.daos.MovieDao
import com.example.opencodetest.database.entities.DatabaseMovie

@Database(entities = arrayOf(DatabaseMovie::class), version = DB_VERSION)
@TypeConverters(StringArrayConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "app_database"

        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            val tempInstance = instance

            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build()
                this.instance = instance
                return instance
            }
        }
    }
}