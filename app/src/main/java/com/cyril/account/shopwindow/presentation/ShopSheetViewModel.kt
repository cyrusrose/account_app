package com.cyril.account.shopwindow.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyril.account.R
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.core.presentation.BindableSpinnerAdapter.SpinnerItem
import com.cyril.account.home.data.repository.PersonalRep
import com.cyril.account.home.domain.Card
import com.cyril.account.utils.*
import com.it.access.util.retryAgain
import com.it.access.util.retryAgainCatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.math.BigDecimal
import java.net.SocketTimeoutException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ShopSheetViewModel @Inject constructor(
    private val personalRep: PersonalRep
) : ViewModel() {
    private val _error = MutableSharedFlow<UiText>()
    val error = _error.asSharedFlow()

    private val _moneyError = MutableSharedFlow<UiText>()
    val moneyError = _moneyError.asSharedFlow()

    private val usersState = MutableStateFlow<UserResp?>(null)

    private val handler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is SocketTimeoutException)
            viewModelScope.launch {
                throwable.message?.let {
                    _error.emit(UiText.DynamicString(it))
                }
            }
        Log.d(DEBUG, "Error: " + throwable.message)
    }

    private val scope = viewModelScope + handler

    val currencies = personalRep.getCurrenciesToCards()
    .retryAgain(_error)
    .transform { it ->
        if (it is Resource.Error) it.message?.let {
            _error.emit(it)
        }
        if (it is Resource.Success)
            emit(it.data)
    }
    .stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _selectedCurrency = MutableStateFlow<SpinnerItem?>(null)
    val selectedCurrency = _selectedCurrency.asStateFlow()

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard = _selectedCard.asStateFlow()

    private val _selectedAcc = MutableStateFlow<Card?>(null)

    val cards = usersState.flatMapLatest {
        if (it == null)
            flowOf(cardEmpty)
        else
            personalRep.getPersonalsToCardsFlat(it.client)
            .retryAgainCatch(_error)
    }
    .stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val helperText = combine(
        _selectedCurrency.filterNotNull(),
        _selectedCard.filterNotNull()
    ) { curr, card ->
        val minSum = card.minAmount?.let {
            convert(USD, curr.value.toInt(), it)
            ?.setScale(2)
        }

        minSum?.let {
            UiText.StringResource(R.string.min_sum_code_title, minSum, curr.text)
        }
    }
    .retryAgain(_error)
    .stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _money = MutableSharedFlow<MoneyState>()

    init {
        _money.mapLatest {
            startAdding(it)
        }
        .retryAgain(_error)
        .launchIn(scope)
    }

    private suspend fun startAdding(it: MoneyState) {
        val card = _selectedCard.value
        if(card == null) {
            _error.emit(UiText.StringResource(R.string.choose_card_title))
            return
        }
        val curr = _selectedCurrency.value?.value?.toInt()
        if (curr == null) {
            _error.emit(UiText.StringResource(R.string.code_error))
            return
        }

        if (card.clss == "client_account") {
            val minAmount = if(card.minAmount != null)
                convert(USD, curr, card.minAmount)?.setScale(2) else null

            if (minAmount == null) {
                _error.emit(UiText.StringResource(R.string.no_min_amout))
                return
            }

            if (it.money < BigDecimal(0.01)) {
                _moneyError.emit(UiText.StringResource(R.string.sum_title))
            }
            else if (it.money < minAmount)
                _moneyError.emit(UiText.StringResource(R.string.sum_less_title, it.money, minAmount))
            else
                confirmCard(curr, card, it.money)
        } else {
            if (it.money > BigDecimal.ZERO && it.money < BigDecimal("0.01")) {
                _moneyError.emit(UiText.StringResource(R.string.sum_title))
                return
            } else
                confirmCard(curr, card, it.money)
        }
    }

    fun setCurrency(currency: SpinnerItem) {
        _selectedCurrency.update {
            currency
        }
    }

    fun setUser(user: UserResp) {
        val mUser = usersState.value
        if (user.id != mUser?.id)
            usersState.update {
                user
            }
    }

    fun setCard(card: Card) {
        _selectedCard.update {
            card
        }
    }

    fun setAcc(acc: Card) {
        _selectedAcc.update {
            acc
        }
    }

    private suspend fun convert(
        fromCode: Int,
        toCode: Int,
        sum: BigDecimal
    ): BigDecimal? {
        val data = personalRep.convert(fromCode, toCode, sum)
        return when (data) {
            is Resource.Success -> data.data
            else -> {
                data.message?.let {
                    _error.emit(it)
                }
                null
            }
        }
    }

    fun addCard(money: BigDecimal) {
        scope.launch {
           _money.emit(MoneyState(money))
        }
    }

    private suspend fun confirmCard(
        code: Int,
        selectedCard: Card,
        money: BigDecimal? = null
    ) {
        val client = usersState.value?.client
        if (client == null ) {
            _error.emit(UiText.StringResource(R.string.client_error))
            return
        }
        val acc = _selectedAcc.value
        if (acc == null) {
            _error.emit(UiText.StringResource(R.string.choose_card_title))
            return
        }

        val it = personalRep.addCard(
            clientId = client.id,
            accountId = UUID.fromString(selectedCard.id),
            code = code,
            money = money,
            senderAccountId = UUID.fromString(acc.id) )

        if (!it.isSuccessful) {
            Log.d(DEBUG, it.errorBody()?.string() ?: "Unknown")
            _error.emit(UiText.StringResource(R.string.card_add_error))
        }
    }
}

data class MoneyState(val money: BigDecimal)