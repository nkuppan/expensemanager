package com.nkuppan.expensemanager.presentation.account.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.hideKeyboard
import com.nkuppan.expensemanager.core.ui.extensions.showSnackBarMessage
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingFragment
import com.nkuppan.expensemanager.core.utils.KEY_DELETE_STATUS
import com.nkuppan.expensemanager.databinding.FragmentAccountCreateBinding
import com.nkuppan.expensemanager.domain.model.Account
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountCreateFragment : BaseBindingFragment<FragmentAccountCreateBinding>() {

    private val account: Account? by lazy {
        navArgs<AccountCreateFragmentArgs>().value.account
    }

    private val viewModel: AccountCreateViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        initObserver()
    }

    private fun initViews() {

        inflateMenuItems()

        binding.saveAccount.setOnClickListener {
            viewModel.onSaveClick()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.accountTypeContainer.setOnCheckedStateChangeListener { _, checkedIds ->
            val paymentMode = viewModel.getPaymentModeById(checkedIds[0])
            viewModel.setPaymentMode(paymentMode)
        }
    }

    private fun inflateMenuItems() {

        account ?: return

        binding.toolbar.inflateMenu(R.menu.menu_account_create)

        binding.toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener if (it.itemId == R.id.action_delete) {
                account?.let { value ->
                    findNavController().navigate(
                        AccountCreateFragmentDirections.actionAccountCreateFragmentToAccountDeleteDialog(
                            value
                        )
                    )
                }
                true
            } else {
                false
            }
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.colorPicker.collectLatest {
                        binding.root.hideKeyboard()
                        openColorPicker()
                    }
                }
                launch {
                    viewModel.accountCreated.collectLatest {
                        findNavController().popBackStack()
                    }
                }
                launch {
                    viewModel.errorMessage.collectLatest {
                        binding.root.showSnackBarMessage(it.asString(requireContext()))
                    }
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>(KEY_DELETE_STATUS)?.observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().popBackStack()
                } else {
                    binding.root.showSnackBarMessage(R.string.account_delete_error_message)
                }
            }

        account?.let {
            viewModel.setAccountValue(it)
            binding.accountTypeContainer.check(viewModel.getViewIdByPaymentMode(it.type))
        }
    }

    private fun openColorPicker() {
        ColorPickerDialog.Builder(requireContext())
            .setTitle(getString(R.string.title_color_picker))
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton(getString(R.string.select),
                ColorEnvelopeListener { envelope, _ ->
                    viewModel.setColorValue(envelope.color)
                }
            )
            .setNegativeButton(
                getString(R.string.cancel)
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAccountCreateBinding {
        return FragmentAccountCreateBinding.inflate(inflater, container, false)
    }

    override fun bindData(binding: FragmentAccountCreateBinding) {
        super.bindData(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner
    }
}