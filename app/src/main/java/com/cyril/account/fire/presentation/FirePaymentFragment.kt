package com.cyril.account.fire.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.databinding.FragmentFirePaymentBinding
import com.cyril.account.home.presentation.CardDiffUtil
import com.cyril.account.start.presentation.StartViewModel
import com.it.access.util.collectLatestLifecycleFlow
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.filterNotNull
import java.math.BigDecimal
import java.util.*

class FirePaymentFragment : Fragment() {
    private val fireVm: FireViewModel by viewModels()
    private val mainVm: MainViewModel by activityViewModels()
    private val startVm: StartViewModel by hiltNavGraphViewModels(R.id.navigation_start)

    private lateinit var ui: FragmentFirePaymentBinding
    private val args: FirePaymentFragmentArgs by navArgs()

    private lateinit var adp: MyCardRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentFirePaymentBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.collectLatestLifecycleFlow(
            startVm.curUser.filterNotNull()
        ) {
            fireVm.setUser(it)
        }

        adp = MyCardRecyclerViewAdapter(ui.content.rv, CardDiffUtil())
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
        ui.content.clientNo.editText
            ?.setText(args.clientNo.toString())

        ui.content.send.setOnClickListener {
            val moneyStr = ui.content.money.editText?.text.toString()
            val clientNoStr = ui.content.clientNo.editText?.text.toString()

            if (moneyStr.isNotBlank() && clientNoStr.isNotBlank()) {
                try {
                    val money = moneyStr.toBigDecimal()
                    val clientNo = clientNoStr.toBigInteger()

                    if (money < BigDecimal(0.01))
                        ui.content.money.error = getString(R.string.sum_title)
                    else
                        adp.card.value?.let {
                            fireVm.sendMoneyByClientNo(money, UUID.fromString(it.id), clientNo, args.clientSsn)
                        }
                } catch (e: Exception) {
                    mainVm.setUserError(getString(R.string.strings_title))
                }
            } else {
                if (moneyStr.isBlank())
                    ui.content.money.error = getString(R.string.not_empty)
                if (clientNoStr.isBlank())
                    ui.content.clientNo.error = getString(R.string.not_empty)
            }
        }

        ui.content.money.editText?.doOnTextChanged { text, _, _, _ ->
            ui.content.money.error = null
        }

        ui.content.clientNo.editText?.doOnTextChanged { text, _, _, _ ->
            ui.content.clientNo.error = null
        }
    }

    private fun observeCards() {
        fireVm.card.observe(viewLifecycleOwner) {
            adp.submitList(it)
        }
    }
}