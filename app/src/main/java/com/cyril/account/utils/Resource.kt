package com.cyril.account.utils

sealed class Resource<T>(val data: T? = null, val message: UiText? = null) {
    class Loading<T>: Resource<T>()
    class Success<T>(data: T?): Resource<T>(data)
    class Error<T>(message: UiText, data: T? = null): Resource<T>(data, message)
}