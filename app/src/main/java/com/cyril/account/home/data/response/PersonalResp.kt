package com.cyril.account.home.data.response

import com.cyril.account.core.utils.RetrofitUtils.mapper
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "clss")
@JsonSubTypes(
    JsonSubTypes.Type(value = PersonalCardResp::class),
    JsonSubTypes.Type(value = PersonalDepositResp::class),
    JsonSubTypes.Type(value = PersonalClientResp::class)
)
@JsonTypeName("personal")
open class PersonalResp(
    val id: UUID,
    val money: BigDecimal,
    val account: AccountResp,
    val currency: CurrencyResp
) {
    override fun toString(): String {
        return mapper.writeValueAsString(this)
    }
}