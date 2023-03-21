package com.cyril.account.shopwindow.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.home.data.utils.CardTypes
import com.cyril.account.home.data.repository.PersonalRep
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.utils.cardEmpty
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.net.SocketTimeoutException

class ShopWindowViewModel(private val app: Application) : AndroidViewModel(app) {
    private val _error = MutableLiveData<MainViewModel.UserError>()
    val error: LiveData<MainViewModel.UserError> = _error

    private val personalRep = PersonalRep()
    private val usersState = MutableStateFlow<UserResp?>(null)

    val accs = usersState.flatMapLatest {
        if (it == null) {
            flow {
                emit(CardTypes(cardEmpty, cardEmpty, cardEmpty))
            }
        } else
            personalRep.getAccountsToCards(app.resources, cardEmpty)
                .retry {
                    val time = it is SocketTimeoutException
                    if (time) {
                        delay(5000)
                        _error.value =
                            MainViewModel.UserError(app.resources.getString(R.string.trying_error))
                        Log.d(MainActivity.DEBUG, it.message ?: "")
                    }
                    time
                }.catch { e ->
                    _error.value =
                        MainViewModel.UserError(app.resources.getString(R.string.working_error))
                    Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
                }
    }
        .asLiveData()

    fun setUser(user: UserResp) {
        val mUser = usersState.value
        if (user.id != mUser?.id)
            usersState.value = user
    }
}