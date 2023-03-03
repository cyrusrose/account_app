package com.cyril.account.history.domain

sealed interface HistoryType

data class History(
    val id: String,
    val title: String,
    val money: String,
    val content: String,
    val time: String
): HistoryType

object Padding: HistoryType

