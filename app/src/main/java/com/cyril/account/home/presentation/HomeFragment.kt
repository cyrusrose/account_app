package com.cyril.account.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.databinding.FragmentHomeBinding
import com.cyril.account.home.domain.Card
import com.cyril.account.start.presentation.StartViewModel
import com.cyril.account.utils.DEBUG
import com.cyril.account.utils.UiText
import com.google.android.material.snackbar.Snackbar
import com.it.access.util.collectLatestLifecycleFlow
import com.it.access.util.collectLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val mainVm: MainViewModel by activityViewModels()
    private val startVm: StartViewModel by hiltNavGraphViewModels(R.id.navigation_start)
    private val homeVm: HomeViewModel by hiltNavGraphViewModels(R.id.navigation_home)

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

        mainVm.navigateToHome()

        viewLifecycleOwner.collectLatestLifecycleFlow(
            startVm.curUser.filterNotNull()
        ) {
            homeVm.setUser(it)
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
        viewLifecycleOwner.collectLifecycleFlow(homeVm.error) {
            val snack = Snackbar.make(ui.root, it.asString(requireContext()), Snackbar.LENGTH_SHORT)
            snack.show()
        }
    }

    private fun setUpAppBar() {
        ui.logout.setOnClickListener {
            val act = HomeFragmentDirections.globalNavigationStart(getString(R.string.bie_title))
            findNavController().navigate(act)
            mainVm.navigateToStart()
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

        viewLifecycleOwner.collectLatestLifecycleFlow(
            homeVm.card.filterNotNull()
        ) {
            cardAdp.submitList(it.cards)
            depAdp.submitList(it.deposits)
            clientAccAdp.submitList(it.clientAccs)
        }

        val list = { card: Card ->
            homeVm.setCard(card)

            findNavController().apply {
                if (currentDestination?.id == R.id.navigation_home) {
                    val act = HomeFragmentDirections.actionNavigationHomeToHomeBottomSheet()
                    navigate(act)
                }
            }
            Unit
        }

        cardAdp.setCardListener(list)
        depAdp.setCardListener(list)
        clientAccAdp.setCardListener(list)
    }

}