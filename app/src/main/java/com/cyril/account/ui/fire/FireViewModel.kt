package com.cyril.account.ui.fire

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.cyril.account.MainActivity
import com.cyril.account.MainViewModel
import com.cyril.account.R
import com.cyril.account.rest.ErrorResp
import com.cyril.account.rest.RetrofitClient
import com.cyril.account.rest.history.HistoryRep
import com.cyril.account.rest.personal.PersonalRep
import com.cyril.account.rest.user.UserResp
import com.cyril.account.ui.home.Card
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import java.net.SocketTimeoutException
import java.util.*

class FireViewModel(private val app: Application) : AndroidViewModel(app) {
    val empty = ""
    private val cardEmpty = listOf(
        Card(empty, "", "", R.drawable.name_svg, app.resources.getColor(R.color.light_grey))
    )

    private val usersState = MutableStateFlow<UserResp?>(null)
    val user: LiveData<UserResp> = usersState.filterNotNull().asLiveData()

    private val _error = MutableLiveData<MainViewModel.UserError>()
    val error: LiveData<MainViewModel.UserError> = _error

    private val histRep = HistoryRep()
    private val personalRep = PersonalRep()

    val card = usersState.flatMapLatest {
        if (it == null) {
            flow {
                emit(cardEmpty)
            }
        } else
            personalRep.getPersonalsToCardsFlat(it.client, cardEmpty)
                .retry {
                    val time = it is SocketTimeoutException
                    if (time) {
                        delay(5000)
                        _error.value = MainViewModel.UserError(app.resources.getString(R.string.trying_error))
                        Log.d(MainActivity.DEBUG, it.message ?: "")
                    }
                    time
                }.catch { e ->
                    _error.value = MainViewModel.UserError(app.resources.getString(R.string.working_error))
                    Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
                }
    }
        .asLiveData()

    fun sendMoneyByClientNo(
        money: BigDecimal,
        senderAccountId: UUID,
        clientNo: BigInteger,
        clientSsn: BigInteger
    ) {
        viewModelScope.launch {
            try {
                val it = histRep.sendMoneyByClientNo(money, senderAccountId, clientNo, clientSsn)
                if (!it.isSuccessful) {
                    Log.d(MainActivity.DEBUG, it.errorBody()?.string() ?: "Unknown")
                    _error.value = MainViewModel.UserError(app.resources.getString(R.string.sending_error))
                }
            } catch (e: Exception) {
                _error.value = MainViewModel.UserError(app.resources.getString(R.string.working_error))
                Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
            }
        }
    }

    fun sendMoneyByPhone(
        money: BigDecimal,
        senderAccountId: UUID,
        phone: String
    ) {
        viewModelScope.launch {
            try {
                val it = histRep.sendMoneyByPhone(money, senderAccountId, phone)
                if (!it.isSuccessful) {
                    Log.d(MainActivity.DEBUG, it.errorBody()?.string() ?: "Unknown")
                    _error.value = MainViewModel.UserError(app.resources.getString(R.string.sending_error))
                }
            } catch (e: Exception) {
                _error.value = MainViewModel.UserError(app.resources.getString(R.string.working_error))
                Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
            }
        }
    }

    fun sendMoneyByCard(
        money: BigDecimal,
        senderAccountId: UUID,
        cardNo: BigInteger
    ) {
        viewModelScope.launch {
            try {
                val it = histRep.sendMoneyByCard(money, senderAccountId, cardNo)
                if (!it.isSuccessful) {
                    val ans = it.errorBody()?.string()
                    Log.d(MainActivity.DEBUG, ans ?: "Unknown")
                    if (ans != null) {
                        val resp = RetrofitClient.mapper.readValue(ans, ErrorResp::class.java)
                        if (resp.message.contains("same account"))
                            _error.value = MainViewModel.UserError(app.resources.getString(R.string.same_error))
                        else if (resp.message.contains("receiver's card"))
                            _error.value = MainViewModel.UserError(app.resources.getString(R.string.receiver_error))
                        else
                            _error.value = MainViewModel.UserError(app.resources.getString(R.string.sending_error))
                    } else
                        _error.value = MainViewModel.UserError(app.resources.getString(R.string.sending_error))
                }
            } catch (e: Exception) {
                _error.value = MainViewModel.UserError(app.resources.getString(R.string.working_error))
                Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
            }
        }
    }

    fun sendMoney(
        money: BigDecimal,
        senderAccountId: UUID,
        receiverAccountId: UUID
    ) {
        viewModelScope.launch {
            try {
                val it = histRep.sendMoney(money, senderAccountId, receiverAccountId)
                if (!it.isSuccessful) {
                    val ans = it.errorBody()?.string()
                    Log.d(MainActivity.DEBUG, ans ?: "Unknown")
                    
                    if (ans != null) {
                        val resp = RetrofitClient.mapper.readValue(ans, ErrorResp::class.java)
                        if (resp.message.contains("same account"))
                            _error.value = MainViewModel.UserError(app.resources.getString(R.string.same_error))
                        else
                            _error.value = MainViewModel.UserError(app.resources.getString(R.string.sending_error))
                    } else
                        _error.value = MainViewModel.UserError(app.resources.getString(R.string.sending_error))
                }
            } catch (e: Exception) {
                _error.value = MainViewModel.UserError(app.resources.getString(R.string.working_error))
                Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
            }
        }
    }

    fun setUser(user: UserResp) {
        val mUser = usersState.value
        if (mUser == null)
            usersState.value = user
        else if (
            user.id != mUser.id ||
            (user.id == mUser.id &&
                user.client.defaultAccount?.id != mUser.client.defaultAccount?.id)
        )
            usersState.value = user
    }
}