package com.cyril.account.history.domain

import android.content.res.Resources
import android.util.Log
import com.cyril.account.R
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.history.data.HistoryRep
import com.cyril.account.utils.Resource
import com.cyril.account.utils.UiText
import com.cyril.account.utils.timePattern
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.transform
import java.net.SocketTimeoutException
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class ToCardsUseCase @Inject constructor(
    private val rep: HistoryRep
) {
    operator fun invoke(clientId: UUID, state: String? = null, via: String? = null, refreshRate: Long = 8000) =
        rep.getHistory(clientId, state, via, refreshRate)
        .transform {
            if (it.isEmpty())
                emit(emptyList<History>())
            else {
                val cards = ArrayList<History>()
                val config = Resources.getSystem().configuration.locales[0].language

                it.forEach {
                    val title = when(config) {
                        Locale("ru").language -> it.titleRu
                        else -> it.title
                    } ?: it.title

                    val content = UiText.StringResource(R.string.content_title, it.via)
                    val money = "${it.money} ${it.currency.letterCode}"

                    val card = History(
                        id = it.id.toString(),
                        title = title,
                        content = content,
                        money = money,
                        time = it.time.format(DateTimeFormatter.ofPattern(timePattern))
                    )

                    cards.add(card)
                }

                emit(cards)
            }
        }
}