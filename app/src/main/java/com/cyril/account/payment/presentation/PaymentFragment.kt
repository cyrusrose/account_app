package com.cyril.account.payment.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.databinding.FragmentPaymentBinding
import com.cyril.account.payment.domain.Payment
import com.cyril.account.payment.domain.Transfer
import com.cyril.account.payment.presentation.TransferRecyclerViewAdapter.TransferListener
import com.cyril.account.utils.sticky.StickyHeaderDecoration
import com.google.android.material.snackbar.Snackbar
import com.it.access.util.collectLatestLifecycleFlow
import com.it.access.util.collectLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class PaymentFragment : Fragment() {
    private val paymentVm: PaymentViewModel by viewModels()

    private lateinit var ui: FragmentPaymentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentPaymentBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setModes()
        displayErrors()
        observeTransfers()
    }

    private fun setModes() {
        ui.root.applyInsetter {
            type(statusBars = true) {
                padding(top = true)
            }
        }
    }

    private fun displayErrors() {
        viewLifecycleOwner.collectLifecycleFlow(paymentVm.error) {
            val snack = Snackbar.make(ui.root, it.asString(requireContext()), Snackbar.LENGTH_SHORT)
            snack.show()
        }
    }

    private fun observeTransfers() {
        paymentVm.setUpTransfers(requireContext().assets)
        val adp = TransferRecyclerViewAdapter(TransferDiffUtil())

        viewLifecycleOwner.collectLatestLifecycleFlow(paymentVm.all.filterNotNull()) {
            adp.submitList(it)
        }

        val nc = findNavController()

        adp.setTransferListener( object: TransferListener {
            override fun getTransfer(t: Transfer) {
                val act = when (t.id) {
                    BY_PHONE -> PaymentFragmentDirections.actionPaymentToFire(transfer = t.title)
                    BY_CARD -> PaymentFragmentDirections.actionPaymentToFireCard(transfer = t.title)
                    EXCHANGE -> PaymentFragmentDirections.actionPaymentToFireAccounts(transfer = t.title)
                    else -> null
                }
                act?.let {
                    nc.navigate(it)
                }
            }

            override fun getPayment(t: Payment) {
                val act = PaymentFragmentDirections.actionPaymentToFirePayment(
                    transfer = t.title,
                    clientNo = t.clientNo,
                    clientSsn = t.clientSsn
                )
                nc.navigate(act)
            }
        })

        with(ui.transferRv) {
            adapter = adp
            addItemDecoration(StickyHeaderDecoration())
        }
    }

    companion object {
        const val BY_PHONE = 1
        const val BY_CARD = 2
        const val EXCHANGE = 3
    }
}