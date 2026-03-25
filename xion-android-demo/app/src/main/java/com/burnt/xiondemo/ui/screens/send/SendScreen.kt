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
import androidx.compose.ui.unit.sp
import com.burnt.xiondemo.ui.components.ErrorBanner
import com.burnt.xiondemo.ui.theme.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormContent(
    uiState: SendUiState,
    viewModel: SendViewModel,
    onReview: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = GreetingText,
        unfocusedTextColor = GreetingText,
        focusedBorderColor = XionOrange,
        unfocusedBorderColor = SubtitleText.copy(alpha = 0.5f),
        focusedLabelColor = XionOrange,
        unfocusedLabelColor = SubtitleText,
        cursorColor = XionOrange
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Send Tokens",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GreetingText
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Token selector
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
                        selectedLabelColor = CardBackground
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.recipient,
            onValueChange = { viewModel.updateRecipient(it) },
            label = { Text("Recipient Address", color = SubtitleText) },
            placeholder = { Text("xion1...", color = SubtitleText) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.recipientError != null,
            supportingText = uiState.recipientError?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            trailingIcon = {
                IconButton(onClick = {
                    clipboard.getText()?.text?.let { viewModel.updateRecipient(it) }
                }) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = SubtitleText)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.amount,
            onValueChange = { viewModel.updateAmount(it) },
            label = { Text("Amount (${uiState.selectedToken.displayName})", color = SubtitleText) },
            placeholder = { Text("0.0", color = SubtitleText) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.amountError != null,
            supportingText = uiState.amountError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.memo,
            onValueChange = { viewModel.updateMemo(it) },
            label = { Text("Memo (optional)", color = SubtitleText) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Fee estimate
        Card(
            colors = CardDefaults.cardColors(containerColor = ScreenBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated Fee", fontSize = 14.sp, color = SubtitleText)
                Text("~0.00625 XION", fontSize = 14.sp, color = SubtitleText)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onReview,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isFormValid,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = XionOrange)
        ) {
            Text("Review Transaction", fontWeight = FontWeight.SemiBold)
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
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GreetingText
        )
        Spacer(modifier = Modifier.height(16.dp))

        ConfirmRow("To", uiState.recipient)
        ConfirmRow("Amount", "${uiState.amount} ${uiState.selectedToken.displayName}")
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
                Text("Cancel", color = GreetingText)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = XionOrange)
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
        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = XionOrange)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Broadcasting transaction...", fontSize = 16.sp, color = GreetingText)
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
            tint = XionGreen,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Transaction Sent!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GreetingText
        )
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = ScreenBackground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tx Hash", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SubtitleText)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = txResult.txHash,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = GreetingText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { clipboard.setText(AnnotatedString(txResult.txHash)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = SubtitleText)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SubtitleText.copy(alpha = 0.2f))

                DetailRow("Status", if (txResult.success) "Success" else "Failed",
                    valueColor = if (txResult.success) XionGreen else XionRed)
                DetailRow("Block Height", numberFormat.format(txResult.height))
                DetailRow("Gas Used", numberFormat.format(txResult.gasUsed.toLongOrNull() ?: 0))
                DetailRow("Gas Wanted", numberFormat.format(txResult.gasWanted.toLongOrNull() ?: 0))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = XionOrange)
        ) {
            Text("Done", fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = GreetingText
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = SubtitleText)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
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
        Text(label, fontSize = 14.sp, color = SubtitleText)
        Text(value, fontSize = 14.sp, color = GreetingText)
    }
}
