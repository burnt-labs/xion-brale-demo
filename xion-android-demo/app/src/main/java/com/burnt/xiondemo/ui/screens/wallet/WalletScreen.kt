package com.burnt.xiondemo.ui.screens.wallet

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.burnt.xiondemo.ui.components.CompactTransactionRow
import com.burnt.xiondemo.ui.components.ErrorBanner
import com.burnt.xiondemo.ui.screens.send.SendSheetContent
import com.burnt.xiondemo.ui.screens.send.SendViewModel
import com.burnt.xiondemo.ui.theme.*
import com.burnt.xiondemo.util.CoinFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun WalletScreen(
    onNavigateToContract: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToOnramp: () -> Unit,
    onNavigateToOfframp: () -> Unit,
    onNavigateToLinkBank: () -> Unit,
    onNavigateToVault: () -> Unit,
    onDisconnected: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sendViewModel: SendViewModel = hiltViewModel()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showSendSheet by remember { mutableStateOf(false) }

    // Refresh balances when screen becomes visible (e.g., returning from onramp/offramp)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.isDisconnected) {
        if (uiState.isDisconnected) onDisconnected()
    }

    if (showSendSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSendSheet = false
                sendViewModel.resetState()
            },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = CardBackground,
            windowInsets = WindowInsets(0)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                SendSheetContent(
                    viewModel = sendViewModel,
                    onDone = {
                        showSendSheet = false
                        sendViewModel.resetState()
                        viewModel.refresh()
                    }
                )
            }
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isBalanceLoading) {
        if (!uiState.isBalanceLoading) isRefreshing = false
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refresh()
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .pullRefresh(pullRefreshState)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Address row with disconnect on the left
        if (uiState.address != null) {
            val shortAddress = "${uiState.address!!.take(8)}...${uiState.address!!.takeLast(4)}"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.disconnect() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Disconnect", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = shortAddress,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SubtitleText
                    )
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(uiState.address!!)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy address",
                            tint = SubtitleText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Grant status warnings
        if (!uiState.grantsActive) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Session grants expired. Please reconnect.", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (uiState.sessionExpiryWarning) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Session expiring soon. Please reconnect.", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Balance card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "XION Balance",
                    fontSize = 14.sp,
                    color = SubtitleText
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.isBalanceLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = XionOrange
                    )
                } else {
                    Text(
                        text = if (uiState.balance != null) CoinFormatter.formatWithDenom(uiState.balance!!) else "\u2014",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreetingText,
                        maxLines = 1
                    )
                }

                if (uiState.sbcBalance != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SubtitleText.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Stablecoin Balance",
                        fontSize = 14.sp,
                        color = SubtitleText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = CoinFormatter.formatWithDenom(uiState.sbcBalance!!, "SBC"),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreetingText,
                        maxLines = 1
                    )
                }

                if (uiState.vaultBalance != null && uiState.vaultBalance != "0") {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SubtitleText.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Vault Balance",
                                fontSize = 14.sp,
                                color = SubtitleText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = CoinFormatter.formatWithDenom(uiState.vaultBalance!!),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreetingText,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Vault",
                            tint = SubtitleText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Send Tokens button
        Button(
            onClick = { showSendSheet = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = XionOrange)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Send Tokens",
                fontWeight = FontWeight.SemiBold
            )
        }

        // Vault button
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onNavigateToVault,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SubtitleText.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = GreetingText
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Vault",
                fontWeight = FontWeight.SemiBold,
                color = GreetingText
            )
        }

        // Bank link status
        Spacer(modifier = Modifier.height(12.dp))
        if (!uiState.bankLinked) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = XionOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Link Bank Account",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GreetingText
                            )
                            Text(
                                text = "Required for buying and selling stablecoins",
                                fontSize = 12.sp,
                                color = SubtitleText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onNavigateToLinkBank,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = XionOrange),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Link", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = XionGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bank Linked",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = XionGreen
                )
            }
        }

        // Buy / Cash Out buttons
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToOnramp,
                enabled = uiState.bankLinked,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = XionGreen,
                    disabledContainerColor = XionGreen.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buy", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onNavigateToOfframp,
                enabled = uiState.bankLinked,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintscanBlue,
                    disabledContainerColor = MintscanBlue.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cash Out", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Transactions section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = GreetingText
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            if (uiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions yet",
                        fontSize = 14.sp,
                        color = SubtitleText
                    )
                }
            } else {
                Column {
                    val recentTxs = uiState.transactions.take(3)
                    recentTxs.forEachIndexed { index, tx ->
                        CompactTransactionRow(transaction = tx)
                        if (index < recentTxs.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // View on Mintscan link
        if (uiState.address != null) {
            TextButton(
                onClick = {
                    val url = "https://www.mintscan.io/xion-testnet/address/${uiState.address}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "View all on Mintscan \u2192",
                    color = MintscanBlue,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Error banner
        ErrorBanner(
            message = uiState.error,
            onDismiss = { viewModel.clearError() },
            onRetry = { viewModel.refresh() }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
    PullRefreshIndicator(
        refreshing = isRefreshing,
        state = pullRefreshState,
        modifier = Modifier.align(Alignment.TopCenter),
        contentColor = XionOrange
    )
    } // Box pull-to-refresh
}
