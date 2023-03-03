package com.cyril.account.ui.history

import androidx.recyclerview.widget.DiffUtil

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