package com.example.opencodetest.custom_views

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import com.example.opencodetest.R


fun arrayToCommaString(strings: Array<String>, take:  Int = 3): String =
    strings.take(take).joinToString(", ")

fun getBitmapFromBytes(byteArray: ByteArray) =
    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

fun getDurationFromMinutes(minutes: Int): String{
    val hours = minutes / 60
    val left = minutes % 60
    return hours.toString() + "ч " + left + "м"
}

fun get5StarsBy10Rating(rating: Float, context: Context): Array<Drawable> {
    val star5Rating = rating / 2
    val starcount =  Math.floor(star5Rating.toDouble()).toInt()
    val left = rating % 2
    var starResult = sequence {
        repeat(starcount) {
            yield(context.getDrawable(R.drawable.ic_baseline_star_24)!!)
        }
    }

    if(starResult.count() == 5) return starResult.toList().toTypedArray()

    starResult =
        if (left >= 0.5)
            starResult + context.getDrawable(R.drawable.ic_baseline_star_half_24)!!
        else
            starResult + context.getDrawable(R.drawable.ic_baseline_star_border_24)!!

    val starsLeft = 5 - starResult.count()
    return sequence {
            yieldAll(starResult)
            repeat(starsLeft) {
                yield(context.getDrawable(R.drawable.ic_baseline_star_border_24)!!)
            }
        }.toList().toTypedArray()
}