package com.cyril.account.rest.personal

import com.cyril.account.rest.RetrofitClient
import java.math.BigDecimal

class CurrencyResp(val code: Int, val letterCode: String, val rate: BigDecimal) {
    override fun toString(): String {
        return RetrofitClient.mapper.writeValueAsString(this)
    }
}