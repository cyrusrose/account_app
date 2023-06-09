package com.cyril.account.history.presentation

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.cyril.account.databinding.HistoryItemBinding
import com.cyril.account.history.domain.History

class HistoryRecyclerViewAdapter(
    private val context: Context,
    util: HistoryDiffUtil
): ListAdapter<History, RecyclerView.ViewHolder>(util) {
    private var ls: HistoryListener? = null

    fun setHistoryListener (p: HistoryListener) {
        ls = p
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent, context, ls)
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
        val ui: HistoryItemBinding,
        private val context: Context,
        private val ls: HistoryListener?
    ): RecyclerView.ViewHolder(ui.root) {
        fun bind(item: History) {
            with(ui) {
                historyNameTitle.text = item.title
                historyMoney.text = item.money
                time.text = item.time
                historyContent.text = item.content.asString(context)
            }

            ui.root.setOnClickListener {
                ls?.getHistory(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup, context: Context, ls: HistoryListener?): ViewHolder {
                return ViewHolder(
                    HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    context,
                    ls
                )
            }
        }
    }
}