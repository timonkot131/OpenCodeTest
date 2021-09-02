package com.example.opencodetest.utility

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observe(owner: LifecycleOwner, func: (T) -> Unit) {
    observe(owner, Observer<T>(func))
}