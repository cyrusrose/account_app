package com.cyril.account.core.data.response

import com.cyril.account.core.data.utils.Phone
import com.cyril.account.home.data.response.PersonalResp
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