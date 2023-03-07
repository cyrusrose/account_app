package com.cyril.account.shopwindow.presentation

import android.app.Application
import android.util.Log
import androidx.core.graphics.toColorInt
import androidx.lifecycle.*
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.home.data.utils.CardTypes
import com.cyril.account.home.data.repository.PersonalRep
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.history.presentation.BindableSpinnerAdapter.SpinnerItem
import com.cyril.account.home.domain.Card
import com.cyril.account.utils.Resource
import com.cyril.account.utils.Resource.Loading
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.net.SocketTimeoutException
import java.util.*

class ShopSheetViewModel(private val app: Application) : AndroidViewModel(app) {
    private val cardEmpty = listOf(
        Card("", "", "", R.drawable.name_svg, "#919191".toColorInt())
    )

    private val _error = MutableLiveData<MainViewModel.UserError>()
    val error: LiveData<MainViewModel.UserError> = _error

    private val personalRep = PersonalRep()
    private val usersState = MutableStateFlow<UserResp?>(null)

    private val _currencies = MutableLiveData<List<SpinnerItem>>()
    val currencies: LiveData<List<SpinnerItem>> = _currencies
    val selectedCurrency = MutableLiveData<SpinnerItem>()

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard = _selectedCard.asStateFlow()

    val cards = usersState.flatMapLatest {
        if (it == null)
            flowOf(cardEmpty)
        else
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

    fun setUser(user: UserResp) {
        val mUser = usersState.value
        if (user.id != mUser?.id)
            usersState.value = user
    }

    fun setCard(card: Card) {
        _selectedCard.update {
            card
        }
    }

    fun setItems() {
        viewModelScope.launch {
            _currencies.value = personalRep.getCurrenciesToCards(app.resources, _error)
        }
    }

    suspend fun convert(
        fromCode: Int,
        toCode: Int,
        sum: BigDecimal
    ): BigDecimal? {
        return personalRep.convert(fromCode, toCode, sum, app.resources, _error)
    }

    fun addCard(acc: Card, selectedCard: Card, money: BigDecimal? = null) {
        viewModelScope.launch {
            try {
                val client = usersState.value?.client
                val code = selectedCurrency.value?.value
                if (client != null && code != null) {
                    val it = personalRep.addCard(
                        clientId = client.id,
                        accountId = UUID.fromString(acc.id),
                        code = code.toInt(),
                        money = money,
                        senderAccountId = UUID.fromString(selectedCard.id) )

                    if (!it.isSuccessful) {
                        Log.d(MainActivity.DEBUG, it.errorBody()?.string() ?: "Unknown")
                        _error.value = MainViewModel.UserError(app.resources.getString(R.string.card_add_error))
                    }
                } else {
                    if (client == null)
                        _error.value = MainViewModel.UserError(app.resources.getString(R.string.client_error))
                    else if(code == null)
                        _error.value = MainViewModel.UserError(app.resources.getString(R.string.code_error))
                }
            } catch (e: Exception) {
                _error.value = MainViewModel.UserError(app.resources.getString(R.string.working_error))
                Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
            }
        }
    }
}