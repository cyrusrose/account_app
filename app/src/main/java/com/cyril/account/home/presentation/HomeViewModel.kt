package com.cyril.account.home.presentation

import android.app.Application
import android.util.Log
import androidx.core.graphics.toColorInt
import androidx.lifecycle.*
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel.UserError
import com.cyril.account.R
import com.cyril.account.home.data.repository.PersonalRep
import com.cyril.account.home.data.utils.CardTypes
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.core.data.response.ClientResp
import com.cyril.account.home.domain.Card
import com.cyril.account.utils.DEBUG
import com.cyril.account.utils.UiText
import com.cyril.account.utils.cardEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val personalRep: PersonalRep
) : ViewModel() {
    private val _selectedItem = MutableSharedFlow<Item>()

    private val _selectedCard = MutableStateFlow<Card?>(null)

    private val usersState = MutableStateFlow<UserResp?>(null)

    init {
        _selectedItem.mapLatest { item ->
            val user = usersState.value
            val card = _selectedCard.value

            if(user != null && card != null) {
                Log.d(DEBUG, "card: ${card.title}")
                when (item) {
                    Item.DEFAULT -> changeDefault(user.client, card)
                    Item.DELETE -> delPersonal(user.client, card)
                    Item.NONE -> Unit
                }
            }
        }
        .launchIn(viewModelScope)
    }

    private val _error = MutableSharedFlow<UiText>()
    val error = _error.asSharedFlow()

    val card = usersState.flatMapLatest {
       if (it == null)
           flowOf(CardTypes(cardEmpty, cardEmpty, cardEmpty))
       else
            personalRep.getPersonalsToCards(it.client)
            .retry {
                val time = it is SocketTimeoutException
                if (time) {
                    delay(5000)
                    _error.emit(UiText.StringResource(R.string.trying_error))
                    Log.d(DEBUG, it.message ?: "")
                }
                time
            }.catch { e ->
                _error.emit(UiText.StringResource(R.string.working_error))
                Log.d(DEBUG, "Caught: ${e.message}")
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setCard(card: Card) {
        _selectedCard.update {
            card
        }
    }

    fun setItem(item: Item) {
        viewModelScope.launch {
            _selectedItem.emit(item)
        }
    }

    fun setUser(user: UserResp) {
        val mUser = usersState.value
        if (mUser == null)
            usersState.update { user }
        else if (
            user.id != mUser.id ||
            (user.id == mUser.id &&
            user.client.defaultAccount?.id != mUser.client.defaultAccount?.id)
        )
            usersState.update { user }
    }

    fun delPersonal(client: ClientResp, card: Card) {
        viewModelScope.launch {
            try {
                val it = personalRep.delPersonal(client.id, UUID.fromString(card.id))
                if (!it.isSuccessful) {
                    Log.d(DEBUG, it.errorBody()?.string() ?: "Unknown")
                    _error.emit(UiText.StringResource(R.string.deleting_accs_error))
                }
            } catch (e: Exception) {
                Log.d(DEBUG, "Caught: ${e.message}")
                _error.emit(UiText.StringResource(R.string.working_error))
            }
        }
    }

    fun changeDefault(client: ClientResp, card: Card) {
        viewModelScope.launch {
            if (client.defaultAccount?.id != UUID.fromString(card.id))
                try {
                    val it = personalRep.changeDefault(client.id, UUID.fromString(card.id))
                    if (!it.isSuccessful) {
                        Log.d(MainActivity.DEBUG, it.errorBody()?.string() ?: "Unknown")
                        _error.emit(UiText.StringResource(R.string.changing_error))
                    }
                } catch (e: Exception) {
                    _error.emit(UiText.StringResource(R.string.working_error))
                    Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
                }
            else
                _error.emit(UiText.StringResource(R.string.already_default_error))
        }
    }
}