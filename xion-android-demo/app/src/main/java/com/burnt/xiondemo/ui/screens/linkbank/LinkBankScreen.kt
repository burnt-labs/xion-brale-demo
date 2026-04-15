package com.burnt.xiondemo.ui.screens.linkbank

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
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
fun LinkBankScreen(
    onDone: () -> Unit,
    viewModel: LinkBankViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Plaid Link launcher
    val plaidLauncher = rememberLauncherForActivityResult(
        contract = OpenPlaidLink()
    ) { result ->
        when (result) {
            is LinkSuccess -> {
                val publicToken = result.publicToken
                val sessionId = result.metadata.linkSessionId
                android.util.Log.d("PlaidLink", "success linkSessionId=$sessionId")
                viewModel.onPlaidSuccess(publicToken, sessionId)
            }
            is LinkExit -> {
                val error = result.error
                val metadata = result.metadata
                android.util.Log.d(
                    "PlaidLink",
                    "exit linkSessionId=${metadata.linkSessionId ?: "nil"} requestId=${metadata.requestId ?: "nil"} error=${error?.errorMessage ?: "nil"}"
                )
                if (error != null) {
                    val msg = "${error.errorCode}: ${error.errorMessage}"
                    viewModel.onPlaidExit(
                        linkSessionId = metadata.linkSessionId,
                        exitRequestId = metadata.requestId,
                        errorMessage = msg
                    )
                } else {
                    viewModel.onPlaidCancelled(
                        linkSessionId = metadata.linkSessionId,
                        exitRequestId = metadata.requestId
                    )
                }
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
                title = { Text("Link Bank Account", color = GreetingText) },
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
            if (!uiState.bankLinked) {
                LinkBankForm(uiState, viewModel)
            } else {
                LinkedContent(uiState, onDone, viewModel)
            }

            ErrorBanner(
                message = uiState.error,
                onDismiss = { viewModel.clearError() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun LinkBankForm(
    uiState: LinkBankUiState,
    viewModel: LinkBankViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Link your bank account via Plaid to enable stablecoin purchases",
            fontSize = 14.sp,
            color = SubtitleText
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.userName,
            onValueChange = { viewModel.updateUserName(it) },
            label = { Text("Legal Name", color = SubtitleText) },
            placeholder = { Text("John Doe", color = SubtitleText) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.userNameError != null,
            supportingText = uiState.userNameError?.let { { Text(it) } },
            singleLine = true,
            textStyle = TextStyle(color = GreetingText),
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
            textStyle = TextStyle(color = GreetingText),
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
            textStyle = TextStyle(color = GreetingText),
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
            singleLine = true,
            textStyle = TextStyle(color = GreetingText),
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

        Spacer(modifier = Modifier.height(24.dp))

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
                Text("Link Bank Account", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (uiState.diagnostics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            PlaidDiagnosticsCard(uiState = uiState, viewModel = viewModel)
        }
    }
}

@Composable
private fun LinkedContent(
    uiState: LinkBankUiState,
    onDone: () -> Unit,
    viewModel: LinkBankViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = XionGreen,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bank Account Linked",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreetingText
                )

                if (!uiState.bankName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.bankName,
                        fontSize = 14.sp,
                        color = SubtitleText
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = { viewModel.unlinkBank() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unlink", color = SubtitleText, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = XionOrange)
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (uiState.diagnostics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            PlaidDiagnosticsCard(uiState = uiState, viewModel = viewModel)
        }
    }
}

@Composable
private fun PlaidDiagnosticsCard(
    uiState: LinkBankUiState,
    viewModel: LinkBankViewModel,
    modifier: Modifier = Modifier
) {
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plaid Debug — last ${uiState.diagnostics.size} session${if (uiState.diagnostics.size == 1) "" else "s"}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreetingText,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(formatDiagnostics(uiState.diagnostics)))
                        copied = true
                    }
                ) {
                    Text(
                        text = if (copied) "Copied!" else "Copy All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = XionOrange
                    )
                }
                TextButton(
                    onClick = {
                        viewModel.clearDiagnostics()
                        copied = false
                    }
                ) {
                    Text(
                        text = "Clear",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SubtitleText
                    )
                }
            }

            uiState.diagnostics.forEach { entry ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ScreenBackground, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${entry.outcome} — ${entry.phone}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreetingText
                    )
                    Text(
                        text = "token req_id: ${entry.tokenRequestId ?: "<not provided>"}",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = SubtitleText
                    )
                    Text(
                        text = "session_id:   ${entry.linkSessionId ?: "<not provided>"}",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = SubtitleText
                    )
                    if (entry.exitRequestId != null) {
                        Text(
                            text = "exit req_id:  ${entry.exitRequestId}",
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = SubtitleText
                        )
                    }
                    if (entry.errorMessage != null) {
                        Text(
                            text = "error: ${entry.errorMessage}",
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

private fun formatDiagnostics(entries: List<PlaidDiagnostic>): String {
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
    return entries.joinToString(separator = "\n\n") { d ->
        """
        [${fmt.format(java.util.Date(d.timestamp))}] ${d.outcome}
          phone:        ${d.phone}
          token req_id: ${d.tokenRequestId ?: "<not provided>"}
          session_id:   ${d.linkSessionId ?: "<not provided>"}
          exit req_id:  ${d.exitRequestId ?: "<n/a>"}
          error:        ${d.errorMessage ?: "<none>"}
        """.trimIndent()
    }
}
