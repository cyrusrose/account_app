package com.cyril.account.rest.user.client

import com.cyril.account.rest.RetrofitClient
import com.cyril.account.rest.personal.PersonalResp
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "clss")
@JsonTypeName("customer")
class CustomerResp(
    id: UUID,
    defaultAccount: PersonalResp?,
    val name: String,
    val surname: String,
    val phone: Phone
) : ClientResp(id, defaultAccount) {
}