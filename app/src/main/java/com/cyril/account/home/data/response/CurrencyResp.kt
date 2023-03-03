package com.cyril.account.home.data.response

import com.cyril.account.core.data.RetrofitClient
import java.math.BigDecimal

class CurrencyResp(val code: Int, val letterCode: String, val rate: BigDecimal) {
    override fun toString(): String {
        return RetrofitClient.mapper.writeValueAsString(this)
    }
}