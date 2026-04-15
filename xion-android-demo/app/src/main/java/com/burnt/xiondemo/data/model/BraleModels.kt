package com.burnt.xiondemo.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Addresses
// ---------------------------------------------------------------------------

@Serializable
data class BraleAddress(
    val id: String,
    val name: String? = null,
    val type: String? = null,
    val address: String? = null,
    val status: String? = null,
    @SerialName("transfer_types") val transferTypes: List<String> = emptyList()
)

@Serializable
data class AddressListResponse(
    val addresses: List<BraleAddress> = emptyList()
)

@Serializable
data class CreateAddressRequest(
    val name: String,
    val address: String? = null,
    @SerialName("transfer_types") val transferTypes: List<String>
)

// ---------------------------------------------------------------------------
// Transfers
// ---------------------------------------------------------------------------

@Serializable
data class BraleAmount(
    val value: String,
    val currency: String
)

@Serializable
data class BraleTransferEndpoint(
    @SerialName("address_id") val addressId: String,
    @SerialName("value_type") val valueType: String,
    @SerialName("transfer_type") val transferType: String,
    @SerialName("transaction_id") val transactionId: String? = null
)

@Serializable
data class BraleTransfer(
    val id: String,
    val status: String,
    val amount: BraleAmount,
    val source: BraleTransferEndpoint? = null,
    val destination: BraleTransferEndpoint? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class TransferListResponse(
    val transfers: List<BraleTransfer> = emptyList()
)

@Serializable
data class CreateTransferRequest(
    val amount: BraleAmount,
    val source: BraleTransferEndpoint,
    val destination: BraleTransferEndpoint
)

// ---------------------------------------------------------------------------
// Plaid
// ---------------------------------------------------------------------------

@Serializable
data class PlaidLinkTokenRequest(
    @SerialName("legal_name") val legalName: String,
    @SerialName("email_address") val emailAddress: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null
)

@Serializable
data class PlaidLinkTokenResponse(
    @SerialName("link_token") val linkToken: String,
    val expiration: String? = null,
    @SerialName("request_id") val requestId: String? = null
)

@Serializable
data class PlaidRegisterRequest(
    @SerialName("public_token") val publicToken: String,
    @SerialName("customer_webhook_url") val customerWebhookUrl: String? = null,
    @SerialName("transfer_types") val transferTypes: List<String> = listOf(
        "ach_debit", "ach_credit", "same_day_ach_credit"
    )
)

@Serializable
data class PlaidRegisterResponse(
    @SerialName("address_id") val addressId: String
)

// ---------------------------------------------------------------------------
// Balance
// ---------------------------------------------------------------------------

@Serializable
data class BraleBalance(
    val available: String? = null,
    val pending: String? = null
)
