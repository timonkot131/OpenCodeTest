package com.example.opencodetest.utility

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive

//стандартный котлиновский резулт нельзя возвращать + он мне не нравится,
//так как нельзя нормально определить свой тип под ошибки и удобно матчить

sealed class Res<in T, in TError>
data class ResOk<T, TError> (val value: T) : Res<T, TError>()
data class ResError<T, TError> (val value: TError) : Res<T, TError>()

inline fun <T,U, TError> Res<T, TError>.map(mapper: (T) -> U): Res<U, TError> {
    if(this is ResOk){
        return ResOk(mapper(this.value))
    }
    else {
        return this as Res<U, TError>
    }
}

suspend inline fun <T,U, TError> Res<T, TError>.mapSus(crossinline mapper: suspend (T) -> U): Res<U, TError> {
    return coroutineScope {
        if(this@mapSus is ResOk && isActive){
            return@coroutineScope ResOk<U, TError>(mapper(this@mapSus.value))
        }
        else {
            return@coroutineScope this as Res<U, TError>
        }
    }
}

inline fun <T,U, TError> Res<T, TError>.bind(mapper: (T) -> Res<U, TError>): Res<U, TError> {
    if(this is ResOk){
        return (mapper(this.value))
    }
    else {
        return this as Res<U, TError>
    }
}

suspend inline fun <T,U, TError> Res<T, TError>.bindSus(crossinline mapper: suspend (T) -> Res<U, TError>): Res<U, TError> {
    return coroutineScope {
        if(this@bindSus is ResOk && isActive){
            return@coroutineScope (mapper(this@bindSus.value))
        }
        else {
            return@coroutineScope this@bindSus as Res<U, TError>
        }
    }
}

