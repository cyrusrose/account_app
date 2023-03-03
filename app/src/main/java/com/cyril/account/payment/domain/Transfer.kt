package com.cyril.account.payment.domain

import java.math.BigInteger

sealed interface TransferType

data class Transfer(
    val id: Int,
    val title: String,
    val description: String
): TransferType

data class Payment(
    val clientNo: BigInteger,
    val clientSsn: BigInteger,
    val title: String
): TransferType

data class Title(val title: String): TransferType

object Padding : TransferType





