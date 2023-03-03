package com.cyril.account

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.cyril.account.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.Insetter.Companion.CONSUME_ALL
import dev.chrisbanes.insetter.applyInsetter

class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var ui: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setModes()
        settingUpNavView()
        displayErrors()
    }

    private fun displayErrors() {
        mainViewModel.error.observe(this) {
            val snack = Snackbar.make(ui.root, it.message, Snackbar.LENGTH_SHORT)
            snack.show()
        }
    }

    private fun setModes() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ui.contentMain.root.applyInsetter {
            type(navigationBars = true) {
                padding(bottom = true)
            }
        }
    }

    private fun padBab(reset: Boolean = false, initPadding: Int) {
        Insetter.builder()
            .setOnApplyInsetsListener { v, windowInsets, _ ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

                if (reset)
                    v.updatePadding(
                        bottom = initPadding
                    )
                else
                    v.updatePadding(
                        bottom = initPadding + insets.bottom
                    )
            }
            .consume(CONSUME_ALL)
            .applyToView(ui.bab)
    }

    private fun settingUpNavView() {
        val nc = findNavController(R.id.nav_host_fragment_activity_main)

        ui.navView.setOnItemSelectedListener {
            val options = NavOptions.Builder()
                .setPopUpTo(R.id.navigation_home, false, true)
                .setLaunchSingleTop(true)
                .setRestoreState(true)

            nc.navigate(it.itemId, bundleOf(), options.build())
            true
        }

        val initPadding = ui.bab.paddingBottom

        mainViewModel.bottomBar.observe(this) {
            ui.navView.visibility = if (it) View.VISIBLE else View.GONE

            if (!it)
               padBab(reset = true, initPadding = initPadding)
            else
                padBab(initPadding = initPadding)
        }

        val items = ui.navView.menu.iterator().asSequence()
            .map { it.itemId }.toList()

        nc.addOnDestinationChangedListener { nc, dest, args ->
            if (dest.id in items) {
                val item = ui.navView.menu.findItem(dest.id)
                item.isChecked = true
            }
//            if (dest.id !in listOf(R.id.navigation_payment))
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }
    }

    private var backPressedTime: Long = 0
    override fun onBackPressed() {
        val nc = findNavController(R.id.nav_host_fragment_activity_main)

        nc.currentDestination?.let { nd ->
            if (nd.id !in listOf(R.id.navigation_home, R.id.navigation_start))
                super.onBackPressed()
            else {
                if (backPressedTime + 3000 > System.currentTimeMillis()) {
                    finish()
                } else {
                    mainViewModel.setUserError(getString(R.string.back_again_title))
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
    }

    companion object {
        const val DEBUG = "cyrus"
    }
}