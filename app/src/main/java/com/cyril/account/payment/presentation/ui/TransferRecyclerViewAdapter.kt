package com.cyril.account.payment.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cyril.account.databinding.PaddingItemBinding
import com.cyril.account.databinding.PaymentItemBinding
import com.cyril.account.databinding.TitleItemBinding
import com.cyril.account.payment.domain.*
import com.cyril.account.utils.sticky.StickyHeader

class TransferRecyclerViewAdapter(util: TransferDiffUtil) : ListAdapter<TransferType, ViewHolder>(util) {
    private var tls: TransferListener? = null

    fun setTransferListener (p: TransferListener) {
        tls = p
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Title -> TITLE
            is Transfer, is Payment -> TRANSFER
            is Padding -> PADDING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TRANSFER -> TransferHolder.from(parent, tls)
            TITLE -> TitleHolder.from(parent)
            else -> PaddingHolder.from(parent)
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is TransferHolder -> holder.bind(getItem(position))
            is TitleHolder -> holder.bind(getItem(position) as Title)
        }
    }

    fun interface TransferListener {
        fun getTransfer(t: Transfer)
        fun getPayment(t: Payment) {}
    }

    class TransferHolder private constructor(
        private val ui: PaymentItemBinding, private val tls: TransferListener?
    ) : ViewHolder(ui.root) {
        fun bind(item: TransferType) {
            if (item is Transfer) {
                with(ui) {
                    myTransferTv.text = item.title
                    myTransferDescriptionTv.text = item.description
                }

                ui.root.setOnClickListener {
                    tls?.getTransfer(item)
                }
            } else if (item is Payment) {
                with(ui) {
                    myTransferTv.text = item.title
                }

                ui.root.setOnClickListener {
                    tls?.getPayment(item)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, tls: TransferListener?) = TransferHolder(
                PaymentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                tls
            )
        }
    }

    class TitleHolder private constructor(
        private val ui: TitleItemBinding
    ) : ViewHolder(ui.root), StickyHeader {
        override var stickyId: Any = ""

        fun bind(item: Title) {
            with(ui) {
                title.text = item.title
            }
            stickyId = item.title
        }

        companion object {
            fun from(parent: ViewGroup) = TitleHolder(
                TitleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    class PaddingHolder private constructor(
        private val ui: PaddingItemBinding
    ): ViewHolder(ui.root) {
        companion object {
            fun from(parent: ViewGroup) = PaddingHolder(
                PaddingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    companion object {
        const val TRANSFER = 0
        const val TITLE = 1
        const val PADDING = 2
    }
}