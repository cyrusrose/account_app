package com.cyril.account.ui.history

sealed interface HistoryType

data class History(
    val id: String,
    val title: String,
    val money: String,
    val content: String,
    val time: String
): HistoryType

object Padding: HistoryType

