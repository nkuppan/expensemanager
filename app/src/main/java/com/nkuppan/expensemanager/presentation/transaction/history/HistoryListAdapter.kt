package com.nkuppan.expensemanager.presentation.transaction.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.databinding.AdapterTransactionChildItemBinding
import com.nkuppan.expensemanager.databinding.AdapterTransactionHeaderItemBinding
import com.nkuppan.expensemanager.domain.model.ActionType


class HistoryListAdapter(
    private val historyItems: MutableList<HistoryListItem>,
    private val callback: ((TransactionUIModel, ActionType) -> Unit)?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (ItemType.values()[viewType]) {
            ItemType.PARENT -> {
                val dataBinding = AdapterTransactionHeaderItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                return TransactionHeaderViewHolder(dataBinding)
            }

            ItemType.CHILD -> {
                val dataBinding = AdapterTransactionChildItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                return TransactionChildViewHolder(dataBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = historyItems[position]

        when (holder) {
            is TransactionHeaderViewHolder -> {

                holder.binding.headerItem.text =
                    item.text?.asString(holder.binding.headerItem.context)

                holder.binding.totalAmount.text =
                    item.totalAmount.asString(holder.binding.totalAmount.context)

                holder.binding.arrow.setImageResource(
                    if (item.expanded) {
                        R.drawable.ic_arrow_up
                    } else {
                        R.drawable.ic_arrow_down
                    }
                )
                holder.binding.headerContainer.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.binding.root.context,
                        android.R.color.transparent
                    )
                )

                holder.binding.root.setOnClickListener {
                    expandOrCollapseParentItem(item, position)
                }
            }

            is TransactionChildViewHolder -> {

                val transaction = item.transaction

                holder.binding.transaction = transaction[0]
                holder.binding.showColor = true

                holder.binding.root.setOnClickListener {
                    holder.binding.transaction?.let {
                        callback?.invoke(it, ActionType.SELECT)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return historyItems[position].type.ordinal
    }

    override fun getItemCount(): Int {
        return historyItems.size
    }

    private fun expandOrCollapseParentItem(singleBoarding: HistoryListItem, position: Int) {
        if (singleBoarding.expanded) {
            collapseParentRow(position)
        } else {
            expandParentRow(position)
        }
    }

    private fun expandParentRow(position: Int) {
        val currentBoardingRow = historyItems[position]
        val services = currentBoardingRow.transaction
        currentBoardingRow.expanded = true
        var nextPosition = position
        if (currentBoardingRow.type == ItemType.PARENT) {
            services.forEach { service ->
                val parentModel = HistoryListItem()
                parentModel.type = ItemType.CHILD
                val subList = mutableListOf<TransactionUIModel>()
                subList.add(service)
                parentModel.transaction = subList
                historyItems.add(++nextPosition, parentModel)
            }
        }
        notifyDataSetChanged()
    }

    private fun collapseParentRow(position: Int) {
        val currentBoardingRow = historyItems[position]
        val services = currentBoardingRow.transaction
        historyItems[position].expanded = false
        if (historyItems[position].type == ItemType.PARENT) {
            services.forEach { _ ->
                historyItems.removeAt(position + 1)
            }
            notifyDataSetChanged()
        }
    }
}

class TransactionHeaderViewHolder(val binding: AdapterTransactionHeaderItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class TransactionChildViewHolder(val binding: AdapterTransactionChildItemBinding) :
    RecyclerView.ViewHolder(binding.root)

enum class ItemType {
    PARENT,
    CHILD
}