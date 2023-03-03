package com.cyril.account.rest.personal

import com.cyril.account.ui.home.Card

data class CardTypes(val cards: List<Card>, val deposits: List<Card>, val clientAccs: List<Card>)
