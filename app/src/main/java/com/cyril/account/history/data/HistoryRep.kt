package com.cyril.account.history.data

import android.content.res.Resources
import android.util.Log
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.R
import com.cyril.account.core.data.RetrofitClient
import com.cyril.account.history.domain.History
import com.cyril.account.core.presentation.BindableSpinnerAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import java.net.HttpURLConnection
import java.time.format.DateTimeFormatter
import java.util.*

class HistoryRep(
    private val histApi: HistoryApi,
    private val dispatcher: CoroutineDispatcher
) {
    fun getHistory(
        clientId: UUID,
        state: String? = null,
        via: String? = null,
        refreshRate: Long = 8000
    ) = flow {
        while (true) {
            val history = histApi.getHistory(clientId, state, via)
            if (history.isSuccessful) {
                if (history.code() == HttpURLConnection.HTTP_OK)
                    emit(history.body()!!)
                else {
                    emit(emptyList())
                    Log.d(MainActivity.DEBUG, "Warning HistoryRep, status ${history.code()}")
                }
            } else {
                Log.d(
                    MainActivity.DEBUG,
                    "Error HistoryRep: UserRep.getUser: " + (history.errorBody()?.string()
                        ?: "Unknown")
                )
                emit(emptyList())
            }
            delay(refreshRate)
        }
    }
        .conflate()

    suspend fun sendMoneyByClientNo(
        money: BigDecimal,
        senderAccountId: UUID,
        clientNo: BigInteger,
        clientSsn: BigInteger
    ) = histApi.sendMoneyByClientNo(money, senderAccountId, clientNo, clientSsn)

    suspend fun sendMoneyByPhone(
        money: BigDecimal,
        senderAccountId: UUID,
        phone: String
    ) = histApi.sendMoneyByPhone(money, senderAccountId, phone)

    suspend fun sendMoneyByCard(
        money: BigDecimal,
        senderAccountId: UUID,
        cardNo: BigInteger
    ) = histApi.sendMoneyByCard(money, senderAccountId, cardNo)

    suspend fun sendMoney(
        money: BigDecimal,
        senderAccountId: UUID,
        receiverAccountId: UUID
    ) = histApi.sendMoney(money, senderAccountId, receiverAccountId)

    suspend fun getTypes(res: Resources) = withContext(dispatcher) {
        buildList {
            res.getStringArray(R.array.value_array)
                .zip(res.getStringArray(R.array.type_array))
                .forEach {
                    add(BindableSpinnerAdapter.SpinnerItem(it.first, it.second))
                }
        }
    }

}