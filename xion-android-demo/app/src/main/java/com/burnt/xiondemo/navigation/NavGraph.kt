package com.burnt.xiondemo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.burnt.xiondemo.ui.screens.connect.ConnectScreen
import com.burnt.xiondemo.ui.screens.contract.ContractScreen
import com.burnt.xiondemo.ui.screens.history.HistoryScreen
import com.burnt.xiondemo.ui.screens.wallet.WalletScreen

object Routes {
    const val CONNECT = "connect"
    const val WALLET = "wallet"
    const val CONTRACT = "contract"
    const val HISTORY = "history"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.CONNECT
    ) {
        composable(Routes.CONNECT) {
            ConnectScreen(
                onConnected = {
                    navController.navigate(Routes.WALLET) {
                        popUpTo(Routes.CONNECT) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.WALLET) {
            WalletScreen(
                onNavigateToContract = { navController.navigate(Routes.CONTRACT) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onDisconnected = {
                    navController.navigate(Routes.CONNECT) {
                        popUpTo(Routes.WALLET) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CONTRACT) {
            ContractScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
