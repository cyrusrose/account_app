package com.cyril.account.home.data.response

import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

@JsonTypeName("personal_card")
class PersonalCardResp(
    id: UUID, money: BigDecimal,
    account: AccountResp, currency: CurrencyResp,
    val cardNo: BigInteger, val cvc: Int,
    val month: Int, val year: Int
): PersonalResp(id, money, account, currency)
