package com.cyril.account.rest

sealed class Result {
    class Loading: Result()
    class Success <T>(val value: T): Result()
    class Failure <T>(val value: T, val error: String): Result()
}
