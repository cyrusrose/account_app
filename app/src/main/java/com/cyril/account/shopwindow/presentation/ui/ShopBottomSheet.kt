package com.cyril.account.shopwindow.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.cyril.account.R
import com.cyril.account.databinding.ShopCardSheetBinding
import com.cyril.account.core.data.response.UserResp
import com.cyril.account.fire.presentation.FireViewModel
import com.cyril.account.fire.presentation.ui.MyCardRecyclerViewAdapter
import com.cyril.account.home.domain.Card
import com.cyril.account.home.presentation.ui.CardDiffUtil
import com.cyril.account.shopwindow.presentation.ShopWindowViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ShopBottomSheet(val user: UserResp, val acc: Card) : BottomSheetDialogFragment() {
    private val shopVm: ShopWindowViewModel by viewModels()
    private val fireVm: FireViewModel by viewModels()

    private lateinit var ui: ShopCardSheetBinding
    private lateinit var adp: MyCardRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = ShopCardSheetBinding.inflate(inflater,  container, false)
        ui.lifecycleOwner = viewLifecycleOwner
        ui.vm = shopVm
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adp = with(ui.rv) {
            MyCardRecyclerViewAdapter(this, CardDiffUtil())
        }

        setOnClicks()
        displayErrors()
        observeCurrency()
        observeCards()
    }

    private fun displayErrors() {
        fireVm.error.observe(viewLifecycleOwner) {
            displayError(it.message)
        }

        shopVm.error.observe(viewLifecycleOwner) {
            displayError(it.message)
        }
    }

    private fun displayError(it: String) {
        val snack = Snackbar.make(ui.root, it, Snackbar.LENGTH_SHORT)
        snack.show()
    }

    private fun setOnClicks() {
        ui.choose.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val card = adp.card.value
                    val moneyStr = ui.money.editText?.text.toString()
                    val money = moneyStr.ifBlank { "0.0" }.toBigDecimal()
                    val curr = shopVm.selectedCurrency.value?.value?.toInt()
                    val minAmount = if(curr != null && acc.minAmount != null)
                        shopVm.convert(USD, curr, acc.minAmount)?.setScale(2) else null
                    if (card != null) {
                        if (acc.clss == "client_account") {
                            if (minAmount != null) {
                                if (money < BigDecimal(0.01)) {
                                    ui.money.error = getString(R.string.sum_title)
                                }
                                else if (money < minAmount)
                                    ui.money.error = getString(R.string.sum_less_title, money, minAmount)
                                else
                                    shopVm.addCard(acc, card, money)
                            } else
                                displayError("No min amount")
                        } else {
                            if (money < BigDecimal.ZERO) {
                                ui.money.error = getString(R.string.sum_other_title, "0.0")
                            } else {
                                shopVm.addCard(acc, card, money)
                            }
                        }
                    } else
                        displayError(getString(R.string.choose_card_title))

                } catch(e: NumberFormatException) {
                    displayError(getString(R.string.strings_title))
                }
            }
        }

        ui.money.editText?.doOnTextChanged { text, _, _, _ ->
            ui.money.error = null
        }
    }

    private fun observeCurrency() {
        shopVm.setUser(user)
        shopVm.setItems()

        shopVm.selectedCurrency.observe(viewLifecycleOwner) { item ->
            viewLifecycleOwner.lifecycleScope.launch {
                acc.minAmount?.let {
                    val minSum = it.let {
                        shopVm.convert(USD, item.value.toInt(), it)
                        ?.setScale(2)
                    }
                    ui.money.helperText =
                        resources.getString(R.string.min_sum_code_title, minSum, item.text)
                }

                ui.money.error = null
            }
        }
    }

    private fun observeCards() {
        fireVm.setUser(user)

        fireVm.card.observe(viewLifecycleOwner) {
            adp.submitList(it)
        }
    }

    companion object {
        const val TAG = "ShopBottomSheet"
        const val USD = 840
    }
}
