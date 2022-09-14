package com.nkuppan.expensemanager.feature.transaction.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingFragment
import com.nkuppan.expensemanager.feature.transaction.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch

class SearchFragment : BaseBindingFragment<FragmentSearchBinding>() {

    private val viewModel: SearchViewModel by viewModels()

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {

        }
    }
}