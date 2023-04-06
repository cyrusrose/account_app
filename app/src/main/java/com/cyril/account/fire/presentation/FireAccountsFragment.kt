package com.cyril.account.fire.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cyril.account.R
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.databinding.FragmentAccountsBinding
import com.cyril.account.home.presentation.CardDiffUtil
import com.cyril.account.start.presentation.StartViewModel
import com.cyril.account.utils.UiText
import com.google.android.material.snackbar.Snackbar
import com.it.access.util.collectLatestLifecycleFlow
import com.it.access.util.collectLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.filterNotNull
import java.util.*

@AndroidEntryPoint
class FireAccountsFragment : Fragment() {
    private val fireVm: FireViewModel by viewModels()
    private val startVm: StartViewModel by hiltNavGraphViewModels(R.id.navigation_start)

    private lateinit var ui: FragmentAccountsBinding
    private val args: FireAccountsFragmentArgs by navArgs()

    private val adpFrom by lazy { MyCardRecyclerViewAdapter(ui.content.fromRv, CardDiffUtil()) }
    private val adpTo by lazy { MyCardRecyclerViewAdapter(ui.content.toRv, CardDiffUtil()) }

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

        viewLifecycleOwner.collectLatestLifecycleFlow(
            startVm.curUser.filterNotNull()
        ) {
            fireVm.setUser(it)
        }

        setModes()
        displayErrors()
        settingUpNavBar()
        observeCards()
        makingTransfer()
    }

    private fun displayErrors() {
        viewLifecycleOwner.collectLifecycleFlow(fireVm.error) {
            val snack = Snackbar.make(ui.root, it.asString(requireContext()), Snackbar.LENGTH_SHORT)
            snack.show()
        }

        viewLifecycleOwner.collectLifecycleFlow(fireVm.moneyError) {
            ui.content.money.error = it.asString(requireContext())
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
        activity.supportActionBar?.title = title
    }

    private fun makingTransfer() {
        ui.content.send.setOnClickListener click@ {
            val moneyStr = ui.content.money.editText?.text.toString()

            if (moneyStr.isBlank()) {
                fireVm.setUpMoneyError(UiText.StringResource(R.string.not_empty))
                return@click
            }

            val money = moneyStr.toBigDecimal()
            val fromCard = adpFrom.card.value
            val toCard = adpTo.card.value
            if (fromCard != null && toCard != null) {
                fireVm.sendMoney(money, UUID.fromString(fromCard.id), UUID.fromString(toCard.id))
            }
        }

        ui.content.money.editText?.doOnTextChanged { text, _, _, _ ->
            ui.content.money.error = null
        }
    }

    private fun observeCards() {
        ui.content.fromRv.isNestedScrollingEnabled = false
        ui.content.toRv.isNestedScrollingEnabled = false

        viewLifecycleOwner.collectLatestLifecycleFlow(
            fireVm.card.filterNotNull()
        ) {
            adpFrom.submitList(it)
            adpTo.submitList(it)
        }
    }
}