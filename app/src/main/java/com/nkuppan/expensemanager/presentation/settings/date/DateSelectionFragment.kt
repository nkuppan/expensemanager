package com.nkuppan.expensemanager.presentation.settings.date

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.hideKeyboard
import com.nkuppan.expensemanager.core.ui.fragment.BaseBottomSheetBindingFragment
import com.nkuppan.expensemanager.data.utils.getDateValue
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.databinding.FragmentDateFilterBinding
import com.nkuppan.expensemanager.domain.model.FilterType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class DateSelectionFragment : BaseBottomSheetBindingFragment<FragmentDateFilterBinding>() {

    private val viewModel: DateFilterViewModel by viewModels()

    override fun inflateLayout(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentDateFilterBinding {
        return FragmentDateFilterBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.customFilterCancel.setOnClickListener {
            //Cancel the custom filter and set the last selected
            if (viewModel.isLastFilterNotCustom()) {
                disableCustomFilterIfShown()
                viewModel.setLastSelectedFilterType()
            }
        }

        binding.customFilterSet.setOnClickListener {
            //Store the date ranges
            viewModel.saveCustomFilterType()
        }

        binding.startDate.setOnClickListener {
            viewModel.showDatePicker(isStartDate = true)
        }

        binding.endDate.setOnClickListener {
            viewModel.showDatePicker(isStartDate = false)
        }

        binding.filterContainer.setOnCheckedStateChangeListener { group, checkedIds ->
            if (group.id == R.id.filter_container) {
                when (checkedIds[0]) {
                    R.id.this_month -> {
                        disableCustomFilterIfShown()
                        viewModel.saveFilterType(FilterType.THIS_MONTH)
                    }

                    R.id.last_month -> {
                        disableCustomFilterIfShown()
                        viewModel.saveFilterType(FilterType.LAST_MONTH)
                    }

                    R.id.last_three_month -> {
                        disableCustomFilterIfShown()
                        viewModel.saveFilterType(FilterType.LAST_THREE_MONTH)
                    }

                    R.id.last_six_month -> {
                        disableCustomFilterIfShown()
                        viewModel.saveFilterType(FilterType.LAST_SIX_MONTH)
                    }

                    R.id.last_year -> {
                        disableCustomFilterIfShown()
                        viewModel.saveFilterType(FilterType.LAST_YEAR)
                    }

                    R.id.all -> {
                        disableCustomFilterIfShown()
                        viewModel.saveFilterType(FilterType.ALL)
                    }

                    R.id.custom -> {
                        binding.customFilterContainer.isVisible = true
                    }
                }
            }
        }

        initObservers()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filterType.collectLatest {
                        setFilterSelection(it)
                    }
                }
                launch {
                    viewModel.customFilterValues.collectLatest {
                        if (it.isNotEmpty()) {
                            binding.startDate.setText(it[0].getDateValue()?.toTransactionDate())
                            binding.endDate.setText(it[1].getDateValue()?.toTransactionDate())
                        }
                    }
                }
                launch {
                    viewModel.lastSelected.collectLatest {
                        setFilterSelection(it)
                    }
                }
                launch {
                    viewModel.dateSelection.collectLatest {
                        val isStartDate = it.first == CustomType.START_DATE
                        showDatePickerDialog(
                            if (isStartDate) {
                                binding.startDate
                            } else {
                                binding.endDate
                            },
                            isStartDate,
                            it.second
                        )
                    }
                }
            }
        }
    }

    private fun setFilterSelection(it: FilterType) {
        when (it) {
            FilterType.THIS_MONTH -> {
                binding.thisMonth.isChecked = true
            }

            FilterType.LAST_MONTH -> {
                binding.lastMonth.isChecked = true
            }

            FilterType.LAST_THREE_MONTH -> {
                binding.lastThreeMonth.isChecked = true
            }

            FilterType.LAST_SIX_MONTH -> {
                binding.lastSixMonth.isChecked = true
            }

            FilterType.LAST_YEAR -> {
                binding.lastYear.isChecked = true
            }

            FilterType.ALL -> {
                binding.all.isChecked = true
            }

            FilterType.CUSTOM -> {
                binding.custom.isChecked = true
            }
        }
    }

    private fun disableCustomFilterIfShown() {
        if (binding.customFilterContainer.isVisible) {
            binding.customFilterContainer.isVisible = false
        }
    }

    private fun showDatePickerDialog(aView: EditText, isStartDate: Boolean, selectedDate: Date?) {

        aView.hideKeyboard()

        val selectedCalendarInstance = Calendar.getInstance()
        selectedDate?.let {
            selectedCalendarInstance.time = it
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val newDate = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                viewModel.setDate(newDate.time, isStartDate)
                aView.setText(newDate.time.toTransactionDate())
            },
            selectedCalendarInstance.get(Calendar.YEAR),
            selectedCalendarInstance.get(Calendar.MONTH),
            selectedCalendarInstance.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }
}