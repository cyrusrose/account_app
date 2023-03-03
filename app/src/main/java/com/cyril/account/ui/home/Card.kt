package com.cyril.account.ui.home

import java.math.BigDecimal

data class Card(
    val id: String,
    val title: String,
    val content: String,
    val imageId: Int,
    val color: Int,
    val clss: String? = null,
    val minAmount: BigDecimal? = null,
    var isChecked: Boolean = false,
    var isDefault: Boolean = false
)