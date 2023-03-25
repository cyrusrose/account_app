package com.cyril.account.utils

import androidx.core.graphics.toColorInt
import com.cyril.account.R
import com.cyril.account.home.domain.Card

val cardEmpty = listOf(
    Card("", "", "", R.drawable.name_svg, "#919191".toColorInt())
)

const val USD = 840

const val baseUrl: String = "http://10.0.2.2:8080/api/v1/"

const val timePattern = "HH:mm:ss"

const val DEBUG = "debug"