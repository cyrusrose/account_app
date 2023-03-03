package com.cyril.account.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.cyril.account.MainActivity
import com.cyril.account.MainViewModel.UserError
import com.cyril.account.R
import com.cyril.account.rest.personal.*
import com.cyril.account.rest.user.UserResp
import com.cyril.account.rest.user.client.ClientResp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.util.*

class HomeViewModel(private val app: Application) : AndroidViewModel(app) {
    val empty = ""
    private val cardEmpty = listOf(
        Card(empty, "", "", R.drawable.name_svg, app.resources.getColor(R.color.light_grey))
    )

    private val personalRep = PersonalRep()
    private val usersState = MutableStateFlow<UserResp?>(null)
    val user: LiveData<UserResp> = usersState.filterNotNull().asLiveData()

    private val _error = MutableLiveData<UserError>()
    val error: LiveData<UserError> = _error

    val card = usersState.flatMapLatest {
       if (it == null) {
           flow {
               emit(CardTypes(cardEmpty, cardEmpty, cardEmpty))
           }
       } else
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

    fun setUser(user: UserResp) {
        val mUser = usersState.value
        if (user.id != mUser?.id)
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