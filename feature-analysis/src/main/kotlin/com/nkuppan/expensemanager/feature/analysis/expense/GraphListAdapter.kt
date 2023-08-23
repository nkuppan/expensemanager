package com.nkuppan.expensemanager.feature.analysis.expense

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Legend
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.core.ui.utils.getColorValue
import com.nkuppan.expensemanager.core.ui.utils.getThemeBackground
import com.nkuppan.expensemanager.feature.analysis.R
import com.nkuppan.expensemanager.feature.analysis.databinding.AdapterCategoryTransactionItemBinding
import com.nkuppan.expensemanager.feature.analysis.databinding.AdapterGraphItemBinding
import com.nkuppan.expensemanager.feature.analysis.databinding.AdapterHeadingItemBinding
import kotlin.math.roundToInt


class CategoryTransactionListAdapter(private val callback: ((GraphData) -> Unit)?) :
    ListAdapter<GraphData, RecyclerView.ViewHolder>(
        object : DiffUtil.ItemCallback<GraphData>() {

            override fun areItemsTheSame(
                oldItem: GraphData,
                newItem: GraphData
            ): Boolean {
                return oldItem.type == newItem.type
            }

            override fun areContentsTheSame(
                oldItem: GraphData,
                newItem: GraphData
            ): Boolean {
                return oldItem.amount == newItem.amount &&
                        oldItem.transactions.count() == newItem.transactions.count() &&
                        oldItem.transactions == newItem.transactions &&
                        oldItem.graphData?.equals(newItem.graphData) == true &&
                        oldItem.type == newItem.type &&
                        oldItem.categoryType == newItem.categoryType
            }
        }
    ) {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (getCategoryItemViewType(viewType)) {
            GraphItemType.GRAPH_CATEGORY_TITLE -> {
                val binding = AdapterHeadingItemBinding.inflate(
                    LayoutInflater.from(viewGroup.context), viewGroup, false
                )
                HeadingItemViewHolder(binding)
            }

            GraphItemType.GRAPH_CATEGORY_ITEM -> {
                val binding = AdapterCategoryTransactionItemBinding.inflate(
                    LayoutInflater.from(viewGroup.context), viewGroup, false
                )
                TransactionViewHolder(binding)
            }

            GraphItemType.GRAPH_ITEM -> {
                val binding = AdapterGraphItemBinding.inflate(
                    LayoutInflater.from(viewGroup.context), viewGroup, false
                )
                GraphItemViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    private fun getCategoryItemViewType(position: Int): GraphItemType {
        return GraphItemType.values()[getItem(position).type.ordinal]
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, aPosition: Int) {

        val data = getItem(aPosition)

        val amount = data.amount

        when (getCategoryItemViewType(aPosition)) {
            GraphItemType.GRAPH_CATEGORY_TITLE -> {
                (holder as HeadingItemViewHolder).binding.apply {
                    title.setText(
                        if (data.categoryType == CategoryType.INCOME)
                            R.string.income
                        else
                            R.string.expense
                    )

                    totalAmount.text = data.amountText.asString(totalAmount.context)
                }
            }

            GraphItemType.GRAPH_CATEGORY_ITEM -> {

                (holder as TransactionViewHolder).binding.apply {

                    val category = data.transactions[0].category

                    categoryName.text =
                        String.format(
                            "%s - %s",
                            category.name,
                            data.amountText.asString(categoryName.context)
                        )

                    val colorValue = getColorValue(category.backgroundColor)

                    val factor = amount.roundToInt()

                    valueBar.progressDrawable.setTint(
                        colorValue
                    )

                    valueBar.max = data.categoryTotalAmount.roundToInt()
                    valueBar.progress = factor

                    categoryBackgroundColor.setColorFilter(
                        colorValue,
                        PorterDuff.Mode.SRC_ATOP
                    )

                    root.setOnClickListener {
                        callback?.invoke(data)
                    }
                }
            }

            GraphItemType.GRAPH_ITEM -> {

                (holder as GraphItemViewHolder).binding.apply {
                    graph.description.isEnabled = false
                    graph.setTouchEnabled(false)
                    graph.setCenterTextSize(10f)

                    // radius of the center hole in percent of maximum radius
                    graph.holeRadius = 50f
                    graph.transparentCircleRadius = 50f
                    graph.setHoleColor(getThemeBackground(graph.context))

                    val legend = graph.legend
                    //legend.textColor = ContextCompat.getColor(graph.context, R.color.icon_color)
                    legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    legend.orientation = Legend.LegendOrientation.HORIZONTAL
                    legend.setDrawInside(false)

                    graph.data = data.graphData
                    graph.invalidate()
                }
            }
        }
    }
}

class GraphItemViewHolder(val binding: AdapterGraphItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class HeadingItemViewHolder(val binding: AdapterHeadingItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class TransactionViewHolder(val binding: AdapterCategoryTransactionItemBinding) :
    RecyclerView.ViewHolder(binding.root)
