package com.cyril.account.rest.user.client

import com.cyril.account.rest.RetrofitClient
import java.math.BigInteger

class ClientNoResp(val clientNo: BigInteger, val clientSsn: BigInteger, val name: String, val nameRu: String?) {
    override fun toString(): String {
        return RetrofitClient.mapper.writeValueAsString(this)
    }
}