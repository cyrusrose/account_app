package com.cyril.account.core.presentation

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.cyril.account.R
import com.cyril.account.databinding.ActivityMainBinding
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val ui: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ui.root)

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

        ui.mainContainer.applyInsetter {
            type(navigationBars = true) {
                padding(bottom = true)
            }
        }

        ui.navView.applyInsetter {

        }
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

        mainViewModel.bottomBar.observe(this) {
            ui.navView.visibility = if (it) View.VISIBLE else View.GONE
        }

        val items = ui.navView.menu.iterator().asSequence()
            .map { it.itemId }.toList()

        nc.addOnDestinationChangedListener { nc, dest, args ->
            if (dest.id != R.id.navigation_start) {
                val color = MaterialColors.getColor(ui.navView, R.attr.colorSurface)
                window.navigationBarColor = color
            } else {
                val color = MaterialColors.getColor(ui.navView, R.attr.backgroundColor)
                window.navigationBarColor = color
            }

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