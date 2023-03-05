package com.cyril.account.history.presentation

import androidx.recyclerview.widget.DiffUtil
import com.cyril.account.history.domain.History

class HistoryDiffUtil: DiffUtil.ItemCallback<History>() {
    override fun areItemsTheSame(oldItem: History, newItem: History) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: History, newItem: History) =
        oldItem == newItem
}