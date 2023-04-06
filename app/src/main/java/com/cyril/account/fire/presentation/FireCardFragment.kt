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
import com.cyril.account.databinding.FragmentFireCardBinding
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
class FireCardFragment : Fragment() {
    private val fireVm: FireViewModel by viewModels()
    private val startVm: StartViewModel by hiltNavGraphViewModels(R.id.navigation_start)

    private lateinit var ui: FragmentFireCardBinding
    private val args: FireCardFragmentArgs by navArgs()

    private val adp by lazy { MyCardRecyclerViewAdapter(ui.content.rv, CardDiffUtil()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentFireCardBinding.inflate(inflater, container, false)
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

        viewLifecycleOwner.collectLifecycleFlow(fireVm.otherError) {
            ui.content.card.error = it.asString(requireContext())
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
            val cardNoStr = ui.content.card.editText?.text.toString()

            if (moneyStr.isBlank()) {
                fireVm.setUpMoneyError(UiText.StringResource(R.string.not_empty))
                return@click
            }
            if (cardNoStr.isBlank())  {
                fireVm.setUpOtherError(UiText.StringResource(R.string.not_empty))
                return@click
            }

            val money = moneyStr.toBigDecimal()
            val cardNo = cardNoStr.toBigInteger()
            adp.card.value?.let {
                fireVm.sendMoneyByCard(money, UUID.fromString(it.id), cardNo)
            }

        }

        ui.content.money.editText?.doOnTextChanged { text, _, _, _ ->
            ui.content.money.error = null
        }

        ui.content.card.editText?.doOnTextChanged { text, _, _, _ ->
            ui.content.card.error = null
        }
    }

    private fun observeCards() {
        ui.content.rv.isNestedScrollingEnabled = false

        viewLifecycleOwner.collectLatestLifecycleFlow(
            fireVm.card.filterNotNull()
        ) {
            adp.submitList(it)
        }
    }
}