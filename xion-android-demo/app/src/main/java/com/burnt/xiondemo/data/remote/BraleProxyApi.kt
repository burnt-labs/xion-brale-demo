package com.burnt.xiondemo.data.remote

import com.burnt.xiondemo.data.model.*
import retrofit2.http.*

interface BraleProxyApi {

    // -- Plaid --

    @POST("plaid/link-token")
    suspend fun createPlaidLinkToken(
        @Body request: PlaidLinkTokenRequest
    ): PlaidLinkTokenResponse

    @POST("plaid/register")
    suspend fun registerBankAccount(
        @Body request: PlaidRegisterRequest
    ): PlaidRegisterResponse

    // -- Addresses --

    @GET("addresses")
    suspend fun getAddresses(
        @Query("type") type: String? = null
    ): AddressListResponse

    @POST("addresses/external")
    suspend fun createExternalAddress(
        @Body request: CreateAddressRequest
    ): BraleAddress

    @GET("addresses/{id}")
    suspend fun getAddress(
        @Path("id") addressId: String
    ): BraleAddress

    @GET("addresses/{id}/balance")
    suspend fun getAddressBalance(
        @Path("id") addressId: String,
        @Query("transfer_type") transferType: String? = null,
        @Query("value_type") valueType: String? = null
    ): BraleBalance

    // -- Transfers --

    @POST("transfers")
    suspend fun createTransfer(
        @Body request: CreateTransferRequest
    ): BraleTransfer

    @GET("transfers/{id}")
    suspend fun getTransfer(
        @Path("id") transferId: String
    ): BraleTransfer

    @GET("transfers")
    suspend fun listTransfers(): TransferListResponse
}
