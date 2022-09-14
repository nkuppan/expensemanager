package com.nkuppan.expensemanager.feature.analysis.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.nkuppan.expensemanager.core.ui.extensions.setSupportedActionBar
import com.nkuppan.expensemanager.core.ui.extensions.setTitle
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingFragment
import com.nkuppan.expensemanager.feature.analysis.R
import com.nkuppan.expensemanager.feature.analysis.databinding.FragmentGraphTabsBinding
import com.nkuppan.expensemanager.feature.analysis.expense.ExpenseGraphFragment
import com.nkuppan.expensemanager.feature.analysis.income.IncomeGraphFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GraphTabFragment : BaseBindingFragment<FragmentGraphTabsBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        constructGraphPages()
    }

    private fun initViews() {

        setSupportedActionBar(binding.toolbar)

        setTitle(R.string.title_graph)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun constructGraphPages() {

        val fragments = ArrayList<Fragment>()

        fragments.add(ExpenseGraphFragment())
        fragments.add(IncomeGraphFragment())

        val fragmentTitles = resources.getStringArray(R.array.graph_items).toList()

        binding.viewPager.adapter = ViewPagerAdapter(this, fragments)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitles[position]
        }.attach()
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentGraphTabsBinding {
        return FragmentGraphTabsBinding.inflate(inflater, container, false)
    }
}