package com.cyril.account.fire.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyril.account.R
import com.cyril.account.core.data.response.ErrorResp
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.history.data.HistoryRep
import com.cyril.account.home.data.repository.PersonalRep
import com.cyril.account.utils.DEBUG
import com.cyril.account.utils.UiText
import com.cyril.account.utils.cardEmpty
import com.cyril.account.utils.phonePattern
import com.fasterxml.jackson.databind.ObjectMapper
import com.it.access.util.retryAgainCatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FireViewModel @Inject constructor(
    private val personalRep: PersonalRep,
    private val histRep: HistoryRep,
    private val mapper: ObjectMapper
) : ViewModel() {
    private val usersState = MutableStateFlow<UserResp?>(null)

    private val _error = MutableSharedFlow<UiText>()
    val error = _error.asSharedFlow()

    private val _moneyError = MutableSharedFlow<UiText>()
    val moneyError = _moneyError.asSharedFlow()

    private val _otherError = MutableSharedFlow<UiText>()
    val otherError = _otherError.asSharedFlow()

    private val handler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            throwable.message?.let {
                _error.emit(UiText.DynamicString(it))
            }
        }
        Log.d(DEBUG, "Error: " + throwable.message)
    }

    private val scope = viewModelScope + handler

    val card = usersState.flatMapLatest {
        if (it == null)
            flowOf(cardEmpty)
        else
            personalRep.getPersonalsToCardsFlat(it.client)
            .retryAgainCatch(_error)
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun sendMoneyByClientNo(
        money: BigDecimal,
        senderAccountId: UUID,
        clientNo: BigInteger,
        clientSsn: BigInteger
    ) {
        scope.launch {
            if (money < BigDecimal("0.01")) {
                _moneyError.emit(UiText.StringResource(R.string.sum_title))
                cancel()
            }

            val it = histRep.sendMoneyByClientNo(money, senderAccountId, clientNo, clientSsn)
            if (!it.isSuccessful) {
                Log.d(DEBUG, it.errorBody()?.string() ?: "Unknown")
                _error.emit(UiText.StringResource(R.string.sending_error))
            }
        }
    }

    fun sendMoneyByPhone(
        money: BigDecimal,
        senderAccountId: UUID,
        phoneNo: String
    ) {
        scope.launch {
            if (money < BigDecimal("0.01")) {
                _moneyError.emit(UiText.StringResource(R.string.sum_title))
                cancel()
            }

            val digits = phoneNo.toCharArray()
                .filter { it.isDigit() }
            if (digits.size != 11) {
                _otherError.emit(UiText.StringResource(R.string.digits_title))
                cancel()
            }

            val itr = digits.iterator()
            val phone = phonePattern.map {
                if (it == '@' && itr.hasNext())
                    itr.next()
                else
                    it
            }.joinToString(separator = "")

            val it = histRep.sendMoneyByPhone(money, senderAccountId, phone)
            if (!it.isSuccessful) {
                Log.d(DEBUG, it.errorBody()?.string() ?: "Unknown")
                _error.emit(UiText.StringResource(R.string.sending_error))
            }
        }
    }

    fun sendMoneyByCard(
        money: BigDecimal,
        senderAccountId: UUID,
        cardNo: BigInteger
    ) {
        scope.launch {
            if (money < BigDecimal("0.01")) {
                _moneyError.emit(UiText.StringResource(R.string.sum_title))
                cancel()
            }

            val it = histRep.sendMoneyByCard(money, senderAccountId, cardNo)
            if (!it.isSuccessful) {
                val ans = it.errorBody()?.string()
                processError(ans)
            }
        }
    }

    private suspend fun processError(ans: String?) {
        if (ans != null) {
            Log.d(DEBUG, ans)

            val resp = mapper.readValue(ans, ErrorResp::class.java)
            if (resp.message.contains("same account"))
                _error.emit(UiText.StringResource(R.string.same_error))
            else if (resp.message.contains("receiver's card"))
                _error.emit(UiText.StringResource(R.string.receiver_error))
            else
                _error.emit(UiText.StringResource(R.string.sending_error))
        } else
            _error.emit(UiText.StringResource(R.string.sending_error))
    }

    fun sendMoney(
        money: BigDecimal,
        senderAccountId: UUID,
        receiverAccountId: UUID
    ) {
        scope.launch {
            if (money < BigDecimal("0.01")) {
                _moneyError.emit(UiText.StringResource(R.string.sum_title))
                cancel()
            }

            val it = histRep.sendMoney(money, senderAccountId, receiverAccountId)
            if (!it.isSuccessful) {
                val ans = it.errorBody()?.string()
                processError(ans)
            }
        }
    }

    fun setUpMoneyError(msg: UiText) {
        scope.launch {
            _moneyError.emit(msg)
        }
    }

    fun setUpOtherError(msg: UiText) {
        scope.launch {
            _otherError.emit(msg)
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

    fun setUpError(msg: UiText) {
        scope.launch {
            _error.emit(msg)
        }
    }
}