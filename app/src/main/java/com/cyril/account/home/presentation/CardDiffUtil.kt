package com.cyril.account.home.presentation

import androidx.recyclerview.widget.DiffUtil
import com.cyril.account.home.domain.Card

class CardDiffUtil: DiffUtil.ItemCallback<Card>() {
    override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
        return oldItem == newItem
    }
}