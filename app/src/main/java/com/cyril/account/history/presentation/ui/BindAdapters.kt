package com.cyril.account.history.presentation.ui

import android.R
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.cyril.account.history.presentation.ui.BindableSpinnerAdapter.SpinnerItem

@BindingAdapter(value = ["app:spinnerItems", "app:selectedSpinnerItem", "app:selectedSpinnerItemAttrChanged"], requireAll = false)
fun setSpinnerItems(spinner: Spinner, spinnerItems: List<SpinnerItem>?, selectedSpinnerItem: SpinnerItem?, listener: InverseBindingListener?) {
    if(spinnerItems == null)
        return

    var selectedItem: SpinnerItem? = null

    if (spinner.selectedItemPosition != NO_POSITION)
        selectedItem = (spinnerItems.get(spinner.selectedItemPosition) as? SpinnerItem)
    if (selectedItem != null && selectedSpinnerItem == selectedItem) {
        return
    }

    spinnerItems.let {
        spinner.adapter = BindableSpinnerAdapter(spinner.context, R.layout.simple_spinner_dropdown_item, it)
        setCurrentSelection(spinner, selectedSpinnerItem)
        setSpinnerListener(spinner, listener)
    }
}

@InverseBindingAdapter(attribute = "app:selectedSpinnerItem")
fun getSelectedSpinnerItem(spinner: Spinner): SpinnerItem {
    return spinner.selectedItem as SpinnerItem
}

fun setSpinnerListener(spinner: Spinner, listener: InverseBindingListener?) {
    listener?.let {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = it.onChange()

            override fun onNothingSelected(adapterView: AdapterView<*>) = it.onChange()
        }
    }
}

fun setCurrentSelection(spinner: Spinner, selectedItem: SpinnerItem?): Boolean {
    selectedItem?.let {
        for (index in 0 until spinner.adapter.count) {
            if (spinner.getItemAtPosition(index) == it.text) {
                spinner.setSelection(index)
                return true
            }
        }
    }

    return false
}