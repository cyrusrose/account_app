package com.cyril.account.home.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.databinding.FragmentHomeBinding
import com.cyril.account.home.domain.Card
import com.cyril.account.start.presentation.StartViewModel
import dev.chrisbanes.insetter.applyInsetter

class HomeFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val startViewModel: StartViewModel by navGraphViewModels(R.id.navigation_start)
    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var ui: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentHomeBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.navigateToHome()

        startViewModel.getUser().observe(viewLifecycleOwner) {
            if (it != null)
                homeViewModel.setUser(it)
        }

        setModes()
        observeCards()
        setUpAppBar()
        displayErrors()
    }

    private fun setModes() {
        ui.content.applyInsetter {
            type(statusBars = true) {
                margin(top = true)
            }
            consume(true)
        }
    }


    private fun displayErrors() {
        homeViewModel.error.observe(viewLifecycleOwner) {
            mainViewModel.setUserError(it)
        }
    }

    private fun setUpAppBar() {
        ui.logout.setOnClickListener {
            val act = HomeFragmentDirections.globalNavigationStart(getString(R.string.bie_title))
            findNavController().navigate(act)
            Log.d(MainActivity.DEBUG, "BIE")

            mainViewModel.navigateToStart()
        }

        ui.profile.setOnClickListener {
            val act = HomeFragmentDirections.actionNavigationHomeToPersonaFragment()
            findNavController().navigate(act)
        }
    }

    private fun observeCards() {
        val cardAdp = CardRecyclerViewAdapter(CardDiffUtil())
        val depAdp = CardRecyclerViewAdapter(CardDiffUtil())
        val clientAccAdp = CardRecyclerViewAdapter(CardDiffUtil())

        with(ui.home.cardRv) {
            adapter = cardAdp
            isNestedScrollingEnabled = false
        }
        with(ui.home.depositRv) {
            adapter = depAdp
            isNestedScrollingEnabled = false
        }
        with(ui.home.accountRv) {
            adapter = clientAccAdp
            isNestedScrollingEnabled = false
        }

        homeViewModel.card.observe(viewLifecycleOwner) {
            cardAdp.submitList(it.cards)
            depAdp.submitList(it.deposits)
            clientAccAdp.submitList(it.clientAccs)
        }

        startViewModel.getUser().observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val list = { card: Card ->
                    val sheet = HomeBottomSheet(user.client, card)
                    sheet.show(parentFragmentManager, HomeBottomSheet.TAG)
                }

                cardAdp.setCardListener(list)
                depAdp.setCardListener(list)
                clientAccAdp.setCardListener(list)
            }
        }
    }

}