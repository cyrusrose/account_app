package com.it.access.util

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cyril.account.R
import com.cyril.account.utils.DEBUG
import com.cyril.account.utils.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException

fun <T> LifecycleOwner.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T)-> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun <T> LifecycleOwner.collectLifecycleFlow(flow: Flow<T>, collect: suspend (T)-> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(collect)
        }
    }
}

fun <T> Flow<T>.retryAgainCatch(error: MutableSharedFlow<UiText>, delay: Long = 5000, post: () -> Unit = {}) =
    this.retry {
        val time = it is SocketTimeoutException || it is ConnectException
        if (time) {
            delay(delay)
            error.emit(UiText.StringResource(R.string.trying_error))
            Log.d(DEBUG, it.message ?: "")
        }
        time
    }.catch { e ->
        error.emit(UiText.StringResource(R.string.working_error))
        Log.d(DEBUG, "Caught: ${e.message}")
        post()
    }

fun <T> Flow<T>.retryAgain(error: MutableSharedFlow<UiText>, delay: Long = 5000) =
    this.retry {
        val time = it is SocketTimeoutException || it is ConnectException
        if (time) {
            delay(delay)
            error.emit(UiText.StringResource(R.string.trying_error))
            Log.d(DEBUG, it.message ?: "")
        }
        time
    }