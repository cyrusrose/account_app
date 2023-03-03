package com.cyril.account.home.data.response

import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

@JsonTypeName("personal_deposit")
class PersonalDepositResp(
    id: UUID, money: BigDecimal,
    account: AccountResp, currency: CurrencyResp,
    val accountNo: BigInteger
): PersonalResp(id, money, account, currency)
