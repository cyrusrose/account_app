package com.cyril.account.core.data

data class ErrorResp(
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)