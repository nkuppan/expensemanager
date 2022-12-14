package com.nkuppan.expensemanager.feature.analysis.income

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
import com.nkuppan.expensemanager.core.ui.extensions.showSnackBarMessage
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingFragment
import com.nkuppan.expensemanager.feature.analysis.R
import com.nkuppan.expensemanager.feature.analysis.databinding.FragmentIncomeGraphListBinding
import com.nkuppan.expensemanager.feature.analysis.expense.CategoryTransactionListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IncomeGraphFragment : BaseBindingFragment<FragmentIncomeGraphListBinding>() {

    private val viewModel: IncomeGraphViewModel by viewModels()

    private val adapter = CategoryTransactionListAdapter {
        viewLifecycleOwner.lifecycleScope.launch {
            //TODO open transaction list screen with category id
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.actionAdd.setOnClickListener {
            findNavController().navigate(
                R.id.action_graphTabFragment_to_transaction_create_navigation,
                Bundle().apply {
                    putSerializable("transaction", null)
                }
            )
        }

        binding.dataRecyclerView.adapter = adapter

        initializeObserver()
    }

    private fun initializeObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.graphItems.collectLatest {
                        val hasRecords = it.isNotEmpty()
                        binding.infoContainer.isVisible = !hasRecords
                        binding.dataRecyclerView.isVisible = hasRecords
                        adapter.submitList(it)
                    }
                }
                launch {
                    viewModel.errorMessage.collectLatest {
                        binding.root.showSnackBarMessage(it.asString(requireContext()))
                    }
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            savedStateHandle.getLiveData<Boolean>("modified_status")
                .observe(viewLifecycleOwner) {
                    if (it) {
                        viewModel.loadIncomeData()
                    }
                }
        }

        viewModel.loadIncomeData()
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentIncomeGraphListBinding {
        return FragmentIncomeGraphListBinding.inflate(inflater, container, false)
    }
}
