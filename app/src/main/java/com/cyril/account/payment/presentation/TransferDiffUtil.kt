package com.cyril.account.payment.presentation

import androidx.recyclerview.widget.DiffUtil
import com.cyril.account.payment.domain.Title
import com.cyril.account.payment.domain.Transfer
import com.cyril.account.payment.domain.TransferType

class TransferDiffUtil: DiffUtil.ItemCallback<TransferType>() {
    override fun areItemsTheSame(oldItem: TransferType, newItem: TransferType) = when {
        oldItem is Transfer && newItem is Transfer -> oldItem.id == newItem.id
        oldItem is Title && newItem is Title -> oldItem == newItem
        else -> oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: TransferType, newItem: TransferType) =
        oldItem == newItem
}