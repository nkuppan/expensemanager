package com.nkuppan.expensemanager.feature.category.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nkuppan.expensemanager.core.model.ActionType
import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingListFragment
import com.nkuppan.expensemanager.feature.category.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CategoryListFragment : BaseBindingListFragment() {

    private val viewModel: CategoryListViewModel by viewModels()

    private val categoryListAdapter: CategoryListAdapter = CategoryListAdapter { category, type ->
        if (type == ActionType.SELECT) {
            navigateToCategoryCreateScreen(category)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(R.string.title_category)

        initializeViews()

        initializeObservers()
    }

    override fun onRefresh() {
        //
    }

    override fun getActionText(): Int {
        return R.string.category_action_text
    }

    override fun getInfoText(): Int {
        return R.string.no_category_available
    }

    override fun getInfoImage(): Int {
        return R.drawable.ic_category_info
    }

    private fun initializeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categories.collectLatest {
                        /*val hasRecords = it.isNotEmpty()
                        showDataContainer(hasRecords)
                        categoryListAdapter.submitList(it)*/
                    }
                }
            }
        }

        onRefresh()
    }

    private fun initializeViews() {

        setAdapter(categoryListAdapter)

        binding.actionAdd.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                navigateToCategoryCreateScreen(null)
            }
        }
    }

    private fun navigateToCategoryCreateScreen(category: Category?) {
        viewLifecycleOwner.lifecycleScope.launch {
            findNavController().navigate(
                R.id.action_categoryListFragment_to_category_create_navigation,
                Bundle().apply {
                    putSerializable("category", category)
                }
            )
        }
    }
}