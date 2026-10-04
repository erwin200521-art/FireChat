package com.example.firechat.util

/** Estado de una operación asíncrona expuesto por los ViewModels. */
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}
