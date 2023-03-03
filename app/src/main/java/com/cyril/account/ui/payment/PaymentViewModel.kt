package com.cyril.account.ui.payment

import android.app.Application
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.*
import com.cyril.account.MainActivity
import com.cyril.account.MainViewModel.*
import com.cyril.account.R
import com.cyril.account.rest.user.UserRep
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.SocketTimeoutException

class PaymentViewModel(val app: Application) : AndroidViewModel(app) {
    private val _error = MutableLiveData<UserError>()
    val error: LiveData<UserError> = _error

    private val userRep = UserRep()

    private suspend fun getTransfers(): List<Transfer> {
        val res = viewModelScope.async(Dispatchers.IO) {
            userRep.getTransfers(Resources.getSystem().configuration, app.applicationContext.assets)
        }
        return res.await()
    }

    private val payment = userRep.getClientNosTOCards()
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

    val all = payment.map {
        listOf<TransferType>() +
        Title(app.resources.getString(R.string.transfers_title)) +
        getTransfers() +
        Title(app.resources.getString(R.string.payment_title)) +
        it +
        Padding
    }
        .asLiveData()

}