package com.cyril.account.history.presentation.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.cyril.account.databinding.HistoryItemBinding
import com.cyril.account.databinding.PaddingItemBinding
import com.cyril.account.history.domain.History
import com.cyril.account.history.domain.HistoryType
import com.cyril.account.history.domain.Padding

class HistoryRecyclerViewAdapter(
    util: HistoryDiffUtil
): ListAdapter<HistoryType, RecyclerView.ViewHolder>(util) {
    private var ls: HistoryListener? = null

    fun setHistoryListener (p: HistoryListener) {
        ls = p
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HISTORY -> ViewHolder.from(parent, ls)
            else -> PaddingHolder.from(parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is History -> HISTORY
            is Padding -> PADDING
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(getItem(position) as History)
        }
    }

    fun interface HistoryListener {
        fun getHistory(h: History)
    }

    class ViewHolder private constructor(
        val ui: HistoryItemBinding, private val ls: HistoryListener?
    ): RecyclerView.ViewHolder(ui.root) {
        fun bind(item: History) {
            with(ui) {
                historyNameTitle.text = item.title
                historyMoney.text = item.money
                time.text = item.time
                historyContent.text = item.content
            }

            ui.root.setOnClickListener {
                ls?.getHistory(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup, ls: HistoryListener?): ViewHolder {
                return ViewHolder(
                    HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    ls
                )
            }
        }
    }


    class PaddingHolder private constructor(
        private val ui: PaddingItemBinding
    ): RecyclerView.ViewHolder(ui.root) {
        companion object {
            fun from(parent: ViewGroup) = PaddingHolder(
                PaddingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    companion object {
        const val HISTORY = 0
        const val PADDING = 1
    }
}