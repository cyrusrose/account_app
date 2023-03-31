package com.cyril.account.payment.presentation

import android.content.res.AssetManager
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyril.account.R
import com.cyril.account.core.data.UserRep
import com.cyril.account.payment.domain.Title
import com.cyril.account.payment.domain.Transfer
import com.cyril.account.payment.domain.TransferType
import com.cyril.account.utils.UiText
import com.it.access.util.retryAgain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val userRep: UserRep
) : ViewModel() {
    private val _error = MutableSharedFlow<UiText>()
    val error = _error.asSharedFlow()

    private val handler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            throwable.message?.let {
                _error.emit(UiText.DynamicString(it))
            }
        }
        Log.d(com.cyril.account.utils.DEBUG, "Error: " + throwable.message)
    }

    private val scope = viewModelScope + handler

    private val _transfers = MutableStateFlow<List<Transfer>?>(null)

    fun setUpTransfers(manager: AssetManager) {
        scope.launch {
            _transfers.update {
                userRep.getTransfers(Resources.getSystem().configuration, manager)
            }
        }
    }

    private val _payments = userRep.getClientNosTOCards()
    .retryAgain(_error)
    .stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val all = combine(
        _transfers.filterNotNull(),
        _payments.filterNotNull()
    ) { transfers, payments ->
        listOf<TransferType>() +
        Title(UiText.StringResource(R.string.transfers_title)) +
        transfers +
        Title(UiText.StringResource(R.string.payment_title)) +
        payments
    }
    .stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}