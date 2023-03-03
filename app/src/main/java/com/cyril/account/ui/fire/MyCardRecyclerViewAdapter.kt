package com.cyril.account.ui.fire

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.selection.*
import androidx.recyclerview.selection.SelectionTracker.SelectionObserver
import androidx.recyclerview.widget.ListAdapter
import com.cyril.account.R
import com.cyril.account.databinding.MyCardItemBinding
import com.cyril.account.ui.home.Card
import com.cyril.account.ui.home.CardDiffUtil


class MyCardRecyclerViewAdapter(rv: RecyclerView, util: CardDiffUtil) : ListAdapter<Card, MyCardRecyclerViewAdapter.ViewHolder>(util) {
    private val _card = MutableLiveData<Card>()
    val card: LiveData<Card> = _card
    private val tracker: SelectionTracker<String>

    init {
        with(rv) {
            adapter = this@MyCardRecyclerViewAdapter

            val provider = MyItemKeyProvider(this@MyCardRecyclerViewAdapter)
            tracker = SelectionTracker.Builder<String>(
                "mySelection",
                this,
                provider,
                MyItemDetailsLookup(this),
                StorageStrategy.createStringStorage()
            ).withSelectionPredicate(
                MyPredicate()
            ).build()
        }
    }

    override fun submitList(list: List<Card>?) {
        if (currentList.isEmpty()) list?.forEach {
            if (it.isDefault) {
                it.isChecked = true
                tracker.let { sel ->
                    sel.select(it.id)
                    _card.value = it
                }

                return@forEach
            }

        }

        super.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        tracker.let {
            holder.bind(item, it.isSelected(item.id))
        }

    }

    inner class MyPredicate(): SelectionTracker.SelectionPredicate<String>() {
        private val provider = MyItemKeyProvider(this@MyCardRecyclerViewAdapter)

        override fun canSetStateForKey(key: String, nextState: Boolean): Boolean {
            return canSetStateAtPosition(provider.getPosition(key), nextState)
        }

        override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
            if (position == RecyclerView.NO_POSITION)
                return true

            val item = getItem(position)

            return if (!item.isChecked) {
                item.isChecked = true
                true
            } else
                nextState
        }

        override fun canSelectMultiple() = false
    }

    class MyItemKeyProvider(private val adapter: MyCardRecyclerViewAdapter) : ItemKeyProvider<String>(SCOPE_CACHED) {
        override fun getKey(position: Int): String =
            adapter.currentList[position].id
        override fun getPosition(key: String): Int =
            adapter.currentList.indexOfFirst {it.id == key}
    }

    class MyItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as ViewHolder).getItemDetails()
            }
            return null
        }
    }

    inner class ViewHolder private constructor(
        private val ui: MyCardItemBinding
    ) : RecyclerView.ViewHolder(ui.root) {
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = bindingAdapterPosition
                override fun getSelectionKey(): String = getItem(bindingAdapterPosition).id
                override fun inSelectionHotspot(e: MotionEvent) = true // single tap

            }


        fun bind(item: Card, isSelected: Boolean) {
            with(ui) {
                cardNameTitle.text = item.title
                cardContent.text = item.content
                cardBackground.setBackgroundColor(item.color)
                cardImage.setImageResource(item.imageId)
                checkImage.setImageResource(
                    if (isSelected) R.drawable.ic_check
                    else R.drawable.ic_unchecked
                )

                if (isSelected)
                    _card.value = item
            }
        }

        constructor(parent: ViewGroup) : this(
            MyCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

}