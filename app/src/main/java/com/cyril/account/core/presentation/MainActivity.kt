package com.cyril.account.core.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.cyril.account.R
import com.cyril.account.databinding.ActivityMainBinding
import com.cyril.account.utils.DEBUG
import com.cyril.account.utils.UiText
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.it.access.util.collectLatestLifecycleFlow
import com.it.access.util.collectLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainVm: MainViewModel by viewModels()
    private val ui: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ui.root)

        setModes()
        displayErrors()
        settingUpNavView()
    }

    private fun displayErrors() {
        collectLifecycleFlow(mainVm.error) {
            val snack = Snackbar.make(ui.root, it.asString(this), Snackbar.LENGTH_SHORT)
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
            // avoid excess padding
        }
    }

    private fun settingUpNavView() {
        val nc = findNavController(R.id.nav_host_fragment_activity_main)

        ui.navView.setOnItemSelectedListener {
            it.onNavDestinationSelected(nc)
                    val options = NavOptions.Builder()
                .setPopUpTo(R.id.navigation_home, false, true)
                .setLaunchSingleTop(true)
                .setRestoreState(true)

            nc.navigate(it.itemId, bundleOf(), options.build())
            true
        }

        collectLifecycleFlow(mainVm.bottomBar) {
            Log.d(DEBUG, "bottomBar: $it")
            ui.navView.visibility = if (it) View.VISIBLE else View.GONE
        }

        val items = ui.navView.menu.iterator().asSequence()
            .map { it.itemId }.toList()

        nc.addOnDestinationChangedListener { nc, dest, args ->
            Log.d(DEBUG, nc.currentDestination?.displayName ?: "name")

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
                    mainVm.setUpError(UiText.StringResource(R.string.back_again_title))
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
    }
}