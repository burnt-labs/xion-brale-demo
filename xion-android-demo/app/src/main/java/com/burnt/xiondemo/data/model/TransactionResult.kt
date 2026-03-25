package com.burnt.xiondemo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionResult(
    val txHash: String,
    val success: Boolean,
    val gasUsed: String,
    val gasWanted: String,
    val height: Long,
    val rawLog: String,
    val timestamp: String = "",
    val fee: String = "",
    val txType: String = "",
    val amount: String = "",
    val amountDenom: String = "",
    val recipient: String = ""
)
