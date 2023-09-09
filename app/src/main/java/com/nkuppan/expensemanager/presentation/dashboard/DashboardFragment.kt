package com.nkuppan.expensemanager.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Description
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.showSnackBarMessage
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingFragment
import com.nkuppan.expensemanager.databinding.FragmentDashboardBinding
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.presentation.transaction.history.TransactionUIModel
import com.nkuppan.expensemanager.presentation.transaction.list.TransactionListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : BaseBindingFragment<FragmentDashboardBinding>() {

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var transactionListAdapter: TransactionListAdapter

    override fun inflateLayout(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentDashboardBinding {
        return FragmentDashboardBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionListAdapter = TransactionListAdapter(showColor = true) { value, _ ->
            viewModel.openTransactionEdit(value.id)
        }

        binding.transactionList.isHorizontalScrollBarEnabled = false
        binding.transactionList.setHasFixedSize(false)
        binding.transactionList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionList.adapter = transactionListAdapter

        binding.lastSevenDayGraph.setDrawGridBackground(false)
        binding.lastSevenDayGraph.description = Description().apply {
            text = ""
        }

        binding.bottomAppBar.setOnItemSelectedListener {
            return@setOnItemSelectedListener when (it.itemId) {
                R.id.action_chart -> {
                    findNavController().navigate(
                        R.id.action_dashboardFragment_to_navigation_analysis
                    )
                    false
                }

                R.id.action_transaction -> {
                    findNavController().navigate(
                        R.id.action_dashboardFragment_to_navigation_transaction_list,
                        Bundle().apply {
                            putInt("category_id", -1)
                            putBoolean("show_color", true)
                        }
                    )
                    false
                }

                R.id.action_history -> {
                    findNavController().navigate(
                        R.id.action_dashboardFragment_to_navigation_transaction_history_list
                    )
                    false
                }

                else -> false
            }
        }

        binding.addTransaction.setOnClickListener {
            navigateTransaction(null)
        }

        binding.viewAll.setOnClickListener {
            findNavController().navigate(
                R.id.action_dashboardFragment_to_navigation_transaction_list,
                Bundle().apply {
                    putInt("category_id", -1)
                    putBoolean("show_color", true)
                }
            )
        }

        binding.toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.action_settings -> {
                    findNavController().navigate(R.id.action_dashboardFragment_to_navigation_settings)
                    true
                }

                else -> {
                    false
                }
            }
        }

        binding.accountContainer.setOnClickListener {
            findNavController().navigate(
                R.id.action_dashboardFragment_to_navigation_account_selection
            )
        }

        binding.dateContainer.setOnClickListener {
            findNavController().navigate(
                R.id.action_dashboardFragment_to_navigation_date_filter
            )
        }

        observeEmitters()
    }

    private fun observeEmitters() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.incomeAmountValue.collectLatest {
                        binding.incomeValue.text = it.asString(requireContext())
                    }
                }
                launch {
                    viewModel.expenseAmountValue.collectLatest {
                        binding.expenseValue.text = it.asString(requireContext())
                    }
                }
                launch {
                    viewModel.totalIncomeValue.collectLatest {
                        binding.balanceValue.text = it.asString(requireContext())
                    }
                }
                launch {
                    viewModel.accountValue.collectLatest {
                        binding.accountValue.text = it
                    }
                }
                launch {
                    viewModel.dateValue.collectLatest {
                        binding.dateValue.text = it
                    }
                }
                launch {
                    viewModel.previousDayAnalysisData.collectLatest {
                        binding.lastSevenDayGraph.data = it
                        binding.lastSevenDayGraph.invalidate()
                    }
                }
                launch {
                    viewModel.transactions.collectLatest {

                        val transactionUIModels: List<TransactionUIModel> = it ?: emptyList()
                        val hasRecords = transactionUIModels.isNotEmpty()
                        binding.transactionList.isVisible = hasRecords
                        binding.viewAll.isVisible = hasRecords
                        binding.emptyView.isVisible = !hasRecords

                        transactionListAdapter.submitList(transactionUIModels)
                    }
                }
                launch {
                    viewModel.openTransaction.collectLatest {
                        navigateTransaction(it)
                    }
                }
                launch {
                    viewModel.errorMessage.collectLatest {
                        binding.root.showSnackBarMessage(it.asString(requireContext()))
                    }
                }
            }
        }
    }

    private fun navigateTransaction(transaction: Transaction?) {
        findNavController().navigate(
            R.id.action_dashboardFragment_to_navigation_transaction_create,
            Bundle().apply {
                putSerializable("transaction", transaction)
            })
    }
}