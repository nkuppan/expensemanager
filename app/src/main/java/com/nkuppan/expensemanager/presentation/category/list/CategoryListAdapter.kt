package com.nkuppan.expensemanager.presentation.category.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nkuppan.expensemanager.databinding.ListItemCategoryBinding
import com.nkuppan.expensemanager.domain.model.ActionType
import com.nkuppan.expensemanager.domain.model.Category


class CategoryListAdapter(
    private val callback: ((Category, ActionType) -> Unit)?
) : ListAdapter<Category, CategoryListViewHolder>(
    object : DiffUtil.ItemCallback<Category>() {

        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.backgroundColor == newItem.backgroundColor &&
                    oldItem.type == newItem.type &&
                    oldItem.isFavorite == newItem.isFavorite &&
                    oldItem.updatedOn == newItem.updatedOn
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListViewHolder {
        return CategoryListViewHolder(
            ListItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryListViewHolder, position: Int) {

        holder.binding.viewModel = getItem(position)

        holder.binding.root.setOnClickListener {
            holder.binding.viewModel?.let {
                callback?.invoke(it, ActionType.SELECT)
            }
        }
    }
}

class CategoryListViewHolder(val binding: ListItemCategoryBinding) :
    RecyclerView.ViewHolder(binding.root)

