package com.cyril.account.core.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyril.account.home.data.repository.PersonalRep
import com.cyril.account.home.presentation.Item
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _bottomBar = MutableLiveData<Boolean>()
    val bottomBar: LiveData<Boolean> get() = _bottomBar

    data class UserError(val message: String)
    private val _error = MutableLiveData<UserError>()
    val error: LiveData<UserError> = _error

    fun navigateToHome() {
        _bottomBar.value = true
    }

    fun navigateToStart() {
        _bottomBar.value = false
    }

    fun setUserError(msg: UserError) {
        _error.value = msg
    }

    fun setUserError(msg: String) {
        _error.value = UserError(msg)
    }
}