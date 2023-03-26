package com.cyril.account.utils

import android.content.Context
import androidx.annotation.StringRes
import com.cyril.account.history.presentation.HistoryViewModel

sealed class UiText {
    data class DynamicString(val value: String) : UiText() {
        fun asString() = value
    }
    class StringResource(
        @StringRes val id: Int,
        vararg val args: Any
    ) : UiText() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other === null) return false

            else return if (other is StringResource)
                id == other.id && args contentEquals other.args
            else false
        }
    }

    fun asString(context: Context): String =
        when(this) {
            is DynamicString -> value
            is StringResource -> context.getString(
                id,
                *args.map {
                    if (it is UiText)
                        it.asString(context)
                    else
                        it
                }.toTypedArray()
            )
        }
}