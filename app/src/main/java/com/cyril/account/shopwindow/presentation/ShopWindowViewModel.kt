package com.cyril.account.shopwindow.presentation

import android.util.Log
import androidx.lifecycle.*
import com.cyril.account.home.data.utils.CardTypes
import com.cyril.account.home.data.repository.PersonalRep
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.utils.DEBUG
import com.cyril.account.utils.UiText
import com.cyril.account.utils.cardEmpty
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
class ShopWindowViewModel @Inject constructor(
    private val personalRep: PersonalRep
) : ViewModel() {
    private val _error = MutableSharedFlow<UiText>()
    val error = _error.asSharedFlow()

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

    val accs = usersState.flatMapLatest {
        if (it == null)
            flowOf(CardTypes(cardEmpty, cardEmpty, cardEmpty))
        else
            personalRep.getAccountsToCards()
            .retryAgainCatch(_error)
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setUser(user: UserResp) {
        val mUser = usersState.value
        if (user.id != mUser?.id)
            usersState.update {
                user
            }
    }
}