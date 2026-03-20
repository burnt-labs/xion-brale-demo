package com.burnt.xiondemo.ui.screens.wallet

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.burnt.xiondemo.ui.components.ActionCard
import com.burnt.xiondemo.ui.components.ErrorBanner
import com.burnt.xiondemo.ui.screens.send.SendSheetContent
import com.burnt.xiondemo.ui.screens.send.SendViewModel
import com.burnt.xiondemo.ui.theme.*
import com.burnt.xiondemo.util.CoinFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateToContract: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onDisconnected: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSendSheet by remember { mutableStateOf(false) }
    val sendViewModel: SendViewModel = hiltViewModel()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Greeting with expandable address
        var addressExpanded by remember { mutableStateOf(false) }
        val clipboardManager = LocalClipboardManager.current
        val shortAddress = uiState.address?.let {
            "${it.take(8)}...${it.takeLast(4)}"
        } ?: ""

        Column(modifier = Modifier.animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Hi $shortAddress,",
                    fontSize = 20.sp,
                    color = SubtitleText
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (!addressExpanded && uiState.address != null) {
                    Text(
                        text = "Show more",
                        fontSize = 12.sp,
                        color = SubtitleText.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { addressExpanded = true }
                    )
                }
            }

            if (addressExpanded && uiState.address != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.address!!,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SubtitleText,
                        modifier = Modifier.weight(1f, fill = false)
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
                Text(
                    text = "Show less",
                    fontSize = 12.sp,
                    color = SubtitleText.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { addressExpanded = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "What would you like to do?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = GreetingText
        )

        // Grant status warning (shown only if withGrantRecovery detects expired grants at tx time)
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

        Spacer(modifier = Modifier.height(24.dp))

        // Balance card - full width
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.isBalanceLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = XionOrange
                    )
                } else {
                    Text(
                        text = if (uiState.balance != null) CoinFormatter.formatWithDenom(uiState.balance!!) else "—",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreetingText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Available Balance",
                    fontSize = 14.sp,
                    color = SubtitleText
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2x2 Action cards grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = "Send",
                description = "Transfer tokens to another address",
                icon = Icons.AutoMirrored.Filled.Send,
                iconTint = Color(0xFF007AFF),
                iconBackground = Color(0xFF007AFF).copy(alpha = 0.12f),
                onClick = { showSendSheet = true },
                modifier = Modifier.weight(1f)
            )
            ActionCard(
                title = "Receive",
                description = "Show your wallet address",
                icon = Icons.Default.QrCode,
                iconTint = Color(0xFF34C759),
                iconBackground = Color(0xFF34C759).copy(alpha = 0.12f),
                onClick = { /* TODO: receive screen */ },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = "Contract",
                description = "Execute smart contracts",
                icon = Icons.Default.Code,
                iconTint = Color(0xFFFF9500),
                iconBackground = Color(0xFFFF9500).copy(alpha = 0.12f),
                onClick = onNavigateToContract,
                modifier = Modifier.weight(1f)
            )
            ActionCard(
                title = "History",
                description = "View past transactions",
                icon = Icons.Default.History,
                iconTint = Color(0xFFAF52DE),
                iconBackground = Color(0xFFAF52DE).copy(alpha = 0.12f),
                onClick = onNavigateToHistory,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Disconnect
        TextButton(
            onClick = { viewModel.disconnect() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Disconnect")
        }

        // Error banner
        ErrorBanner(
            message = uiState.error,
            onDismiss = { viewModel.clearError() }
        )
    }
}
