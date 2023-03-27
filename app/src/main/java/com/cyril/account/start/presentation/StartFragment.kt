package com.cyril.account.start.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.databinding.FragmentStartBinding
import com.google.android.material.snackbar.Snackbar
import com.it.access.util.collectLatestLifecycleFlow
import com.it.access.util.collectLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class StartFragment : Fragment() {
    private val startVm: StartViewModel by hiltNavGraphViewModels(R.id.navigation_start)
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var ui: FragmentStartBinding
    private val args: StartFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentStartBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.navigateToStart()

        login()
        errors()
    }

    private fun displayError(msg: String) {
        val snack = Snackbar.make(ui.root, msg, Snackbar.LENGTH_SHORT)
        snack.show()
    }

    private fun errors() {
        args.error?.let {
            startVm.showError(it)
        }

        viewLifecycleOwner.collectLifecycleFlow(startVm.error) {
            displayError(it.asString(requireContext()))
        }
    }

    private fun login() {
        val nc = findNavController()

        viewLifecycleOwner.collectLatestLifecycleFlow(
            startVm.curUser.filterNotNull()
        ) {
            val act = StartFragmentDirections.actionStartToHome()
            nc.navigate(act)
        }

        ui.enter.setOnClickListener {
            val login = ui.login.editText?.text.toString()
            val password = ui.password.editText?.text.toString()

            if (login.isNotBlank() && password.isNotBlank()) {
                startVm.getUser(login, password)
            } else {
                if (login.isBlank())
                    ui.login.error = getString(R.string.not_empty)
                if (password.isBlank())
                    ui.password.error = getString(R.string.not_empty)
            }
        }

        ui.login.editText?.doOnTextChanged { text, _, _, _ ->
            ui.login.error = null
        }

        ui.password.editText?.doOnTextChanged { text, _, _, _ ->
            ui.password.error = null
        }
    }

}