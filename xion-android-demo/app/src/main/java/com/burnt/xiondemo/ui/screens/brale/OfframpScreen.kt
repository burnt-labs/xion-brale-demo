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
fun OfframpScreen(
    onDone: () -> Unit,
    viewModel: OfframpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxWidth()) {
        when (uiState.step) {
            OfframpStep.FORM -> OfframpForm(uiState, viewModel, onDone)
            OfframpStep.DEPOSITING -> DepositingContent()
            OfframpStep.PROCESSING -> ProcessingOfframpContent()
            OfframpStep.STATUS -> OfframpStatusContent(uiState, onDone, viewModel)
        }

        ErrorBanner(
            message = uiState.error,
            onDismiss = { viewModel.clearError() },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun OfframpForm(
    uiState: OfframpUiState,
    viewModel: OfframpViewModel,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Cash Out",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Convert stablecoins to USD and withdraw to your bank account",
            style = MaterialTheme.typography.bodyMedium,
            color = SubtitleText
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bank status
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.bankLinked) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (uiState.bankLinked) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (uiState.bankLinked) "Bank Account Linked" else "Link a bank account first (use Buy flow)",
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Amount
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
            leadingIcon = { Text("$", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("You send", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (uiState.amount.isNotEmpty()) "~${uiState.amount} SBC" else "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("You receive", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (uiState.amount.isNotEmpty()) "\$${uiState.amount}" else "\$0.00",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Method", style = MaterialTheme.typography.bodyMedium, color = SubtitleText)
                    Text("Same-Day ACH", style = MaterialTheme.typography.bodyMedium, color = SubtitleText)
                }
            }
        }

        if (uiState.custodialAddress != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Stablecoins will be sent to Brale custodial address for processing",
                style = MaterialTheme.typography.bodySmall,
                color = SubtitleText
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.submitOfframp() },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.isFormValid && !uiState.isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MintscanBlue)
        ) {
            Text("Cash Out")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DepositingContent() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sending stablecoins to Brale...", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Broadcasting on-chain transaction",
            style = MaterialTheme.typography.bodySmall,
            color = SubtitleText
        )
    }
}

@Composable
private fun ProcessingOfframpContent() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Creating offramp transfer...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun OfframpStatusContent(
    uiState: OfframpUiState,
    onDone: () -> Unit,
    viewModel: OfframpViewModel
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
            else -> MintscanBlue
        }

        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = when (transfer.status) {
                "complete" -> "Cash Out Complete!"
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
                uiState.depositTxHash?.let { StatusRow("On-chain Tx", it.take(12) + "...") }
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
