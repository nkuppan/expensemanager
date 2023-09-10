package com.nkuppan.expensemanager.presentation.category.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButtonToggleGroup
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.hideKeyboard
import com.nkuppan.expensemanager.core.ui.extensions.showSnackBarMessage
import com.nkuppan.expensemanager.core.ui.fragment.BaseBindingFragment
import com.nkuppan.expensemanager.core.utils.KEY_DELETE_STATUS
import com.nkuppan.expensemanager.databinding.FragmentCategoryCreateBinding
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CategoryCreateFragment : BaseBindingFragment<FragmentCategoryCreateBinding>(),
    MaterialButtonToggleGroup.OnButtonCheckedListener {

    private val category: Category? by lazy {
        navArgs<CategoryCreateFragmentArgs>().value.category
    }

    private val viewModel: CategoryCreateViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        initViews()

        initObserver()
    }

    private fun initViews() {

        inflateMenuItems()

        binding.actionSave.setOnClickListener {
            viewModel.onSaveClick()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.categoryTypeContainer.addOnButtonCheckedListener(this)
    }

    override fun onDestroyView() {
        binding.categoryTypeContainer.removeOnButtonCheckedListener(this)
        super.onDestroyView()
    }

    private fun initObserver() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.categoryCreated.collectLatest {
                        findNavController().popBackStack()
                    }
                }

                launch {
                    viewModel.categoryType.collectLatest {
                        binding.categoryTypeContainer.check(
                            if (it == CategoryType.EXPENSE) {
                                R.id.expense_option
                            } else {
                                R.id.income_option
                            }
                        )
                    }
                }

                launch {
                    viewModel.colorPicker.collectLatest {
                        binding.root.hideKeyboard()
                        viewModel.colorValue.value.let { _ ->
                            openColorPicker()
                        }
                    }
                }

                launch {
                    viewModel.errorMessage.collectLatest {
                        binding.root.showSnackBarMessage(it.asString(requireContext()))
                    }
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>(KEY_DELETE_STATUS)?.observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().popBackStack()
                } else {
                    binding.root.showSnackBarMessage(R.string.category_delete_error_message)
                }
            }

        category?.let {
            viewModel.setCategoryValue(it)
        }
    }

    private fun openColorPicker() {
        ColorPickerDialog.Builder(requireContext())
            .setTitle(getString(R.string.title_color_picker))
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton(getString(R.string.select),
                ColorEnvelopeListener { envelope, _ ->
                    viewModel.setColorValue(envelope.color)
                }
            )
            .setNegativeButton(
                getString(R.string.cancel)
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }


    private fun inflateMenuItems() {

        category ?: return

        binding.toolbar.inflateMenu(R.menu.menu_category_create)

        binding.toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener if (it.itemId == R.id.action_delete) {
                category?.let { value ->
                    findNavController().navigate(
                        CategoryCreateFragmentDirections.actionCategoryCreateFragmentToCategoryDeleteDialog(
                            value
                        )
                    )
                }
                true
            } else {
                false
            }
        }
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentCategoryCreateBinding {
        return FragmentCategoryCreateBinding.inflate(inflater, container, false)
    }

    override fun bindData(binding: FragmentCategoryCreateBinding) {
        super.bindData(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner
    }

    override fun onButtonChecked(
        group: MaterialButtonToggleGroup?,
        checkedId: Int,
        isChecked: Boolean
    ) {
        if (isChecked) {
            if (checkedId == R.id.expense_option) {
                viewModel.setCategoryType(CategoryType.EXPENSE)
            } else if (checkedId == R.id.income_option) {
                viewModel.setCategoryType(CategoryType.INCOME)
            }
        }
    }
}