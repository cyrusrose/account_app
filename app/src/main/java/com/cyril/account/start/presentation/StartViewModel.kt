package com.cyril.account.start.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel.UserError
import com.cyril.account.R
import com.cyril.account.core.data.UserRep
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.utils.DEBUG
import com.cyril.account.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val userRep: UserRep
) : ViewModel() {
    private data class UserInput(val login: String, val password: String)

    override fun onCleared() {
        super.onCleared()
        Log.d(DEBUG, "StartViewModel cleared")
    }

    private val usersState = MutableStateFlow<UserInput?>(null)

    private val _error = MutableSharedFlow<UiText>()
    val error = _error.asSharedFlow()

    val curUser = usersState.filterNotNull().flatMapLatest {
        userRep.getUser(it.login, it.password).onEach { value ->
            if (value == null)
                _error.emit(UiText.StringResource(R.string.login_error))
        }
        .retry {
            val time = it is SocketTimeoutException
            if (time) {
                delay(5000)
                _error.emit(UiText.StringResource(R.string.trying_error))
                Log.d(MainActivity.DEBUG, it.message ?: "")
            }
            time
        }.catch { e ->
            _error.emit(UiText.StringResource(R.string.working_error))
            Log.d(MainActivity.DEBUG, "Caught: ${e.message}")
            usersState.update {
                null
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun getUser(login: String, password: String): StateFlow<UserResp?> {
        if (login.isNotBlank() && password.isNotBlank())
            usersState.update {
                UserInput(login, password)
            }
        return curUser
    }

    fun resetUser(): StateFlow<UserResp?> {
        usersState.update {
            null
        }
        return curUser
    }

    fun showError(msg: UiText) {
        viewModelScope.launch {
            _error.emit(msg)
        }
    }

    fun showError(msg: String) = showError(UiText.DynamicString(msg))
}