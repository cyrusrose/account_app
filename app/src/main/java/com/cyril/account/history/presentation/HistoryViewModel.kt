package com.cyril.account.history.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel.UserError
import com.cyril.account.R
import com.cyril.account.history.data.HistoryRep
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.core.presentation.BindableSpinnerAdapter.SpinnerItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class HistoryViewModel(private val app: Application) : AndroidViewModel(app) {
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

    private val historyRep = HistoryRep()
    private val usersState = MutableStateFlow<HistoryState?>(null)

    private val _error = MutableLiveData<UserError>()
    val error: LiveData<UserError> = _error

    private val _items = MutableLiveData<List<SpinnerItem>>()
    val items: LiveData<List<SpinnerItem>> = _items
    val selectedItem = MutableLiveData<SpinnerItem>()

    val history = usersState.filterNotNull().distinctUntilChanged()
    .flatMapLatest {
        historyRep.getHistoryToCards(app.resources, it.user.client.id, it.state, it.via)
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
        if (user.id != usersState.value?.user?.id)
            usersState.value = HistoryState(user)
    }

    fun setFilter(state: String? = null, via: String? = null) {
        usersState.value?.let {
            usersState.value = HistoryState(it.user, state, via)
        }
    }

    fun setItems() {
        viewModelScope.launch {
            _items.value = historyRep.getTypes(app.resources)
        }
    }

}