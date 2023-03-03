package com.cyril.account.core.data.response

import com.cyril.account.core.data.RetrofitClient.mapper
import java.util.UUID

data class UserResp(
    val id: UUID,
    val login: String,
    val password: String,
    val client: ClientResp
) {
    override fun toString(): String {
        return mapper.writeValueAsString(this)
    }
}