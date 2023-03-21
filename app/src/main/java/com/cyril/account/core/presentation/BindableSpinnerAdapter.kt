package com.cyril.account.core.presentation


import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class BindableSpinnerAdapter (context: Context, textViewResourceId: Int, private val values: List<SpinnerItem>) :
    ArrayAdapter<BindableSpinnerAdapter.SpinnerItem>(context, textViewResourceId, values) {

    override fun getCount() = values.size

    override fun getItem(position: Int) = values[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getView(position, convertView, parent) as TextView
        label.text = values[position].text
        return label
    }

    fun getPosition(text: String): Int {
        for ((elem, pos) in values.zip(0..count)) {
            if (elem.text == text)
                return pos
        }
        return RecyclerView.NO_POSITION
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getDropDownView(position, convertView, parent) as TextView
        label.text = values[position].text
        return label
    }

    data class SpinnerItem(val value: String, val text: String)
}