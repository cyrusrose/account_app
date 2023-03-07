package com.cyril.account.home.presentation

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.cyril.account.R
import com.cyril.account.databinding.HomeCardSheetBinding
import com.cyril.account.core.presentation.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HomeBottomSheet : BottomSheetDialogFragment() {
    private lateinit var ui: HomeCardSheetBinding
    private val vm: HomeViewModel by navGraphViewModels(R.id.navigation_home)

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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        Log.d("cyrus", "dismissed")
    }

    private fun setOnClicks() {
        ui.def.setOnClickListener {
            vm.setItem(Item.DEFAULT)
            dismiss()
        }

        ui.del.setOnClickListener {
            vm.setItem(Item.DELETE)
            dismiss()
        }
    }

    companion object {
        const val TAG = "HomeBottomSheet"
    }
}

enum class Item {
    DEFAULT, DELETE, NONE
}