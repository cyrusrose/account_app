package com.cyril.account.ui.fire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cyril.account.MainActivity
import com.cyril.account.MainViewModel
import com.cyril.account.R
import com.cyril.account.databinding.FragmentAccountsBinding
import com.cyril.account.ui.home.CardDiffUtil
import com.cyril.account.ui.home.HomeFragmentDirections
import com.cyril.account.ui.start.StartViewModel
import dev.chrisbanes.insetter.applyInsetter
import java.math.BigDecimal
import java.util.*


class FireAccountsFragment : Fragment() {
    private val fireVm: FireViewModel by viewModels()
    private val mainVm: MainViewModel by activityViewModels()
    private val startVm: StartViewModel by navGraphViewModels(R.id.navigation_start)

    private lateinit var ui: FragmentAccountsBinding
    private val args: FireAccountsFragmentArgs by navArgs()

    private lateinit var adpFrom: MyCardRecyclerViewAdapter
    private lateinit var adpTo: MyCardRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentAccountsBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startVm.getUser().observe(viewLifecycleOwner) {
            if (it != null)
                fireVm.setUser(it)
        }

        adpFrom = MyCardRecyclerViewAdapter(ui.content.fromRv, CardDiffUtil())
        adpTo = MyCardRecyclerViewAdapter(ui.content.toRv, CardDiffUtil())

        setModes()
        displayErrors()
        settingUpNavBar()
        observeCards()
        makingTransfer()
    }

    private fun displayErrors() {
        fireVm.error.observe(viewLifecycleOwner) {
            mainVm.setUserError(it)
        }
    }

    private fun setModes() {
        ui.tb.applyInsetter {
            type(statusBars = true) {
                margin(top = true)
            }
            consume(true)
        }
    }

    private fun settingUpNavBar() {
        val nc = findNavController()
        val activity = requireActivity() as MainActivity
        activity.setSupportActionBar(ui.tb)
        val title = args.transfer

        val conf = AppBarConfiguration(
            setOf( R.id.navigation_payment )
        )
        ui.tb.setupWithNavController(nc, conf)
        ui.tb.title = title
    }

    private fun makingTransfer() {
        ui.content.send.setOnClickListener {
            val moneyStr = ui.content.money.editText?.text.toString()

            if (moneyStr.isNotBlank()) {
                try {
                    val money = moneyStr.toBigDecimal()

                    if (money < BigDecimal(0.01))
                        ui.content.money.error = getString(R.string.sum_title)
                    else {
                        val fromCard = adpFrom.card.value
                        val toCard = adpTo.card.value
                        if (fromCard != null && toCard != null) {
                            fireVm.sendMoney(money, UUID.fromString(fromCard.id), UUID.fromString(toCard.id))
                        }
                    }
                } catch (e: Exception) {
                    mainVm.setUserError(getString(R.string.strings_title))
                }
            } else {
                if (moneyStr.isBlank())
                    ui.content.money.error = getString(R.string.not_empty)
            }

        }

        ui.content.money.editText?.doOnTextChanged { text, _, _, _ ->
            ui.content.money.error = null
        }
    }

    private fun observeCards() {
        fireVm.card.observe(viewLifecycleOwner) {
            adpFrom.submitList(it)
            adpTo.submitList(it)
        }
    }
}