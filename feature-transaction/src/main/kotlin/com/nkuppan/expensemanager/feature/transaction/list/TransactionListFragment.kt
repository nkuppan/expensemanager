package com.nkuppan.expensemanager.feature.transaction.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nkuppan.expensemanager.core.ui.extensions.showSnackBarMessage
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingListFragment
import com.nkuppan.expensemanager.feature.transaction.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TransactionListFragment : BaseBindingListFragment() {

    private val viewModel: TransactionListViewModel by viewModels()

    private val args: TransactionListFragmentArgs by navArgs()

    private lateinit var transactionListAdapter: TransactionListAdapter

    private val categoryId: Int by lazy {
        args.categoryId
    }

    private val showColor: Boolean by lazy {
        args.showColor
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(R.string.title_transaction)

        initViews()

        initializeObservers()
    }

    override fun onRefresh() {
        viewModel.loadTransactions()
    }

    override fun getActionText(): Int {
        return R.string.transaction_action_text
    }

    override fun getInfoText(): Int {
        return R.string.no_transactions_available
    }

    override fun getInfoImage(): Int {
        return R.drawable.ic_transaction
    }

    private fun initViews() {

        binding.actionAdd.setOnClickListener {
            findNavController().navigate(
                R.id.action_transactionListFragment_to_transaction_create_navigation,
                Bundle().apply {
                    putSerializable("transaction", null)
                }
            )
        }

        transactionListAdapter = TransactionListAdapter(showColor) { value, _ ->
            viewModel.openTransactionEdit(value.id)
        }

        setAdapter(transactionListAdapter)
    }

    private fun initializeObservers() {

        viewModel.categoryId = categoryId

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.transactionList.collectLatest {
                        val hasRecords = it.isNotEmpty()
                        showDataContainer(hasRecords)
                        transactionListAdapter.submitList(it)
                    }
                }

                launch {
                    viewModel.openTransaction.collectLatest {
                        findNavController().navigate(
                            R.id.action_transactionListFragment_to_transaction_create_navigation,
                            Bundle().apply {
                                putSerializable("transaction", it)
                            }
                        )
                    }
                }

                launch {
                    viewModel.errorMessage.collectLatest {
                        binding.root.showSnackBarMessage(it.asString(requireContext()))
                    }
                }
            }
        }

        onRefresh()
    }
}