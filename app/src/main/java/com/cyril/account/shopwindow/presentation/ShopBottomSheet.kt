package com.cyril.account.shopwindow.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.doOnTextChanged
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.cyril.account.R
import com.cyril.account.core.presentation.BindableSpinnerAdapter
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.databinding.ShopCardSheetBinding
import com.cyril.account.fire.presentation.MyCardRecyclerViewAdapter
import com.cyril.account.home.presentation.CardDiffUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.it.access.util.collectLatestLifecycleFlow
import com.it.access.util.collectLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class ShopBottomSheet : BottomSheetDialogFragment() {
    private val shopVm: ShopSheetViewModel by hiltNavGraphViewModels(R.id.navigation_shopwindow)

    private lateinit var ui: ShopCardSheetBinding
    private val adp by lazy {
        MyCardRecyclerViewAdapter(ui.rv, CardDiffUtil())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = ShopCardSheetBinding.inflate(inflater,  container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeCurrency()
        observeCards()
        displayErrors()
        setOnClicks()
    }

    private fun displayErrors() {
        viewLifecycleOwner.collectLifecycleFlow(shopVm.error) {
            displayError(it.asString(requireContext()))
        }

        viewLifecycleOwner.collectLifecycleFlow(shopVm.moneyError) {
            ui.money.error = it.asString(requireContext())
        }
    }

    private fun displayError(it: String) {
        val snack = Snackbar.make(ui.root, it, Snackbar.LENGTH_SHORT)
        snack.show()
    }

    private fun setOnClicks() {
        ui.choose.setOnClickListener click@ {
            val moneyStr = ui.money.editText?.text.toString()
            val money = moneyStr.ifBlank { "0.00" }.toBigDecimal()

            shopVm.addCard(money)
        }

        ui.money.editText?.doOnTextChanged { text, _, _, _ ->
            ui.money.error = null
        }
    }

    private fun observeCurrency() {
        val adapter = BindableSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mutableListOf())
        ui.spinner.adapter = adapter

        viewLifecycleOwner.collectLatestLifecycleFlow(
            shopVm.currencies.filterNotNull()
        ) {
            adapter.clear()
            adapter.addAll(it)

            shopVm.selectedCurrency.value?.let {
                val pos = adapter.getPosition(it.text)
                if (pos != RecyclerView.NO_POSITION)
                    ui.spinner.setSelection(pos)
            }
        }

        ui.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                shopVm.currencies.value?.get(p2)?.let {
                    shopVm.setCurrency(it)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }

        viewLifecycleOwner.collectLatestLifecycleFlow(
            shopVm.helperText.filterNotNull()
        ) {
            ui.money.helperText = it.asString(requireContext())
            ui.money.error = null
        }
    }

    private fun observeCards() {
        adp.card.observe(viewLifecycleOwner) {
            shopVm.setAcc(it)
        }

        viewLifecycleOwner.collectLifecycleFlow(
            shopVm.cards.filterNotNull()
        ) {
            adp.submitList(it)
        }
    }

    companion object {
        const val TAG = "ShopBottomSheet"
    }
}
