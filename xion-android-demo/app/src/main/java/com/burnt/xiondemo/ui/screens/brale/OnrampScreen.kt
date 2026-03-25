package com.burnt.xiondemo.ui.screens.brale

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.burnt.xiondemo.ui.components.ErrorBanner
import com.burnt.xiondemo.ui.theme.*

@Composable
fun OnrampScreen(
    onDone: () -> Unit,
    viewModel: OnrampViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxWidth()) {
        when (uiState.step) {
            OnrampStep.FORM -> OnrampForm(uiState, viewModel, onDone)
            OnrampStep.LINKING_BANK -> LinkingBankContent(uiState)
            OnrampStep.PROCESSING -> ProcessingContent()
            OnrampStep.STATUS -> TransferStatusContent(uiState, onDone, viewModel)
        }

        ErrorBanner(
            message = uiState.error,
            onDismiss = { viewModel.clearError() },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun OnrampForm(
    uiState: OnrampUiState,
    viewModel: OnrampViewModel,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Buy Stablecoins",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Convert USD from your bank account to stablecoins on Xion",
            style = MaterialTheme.typography.bodyMedium,
            color = SubtitleText
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bank account status
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.bankLinked) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (uiState.bankLinked) Icons.Default.CheckCircle else Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.bankLinked) "Bank Account Linked" else "No Bank Account",
                        fontWeight = FontWeight.Medium
                    )
                    if (!uiState.bankLinked) {
                        Text(
                            text = "Link your bank account to buy stablecoins",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtitleText
                        )
                    }
                }
                if (!uiState.bankLinked) {
                    Button(
                        onClick = { viewModel.requestPlaidLinkToken(name = "User", email = "") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Link", fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Amount input
        OutlinedTextField(
            value = uiState.amount,
            onValueChange = { viewModel.updateAmount(it) },
            label = { Text("Amount (USD)") },
            placeholder = { Text("100.00") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.amountError != null,
            supportingText = uiState.amountError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Text("$", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fee info
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("You pay", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (uiState.amount.isNotEmpty()) "\$${uiState.amount}" else "\$0.00",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("You receive", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (uiState.amount.isNotEmpty()) "~${uiState.amount} SBC" else "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Method", style = MaterialTheme.typography.bodyMedium, color = SubtitleText)
                    Text("ACH Debit", style = MaterialTheme.typography.bodyMedium, color = SubtitleText)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.submitOnramp() },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.isFormValid && !uiState.isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = XionOrange)
        ) {
            Text("Buy Stablecoins")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LinkingBankContent(uiState: OnrampUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Connecting to Plaid...", style = MaterialTheme.typography.bodyLarge)
        } else if (uiState.plaidLinkToken != null) {
            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Plaid Link Ready", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Link token: ${uiState.plaidLinkToken.take(20)}...",
                style = MaterialTheme.typography.bodySmall,
                color = SubtitleText
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Plaid Link SDK integration pending.\nFor now, test with the proxy API directly.",
                style = MaterialTheme.typography.bodyMedium,
                color = SubtitleText
            )
        }
    }
}

@Composable
private fun ProcessingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Creating transfer...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TransferStatusContent(
    uiState: OnrampUiState,
    onDone: () -> Unit,
    viewModel: OnrampViewModel
) {
    val transfer = uiState.transfer ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = when (transfer.status) {
            "complete" -> Icons.Default.CheckCircle
            "failed", "canceled" -> Icons.Default.Error
            else -> Icons.Default.Pending
        }
        val tint = when (transfer.status) {
            "complete" -> XionGreen
            "failed", "canceled" -> XionRed
            else -> XionOrange
        }

        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = when (transfer.status) {
                "complete" -> "Purchase Complete!"
                "failed" -> "Transfer Failed"
                "canceled" -> "Transfer Canceled"
                "processing" -> "Processing..."
                else -> "Pending..."
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatusRow("Transfer ID", transfer.id.take(12) + "...")
                StatusRow("Amount", "\$${transfer.amount.value} ${transfer.amount.currency}")
                StatusRow("Status", transfer.status.replaceFirstChar { it.uppercase() })
                transfer.createdAt?.let { StatusRow("Created", it.take(19).replace("T", " ")) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.reset()
                onDone()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SubtitleText)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
