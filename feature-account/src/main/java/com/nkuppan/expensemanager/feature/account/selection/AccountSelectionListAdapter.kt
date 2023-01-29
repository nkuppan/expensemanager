package com.nkuppan.expensemanager.feature.account.selection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nkuppan.expensemanager.core.model.ActionType
import com.nkuppan.expensemanager.feature.account.databinding.ListItemAccountSelectionBinding


class AccountSelectionListAdapter(
    private val callback: ((AccountSelectionUIState, ActionType) -> Unit)?
) : ListAdapter<AccountSelectionUIState, AccountListViewHolder>(
    object : DiffUtil.ItemCallback<AccountSelectionUIState>() {

        override fun areItemsTheSame(oldItem: AccountSelectionUIState, newItem: AccountSelectionUIState): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AccountSelectionUIState, newItem: AccountSelectionUIState): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.backgroundColor == newItem.backgroundColor &&
                    oldItem.isSelected == newItem.isSelected &&
                    oldItem.accountIcon == newItem.accountIcon
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountListViewHolder {
        return AccountListViewHolder(
            ListItemAccountSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: AccountListViewHolder, position: Int) {

        holder.binding.viewModel = getItem(position)

        holder.binding.root.setOnClickListener {
            holder.binding.viewModel?.let {
                callback?.invoke(it, ActionType.SELECT)
            }
        }
    }
}

class AccountListViewHolder(val binding: ListItemAccountSelectionBinding) :
    RecyclerView.ViewHolder(binding.root)

