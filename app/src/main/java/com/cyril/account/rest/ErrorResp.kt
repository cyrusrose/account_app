package com.cyril.account.rest

data class ErrorResp(
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)