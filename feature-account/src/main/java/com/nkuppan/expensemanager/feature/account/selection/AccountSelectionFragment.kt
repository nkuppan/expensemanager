package com.nkuppan.expensemanager.feature.account.selection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.nkuppan.expensemanager.core.model.ActionType
import com.nkuppan.expensemanager.core.ui.fragment.BaseBottomSheetBindingFragment
import com.nkuppan.expensemanager.feature.account.databinding.DialogAccountSelectionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountSelectionFragment : BaseBottomSheetBindingFragment<DialogAccountSelectionBinding>() {

    private val viewModel: AccountSelectionListViewModel by viewModels()

    private val accountSelectionListAdapter = AccountSelectionListAdapter { account, type ->
        if (type == ActionType.SELECT) {
            viewModel.selectThisAccount(account)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountSelectionList.setHasFixedSize(false)
        binding.accountSelectionList.itemAnimator = DefaultItemAnimator()
        binding.accountSelectionList.layoutManager = LinearLayoutManager(requireContext())

        binding.accountSelectionList.adapter = accountSelectionListAdapter

        initObservers()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.accounts.collectLatest {
                        accountSelectionListAdapter.submitList(it)
                    }
                }
            }
        }
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): DialogAccountSelectionBinding {
        return DialogAccountSelectionBinding.inflate(inflater, container, false)
    }
}