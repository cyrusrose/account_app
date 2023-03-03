package com.cyril.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cyril.account.rest.personal.PersonalRep

class MainViewModel : ViewModel() {
    private val _bottomBar = MutableLiveData<Boolean>()
    val bottomBar: LiveData<Boolean> get() = _bottomBar

    private val personalRep = PersonalRep()

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