package com.cyril.account.home.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.cyril.account.databinding.HomeCardSheetBinding
import com.cyril.account.core.data.response.ClientResp
import com.cyril.account.home.presentation.HomeViewModel
import com.cyril.account.home.domain.Card
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class HomeBottomSheet(val client: ClientResp, val card: Card) : BottomSheetDialogFragment() {
    private lateinit var ui: HomeCardSheetBinding
    private val vm: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = HomeCardSheetBinding.inflate(inflater,  container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClicks()
    }

    private fun displayError(it: String) {
        val snack = Snackbar.make(ui.root, it, Snackbar.LENGTH_SHORT)
        snack.show()
    }

    private fun setOnClicks() {
        ui.def.setOnClickListener {
            vm.changeDefault(client, card)
            dismiss()
        }

        ui.del.setOnClickListener {
            vm.delPersonal(client, card)
            dismiss()
        }
    }

    companion object {
        const val TAG = "HomeBottomSheet"
    }
}
