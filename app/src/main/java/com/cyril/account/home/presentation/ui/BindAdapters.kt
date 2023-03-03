package com.cyril.account.home.presentation.ui

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.databinding.BindingAdapter
import com.cyril.account.R
import com.cyril.account.databinding.CardItemBinding
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.core.data.response.ClientResp
import com.cyril.account.core.data.response.CorporateResp
import com.cyril.account.core.data.response.CustomerResp
import java.util.*

@BindingAdapter("app:client_name")
fun clientName(view: TextView, client: ClientResp?) {
    val config = Resources.getSystem().configuration.locales[0].language

    if (client != null)
        view.text = when(client) {
            is CustomerResp -> "${client.surname} ${client.name}"
            is CorporateResp -> when(config) {
                Locale("ru").language -> client.nameRu
                else -> client.name
            } ?: client.name
            else -> null
        }
}

@BindingAdapter("app:client_phone")
fun clientPhone(view: TextView, client: ClientResp?) {
    val config = Resources.getSystem().configuration.locales[0].language

    if (client != null)
        view.text = when(client) {
            is CustomerResp -> client.phone.toString()
            else -> null
        }
}

@BindingAdapter("app:client_hide")
fun clientHide(view: View, client: ClientResp?) {
    if (client != null)
        view.visibility = when(client) {
            is CustomerResp ->  View.VISIBLE
            else -> View.GONE
        }
}

@BindingAdapter("app:personal")
fun personal(view: View, user: UserResp?) {
    val config = Resources.getSystem().configuration.locales[0].language

    with(CardItemBinding.bind(view.findViewById(R.id.card_item))) {
        val personal = user?.client?.defaultAccount
        if (personal != null) {
            val acc = personal.account
            val curr = personal.currency

            val title = when(config) {
                Locale("ru").language -> acc.titleRu
                else -> acc.title
            } ?: acc.title
            val content = "${personal.money} ${curr.letterCode}"


            this.cardNameTitle.text = title
            this.cardContent.text = content
            this.cardBackground.setBackgroundColor(acc.color.toColorInt())
            this.cardImage.setImageResource(R.drawable.name_svg)
        }
    }

}


