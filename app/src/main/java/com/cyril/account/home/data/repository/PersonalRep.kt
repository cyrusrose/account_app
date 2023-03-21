package com.cyril.account.home.data.repository

import android.content.res.Resources
import android.util.Log
import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel.UserError
import com.cyril.account.R
import com.cyril.account.core.data.RetrofitClient
import com.cyril.account.home.data.api.PersonalApi
import com.cyril.account.home.data.utils.CardTypes
import com.cyril.account.core.data.response.ClientResp
import com.cyril.account.core.presentation.BindableSpinnerAdapter
import com.cyril.account.home.data.response.*
import com.cyril.account.home.domain.Card
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.util.*

class PersonalRep() {
    val personalApi = RetrofitClient.get().create(PersonalApi::class.java)

    fun getPersonals(clientId: UUID, refreshRate: Long = 8000) = flow {
        Log.d(MainActivity.DEBUG, "Here PersonalRep")
        while(true) {
            Log.d(MainActivity.DEBUG, "While PersonalRep")

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
        .flowOn(Dispatchers.Default)
        .conflate()

    fun getPersonalsToCards(client: ClientResp, cardEmpty: List<Card>, refreshRate: Long = 8000) =
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

    fun getPersonalsToCardsFlat(client: ClientResp, cardEmpty: List<Card>, refreshRate: Long = 8000) =
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

    private fun buildCard(config: String, it: AccountResp, res: Resources): Card {
        val title = when(config) {
            Locale("ru").language -> it.titleRu
            else -> it.title
        } ?: it.title
        val content = buildList {
            add(when(config) {
                Locale("ru").language -> it.contentRu
                else -> it.content
            } ?: it.content)
            add(res.getString(R.string.interest_title, it.anualInterestRate))
            it.minAmount?.let {
                add(res.getString(R.string.min_sum_title, it))
                add("USD")
            }
            it.monthsPeriod?.let {
                add(res.getString(R.string.months_title, it.toString()))
            }
        }
        .joinToString(separator = " ")

        return Card(
            id = it.id.toString(),
            clss = it.clss,
            title = title,
            content = content,
            imageId = R.drawable.name_svg,
            color = it.color.toColorInt(),
            minAmount = it.minAmount
        )
    }

    suspend fun delPersonal(id: UUID, persId: UUID) = withContext(Dispatchers.Default) {
        personalApi.delPersonal(id, persId)
    }

    suspend fun changeDefault(id: UUID, persId: UUID) = withContext(Dispatchers.Default) {
        personalApi.changeDefault(id, persId)
    }

    suspend fun addCard(
        clientId: UUID,
        accountId: UUID,
        code: Int,
        money: BigDecimal? = null,
        senderAccountId: UUID? = null
    ) = withContext(Dispatchers.Default) {
        personalApi.addCard(clientId, accountId, code, money, senderAccountId)
    }

    fun getAccounts(refreshRate: Long = 8000) = flow {
        Log.d(MainActivity.DEBUG, "Here PersonalRep")
        while(true) {
            Log.d(MainActivity.DEBUG, "While PersonalRep")

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
        .flowOn(Dispatchers.Default)
        .conflate()

    fun getAccountsToCards(res: Resources, cardEmpty: List<Card>, refreshRate: Long = 8000) =
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
                    val card = buildCard(config, it, res)

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

    suspend fun getCurrenciesToCards(res: Resources, error: MutableLiveData<UserError>) = coroutineScope {
        buildList {
            val ans = withContext(Dispatchers.Default) {
                getCurrencies()
            }
            if (ans.isSuccessful)
                ans.body()?.let {
                    it.forEach {
                        add(BindableSpinnerAdapter.SpinnerItem(it.code.toString(), it.letterCode))
                    }
                }
            else
                error.value = UserError(res.getString(R.string.codes_error))
        }
    }

    suspend fun convert(
        fromCode: Int,
        toCode: Int,
        sum: BigDecimal,
        res: Resources,
        error: MutableLiveData<UserError>
    ) = coroutineScope {
        val ans = withContext(Dispatchers.Default) {
            personalApi.convert(fromCode, toCode, sum)
        }

        if (ans.isSuccessful)
            ans.body()
        else {
            error.value = UserError(res.getString(R.string.codes_error))
            null
        }
    }
}
