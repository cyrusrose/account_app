package com.cyril.account.core.data.response

import com.cyril.account.core.data.RetrofitClient
import com.cyril.account.home.data.response.PersonalResp
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "clss")
@JsonSubTypes(
    JsonSubTypes.Type(value = CustomerResp::class),
    JsonSubTypes.Type(value = CorporateResp::class)
)
@JsonTypeName("client")
open class ClientResp(
    val id: UUID,
    val defaultAccount: PersonalResp?
) {
    override fun toString(): String {
        return RetrofitClient.mapper.writeValueAsString(this)
    }
}