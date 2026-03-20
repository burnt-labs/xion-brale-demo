package com.burnt.xiondemo.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object CoinFormatter {

    private val displayFormat = DecimalFormat("#,##0.######")

    /**
     * Convert micro-denomination (uxion) to display denomination (XION).
     * 1 XION = 1,000,000 uxion
     */
    fun microToDisplay(microAmount: String): String {
        return try {
            val micro = BigDecimal(microAmount)
            val display = micro.divide(BigDecimal(1_000_000), Constants.DECIMALS, RoundingMode.DOWN)
            displayFormat.format(display)
        } catch (e: Exception) {
            "0"
        }
    }

    /**
     * Convert display denomination (XION) to micro-denomination (uxion).
     */
    fun displayToMicro(displayAmount: String): String {
        return try {
            val display = BigDecimal(displayAmount)
            val micro = display.multiply(BigDecimal(1_000_000)).setScale(0, RoundingMode.DOWN)
            micro.toPlainString()
        } catch (e: Exception) {
            "0"
        }
    }

    /**
     * Format a micro amount with denom for display: "1.5 XION"
     */
    fun formatWithDenom(microAmount: String): String {
        return "${microToDisplay(microAmount)} ${Constants.DISPLAY_DENOM}"
    }

    /**
     * Validate that an amount string is a valid positive number.
     */
    fun isValidAmount(amount: String): Boolean {
        return try {
            val value = BigDecimal(amount)
            value > BigDecimal.ZERO
        } catch (e: Exception) {
            false
        }
    }
}
