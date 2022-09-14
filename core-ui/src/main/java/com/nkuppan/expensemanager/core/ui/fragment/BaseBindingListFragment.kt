package com.nkuppan.expensemanager.core.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nkuppan.expensemanager.core.ui.databinding.FragmentListBinding
import kotlinx.coroutines.launch

abstract class BaseBindingListFragment : BaseBindingFragment<FragmentListBinding>() {

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentListBinding {
        return FragmentListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.infoText.setText(getInfoText())
        binding.infoActionText.setText(getActionText())
        binding.infoImage.setImageResource(getInfoImage())

        binding.swipeRefreshContainer.setOnRefreshListener {
            startRefreshContainer()
        }

        binding.toolbar.setNavigationOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                findNavController().popBackStack()
            }
        }

        binding.swipeRefreshContainer.isRefreshing = true
    }

    fun setToolbarTitle(@StringRes title: Int) {
        binding.toolbar.setTitle(title)
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        binding.dataRecyclerView.adapter = adapter
    }

    fun showDataContainer(hasRecords: Boolean) {
        binding.infoContainer.isVisible = !hasRecords
        binding.dataRecyclerView.isVisible = hasRecords
        binding.swipeRefreshContainer.isRefreshing = false
    }

    private fun startRefreshContainer() {
        binding.swipeRefreshContainer.isRefreshing = true
        onRefresh()
    }

    abstract fun onRefresh()

    @StringRes
    abstract fun getActionText(): Int

    @StringRes
    abstract fun getInfoText(): Int

    @DrawableRes
    abstract fun getInfoImage(): Int
}