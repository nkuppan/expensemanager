package com.nkuppan.expensemanager.presentation.settings.currency

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Currency
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrencyDialogFragment : DialogFragment() {

    private val args: CurrencyDialogFragmentArgs by navArgs()

    private val currency: Currency by lazy { args.currency }

    private val viewModel: CurrencyViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currencies = viewModel.currencies.value
        val currencyTitles = currencies.map { currency ->
            "${getString(currency.name)} (${getString(currency.type)})"
        }.toTypedArray()
        val checkedItem = currencies.indexOfFirst {
            getString(it.type) == getString(currency.type)
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.choose_currency)
            .setSingleChoiceItems(currencyTitles, checkedItem) { _, which ->
                val whichCurrency = currencies[which]
                viewModel.setCurrency(whichCurrency)
                dismiss()
            }
            .create()
    }
}
