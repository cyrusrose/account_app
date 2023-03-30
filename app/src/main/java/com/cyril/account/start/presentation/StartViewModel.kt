package com.cyril.account.start.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyril.account.R
import com.cyril.account.core.data.UserRep
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.utils.DEBUG
import com.cyril.account.utils.UiText
import com.it.access.util.retryAgain
import com.it.access.util.retryAgainCatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
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

    val curUser = usersState.filterNotNull().flatMapLatest {
        userRep.getUser(it.login, it.password).onEach { value ->
            if (value == null)
                _error.emit(UiText.StringResource(R.string.login_error))
        }
        .retryAgainCatch(_error) {
            usersState.update {
                null
            }
        }
    }.stateIn(
        scope = scope,
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
        scope.launch {
            _error.emit(msg)
        }
    }

    fun showError(msg: String) = showError(UiText.DynamicString(msg))
}