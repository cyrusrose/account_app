package com.cyril.account.core.data.response

import com.cyril.account.core.utils.RetrofitUtils.mapper
import java.math.BigInteger

class ClientNoResp(val clientNo: BigInteger, val clientSsn: BigInteger, val name: String, val nameRu: String?) {
    override fun toString(): String {
        return mapper.writeValueAsString(this)
    }
}