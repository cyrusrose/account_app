package com.cyril.account.shopwindow.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.databinding.FragmentShopwindowBinding
import com.cyril.account.home.domain.Card
import com.cyril.account.home.presentation.CardDiffUtil
import com.cyril.account.home.presentation.CardRecyclerViewAdapter
import com.cyril.account.home.presentation.HomeFragmentDirections
import com.cyril.account.start.presentation.StartViewModel
import com.it.access.util.collectLatestLifecycleFlow
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull

class ShopWindowFragment : Fragment() {
    private val mainVm: MainViewModel by activityViewModels()
    private val startVm: StartViewModel by hiltNavGraphViewModels(R.id.navigation_start)
    private val shopVm: ShopWindowViewModel by viewModels()
    private val sheetVm: ShopSheetViewModel by navGraphViewModels(R.id.navigation_shopwindow)

    private lateinit var ui: FragmentShopwindowBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentShopwindowBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainVm.navigateToHome()

        viewLifecycleOwner.collectLatestLifecycleFlow(
            startVm.curUser.filterNotNull()
        ) {
            shopVm.setUser(it)
            sheetVm.setUser(it)
        }

        setModes()
        observeCards()
        displayErrors()
    }

    private fun setModes() {
        ui.content.applyInsetter {
            type(statusBars = true) {
                padding(top = true)
            }
            consume(true)
        }
    }

    private fun displayErrors() {
        shopVm.error.observe(viewLifecycleOwner) {
            mainVm.setUserError(it)
        }
    }

    private fun observeCards() {
        val cardAdp = CardRecyclerViewAdapter(CardDiffUtil())
        val depAdp = CardRecyclerViewAdapter(CardDiffUtil())
        val clientAccAdp = CardRecyclerViewAdapter(CardDiffUtil())

        with(ui.card) { adapter = cardAdp }
        with(ui.deposit) { adapter = depAdp }
        with(ui.account) { adapter = clientAccAdp }

        shopVm.accs.observe(viewLifecycleOwner) {
            cardAdp.submitList(it.cards)
            depAdp.submitList(it.deposits)
            clientAccAdp.submitList(it.clientAccs)
        }

        val list = { card: Card ->
            sheetVm.setCard(card)

            findNavController().apply {
                if (currentDestination?.id == R.id.navigation_shopwindow) {
                    val act = ShopWindowFragmentDirections.actionNavigationShopwindowToShopBottomSheet()
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