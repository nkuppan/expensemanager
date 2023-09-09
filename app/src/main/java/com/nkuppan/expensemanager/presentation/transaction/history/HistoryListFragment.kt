package com.nkuppan.expensemanager.presentation.transaction.history

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
import com.nkuppan.expensemanager.domain.model.ActionType
import com.nkuppan.expensemanager.domain.model.Transaction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryListFragment : BaseBindingListFragment() {

    private val viewModel: HistoryListViewModel by viewModels()

    private val historyList = mutableListOf<HistoryListItem>()

    private val historyListAdapter: HistoryListAdapter =
        HistoryListAdapter(historyList) { uiModel, type ->
            if (type == ActionType.SELECT) {
                viewModel.openTransactionEdit(uiModel.id)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(R.string.title_history)

        initializeViews()

        initObservers()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.transactionHistory.collectLatest {
                        val hasRecords = it.isNotEmpty()
                        showDataContainer(hasRecords)
                        historyList.clear()
                        historyList.addAll(it)
                        historyListAdapter.notifyDataSetChanged()
                    }
                }

                launch {
                    viewModel.errorMessage.collectLatest {
                        binding.root.showSnackBarMessage(it.asString(requireContext()))
                    }
                }

                launch {
                    viewModel.openTransaction.collectLatest {
                        navigateToTransactionCreateScreen(it)
                    }
                }
            }
        }

        onRefresh()
    }

    override fun onRefresh() {
        viewModel.loadTransactions()
    }

    override fun getActionText(): Int {
        return R.string.transaction_action_text
    }

    override fun getInfoText(): Int {
        return R.string.no_history_available
    }

    override fun getInfoImage(): Int {
        return R.drawable.ic_history_info
    }

    private fun initializeViews() {

        setAdapter(historyListAdapter)

        binding.actionAdd.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                navigateToTransactionCreateScreen(null)
            }
        }
    }

    private fun navigateToTransactionCreateScreen(transaction: Transaction?) {
        viewLifecycleOwner.lifecycleScope.launch {
            findNavController().navigate(
                R.id.action_historyListFragment_to_transaction_create_navigation,
                Bundle().apply {
                    putSerializable("transaction", transaction)
                }
            )
        }
    }
}
