package com.nkuppan.expensemanager.feature.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingFragment
import com.nkuppan.expensemanager.feature.dashboard.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : BaseBindingFragment<FragmentDashboardBinding>() {

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentDashboardBinding {
        return FragmentDashboardBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountList.setOnClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardFragmentToNavigationAccountList()
            )
        }
        binding.categoryList.setOnClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardFragmentToNavigationCategoryList()
            )
        }
        binding.transactionList.setOnClickListener {
            findNavController().navigate(
                R.id.action_dashboardFragment_to_navigation_transaction_list,
                Bundle().apply {
                    putInt("category_id", -1)
                    putBoolean("show_color", true)
                }
            )
        }
        binding.graphTabs.setOnClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardFragmentToNavigationAnalysis()
            )
        }
        binding.history.setOnClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardFragmentToNavigationTransactionHistoryList()
            )
        }
        binding.settings.setOnClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardFragmentToNavigationSettings()
            )
        }
    }
}