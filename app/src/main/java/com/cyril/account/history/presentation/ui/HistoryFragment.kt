package com.cyril.account.history.presentation.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.databinding.FragmentHistoryBinding
import com.cyril.account.history.domain.*
import com.cyril.account.history.presentation.HistoryViewModel
import com.cyril.account.start.presentation.StartViewModel
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
        ui.lifecycleOwner = viewLifecycleOwner
        ui.vm = histVm
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
            cardAdp.submitList(
                listOf<HistoryType>() + it + Padding
            )
        }

        histVm.setItems()
    }

    private fun searchText() {
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
