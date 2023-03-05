package com.cyril.account.shopwindow.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.databinding.FragmentShopwindowBinding
import com.cyril.account.home.domain.Card
import com.cyril.account.home.presentation.CardDiffUtil
import com.cyril.account.home.presentation.CardRecyclerViewAdapter
import com.cyril.account.start.presentation.StartViewModel
import dev.chrisbanes.insetter.applyInsetter

class ShopWindowFragment : Fragment() {
    private val mainVm: MainViewModel by activityViewModels()
    private val startVm: StartViewModel by navGraphViewModels(R.id.navigation_start)
    private val shopVm: ShopWindowViewModel by viewModels()

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

        startVm.getUser().observe(viewLifecycleOwner) {
            if (it != null)
                shopVm.setUser(it)
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

        startVm.getUser().observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val list = { card: Card ->
                    val sheet = ShopBottomSheet(user, card)
                    sheet.show(parentFragmentManager, ShopBottomSheet.TAG)
                }

                cardAdp.setCardListener(list)
                depAdp.setCardListener(list)
                clientAccAdp.setCardListener(list)
            }
        }
    }
}