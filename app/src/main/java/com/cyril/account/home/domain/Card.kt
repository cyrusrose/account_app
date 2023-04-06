package com.cyril.account.home.domain

import com.cyril.account.utils.UiText
import java.math.BigDecimal

class Card(
    val id: String,
    val title: String,
    val content: String,
    val imageId: Int,
    val color: Int,
    val contentList: List<UiText>? = null,
    val clss: String? = null,
    val minAmount: BigDecimal? = null,
    var isChecked: Boolean = false,
    var isDefault: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Card

        if (id != other.id) return false
        if (title != other.title) return false
        if (content != other.content) return false
        if (imageId != other.imageId) return false
        if (color != other.color) return false
        if (contentList != other.contentList) return false
        if (clss != other.clss) return false
        if (minAmount != other.minAmount) return false
        // ignore if (isChecked != other.isChecked)
        if (isDefault != other.isDefault) return false

        return true
    }
}