package com.example.opencodetest.utility

//стандартный котлиновский резулт нельзя возвращать + он мне не нравится,
//так как нельзя нормально определить свой тип под ошибки и удобно матчить

sealed class Res<in T, in TError>
data class ResOk<T, TError> (val value: T) : Res<T, TError>()
data class ResError<T, TError> (val value: TError) : Res<T, TError>()

fun <T,U, TError> Res<T, TError>.map(mapper: (T) -> U): Res<U, TError> {
    if(this is ResOk){
        return ResOk(mapper(this.value))
    }
    else {
        return this as Res<U, TError>
    }
}

fun <T,U, TError> Res<T, TError>.bind(mapper: (T) -> Res<U, TError>): Res<U, TError> {
    if(this is ResOk){
        return (mapper(this.value))
    }
    else {
        return this as Res<U, TError>
    }
}