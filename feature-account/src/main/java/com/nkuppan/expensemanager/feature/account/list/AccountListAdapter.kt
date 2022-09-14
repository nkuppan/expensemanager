package com.nkuppan.expensemanager.feature.account.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nkuppan.expensemanager.core.model.ActionType
import com.nkuppan.expensemanager.feature.account.databinding.ListItemAccountBinding


class AccountListAdapter(
    private val callback: ((AccountUIState, ActionType) -> Unit)?
) : ListAdapter<AccountUIState, AccountListViewHolder>(
    object : DiffUtil.ItemCallback<AccountUIState>() {

        override fun areItemsTheSame(oldItem: AccountUIState, newItem: AccountUIState): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AccountUIState, newItem: AccountUIState): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.backgroundColor == newItem.backgroundColor &&
                    oldItem.accountIcon == newItem.accountIcon
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountListViewHolder {
        return AccountListViewHolder(
            ListItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

class AccountListViewHolder(val binding: ListItemAccountBinding) :
    RecyclerView.ViewHolder(binding.root)

