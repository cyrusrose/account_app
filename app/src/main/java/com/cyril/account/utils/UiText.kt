package com.cyril.account.utils

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @StringRes val id: Int,
        vararg val args: Any
    ) : UiText()

    fun asString(context: Context): String =
        when(this) {
            is DynamicString -> value
            is StringResource -> context.getString(
                id,
                args.map {
                    if (it is UiText)
                        it.asString(context)
                    else
                        it
                }
            )
        }
}