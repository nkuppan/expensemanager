package com.nkuppan.expensemanager.presentation.account.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.showSnackBarMessage
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingListFragment
import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.ActionType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountListFragment : BaseBindingListFragment() {

    private val viewModel: AccountListViewModel by viewModels()

    private val accountListAdapter: AccountListAdapter = AccountListAdapter { account, type ->
        if (type == ActionType.SELECT) {
            viewModel.openAccountToEdit(account.id)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(R.string.wallets_amp_accounts)

        initializeViews()

        initObservers()
    }

    override fun onRefresh() {
        viewModel.loadAccounts()
    }

    override fun getActionText(): Int {
        return R.string.account_action_text
    }

    override fun getInfoText(): Int {
        return R.string.no_account_available
    }

    override fun getInfoImage(): Int {
        return R.drawable.ic_wallet
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.accounts.collectLatest {
                        val hasRecords = it.isNotEmpty()
                        showDataContainer(hasRecords)
                        accountListAdapter.submitList(it)
                    }
                }

                launch {
                    viewModel.errorMessage.collectLatest {
                        binding.root.showSnackBarMessage(it.asString(requireContext()))
                    }
                }

                launch {
                    viewModel.openAccount.collectLatest {
                        navigateToAccountCreateScreen(it)
                    }
                }
            }
        }

        viewModel.loadAccounts()
    }

    private fun initializeViews() {
        setAdapter(accountListAdapter)

        binding.actionAdd.setOnClickListener {
            navigateToAccountCreateScreen(null)
        }
    }

    private fun navigateToAccountCreateScreen(account: Account?) {
        viewLifecycleOwner.lifecycleScope.launch {
            findNavController().navigate(
                R.id.action_accountListFragment_to_navigation_account_create,
                Bundle().apply {
                    putSerializable("account", account)
                }
            )
        }
    }
}