package com.cyril.account.home.data.response

import com.cyril.account.core.utils.RetrofitUtils.mapper
import java.math.BigDecimal
import java.util.*

open class AccountResp(
    val id: UUID,
    val clss: String,
    val no: Int, val color: String,
    val content: String, val contentRu: String?,
    val title: String, val titleRu: String?,
    val anualInterestRate: BigDecimal,
    val monthsPeriod: Int?,
    val minAmount: BigDecimal?

) {
    override fun toString(): String {
        return mapper.writeValueAsString(this)
    }
}
