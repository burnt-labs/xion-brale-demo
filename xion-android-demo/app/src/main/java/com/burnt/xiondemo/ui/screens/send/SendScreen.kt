package com.burnt.xiondemo.ui.screens.send

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.burnt.xiondemo.ui.components.ErrorBanner
import java.text.NumberFormat
import java.util.Locale

private enum class SendStep { FORM, CONFIRM, LOADING, SUCCESS }

@Composable
fun SendSheetContent(
    viewModel: SendViewModel,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val sendStep by remember {
        derivedStateOf {
            when {
                uiState.txResult != null -> SendStep.SUCCESS
                uiState.isLoading -> SendStep.LOADING
                else -> SendStep.FORM
            }
        }
    }

    var showConfirm by remember { mutableStateOf(false) }

    // Reset confirm state when sheet content recomposes from a reset
    LaunchedEffect(uiState.txResult, uiState.isLoading) {
        if (uiState.txResult == null && !uiState.isLoading) {
            showConfirm = false
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            sendStep == SendStep.SUCCESS -> SuccessContent(uiState, onDone)
            sendStep == SendStep.LOADING -> LoadingContent()
            showConfirm -> ConfirmContent(
                uiState = uiState,
                onCancel = { showConfirm = false },
                onConfirm = {
                    showConfirm = false
                    viewModel.send()
                }
            )
            else -> FormContent(
                uiState = uiState,
                viewModel = viewModel,
                onReview = { showConfirm = true }
            )
        }

        ErrorBanner(
            message = uiState.error,
            onDismiss = { viewModel.clearError() },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun FormContent(
    uiState: SendUiState,
    viewModel: SendViewModel,
    onReview: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Send Tokens",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = uiState.recipient,
            onValueChange = { viewModel.updateRecipient(it) },
            label = { Text("Recipient Address") },
            placeholder = { Text("xion1...") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.recipientError != null,
            supportingText = uiState.recipientError?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                IconButton(onClick = {
                    clipboard.getText()?.text?.let { viewModel.updateRecipient(it) }
                }) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.amount,
            onValueChange = { viewModel.updateAmount(it) },
            label = { Text("Amount (XION)") },
            placeholder = { Text("0.0") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.amountError != null,
            supportingText = uiState.amountError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.memo,
            onValueChange = { viewModel.updateMemo(it) },
            label = { Text("Memo (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Fee estimate
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated Fee", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "~0.00625 XION",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onReview,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isFormValid,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Review Transaction")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ConfirmContent(
    uiState: SendUiState,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Confirm Transaction",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        ConfirmRow("To", uiState.recipient)
        ConfirmRow("Amount", "${uiState.amount} XION")
        if (uiState.memo.isNotBlank()) {
            ConfirmRow("Memo", uiState.memo)
        }
        ConfirmRow("Est. Fee", "~0.00625 XION")

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Broadcasting transaction...",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SuccessContent(
    uiState: SendUiState,
    onDone: () -> Unit
) {
    val txResult = uiState.txResult ?: return
    val clipboard = LocalClipboardManager.current
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Transaction Sent!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Transaction details card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Tx Hash row
                Text(
                    text = "Tx Hash",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = txResult.txHash,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { clipboard.setText(AnnotatedString(txResult.txHash)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Status
                DetailRow(
                    label = "Status",
                    value = if (txResult.success) "Success" else "Failed",
                    valueColor = if (txResult.success) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )

                // Block Height
                DetailRow(
                    label = "Block Height",
                    value = numberFormat.format(txResult.height)
                )

                // Gas Used
                DetailRow(
                    label = "Gas Used",
                    value = numberFormat.format(txResult.gasUsed.toLongOrNull() ?: 0)
                )

                // Gas Wanted
                DetailRow(
                    label = "Gas Wanted",
                    value = numberFormat.format(txResult.gasWanted.toLongOrNull() ?: 0)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
