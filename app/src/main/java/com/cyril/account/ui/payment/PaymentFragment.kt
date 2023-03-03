package com.cyril.account.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cyril.account.MainViewModel
import com.cyril.account.databinding.FragmentPaymentBinding
import com.cyril.account.ui.payment.TransferRecyclerViewAdapter.TransferListener
import com.cyril.account.ui.sticky.StickyHeaderDecoration

class PaymentFragment : Fragment() {
    private val paymentVm: PaymentViewModel by viewModels()
    private val mainVm: MainViewModel by activityViewModels()

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

        displayErrors()
        observeTransfers()
    }

    private fun displayErrors() {
        paymentVm.error.observe(viewLifecycleOwner) {
            mainVm.setUserError(it)
        }
    }

    private fun observeTransfers() {
        val adp = TransferRecyclerViewAdapter(TransferDiffUtil())

        paymentVm.all.observe(viewLifecycleOwner) {
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