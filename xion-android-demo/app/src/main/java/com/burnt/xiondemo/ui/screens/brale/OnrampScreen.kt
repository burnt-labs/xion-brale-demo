package com.burnt.xiondemo.ui.screens.brale

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.burnt.xiondemo.ui.components.ErrorBanner
import com.burnt.xiondemo.ui.theme.*
import com.plaid.link.OpenPlaidLink
import com.plaid.link.configuration.LinkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnrampScreen(
    onDone: () -> Unit,
    viewModel: OnrampViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Plaid Link launcher
    val plaidLauncher = rememberLauncherForActivityResult(
        contract = OpenPlaidLink()
    ) { result ->
        when (result) {
            is LinkSuccess -> {
                val publicToken = result.publicToken
                viewModel.onPlaidSuccess(publicToken)
            }
            is LinkExit -> {
                val error = result.error
                val metadata = result.metadata
                val errorMsg = buildString {
                    append("[PLAID EXIT] ")
                    if (error != null) {
                        append("code=${error.errorCode}, ")
                        append("message=${error.errorMessage}, ")
                        append("display=${error.displayMessage}")
                    } else {
                        append("No error (user closed)")
                    }
                    append(" | institutionId=${metadata.institution?.id}")
                    append(", institutionName=${metadata.institution?.name}")
                    append(", linkSessionId=${metadata.linkSessionId}")
                    append(", status=${metadata.status}")
                }
                viewModel.onPlaidExit(errorMsg)
            }
        }
    }

    // Launch Plaid Link when we have a token
    LaunchedEffect(uiState.plaidLinkToken) {
        val token = uiState.plaidLinkToken ?: return@LaunchedEffect
        val config = LinkTokenConfiguration.Builder()
            .token(token)
            .build()
        plaidLauncher.launch(config)
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = { Text("Buy Stablecoins", color = GreetingText) },
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
                OnrampStep.FORM -> OnrampForm(uiState, viewModel)
                OnrampStep.PROCESSING -> ProcessingContent()
                OnrampStep.STATUS -> TransferStatusContent(uiState, onDone, viewModel)
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
private fun OnrampForm(
    uiState: OnrampUiState,
    viewModel: OnrampViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Convert USD from your bank account to stablecoins on Xion",
            fontSize = 14.sp,
            color = SubtitleText
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bank account status
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.bankLinked) Icons.Default.CheckCircle else Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (uiState.bankLinked) XionGreen else SubtitleText
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.bankLinked) (uiState.bankName ?: "Bank Account Linked") else "No Bank Account",
                            fontWeight = FontWeight.Medium,
                            color = GreetingText
                        )
                        if (!uiState.bankLinked) {
                            Text(
                                text = "Link your bank account via Plaid to buy stablecoins",
                                fontSize = 12.sp,
                                color = SubtitleText
                            )
                        }
                    }
                    if (uiState.bankLinked) {
                        TextButton(onClick = { viewModel.unlinkBank() }) {
                            Text("Unlink", color = SubtitleText, fontSize = 13.sp)
                        }
                    }
                }

                if (!uiState.bankLinked) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.userName,
                        onValueChange = { viewModel.updateUserName(it) },
                        label = { Text("Legal Name", color = SubtitleText) },
                        placeholder = { Text("John Doe", color = SubtitleText) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.userNameError != null,
                        supportingText = uiState.userNameError?.let { { Text(it) } },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = GreetingText),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GreetingText,
                            unfocusedTextColor = GreetingText,
                            focusedBorderColor = XionOrange,
                            unfocusedBorderColor = SubtitleText.copy(alpha = 0.5f),
                            focusedLabelColor = XionOrange,
                            unfocusedLabelColor = SubtitleText,
                            cursorColor = XionOrange
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SubtitleText)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.userEmail,
                        onValueChange = { viewModel.updateUserEmail(it) },
                        label = { Text("Email Address", color = SubtitleText) },
                        placeholder = { Text("you@example.com", color = SubtitleText) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.userEmailError != null,
                        supportingText = uiState.userEmailError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = GreetingText),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GreetingText,
                            unfocusedTextColor = GreetingText,
                            focusedBorderColor = XionOrange,
                            unfocusedBorderColor = SubtitleText.copy(alpha = 0.5f),
                            focusedLabelColor = XionOrange,
                            unfocusedLabelColor = SubtitleText,
                            cursorColor = XionOrange
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = SubtitleText)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.userPhone,
                        onValueChange = { viewModel.updateUserPhone(it) },
                        label = { Text("Phone Number", color = SubtitleText) },
                        placeholder = { Text("+15551234567", color = SubtitleText) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.userPhoneError != null,
                        supportingText = uiState.userPhoneError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = GreetingText),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GreetingText,
                            unfocusedTextColor = GreetingText,
                            focusedBorderColor = XionOrange,
                            unfocusedBorderColor = SubtitleText.copy(alpha = 0.5f),
                            focusedLabelColor = XionOrange,
                            unfocusedLabelColor = SubtitleText,
                            cursorColor = XionOrange
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = SubtitleText)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.userDob,
                        onValueChange = { viewModel.updateUserDob(it) },
                        label = { Text("Date of Birth", color = SubtitleText) },
                        placeholder = { Text("1990-01-15", color = SubtitleText) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.userDobError != null,
                        supportingText = uiState.userDobError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = GreetingText),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GreetingText,
                            unfocusedTextColor = GreetingText,
                            focusedBorderColor = XionOrange,
                            unfocusedBorderColor = SubtitleText.copy(alpha = 0.5f),
                            focusedLabelColor = XionOrange,
                            unfocusedLabelColor = SubtitleText,
                            cursorColor = XionOrange
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = SubtitleText)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.requestPlaidLinkToken() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = XionOrange),
                        enabled = viewModel.isLinkFormValid && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CardBackground, strokeWidth = 2.dp)
                        } else {
                            Text("Link Bank Account", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Amount input
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
                focusedBorderColor = XionOrange,
                unfocusedBorderColor = SubtitleText.copy(alpha = 0.5f),
                focusedLabelColor = XionOrange,
                unfocusedLabelColor = SubtitleText,
                cursorColor = XionOrange
            ),
            leadingIcon = {
                Text("$", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GreetingText)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fee info card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("You pay", if (uiState.amount.isNotEmpty()) "\$${uiState.amount}" else "\$0.00")
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("You receive", if (uiState.amount.isNotEmpty()) "~${uiState.amount} SBC" else "\u2014")
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Method", "ACH Debit", valueColor = SubtitleText)
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
            Text("Buy Stablecoins", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProcessingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = XionOrange)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Waiting for tokens...", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = GreetingText)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Checking your wallet for incoming stablecoins",
            fontSize = 13.sp,
            color = SubtitleText
        )
    }
}

@Composable
private fun TransferStatusContent(
    uiState: OnrampUiState,
    onDone: () -> Unit,
    viewModel: OnrampViewModel
) {
    val transfer = uiState.transfer
    val tokensReceived = uiState.tokensReceived

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (tokensReceived) Icons.Default.CheckCircle else Icons.Default.Pending,
            contentDescription = null,
            tint = if (tokensReceived) XionGreen else XionOrange,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (tokensReceived) "Tokens Received!" else "Transfer Submitted",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GreetingText
        )
        if (tokensReceived && uiState.receivedAmount != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${com.burnt.xiondemo.util.CoinFormatter.formatWithDenom(uiState.receivedAmount, "SBC")} added to your wallet",
                fontSize = 14.sp,
                color = SubtitleText
            )
        }
        if (!tokensReceived) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tokens are being minted. This may take a few minutes.",
                fontSize = 13.sp,
                color = SubtitleText
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (transfer != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow("Transfer ID", transfer.id.take(12) + "...")
                    DetailRow("Amount", "\$${transfer.amount.value} ${transfer.amount.currency}")
                    DetailRow("Tokens", if (tokensReceived) "Received" else "Pending", valueColor = if (tokensReceived) XionGreen else XionOrange)
                    transfer.createdAt?.let { DetailRow("Created", it.take(19).replace("T", " ")) }
                }
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
            Text(if (tokensReceived) "Done" else "Close", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun DetailRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = GreetingText) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = SubtitleText)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
