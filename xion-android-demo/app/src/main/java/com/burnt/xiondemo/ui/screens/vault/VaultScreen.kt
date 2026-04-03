package com.burnt.xiondemo.ui.screens.vault

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.burnt.xiondemo.ui.components.ErrorBanner
import com.burnt.xiondemo.ui.screens.send.SendToken
import com.burnt.xiondemo.ui.theme.*
import com.burnt.xiondemo.util.CoinFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onBack: () -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllBalances()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GreetingText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBackground)
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Vault balance card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Vault Balance", fontSize = 14.sp, color = SubtitleText)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.isBalanceLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = XionOrange
                            )
                        } else {
                            Text(
                                text = if (uiState.vaultBalance != null) CoinFormatter.formatWithDenom(uiState.vaultBalance!!) else "0 XION",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreetingText
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vault",
                        tint = SubtitleText,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available wallet balances
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Available to Deposit", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SubtitleText)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("XION", fontSize = 15.sp, color = GreetingText)
                        Text(
                            text = uiState.walletBalance?.let { CoinFormatter.formatWithDenom(it) } ?: "—",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreetingText
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SBC", fontSize = 15.sp, color = GreetingText)
                        Text(
                            text = uiState.walletSbcBalance?.let { CoinFormatter.formatWithDenom(it, "SBC") } ?: "—",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreetingText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Success state
            if (uiState.txHash != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = XionGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Transaction Successful", fontWeight = FontWeight.SemiBold, color = XionGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.txHash!!,
                            fontSize = 12.sp,
                            color = SubtitleText,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = XionOrange)
                ) {
                    Text("New Transaction", fontWeight = FontWeight.SemiBold)
                }
            } else {
                // Token selector
                Text("Token", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SubtitleText)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SendToken.entries.forEach { token ->
                        FilterChip(
                            selected = uiState.selectedToken == token,
                            onClick = { viewModel.selectToken(token) },
                            label = { Text(token.displayName, color = if (uiState.selectedToken == token) CardBackground else GreetingText) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = XionOrange,
                                containerColor = CardBackground
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount input
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.updateAmount(it) },
                    label = { Text("Amount (${uiState.selectedToken.displayName})") },
                    placeholder = { Text("0.0", color = SubtitleText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    textStyle = TextStyle(color = GreetingText, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GreetingText,
                        unfocusedTextColor = GreetingText,
                        focusedBorderColor = XionOrange,
                        unfocusedBorderColor = SubtitleText.copy(alpha = 0.4f),
                        focusedLabelColor = XionOrange,
                        unfocusedLabelColor = SubtitleText,
                        cursorColor = XionOrange
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Deposit / Withdraw buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.deposit() },
                        enabled = uiState.amount.isNotBlank() && !uiState.isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = XionGreen,
                            contentColor = Color.White,
                            disabledContainerColor = XionGreen.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Deposit", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(
                        onClick = { viewModel.withdraw() },
                        enabled = uiState.amount.isNotBlank() && !uiState.isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintscanBlue,
                            contentColor = Color.White,
                            disabledContainerColor = MintscanBlue.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Withdraw", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Withdraw All button
                OutlinedButton(
                    onClick = { viewModel.withdrawAll() },
                    enabled = !uiState.isLoading && uiState.vaultBalance != null && uiState.vaultBalance != "0",
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SubtitleText.copy(alpha = 0.3f))
                ) {
                    Text("Withdraw All", fontWeight = FontWeight.SemiBold, color = GreetingText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ErrorBanner(
                message = uiState.error,
                onDismiss = { viewModel.clearError() },
                onRetry = null
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
