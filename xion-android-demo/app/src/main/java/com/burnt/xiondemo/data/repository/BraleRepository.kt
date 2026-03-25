package com.burnt.xiondemo.data.repository

import com.burnt.xiondemo.data.model.*
import com.burnt.xiondemo.data.remote.BraleProxyApi
import com.burnt.xiondemo.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

interface BraleRepository {
    suspend fun createPlaidLinkToken(name: String, email: String): PlaidLinkTokenResponse
    suspend fun registerBankAccount(publicToken: String): String
    suspend fun getInternalAddresses(): List<BraleAddress>
    suspend fun registerXionAddress(walletAddress: String): BraleAddress
    suspend fun createOnrampTransfer(
        amount: String,
        bankAddressId: String,
        xionAddressId: String
    ): BraleTransfer
    suspend fun createOfframpTransfer(
        amount: String,
        custodialAddressId: String,
        bankAddressId: String
    ): BraleTransfer
    suspend fun getTransfer(transferId: String): BraleTransfer
    suspend fun listTransfers(): List<BraleTransfer>
}

@Singleton
class BraleRepositoryImpl @Inject constructor(
    private val api: BraleProxyApi
) : BraleRepository {

    override suspend fun createPlaidLinkToken(name: String, email: String): PlaidLinkTokenResponse {
        return api.createPlaidLinkToken(
            PlaidLinkTokenRequest(legalName = name, emailAddress = email)
        )
    }

    override suspend fun registerBankAccount(publicToken: String): String {
        return api.registerBankAccount(PlaidRegisterRequest(publicToken = publicToken)).addressId
    }

    override suspend fun getInternalAddresses(): List<BraleAddress> {
        return api.getAddresses(type = "internal").data
    }

    override suspend fun registerXionAddress(walletAddress: String): BraleAddress {
        return api.createExternalAddress(
            CreateAddressRequest(
                name = "XION Wallet $walletAddress",
                walletAddress = walletAddress,
                transferTypes = listOf(Constants.BRALE_TRANSFER_TYPE)
            )
        )
    }

    override suspend fun createOnrampTransfer(
        amount: String,
        bankAddressId: String,
        xionAddressId: String
    ): BraleTransfer {
        return api.createTransfer(
            CreateTransferRequest(
                amount = BraleAmount(value = amount, currency = Constants.BRALE_FIAT_CURRENCY),
                source = BraleTransferEndpoint(
                    addressId = bankAddressId,
                    valueType = Constants.BRALE_FIAT_VALUE_TYPE,
                    transferType = Constants.BRALE_ACH_DEBIT_TYPE
                ),
                destination = BraleTransferEndpoint(
                    addressId = xionAddressId,
                    valueType = Constants.BRALE_STABLECOIN_DENOM,
                    transferType = Constants.BRALE_TRANSFER_TYPE
                )
            )
        )
    }

    override suspend fun createOfframpTransfer(
        amount: String,
        custodialAddressId: String,
        bankAddressId: String
    ): BraleTransfer {
        return api.createTransfer(
            CreateTransferRequest(
                amount = BraleAmount(value = amount, currency = Constants.BRALE_FIAT_CURRENCY),
                source = BraleTransferEndpoint(
                    addressId = custodialAddressId,
                    valueType = Constants.BRALE_STABLECOIN_DENOM,
                    transferType = Constants.BRALE_TRANSFER_TYPE
                ),
                destination = BraleTransferEndpoint(
                    addressId = bankAddressId,
                    valueType = Constants.BRALE_FIAT_VALUE_TYPE,
                    transferType = Constants.BRALE_ACH_CREDIT_TYPE
                )
            )
        )
    }

    override suspend fun getTransfer(transferId: String): BraleTransfer {
        return api.getTransfer(transferId)
    }

    override suspend fun listTransfers(): List<BraleTransfer> {
        return api.listTransfers().data
    }
}
