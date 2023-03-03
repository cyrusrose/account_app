package com.cyril.account.ui.payment

import androidx.recyclerview.widget.DiffUtil

class TransferDiffUtil: DiffUtil.ItemCallback<TransferType>() {
    override fun areItemsTheSame(oldItem: TransferType, newItem: TransferType): Boolean {
        return when {
            oldItem is Transfer && newItem is Transfer -> oldItem.id == newItem.id
            oldItem is Title && newItem is Title -> oldItem == newItem
            else -> oldItem == newItem
        }
    }

    override fun areContentsTheSame(oldItem: TransferType, newItem: TransferType): Boolean {
        return oldItem == newItem
    }
}