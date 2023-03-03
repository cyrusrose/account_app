package com.cyril.account.history.presentation.ui

import androidx.recyclerview.widget.DiffUtil
import com.cyril.account.history.domain.History
import com.cyril.account.history.domain.HistoryType

class HistoryDiffUtil: DiffUtil.ItemCallback<HistoryType>() {
    override fun areItemsTheSame(oldItem: HistoryType, newItem: HistoryType): Boolean {
        return when {
            oldItem is History && newItem is History -> oldItem.id == newItem.id
            else -> oldItem == newItem
        }

    }

    override fun areContentsTheSame(oldItem: HistoryType, newItem: HistoryType): Boolean {
        return oldItem == newItem
    }
}