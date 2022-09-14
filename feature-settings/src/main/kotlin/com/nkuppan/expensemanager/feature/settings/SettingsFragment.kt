package com.nkuppan.expensemanager.feature.settings

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesActivity
import com.google.android.play.core.review.ReviewManagerFactory
import com.nkuppan.expensemanager.core.ui.extensions.enable
import com.nkuppan.expensemanager.core.ui.extensions.setAllOnClickListener
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingFragment
import com.nkuppan.expensemanager.feature.settings.databinding.FragmentSettingsBinding
import com.nkuppan.expensemanager.feature.settings.utils.openEmailToOption
import com.nkuppan.expensemanager.feature.settings.utils.openWebPage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class SettingsFragment : BaseBindingFragment<FragmentSettingsBinding>() {

    private val directoryPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                val directory = DocumentFile.fromTreeUri(requireContext(), uri)
                directory?.createFile("xls", "sample.xls")?.let { excelFile ->
                    viewModel.exportAsDatabase(excelFile)
                }
            }
        }
    }

    private val locationPermissionReceiver = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionResult ->
        if (permissionResult.values.none { it }) {
            launchDirectoryPicker()
        }
    }

    private val viewModel: SettingsViewModel by viewModels()

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        initObservers()

        initClickListeners()
    }

    private fun initClickListeners() {

        binding.themeContainer.setAllOnClickListener {
            viewModel.theme.value.let {
                findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToThemeDialogFragment(
                        it
                    )
                )
            }
        }

        binding.currencyContainer.setAllOnClickListener {
            viewModel.currency.value.let {
                findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToCurrencyDialogFragment(
                        it
                    )
                )
            }
        }

        binding.setReminderContainer.setAllOnClickListener {
            viewModel.reminderStatus.value.let {
                viewModel.updateReminderStatus(!it)
            }
        }

        binding.spreadSheetExportContainer.setAllOnClickListener {
            locationPermissionReceiver.launch(getStoragePermissions())
        }

        binding.exportAsDbContainer.setAllOnClickListener {
            launchDirectoryPicker()
        }

        binding.importAsDbContainer.setAllOnClickListener {
            viewModel.importDatabase()
        }

        binding.rateUs.setOnClickListener {
            launchReviewWorkflow()
        }

        binding.openSourceLicenses.setOnClickListener {
            Intent(requireContext(), OssLicensesActivity::class.java).apply {
                startActivity(this)
            }
        }

        binding.mail.setOnClickListener {
            requireContext().openEmailToOption("naveenkumarn2@gmail.com")
        }

        binding.github.setOnClickListener {
            requireContext().openWebPage("https://www.github.com/nkuppan")
        }

        binding.twitter.setOnClickListener {
            requireContext().openWebPage("https://www.twitter.com/naveenkumarn27")
        }
    }

    private fun launchDirectoryPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        directoryPicker.launch(intent)
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.theme.collectLatest {
                        binding.selectedTheme.text = requireContext().getString(it.titleResId)
                    }
                }
                launch {
                    viewModel.currency.collectLatest {
                        binding.selectedCurrency.text = requireContext().getString(it.type)
                    }
                }
                launch {
                    viewModel.reminderStatus.collectLatest {
                        binding.dailyReminderStatus.isChecked = it
                        binding.dailyReminderTimeContainer.enable(it)

                        if (it) {
                            binding.dailyReminderTimeContainer.setAllOnClickListener {
                                val c: Calendar = Calendar.getInstance()
                                val mHour = c.get(Calendar.HOUR_OF_DAY)
                                val mMinute = c.get(Calendar.MINUTE)

                                val timePickerDialog = TimePickerDialog(
                                    requireContext(),
                                    { view, hourOfDay, minute ->
                                        //Set timer value
                                    },
                                    mHour,
                                    mMinute,
                                    false
                                )
                                timePickerDialog.show()
                            }
                        } else {
                            binding.dailyReminderTimeContainer.setAllOnClickListener(null)
                        }
                    }
                }
            }
        }
    }

    private fun launchReviewWorkflow() {
        val manager = ReviewManagerFactory.create(requireContext())
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                reviewInfo?.let {
                    val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }
                }
            }
        }
    }

    private fun getStoragePermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return permissions.toTypedArray()
    }
}