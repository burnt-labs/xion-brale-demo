package com.burnt.xiondemo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BalanceInfo(
    val amount: String,
    val denom: String
)
