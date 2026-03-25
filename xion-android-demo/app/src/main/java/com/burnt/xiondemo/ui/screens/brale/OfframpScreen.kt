package com.burnt.xiondemo.ui.screens.brale

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfframpScreen(
    onDone: () -> Unit,
    viewModel: OfframpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = { Text("Cash Out", color = GreetingText) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GreetingText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBackground)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState.step) {
                OfframpStep.FORM -> OfframpForm(uiState, viewModel)
                OfframpStep.DEPOSITING -> DepositingContent()
                OfframpStep.PROCESSING -> ProcessingOfframpContent()
                OfframpStep.STATUS -> OfframpStatusContent(uiState, onDone, viewModel)
            }

            ErrorBanner(
                message = uiState.error,
                onDismiss = { viewModel.clearError() },
                modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun OfframpForm(
    uiState: OfframpUiState,
    viewModel: OfframpViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Convert stablecoins to USD and withdraw to your bank account",
            fontSize = 14.sp,
            color = SubtitleText
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bank status
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (uiState.bankLinked) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (uiState.bankLinked) XionGreen else XionOrange
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (uiState.bankLinked) "Bank Account Linked" else "Link a bank account first (use Buy flow)",
                    fontWeight = FontWeight.Medium,
                    color = GreetingText
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Amount
        OutlinedTextField(
            value = uiState.amount,
            onValueChange = { viewModel.updateAmount(it) },
            label = { Text("Amount (USD)", color = SubtitleText) },
            placeholder = { Text("100.00", color = SubtitleText) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.amountError != null,
            supportingText = uiState.amountError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GreetingText,
                unfocusedTextColor = GreetingText,
                focusedBorderColor = MintscanBlue,
                unfocusedBorderColor = SubtitleText.copy(alpha = 0.5f),
                focusedLabelColor = MintscanBlue,
                unfocusedLabelColor = SubtitleText,
                cursorColor = MintscanBlue
            ),
            leadingIcon = { Text("$", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GreetingText) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("You send", if (uiState.amount.isNotEmpty()) "~${uiState.amount} SBC" else "\u2014")
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("You receive", if (uiState.amount.isNotEmpty()) "\$${uiState.amount}" else "\$0.00")
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Method", "Same-Day ACH", valueColor = SubtitleText)
            }
        }

        if (uiState.custodialAddress != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Stablecoins will be sent to Brale custodial address for processing",
                fontSize = 12.sp,
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
            Text("Cash Out", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DepositingContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = XionOrange)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sending stablecoins to Brale...", fontSize = 16.sp, color = GreetingText)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Broadcasting on-chain transaction", fontSize = 12.sp, color = SubtitleText)
    }
}

@Composable
private fun ProcessingOfframpContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = XionOrange)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Creating offramp transfer...", fontSize = 16.sp, color = GreetingText)
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
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GreetingText
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("Transfer ID", transfer.id.take(12) + "...")
                DetailRow("Amount", "\$${transfer.amount.value} ${transfer.amount.currency}")
                DetailRow("Status", transfer.status.replaceFirstChar { it.uppercase() })
                uiState.depositTxHash?.let { DetailRow("On-chain Tx", it.take(12) + "...") }
                transfer.createdAt?.let { DetailRow("Created", it.take(19).replace("T", " ")) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.reset()
                onDone()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = XionOrange)
        ) {
            Text("Done", fontWeight = FontWeight.SemiBold)
        }
    }
}
