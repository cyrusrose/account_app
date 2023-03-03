package com.cyril.account.ui.home

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.cyril.account.databinding.CardItemBinding


class CardRecyclerViewAdapter(util: CardDiffUtil) : ListAdapter<Card, CardRecyclerViewAdapter.ViewHolder>(util) {
    private var ls: CardListener? = null

    fun setCardListener (p: CardListener) {
        ls = p
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, ls)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    fun interface CardListener {
        fun getCard(t: Card)
    }

    class ViewHolder private constructor(
        private val ui: CardItemBinding, private val ls: CardListener?
    ) : RecyclerView.ViewHolder(ui.root) {
        fun bind(item: Card) {
            with(ui) {
                cardNameTitle.text = item.title
                cardContent.text = item.content
                cardBackground.setBackgroundColor(item.color)
                cardImage.setImageResource(item.imageId)
            }

            ui.root.setOnClickListener {
                ls?.getCard(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup, ls: CardListener?): ViewHolder {
                return ViewHolder(
                    CardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    ls
                )
            }
        }
    }

}