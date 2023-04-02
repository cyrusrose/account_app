package com.cyril.account.core.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyril.account.home.data.repository.PersonalRep
import com.cyril.account.home.presentation.Item
import com.cyril.account.utils.UiText
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _bottomBar = MutableSharedFlow<Boolean>(replay = 1)
    val bottomBar = _bottomBar.asSharedFlow()

    private val _error = MutableSharedFlow<UiText>()
    val error = _error.asSharedFlow()

    fun navigateToHome() {
        viewModelScope.launch {
            _bottomBar.emit(true)
        }
    }

    fun navigateToStart() {
        viewModelScope.launch {
            _bottomBar.emit(false)
        }
    }

    fun setUpError(msg: UiText) {
        viewModelScope.launch {
            _error.emit(msg)
        }
    }
}