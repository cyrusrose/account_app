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
import com.it.access.util.retryAgain
import com.it.access.util.retryAgainCatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.SocketTimeoutException
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val personalRep: PersonalRep
) : ViewModel() {
    private val _selectedItem = MutableSharedFlow<Item>()

    private val _selectedCard = MutableStateFlow<Card?>(null)

    private val usersState = MutableStateFlow<UserResp?>(null)

    private val _error = MutableSharedFlow<UiText>()
    val error = _error.asSharedFlow()

    private val handler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            throwable.message?.let {
                _error.emit(UiText.DynamicString(it))
            }
        }
        Log.d(DEBUG, "Error: " + throwable.message)
    }

    private val scope = viewModelScope + handler

    init {
        _selectedItem.mapLatest { item ->
            val user = usersState.value
            val card = _selectedCard.value

            if (user != null && card != null) {
                Log.d(DEBUG, "card: ${card.title}")
                when (item) {
                    Item.DEFAULT -> changeDefault(user.client, card)
                    Item.DELETE -> delPersonal(user.client, card)
                    Item.NONE -> Unit
                }
            }
        }
            .retryAgain(_error)
            .launchIn(scope)
    }

    val card = usersState.flatMapLatest {
        if (it == null)
            flowOf(CardTypes(cardEmpty, cardEmpty, cardEmpty))
        else
            personalRep.getPersonalsToCards(it.client)
            .retryAgainCatch(_error)
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setCard(card: Card) {
        _selectedCard.update {
            card
        }
    }

    fun setItem(item: Item) {
        scope.launch {
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

    private suspend fun delPersonal(client: ClientResp, card: Card) {
        val it = personalRep.delPersonal(client.id, UUID.fromString(card.id))
        if (!it.isSuccessful) {
            Log.d(DEBUG, it.errorBody()?.string() ?: "Unknown")
            _error.emit(UiText.StringResource(R.string.deleting_accs_error))
        }
    }

    private suspend fun changeDefault(client: ClientResp, card: Card) {
        if (client.defaultAccount?.id != UUID.fromString(card.id)) {
            val it = personalRep.changeDefault(client.id, UUID.fromString(card.id))
            if (!it.isSuccessful) {
                Log.d(MainActivity.DEBUG, it.errorBody()?.string() ?: "Unknown")
                _error.emit(UiText.StringResource(R.string.changing_error))
            }
        } else
            _error.emit(UiText.StringResource(R.string.already_default_error))
    }
}