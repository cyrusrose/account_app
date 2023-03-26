package com.cyril.account.start.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel.UserError
import com.cyril.account.R
import com.cyril.account.core.data.UserRep
import com.cyril.account.core.data.response.UserResp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.net.SocketTimeoutException

class StartViewModel(private val app: Application) : AndroidViewModel(app) {
    private data class UserInput(val login: String, val password: String)

    override fun onCleared() {
        super.onCleared()

        Log.d("cyrus", "StartViewModel cleared")
    }

    private val userRep = UserRep()
    private val usersState = MutableStateFlow<UserInput?>(null)

    private val _userError = MutableLiveData<UserError>()
    val userError: LiveData<UserError> = _userError

    val curUser = usersState.filterNotNull().flatMapLatest {
        userRep.getUser(it.login, it.password)
        .retry {
            val time = it is SocketTimeoutException
            if (time) {
                delay(5000)
                _userError.value = UserError(app.resources.getString(R.string.trying_error))
                Log.d(MainActivity.DEBUG, it.message ?: "")
            }
            time
        }.catch { e ->
            _userError.value = UserError(app.resources.getString(R.string.working_error))
            Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
            usersState.value = null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val usersOld =  curUser.asLiveData()


    fun getUser(login: String, password: String, subscribe: Boolean = false): LiveData<UserResp?> {
        if (login.isNotBlank() && password.isNotBlank())
            usersState.value = UserInput(login, password)
        else
            usersState.value = null
        return usersOld
    }

    fun getUser(): LiveData<UserResp?> {
        return usersOld
    }
}