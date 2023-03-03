package com.cyril.account.core.data.response

import com.cyril.account.core.data.RetrofitClient
import java.math.BigInteger

class ClientNoResp(val clientNo: BigInteger, val clientSsn: BigInteger, val name: String, val nameRu: String?) {
    override fun toString(): String {
        return RetrofitClient.mapper.writeValueAsString(this)
    }
}