package com.burnt.xiondemo.ui.screens.connect

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.burnt.xiondemo.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.burnt.xiondemo.ui.components.ErrorBanner
import com.burnt.xiondemo.ui.components.LoadingOverlay
import com.burnt.xiondemo.ui.theme.*

@Composable
fun ConnectScreen(
    onConnected: () -> Unit,
    viewModel: ConnectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isConnected) {
        if (uiState.isConnected) onConnected()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_xion_logo),
                contentDescription = "XION Logo",
                modifier = Modifier
                    .width(160.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome,",
                    fontSize = 32.sp,
                    color = GreetingText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Connect to get started",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreetingText
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        (context as? Activity)?.let { activity ->
                            viewModel.startOAuthFlow(activity)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = XionOrange)
                ) {
                    Text(
                        text = "Connect Wallet",
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Error banner
            ErrorBanner(
                message = uiState.error,
                onDismiss = { viewModel.clearError() },
                onRetry = { viewModel.retryRestore() },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
            )
        }

        // Loading overlay — outside padding so it covers the full screen
        LoadingOverlay(
            isVisible = uiState.isLoading,
            message = uiState.loadingMessage
        )
    }
}
