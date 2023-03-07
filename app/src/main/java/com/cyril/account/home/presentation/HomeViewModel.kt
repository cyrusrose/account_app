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
import com.cyril.account.utils.cardEmpty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val app: Application) : AndroidViewModel(app) {
    private val _selectedItem = MutableSharedFlow<Item>()

    private val _selectedCard = MutableStateFlow<Card?>(null)

    private val personalRep = PersonalRep()

    private val usersState = MutableStateFlow<UserResp?>(null)
    val user: LiveData<UserResp> = usersState.filterNotNull().asLiveData()

    init {
        _selectedItem.mapLatest { item ->
            val user = usersState.value
            val card = _selectedCard.value

            if(user != null && card != null) {
                Log.d("cyrus", "card: ${card.title}")
                when (item) {
                    Item.DEFAULT -> {
                        Log.d("cyrus", item.name)
                        changeDefault(user.client, card)
                    }
                    Item.DELETE -> delPersonal(user.client, card)
                    Item.NONE -> Unit
                }
            }
        }
        .launchIn(viewModelScope)
    }

    private val _error = MutableLiveData<UserError>()
    val error: LiveData<UserError> = _error

    val card = usersState.flatMapLatest {
       if (it == null)
           flowOf(CardTypes(cardEmpty, cardEmpty, cardEmpty))
       else
            personalRep.getPersonalsToCards(it.client, cardEmpty)
            .retry {
                val time = it is SocketTimeoutException
                if (time) {
                    delay(5000)
                    _error.value = UserError(app.resources.getString(R.string.trying_error))
                    Log.d(MainActivity.DEBUG, it.message ?: "")
                }
                time
            }.catch { e ->
                _error.value = UserError(app.resources.getString(R.string.working_error))
                Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
            }
    }
        .asLiveData()

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
            usersState.value = user
        else if (
            user.id != mUser.id ||
            (user.id == mUser.id &&
                    user.client.defaultAccount?.id != mUser.client.defaultAccount?.id)
        )
            usersState.value = user
    }

    fun delPersonal(client: ClientResp, card: Card) {
        viewModelScope.launch {
            try {
                val it = personalRep.delPersonal(client.id, UUID.fromString(card.id))
                if (!it.isSuccessful) {
                    Log.d(MainActivity.DEBUG, it.errorBody()?.string() ?: "Unknown")
                    _error.value = UserError("Error during deleting account")
                }
            } catch (e: Exception) {
                Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
                _error.value = UserError("Unscheduled work on the server")
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
                            _error.value = UserError("Error during changing account")
                        }
                    } catch (e: Exception) {
                        _error.value = UserError("Unscheduled work on the server")
                        Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
                    }
            else {
                _error.value = UserError("It's already default")
            }
        }
    }
}

data class SelectedCard(val user: UserResp, val card: Card)
