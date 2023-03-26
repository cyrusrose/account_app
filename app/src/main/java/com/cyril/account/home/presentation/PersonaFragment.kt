package com.cyril.account.home.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.R
import com.cyril.account.databinding.FragmentPersonaBinding
import com.cyril.account.start.presentation.StartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonaFragment : Fragment() {
    private lateinit var ui: FragmentPersonaBinding
    private val startVm: StartViewModel by navGraphViewModels(R.id.navigation_start)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentPersonaBinding.inflate(inflater, container, false)
        ui.contentPersona.lifecycleOwner = viewLifecycleOwner
        ui.contentPersona.vm = startVm
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingUpNavBar()
    }

    private fun settingUpNavBar() {
        val nc = findNavController()
        val activity = requireActivity() as MainActivity
        activity.setSupportActionBar(ui.fireTb)

        val conf = AppBarConfiguration( setOf( R.id.navigation_home ) )
        ui.fireTb.setupWithNavController(nc, conf)
        ui.fireTb.title = null
    }
}