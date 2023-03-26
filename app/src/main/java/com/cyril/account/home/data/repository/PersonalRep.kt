package com.cyril.account.home.data.repository

import android.content.res.Resources
import android.util.Log
import androidx.core.graphics.toColorInt
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.R
import com.cyril.account.home.data.api.PersonalApi
import com.cyril.account.home.data.utils.CardTypes
import com.cyril.account.core.data.response.ClientResp
import com.cyril.account.core.presentation.BindableSpinnerAdapter
import com.cyril.account.home.data.response.*
import com.cyril.account.home.domain.Card
import com.cyril.account.utils.Resource
import com.cyril.account.utils.UiText
import com.cyril.account.utils.cardEmpty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.util.*

class PersonalRep(
    private val personalApi: PersonalApi,
    private val dispatcher: CoroutineDispatcher
) {
    fun getPersonals(clientId: UUID, refreshRate: Long = 8000) = flow {
        while(true) {
            val personals = personalApi.getPersonal(clientId)
            if(personals.isSuccessful) {
                if (personals.code() == HttpURLConnection.HTTP_OK)
                    emit(personals.body()!!)
                else
                    emit(emptyList())
                Log.d(MainActivity.DEBUG, "Emit PersonalRep, status ${personals.code()}")
            } else {
                Log.d(MainActivity.DEBUG, "Error PersonalRep: UserRep.getUser: " + (personals.errorBody()?.string() ?: "Unknown"))
                emit(emptyList())
            }
            delay(refreshRate)
        }
    }
        .conflate()

    fun getPersonalsToCards(client: ClientResp, refreshRate: Long = 8000) =
        getPersonals(client.id, refreshRate)
        .transform {
            if (it.isEmpty())
                emit(CardTypes(cardEmpty, cardEmpty, cardEmpty))
            else {
                val cards = ArrayList<Card>()
                val deposits = ArrayList<Card>()
                val clientAccs = ArrayList<Card>()

                val config = Resources.getSystem().configuration.locales[0].language

                it.forEach {
                    val card = buildCard(config, it, client.defaultAccount)

                    when(it) {
                        is PersonalCardResp -> cards.add(card)
                        is PersonalDepositResp -> deposits.add(card)
                        is PersonalClientResp -> clientAccs.add(card)
                    }
                }

                emit(CardTypes(
                    cards.ifEmpty { cardEmpty },
                    deposits.ifEmpty { cardEmpty },
                    clientAccs.ifEmpty { cardEmpty }
                ))
            }
        }

    fun getPersonalsToCardsFlat(client: ClientResp, refreshRate: Long = 8000) =
        getPersonals(client.id, refreshRate)
            .transform {
                if (it.isEmpty())
                    emit(cardEmpty)
                else {
                    val cards = ArrayList<Card>()

                    val config = Resources.getSystem().configuration.locales[0].language

                    it.forEach {
                        val card = buildCard(config, it, client.defaultAccount)
                        cards.add(card)
                    }

                    emit( cards.ifEmpty { cardEmpty } )
                }
            }

    private fun buildCard(config: String, it: PersonalResp, default: PersonalResp?): Card {
        val title = when(config) {
            Locale("ru").language -> it.account.titleRu
            else -> it.account.title
        } ?: it.account.title
        val content = "${it.money} ${it.currency.letterCode}"

        return Card(
            id = it.id.toString(),
            title = title,
            content = content,
            imageId = R.drawable.name_svg,
            color = it.account.color.toColorInt(),
            isDefault = (it.id == default?.id)
        )
    }

    private fun buildCard(config: String, it: AccountResp): Card {
        val title = when(config) {
            Locale("ru").language -> it.titleRu
            else -> it.title
        } ?: it.title
        val content = buildList {
            add(UiText.DynamicString(
                when(config) {
                    Locale("ru").language -> it.contentRu
                    else -> it.content
                } ?: it.content
            ))
            add(UiText.StringResource(R.string.interest_title, it.anualInterestRate))
            it.minAmount?.let {
                add(UiText.StringResource(R.string.min_sum_title, it))
                add(UiText.DynamicString("USD"))
            }
            it.monthsPeriod?.let {
                add(UiText.StringResource(R.string.months_title, it.toString()))
            }
        }

        return Card(
            id = it.id.toString(),
            clss = it.clss,
            title = title,
            content = "",
            contentList = content,
            imageId = R.drawable.name_svg,
            color = it.color.toColorInt(),
            minAmount = it.minAmount
        )
    }

    suspend fun delPersonal(id: UUID, persId: UUID) = withContext(dispatcher) {
        personalApi.delPersonal(id, persId)
    }

    suspend fun changeDefault(id: UUID, persId: UUID) = withContext(dispatcher) {
        personalApi.changeDefault(id, persId)
    }

    suspend fun addCard(
        clientId: UUID,
        accountId: UUID,
        code: Int,
        money: BigDecimal? = null,
        senderAccountId: UUID? = null
    ) = withContext(dispatcher) {
        personalApi.addCard(clientId, accountId, code, money, senderAccountId)
    }

    fun getAccounts(refreshRate: Long = 8000) = flow {
        while(true) {
            val personals = personalApi.getAccounts()
            if(personals.isSuccessful) {
                if (personals.code() == HttpURLConnection.HTTP_OK)
                    emit(personals.body()!!)
                else
                    emit(emptyList())
                Log.d(MainActivity.DEBUG, "Emit PersonalRep, status ${personals.code()}")
            } else {
                Log.d(MainActivity.DEBUG, "Error PersonalRep: UserRep.getUser: " + (personals.errorBody()?.string() ?: "Unknown"))
                emit(emptyList())
            }
            delay(refreshRate)
        }
    }
        .conflate()

    fun getAccountsToCards(refreshRate: Long = 8000) =
        getAccounts(refreshRate)
        .transform {
            if (it.isEmpty())
                emit(CardTypes(cardEmpty, cardEmpty, cardEmpty))
            else {
                val cards = ArrayList<Card>()
                val deposits = ArrayList<Card>()
                val clientAccs = ArrayList<Card>()

                val config = Resources.getSystem().configuration.locales[0].language

                it.forEach {
                    val card = buildCard(config, it)

                    when(it.clss) {
                        "card" -> cards.add(card)
                        "deposit" -> deposits.add(card)
                        "client_account" -> clientAccs.add(card)
                    }
                }

                emit(CardTypes(
                    cards.ifEmpty { cardEmpty },
                    deposits.ifEmpty { cardEmpty },
                    clientAccs.ifEmpty { cardEmpty }
                ))
            }
        }

    suspend fun getCurrencies() = personalApi.getCurrencies()

    suspend fun getCurrenciesToCards() = coroutineScope {
        val ans = withContext(dispatcher) {
            getCurrencies()
        }

        if (ans.isSuccessful) Resource.Success(buildList {
            ans.body()?.let {
                it.forEach {
                    add(BindableSpinnerAdapter.SpinnerItem(it.code.toString(), it.letterCode))
                }
            }
        })
        else
            Resource.Error(UiText.StringResource(R.string.codes_error))

    }

    suspend fun convert(
        fromCode: Int,
        toCode: Int,
        sum: BigDecimal
    ) = coroutineScope {
        val ans = withContext(dispatcher) {
            personalApi.convert(fromCode, toCode, sum)
        }

        if (ans.isSuccessful)
            Resource.Success(ans.body())
        else
            Resource.Error(UiText.StringResource(R.string.codes_error))
    }
}
