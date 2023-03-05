package com.cyril.account.start.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.databinding.FragmentStartBinding
import com.google.android.material.snackbar.Snackbar

class StartFragment : Fragment() {
    private val startViewModel: StartViewModel by navGraphViewModels(R.id.navigation_start)
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

        setModes()
        login()
        errors()
    }

    private fun setModes() {

    }

    private fun errors() {
        args.error?.let {
            mainViewModel.setUserError(it)
        }

        startViewModel.userError.observe(viewLifecycleOwner) {
            mainViewModel.setUserError(it)
        }
    }

    private fun login() {

        val nc = findNavController()

        startViewModel.getUser("", "").observe(viewLifecycleOwner) {
            if (it !== null) {
                Log.d(MainActivity.DEBUG, "To Sign In")
                val act = StartFragmentDirections.actionStartToHome()
                nc.navigate(act)
            } else
                Snackbar.make(ui.root, getString(R.string.failed_to_sign_in), Snackbar.LENGTH_SHORT).show()
        }

        ui.enter.setOnClickListener {
            val login = ui.login.editText?.text.toString()
            val password = ui.password.editText?.text.toString()

            if (login.isNotBlank() && password.isNotBlank()) {
                startViewModel.getUser(login, password)
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