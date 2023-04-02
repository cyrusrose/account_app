package com.cyril.account.core.data.response

data class ErrorResp(
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)