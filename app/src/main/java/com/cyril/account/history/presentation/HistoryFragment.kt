package com.cyril.account.history.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.core.presentation.BindableSpinnerAdapter
import com.cyril.account.databinding.FragmentHistoryBinding
import com.cyril.account.start.presentation.StartViewModel
import com.it.access.util.collectLatestLifecycleFlow
import dev.chrisbanes.insetter.applyInsetter

class HistoryFragment : Fragment() {
    private val mainVm: MainViewModel by activityViewModels()
    private val startVm: StartViewModel by navGraphViewModels(R.id.navigation_start)
    private val histVm: HistoryViewModel by viewModels()

    private lateinit var ui: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentHistoryBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startVm.getUser().observe(viewLifecycleOwner) {
            if (it != null)
                histVm.setUser(it)
        }

        setModes()
        observeHistory()
        searchText()
        displayErrors()
    }

    private fun displayErrors() {
        histVm.error.observe(viewLifecycleOwner) {
            mainVm.setUserError(it)
        }
    }

    private fun setModes() {
        ui.content.applyInsetter {
            type(statusBars = true) {
                margin(top = true)
            }
            consume(true)
        }
    }

    private fun observeHistory() {
        val cardAdp = HistoryRecyclerViewAdapter(HistoryDiffUtil())

        with(ui.contentHistory.historyRv) { adapter = cardAdp }

        histVm.history.observe(viewLifecycleOwner) {
            cardAdp.submitList(it)
        }

        histVm.setItems()
    }

    private fun searchText() {
        val adapter = BindableSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mutableListOf())
        ui.spinner.adapter = adapter

        histVm.items.observe(viewLifecycleOwner) {
            adapter.clear()
            adapter.addAll(it)

            histVm.selectedItem.value?.let {
                val pos = adapter.getPosition(it.text)
                if (pos != RecyclerView.NO_POSITION)
                    ui.spinner.setSelection(pos)
            }
        }

        ui.spinner.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                histVm.selectedItem.value = histVm.items.value?.get(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }

        ui.search.setStartIconOnClickListener {
            histVm.selectedItem.value?.let { state ->
                val via = ui.search.editText?.text.toString().ifBlank { null }
                histVm.setFilter(state.value, via)

                with(requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
                    hideSoftInputFromWindow(it.windowToken, 0)
                }
            }
        }
    }

}
