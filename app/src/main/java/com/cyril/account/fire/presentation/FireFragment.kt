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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cyril.account.core.presentation.MainActivity
import com.cyril.account.core.presentation.MainViewModel
import com.cyril.account.R
import com.cyril.account.databinding.FragmentFireBinding
import com.cyril.account.home.presentation.CardDiffUtil
import com.cyril.account.start.presentation.StartViewModel
import dev.chrisbanes.insetter.applyInsetter
import java.math.BigDecimal
import java.util.*

class FireFragment : Fragment() {
    private val fireVm: FireViewModel by viewModels()
    private val mainVm: MainViewModel by activityViewModels()
    private val startVm: StartViewModel by navGraphViewModels(R.id.navigation_start)

    private lateinit var ui: FragmentFireBinding
    private val args: FireFragmentArgs by navArgs()

    private lateinit var adp: MyCardRecyclerViewAdapter

    private val getContact = registerForActivityResult(PhoneContact()) {
        it?.let {
            settingUpContact(it)
        }
    }

    private val getPermissions = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getContact.launch(null)
        } else {
            mainVm.setUserError(getString(R.string.contacts_title))
        }
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

        startVm.getUser().observe(viewLifecycleOwner) {
            if (it != null)
                fireVm.setUser(it)
        }

        adp = MyCardRecyclerViewAdapter(ui.content.rv, CardDiffUtil())

        setModes()
        displayErrors()
        settingUpNavBar()
        observeCards()
        readContacts()
        makeTransfer()
    }

    private fun displayErrors() {
        fireVm.error.observe(viewLifecycleOwner) {
            mainVm.setUserError(it)
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
        ui.content.send.setOnClickListener {
            val moneyStr = ui.content.money.editText?.text.toString()
            val phoneNo = ui.content.phone.editText?.text.toString()

            if (moneyStr.isNotBlank() && phoneNo.isNotBlank()) {
                try {
                    val money = moneyStr.toBigDecimal()
                    val digits = phoneNo.toCharArray()
                        .filter { it.isDigit() }
                    if (digits.size != 11) {
                        ui.content.phone.error = getString(R.string.digits_title)
                        return@setOnClickListener
                    }

                    val itr = digits.iterator()
                    val phone = "+@ (@@@) @@@-@@-@@".map {
                        if (it == '@' && itr.hasNext())
                            itr.next()
                        else
                            it
                    }.joinToString(separator = "")

                    if (money < BigDecimal(0.01))
                        ui.content.money.error = getString(R.string.sum_title)
                    else
                        adp.card.value?.let {
                            fireVm.sendMoneyByPhone(money, UUID.fromString(it.id), phone)
                        }
                } catch (e: Exception) {
                    mainVm.setUserError(getString(R.string.strings_title))
                }
            } else {
                if (moneyStr.isBlank())
                    ui.content.money.error = getString(R.string.not_empty)
                if (phoneNo.isBlank())
                    ui.content.phone.error = getString(R.string.not_empty)
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
        fireVm.card.observe(viewLifecycleOwner) {
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
        ui.tb.title = title
    }

    private fun settingUpContact(contactUri: Uri) {
        val projection = arrayOf( CommonDataKinds.Phone.NUMBER )

        requireActivity().contentResolver.query(contactUri, projection, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)
                val phone = cursor.getString(idx)

                with(ui.content.phone.editText) {
                    this?.setText(phone)
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