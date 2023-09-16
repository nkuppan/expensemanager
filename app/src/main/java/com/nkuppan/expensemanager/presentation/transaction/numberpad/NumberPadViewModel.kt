package com.nkuppan.expensemanager.presentation.transaction.numberpad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@HiltViewModel
class NumberPadViewModel @Inject constructor() : ViewModel() {

    private val _calculatedAmount = MutableStateFlow("0")
    val calculatedAmount = _calculatedAmount.asStateFlow()

    private val _calculatedAmountString = MutableStateFlow("")
    val calculatedAmountString = _calculatedAmountString.asStateFlow()

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

    fun appendString(character: String) {

        viewModelScope.launch {

            if (character.isEmpty()) {
                removeLastCharacter()
                return@launch
            }

            if (character == "." && isContainDotAlready()) {
                return@launch
            }

            if (ACTION_ARRAY.contains(character.firstOrNull()) && isLastItemIsActionCharacter()) {
                removeLastCharacter()
            }

            val currentString = _calculatedAmountString.value

            if (currentString == character || (currentString == "0" && character == "00")) {
                return@launch
            }

            val newString = currentString + character

            if (newString.isEmpty()) {
                clearAmount()
                return@launch
            }

            kotlin.runCatching {
                evaluate(newString)
            }.onSuccess {
                _calculatedAmount.value = String.format("%.2f", it)
                _calculatedAmountString.value = newString
            }.onFailure {
                _calculatedAmountString.value = newString
            }
        }
    }

    private fun removeLastCharacter() {

        val calculatorString = _calculatedAmountString.value

        if (calculatorString.isBlank()) {
            return
        }

        _calculatedAmountString.value = String.format(
            "%s", calculatorString.substring(0, calculatorString.length - 1)
        )
    }

    fun clearAmount() {
        viewModelScope.launch {
            _calculatedAmount.value = "0"
            _calculatedAmountString.value = ""
        }
    }

    private fun isLastItemIsActionCharacter(): Boolean {
        val numberString = _calculatedAmountString.value
        return numberString.isNotBlank() && ACTION_ARRAY.contains(numberString[numberString.length - 1])
    }

    private fun isContainDotAlready(): Boolean {
        val numberString = _calculatedAmountString.value.toString()

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

    companion object {
        private val ACTION_ARRAY = arrayOf('+', '-', '*', '/')
    }
}