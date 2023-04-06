package com.cyril.account.fire.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.R
import com.cyril.account.databinding.FragmentFireBinding
import com.cyril.account.home.presentation.CardDiffUtil
import com.cyril.account.start.presentation.StartViewModel
import com.cyril.account.utils.UiText
import com.google.android.material.snackbar.Snackbar
import com.it.access.util.collectLatestLifecycleFlow
import com.it.access.util.collectLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.filterNotNull
import java.util.*

@AndroidEntryPoint
class FireFragment : Fragment() {
    private val fireVm: FireViewModel by viewModels()
    private val startVm: StartViewModel by hiltNavGraphViewModels(R.id.navigation_start)

    private lateinit var ui: FragmentFireBinding
    private val args: FireFragmentArgs by navArgs()

    private val adp by lazy { MyCardRecyclerViewAdapter(ui.content.rv, CardDiffUtil()) }

    private val getContact = registerForActivityResult(PhoneContact()) {
        it?.let {
            settingUpContact(it)
        }
    }

    private val getPermissions = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted)
            getContact.launch(null)
        else
            fireVm.setUpError(UiText.StringResource(R.string.contacts_title))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentFireBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.collectLatestLifecycleFlow(
            startVm.curUser.filterNotNull()
        ) {
            fireVm.setUser(it)
        }

        setModes()
        displayErrors()
        settingUpNavBar()
        observeCards()
        readContacts()
        makeTransfer()
    }

    private fun displayErrors() {
        viewLifecycleOwner.collectLifecycleFlow(fireVm.error) {
            val snack = Snackbar.make(ui.root, it.asString(requireContext()), Snackbar.LENGTH_SHORT)
            snack.show()
        }

        viewLifecycleOwner.collectLifecycleFlow(fireVm.moneyError) {
            ui.content.money.error = it.asString(requireContext())
        }

        viewLifecycleOwner.collectLifecycleFlow(fireVm.otherError) {
            ui.content.phone.error = it.asString(requireContext())
        }
    }

    private fun setModes() {
        ui.tb.applyInsetter {
            type(statusBars = true) {
                margin(top = true)
            }
            consume(true)
        }
    }

    private fun makeTransfer() {
        ui.content.send.setOnClickListener click@ {
            val moneyStr = ui.content.money.editText?.text.toString()
            val phoneNo = ui.content.phone.editText?.text.toString()

            if (moneyStr.isBlank()) {
                fireVm.setUpMoneyError(UiText.StringResource(R.string.not_empty))
                return@click
            }
            if (phoneNo.isBlank())  {
                fireVm.setUpOtherError(UiText.StringResource(R.string.not_empty))
                return@click
            }

            val money = moneyStr.toBigDecimal()
            adp.card.value?.let {
                fireVm.sendMoneyByPhone(money, UUID.fromString(it.id), phoneNo)
            }
        }

        ui.content.money.editText?.doOnTextChanged { text, _, _, _ ->
            ui.content.money.error = null
        }

        ui.content.phone.editText?.doOnTextChanged { text, _, _, _ ->
            ui.content.phone.error = null
        }
    }

    private fun readContacts() {
        ui.content.phone.setEndIconOnClickListener {
            getPermissions.launch(PERMISSIONS)
        }
    }

    private fun observeCards() {
        ui.content.rv.isNestedScrollingEnabled = false

        viewLifecycleOwner.collectLatestLifecycleFlow(
            fireVm.card.filterNotNull()
        ) {
            adp.submitList(it)
        }
    }


    private fun settingUpNavBar() {
        val nc = findNavController()
        val activity = requireActivity() as MainActivity
        activity.setSupportActionBar(ui.tb)
        val title = args.transfer

        val conf = AppBarConfiguration(
            setOf( R.id.navigation_payment )
        )
        ui.tb.setupWithNavController(nc, conf)
        activity.supportActionBar?.title = title
    }

    private fun settingUpContact(contactUri: Uri) {
        val projection = arrayOf( CommonDataKinds.Phone.NUMBER )

        requireActivity().contentResolver.query(contactUri, projection, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)
                val phone = cursor.getString(idx)

                ui.content.phone.editText?.apply {
                    setText(phone)
                }
            }
        }
    }

    class PhoneContact : ActivityResultContract<Void?, Uri?>() {
        override fun createIntent(context: Context, input: Void?): Intent {
            return Intent(Intent.ACTION_PICK).setType(CommonDataKinds.Phone.CONTENT_TYPE)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
        }
    }

    companion object {
        const val PERMISSIONS = Manifest.permission.READ_CONTACTS
    }
}