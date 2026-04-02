package com.burnt.xiondemo.di

import com.burnt.xiondemo.data.model.WalletState
import com.burnt.xiondemo.data.repository.XionRepository
import javax.inject.Inject
import javax.inject.Singleton

interface WalletAddressProvider {
    fun getWalletAddress(): String?
}

@Singleton
class WalletAddressProviderImpl @Inject constructor(
    private val xionRepository: XionRepository
) : WalletAddressProvider {
    override fun getWalletAddress(): String? =
        (xionRepository.walletState.value as? WalletState.Connected)?.metaAccountAddress
}
