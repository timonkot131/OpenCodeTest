package com.example.opencodetest.database.converters

import androidx.room.TypeConverter

class StringArrayConverter {
    @TypeConverter
    fun fromString(stringListString: String?): Array<String>? {
        return stringListString?.split(",")?.map { it }?.toTypedArray()
    }

    @TypeConverter
    fun toString(stringList: Array<String>?): String? {
        return stringList?.joinToString(separator = ",")
    }
}