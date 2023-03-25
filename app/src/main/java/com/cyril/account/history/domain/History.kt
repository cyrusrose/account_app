package com.cyril.account.history.domain

import com.cyril.account.utils.UiText

data class History(
    val id: String,
    val title: String,
    val money: String,
    val content: UiText.StringResource,
    val time: String
)

