package com.nkuppan.expensemanager.presentation.transaction.numberpad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.utils.KEY_AMOUNT
import com.nkuppan.expensemanager.databinding.AlertNumPadBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@AndroidEntryPoint
class NumberPadDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding: AlertNumPadBinding

    private val args: NumberPadDialogFragmentArgs by navArgs()

    private val viewModel: NumberPadViewModel by viewModels()

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.number_one -> {
                addThisValue("1")
            }

            R.id.number_two -> {
                addThisValue("2")
            }

            R.id.number_three -> {
                addThisValue("3")
            }

            R.id.number_four -> {
                addThisValue("4")
            }

            R.id.number_five -> {
                addThisValue("5")
            }

            R.id.number_six -> {
                addThisValue("6")
            }

            R.id.number_seven -> {
                addThisValue("7")
            }

            R.id.number_eight -> {
                addThisValue("8")
            }

            R.id.number_nine -> {
                addThisValue("9")
            }

            R.id.number_zero -> {
                addThisValue("0")
            }

            R.id.dot -> {

                if (isContainDotAlready()) {
                    return
                }

                addThisValue(".")
            }

            R.id.plus -> {

                if (isLastItemIsActionCharacter()) {
                    backspace()
                }

                addThisValue("+")
            }

            R.id.minus -> {

                if (isLastItemIsActionCharacter()) {
                    backspace()
                }

                addThisValue("-")
            }

            R.id.multiply -> {

                if (isLastItemIsActionCharacter()) {
                    backspace()
                }

                addThisValue("*")
            }

            R.id.divide -> {

                if (isLastItemIsActionCharacter()) {
                    backspace()
                }

                addThisValue("/")
            }

            R.id.equals -> {
                submitValue()
            }

            R.id.action_delete -> {
                backspace()
            }

            R.id.ok_button -> {

                submitValue()

                findNavController().also {
                    it.previousBackStackEntry?.savedStateHandle?.set(
                        KEY_AMOUNT,
                        binding.inputNumber.text.toString()
                    )
                    it.popBackStack()
                }
            }

            R.id.cancel_button -> {
                findNavController().popBackStack()
            }
        }
    }

    private fun backspace() {
        if (binding.inputNumber.text.toString().isBlank()) {
            return
        }

        binding.inputNumber.text = String.format(
            "%s", binding.inputNumber.text.toString().substring(
                0,
                binding.inputNumber.text.toString().length - 1
            )
        )
    }

    private fun addThisValue(value: String) {
        clearValueIfZero()

        binding.inputNumber.text =
            String.format("%s%s", binding.inputNumber.text.toString(), value)
    }

    private fun clearValueIfZero() {
        if (binding.inputNumber.text.toString() == "0.0") {
            binding.inputNumber.text = ""
        }
    }

    private fun submitValue() {
        try {
            var executedValue = evaluate(binding.inputNumber.text.toString())
            if (executedValue < 0) {
                executedValue = 0.0
            }
            binding.inputNumber.text = String.format("%.2f", executedValue)

        } catch (aException: Exception) {
            aException.printStackTrace()
        }
    }

    private fun isLastItemIsActionCharacter(): Boolean {
        val numberString = binding.inputNumber.text.toString()
        return numberString.isNotBlank() && ACTION_ARRAY.contains(numberString[numberString.length - 1])
    }

    private fun isContainDotAlready(): Boolean {
        val numberString = binding.inputNumber.text.toString()

        if (numberString.isNotBlank()) {
            val splitArray = numberString.split("[+\\-*/]".toRegex())

            if (splitArray.isNotEmpty()) {
                val dotSplitArray = splitArray[splitArray.size - 1].split("[.]".toRegex())
                return dotSplitArray.size > 1
            }

            return false
        } else {
            return false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AlertNumPadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        initObservers()
    }

    private fun initObservers() {

    }

    private fun initViews() {
        binding.numberZero.setOnClickListener(this)
        binding.numberOne.setOnClickListener(this)
        binding.numberTwo.setOnClickListener(this)
        binding.numberThree.setOnClickListener(this)
        binding.numberFour.setOnClickListener(this)
        binding.numberFive.setOnClickListener(this)
        binding.numberSix.setOnClickListener(this)
        binding.numberSeven.setOnClickListener(this)
        binding.numberEight.setOnClickListener(this)
        binding.numberNine.setOnClickListener(this)

        binding.dot.setOnClickListener(this)
        binding.actionDelete.setOnClickListener(this)

        binding.plus.setOnClickListener(this)
        binding.minus.setOnClickListener(this)
        binding.divide.setOnClickListener(this)
        binding.multiply.setOnClickListener(this)
        binding.equals.setOnClickListener(this)

        binding.okButton.setOnClickListener(this)
        binding.cancelButton.setOnClickListener(this)

        binding.inputNumber.text = "${args.amount}"
    }

    private fun evaluate(str: String): Double {

        return object : Any() {

            var pos = -1

            var ch: Int = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm() // addition
                        eat('-'.code) -> x -= parseTerm() // subtraction
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor() // multiplication
                        eat('/'.code) -> x /= parseFactor() // division
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "sin" -> sin(Math.toRadians(x))
                        "cos" -> cos(Math.toRadians(x))
                        "tan" -> tan(Math.toRadians(x))
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation

                return x
            }
        }.parse()
    }

    companion object {
        private val ACTION_ARRAY = arrayOf('+', '-', '*', '/')
    }
}