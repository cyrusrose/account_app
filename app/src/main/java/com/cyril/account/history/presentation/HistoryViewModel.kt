package com.cyril.account.history.presentation

import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.*
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.R
import com.cyril.account.history.data.HistoryRep
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.core.presentation.BindableSpinnerAdapter.SpinnerItem
import com.cyril.account.history.domain.ToCardsUseCase
import com.cyril.account.utils.DEBUG
import com.cyril.account.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRep: HistoryRep,
    private val toCardsUseCase: ToCardsUseCase
) : ViewModel() {
    private class HistoryState(val user: UserResp, val state: String? = null, val via: String? = null) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other === null) return false

            return if (other is HistoryState)
                user.id == other.user.id
                && state == other.state
                && via == other.via
            else false
        }
    }

    private val usersState = MutableStateFlow<HistoryState?>(null)

    private val _error = MutableSharedFlow<UiText>()
    val error = _error.asSharedFlow()

    private val _items = MutableStateFlow<List<SpinnerItem>?>(null)
    val items = _items.asStateFlow()

    fun initItems(res: Resources) {
        viewModelScope.launch {
            _items.update {
                historyRep.getTypes(res)
            }
        }
    }

    private val _selectedItem = MutableStateFlow<SpinnerItem?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    val history = usersState.filterNotNull()
    .flatMapLatest {
        toCardsUseCase(it.user.client.id, it.state, it.via)
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
    }

    fun setUser(user: UserResp) {
        if (user.id != usersState.value?.user?.id)
            usersState.update {
                HistoryState(user)
            }
    }

    fun setFilter(state: String? = null, via: String? = null) {
        usersState.value?.let { histState ->
            usersState.update {
                HistoryState(histState.user, state, via)
            }
        }
    }

    fun setItem(item: SpinnerItem) {
        _selectedItem.update {
            item
        }
    }
}