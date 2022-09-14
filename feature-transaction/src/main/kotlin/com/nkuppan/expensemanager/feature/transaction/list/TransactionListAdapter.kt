package com.nkuppan.expensemanager.feature.transaction.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nkuppan.expensemanager.core.model.ActionType
import com.nkuppan.expensemanager.feature.transaction.databinding.AdapterTransactionChildItemBinding
import com.nkuppan.expensemanager.feature.transaction.history.TransactionUIModel


class TransactionListAdapter(
    private val showColor: Boolean,
    private val callback: ((TransactionUIModel, ActionType) -> Unit)?
) : ListAdapter<TransactionUIModel, TransactionListViewHolder>(
    object : DiffUtil.ItemCallback<TransactionUIModel>() {

        override fun areItemsTheSame(
            oldItem: TransactionUIModel,
            newItem: TransactionUIModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: TransactionUIModel,
            newItem: TransactionUIModel
        ): Boolean {
            return oldItem.notes == newItem.notes &&
                    oldItem.amount == newItem.amount &&
                    oldItem.notes == newItem.notes &&
                    oldItem.categoryName == newItem.categoryName &&
                    oldItem.categoryBackgroundColor == newItem.categoryBackgroundColor
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionListViewHolder {
        return TransactionListViewHolder(
            AdapterTransactionChildItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TransactionListViewHolder, position: Int) {

        holder.binding.transaction = getItem(position)
        holder.binding.showColor = showColor

        holder.binding.root.setOnClickListener {
            holder.binding.transaction?.let {
                callback?.invoke(it, ActionType.SELECT)
            }
        }
    }
}


class TransactionListViewHolder(val binding: AdapterTransactionChildItemBinding) :
    RecyclerView.ViewHolder(binding.root)
