package com.cyril.account.rest.user.client

import com.cyril.account.rest.RetrofitClient
import com.cyril.account.rest.personal.PersonalResp
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigInteger
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "clss")
@JsonTypeName("corporate")
class CorporateResp(
    id: UUID,
    defaultAccount: PersonalResp?,
    val name: String,
    val nameRu: String?,
    val clientSsn: BigInteger
) : ClientResp(id, defaultAccount) {
}