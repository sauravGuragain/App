package com.fmcg.app.util

/**
 * Uniform result wrapper passed from the data layer up to the UI.
 * Every repository call returns one of these instead of throwing.
 */
sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}
