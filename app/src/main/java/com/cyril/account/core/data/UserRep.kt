package com.cyril.account.core.data

import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import com.cyril.account.payment.domain.Payment
import com.cyril.account.payment.domain.Transfer
import com.cyril.account.utils.DEBUG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.BufferedInputStream
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.util.*

class UserRep(
    private val userApi: UserApi,
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun getTransfers(config: Configuration, res: AssetManager) = coroutineScope {
        val gson = Gson()
        var sc: BufferedInputStream? = null
        val type: Type = object : TypeToken<List<Transfer>>() { }.type

        try {
            val file = when(config.locales[0].language) {
                Locale("ru").language -> "ru/transfers.json"
                else -> "en/transfers.json"
            }

            with(dispatcher) {
                sc = BufferedInputStream(res.open(file))
                val jsonString = sc!!.readBytes().toString(Charsets.UTF_8)
                gson.fromJson(jsonString, type)
            }

        } catch (e: Exception) {
            emptyList<Transfer>()
        }
        finally {
            with(dispatcher) {
                sc?.close()
            }
        }
    }

    fun getUser(login: String, password: String, refreshRate: Long = 6000) = flow {
        while(true) {
            val user = userApi.getUserWithClient(login, password)
            if(user.isSuccessful) {
                if (user.code() == HttpURLConnection.HTTP_OK) {
                    emit(user.body())
                } else {
                    Log.d(DEBUG, "Warning: UserRep.getUser")
                    emit(null)
                    break
                }
            } else {
                Log.d(DEBUG, "Error: UserRep.getUser: " + (user.errorBody()?.string() ?: "Unknown"))
                emit(null)
                break
            }
            delay(refreshRate)
        }
    }
        .conflate()

    fun getClientNos(ssn: String? = null, refreshRate: Long = 10000) = flow {
        while(true) {
            val nos = userApi.getClientNos(ssn)
            if(nos.isSuccessful) {
                if (nos.code() == HttpURLConnection.HTTP_OK) {
                    emit(nos.body()!!)
                } else {
                    Log.d(DEBUG, "Warning: UserRep.getClientNos, code ${nos.code()}")
                    emit(emptyList())
                }
            } else {
                Log.d(DEBUG, "Error: UserRep.getClientNos: " + (nos.errorBody()?.string() ?: "Unknown"))
                emit(emptyList())
            }
            delay(refreshRate)
        }
    }
        .conflate()

    fun getClientNosTOCards(ssn: String? = null, refreshRate: Long = 10000) =
        getClientNos(ssn, refreshRate).transform {
            if (it.isEmpty())
                emit(emptyList())
            else {
                val cards = ArrayList<Payment>()
                val config = Resources.getSystem().configuration.locales[0].language

                it.forEach {
                    val title = when(config) {
                        Locale("ru").language -> it.nameRu
                        else -> it.name
                    } ?: it.name

                    val card = Payment(
                        it.clientNo,
                        it.clientSsn,
                        title
                    )

                    cards.add(card)
                }

                emit(cards)
            }
        }
}