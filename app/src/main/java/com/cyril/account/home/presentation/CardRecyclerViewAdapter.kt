package com.cyril.account.home.presentation

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.cyril.account.databinding.CardItemBinding
import com.cyril.account.home.domain.Card


class CardRecyclerViewAdapter(util: CardDiffUtil, private val context: Context? = null) : ListAdapter<Card, CardRecyclerViewAdapter.ViewHolder>(util) {
    private var ls: CardListener? = null

    fun setCardListener (p: CardListener) {
        ls = p
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, ls, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    fun interface CardListener {
        fun getCard(t: Card)
    }

    class ViewHolder private constructor(
        private val ui: CardItemBinding, private val ls: CardListener?,
        private val context: Context? = null
    ) : RecyclerView.ViewHolder(ui.root) {
        fun bind(item: Card) {
            with(ui) {
                cardNameTitle.text = item.title
                if(item.content.isNotBlank())
                    cardContent.text = item.content
                else if(item.contentList != null && context != null)
                    cardContent.text = item.contentList.joinToString(separator = " ") {
                        it.asString(context)
                    }
                cardBackground.setBackgroundColor(item.color)
                cardImage.setImageResource(item.imageId)
            }

            ui.root.setOnClickListener {
                ls?.getCard(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup, ls: CardListener?, context: Context? = null): ViewHolder {
                return ViewHolder(
                    CardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    ls,
                    context
                )
            }
        }
    }

}