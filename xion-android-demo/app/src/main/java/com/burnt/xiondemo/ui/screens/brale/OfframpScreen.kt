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
                    text = if (uiState.bankLinked) (uiState.bankName ?: "Bank Account Linked") else "Link a bank account first (use Buy flow)",
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
        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = MintscanBlue)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sending stablecoins to Brale...", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = GreetingText)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Broadcasting on-chain transaction", fontSize = 13.sp, color = SubtitleText)
    }
}

@Composable
private fun ProcessingOfframpContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = MintscanBlue)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Creating cash out transfer...", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = GreetingText)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Setting up ACH credit to your bank", fontSize = 13.sp, color = SubtitleText)
    }
}

@Composable
private fun OfframpStatusContent(
    uiState: OfframpUiState,
    onDone: () -> Unit,
    viewModel: OfframpViewModel
) {
    val hasError = uiState.error != null && !uiState.depositConfirmed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (hasError) Icons.Default.Error else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (hasError) XionRed else XionGreen,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hasError) "Cash Out Failed" else "Cash Out Submitted!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GreetingText
        )
        if (!hasError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your stablecoins have been sent to Brale. The USD will be deposited to your bank account within 1\u20133 business days via ACH.",
                fontSize = 13.sp,
                color = SubtitleText,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                uiState.transfer?.let { transfer ->
                    DetailRow("Transfer ID", transfer.id.take(12) + "...")
                    DetailRow("Amount", "\$${transfer.amount.value} ${transfer.amount.currency}")
                }
                if (uiState.depositConfirmed) {
                    DetailRow("On-chain Deposit", "Confirmed", valueColor = XionGreen)
                }
                DetailRow("Bank Transfer", "In Progress")
                uiState.depositTxHash?.let { DetailRow("Tx Hash", it.take(16) + "...") }
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
            colors = ButtonDefaults.buttonColors(containerColor = MintscanBlue)
        ) {
            Text("Done", fontWeight = FontWeight.SemiBold)
        }
    }
}
