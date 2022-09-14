package com.nkuppan.expensemanager.feature.transaction.create

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nkuppan.expensemanager.core.common.utils.KEY_AMOUNT
import com.nkuppan.expensemanager.core.common.utils.KEY_DELETE_STATUS
import com.nkuppan.expensemanager.core.common.utils.KEY_MODIFIED
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.core.ui.extensions.hideKeyboard
import com.nkuppan.expensemanager.core.ui.extensions.showSnackBarMessage
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingFragment
import com.nkuppan.expensemanager.feature.transaction.R
import com.nkuppan.expensemanager.feature.transaction.databinding.FragmentTransactionCreateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class TransactionCreateFragment : BaseBindingFragment<FragmentTransactionCreateBinding>() {

    private val args: TransactionCreateFragmentArgs by navArgs()

    private val transaction: Transaction? by lazy {
        args.transaction
    }

    private val viewModel: TransactionCreateViewModel by viewModels()

    private lateinit var categoryAdapter: ArrayAdapter<String>

    private lateinit var accountAdapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        initializeObservers()
    }

    private fun initViews() {

        initToolbar()

        initCategoryList()

        initAccountList()
    }

    private fun initToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        inflateMenu()
    }

    private fun inflateMenu() {

        transaction ?: return

        binding.toolbar.inflateMenu(R.menu.menu_transaction_create)

        binding.toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener if (it.itemId == com.nkuppan.expensemanager.feature.category.R.id.action_delete) {
                transaction?.let { value ->
                    findNavController().navigate(
                        TransactionCreateFragmentDirections.actionTransactionCreateFragmentToTransactionDeleteDialog(
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

    private fun initAccountList() {

        accountAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            mutableListOf<String>()
        )

        binding.accountTypeSelection.adapter = accountAdapter

        binding.accountTypeSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //Do nothing
                }

                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    viewModel.selectedAccountId = position
                }
            }
    }

    private fun initCategoryList() {
        categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            mutableListOf<String>()
        )

        binding.categoryTypeSelection.adapter = categoryAdapter

        binding.categoryTypeSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //Do nothing
                }

                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    viewModel.selectedCategoryId = position
                }
            }
    }

    private fun initializeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categories.collectLatest { categories ->

                        if (categories.isNotEmpty()) {

                            categoryAdapter.clear()
                            categoryAdapter.addAll(categories)
                            categoryAdapter.notifyDataSetChanged()

                            binding.categoryTypeSelection.setSelection(viewModel.selectedCategoryId)

                            openAmountSelection(binding)
                        }
                    }
                }
                launch {
                    viewModel.accounts.collectLatest { accounts ->

                        if (accounts.isNotEmpty()) {
                            accountAdapter.clear()
                            accountAdapter.addAll(accounts)
                            accountAdapter.notifyDataSetChanged()
                            binding.accountTypeSelection.setSelection(viewModel.selectedAccountId)
                        }
                    }
                }
                launch {
                    viewModel.amountClick.collectLatest {
                        openAmountSelection(binding)
                    }
                }
                launch {
                    viewModel.dateClick.collectLatest {
                        showDatePickerDialog(binding.accountTypeAdd)
                    }
                }
                launch {
                    viewModel.categoryClick.collectLatest {
                        navigateCategoryAddPage()
                    }
                }
                launch {
                    viewModel.accountClick.collectLatest {
                        navigateAccountAddPage()
                    }
                }
                launch {
                    viewModel.transactionCreated.collectLatest {
                        findNavController().also {
                            it.previousBackStackEntry?.savedStateHandle?.set(
                                KEY_MODIFIED,
                                true
                            )
                            it.popBackStack()
                        }
                    }
                }
                launch {
                    viewModel.snackbarMessage.collectLatest {
                        binding.root.showSnackBarMessage(it.asString(requireContext()))
                    }
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            savedStateHandle.getLiveData<Boolean>(KEY_DELETE_STATUS)
                .observe(viewLifecycleOwner) { status ->
                    if (status) {
                        findNavController().also {
                            it.previousBackStackEntry?.savedStateHandle?.set(
                                KEY_MODIFIED,
                                true
                            )
                            it.popBackStack()
                        }
                    } else {
                        binding.root.showSnackBarMessage(R.string.transaction_delete_error_message)
                    }
                }
            savedStateHandle.getLiveData<String>(KEY_AMOUNT).observe(viewLifecycleOwner) {
                viewModel.setAmount(it.toDoubleOrNull() ?: 0.0)
            }
        }

        viewModel.loadAccounts()

        viewModel.loadCategories()

        args.transaction?.let {
            viewModel.setTransaction(it)
        }
    }

    private fun navigateCategoryAddPage() {
        findNavController().navigate(
            R.id.action_transactionCreateFragment_to_navigation_category_create,
            Bundle().apply {
                putSerializable("category", null)
            }
        )
    }

    private fun navigateAccountAddPage() {
        findNavController().navigate(
            R.id.action_transactionCreateFragment_to_navigation_account_create,
            Bundle().apply {
                putSerializable("account", null)
            }
        )
    }

    private fun showDatePickerDialog(aView: ImageView) {

        aView.hideKeyboard()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val newDate = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                viewModel.setDate(newDate.time)
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun openAmountSelection(binding: FragmentTransactionCreateBinding) {

        binding.root.hideKeyboard()

        findNavController().navigate(
            TransactionCreateFragmentDirections.actionTransactionCreateFragmentToNumberPadDialogFragment(
                viewModel.getAmountValue().toFloat()
            )
        )
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentTransactionCreateBinding {
        return FragmentTransactionCreateBinding.inflate(inflater, container, false)
    }

    override fun bindData(binding: FragmentTransactionCreateBinding) {
        super.bindData(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner
    }
}