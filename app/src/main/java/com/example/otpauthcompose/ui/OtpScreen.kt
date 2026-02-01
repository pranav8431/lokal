package com.example.otpauthcompose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.otpauthcompose.ui.theme.OtpAuthComposeTheme
import com.example.otpauthcompose.viewmodel.AuthState

/**
 * OtpScreen displays the OTP entry form after email submission.
 * 
 * Shows:
 * - Generated OTP (for demo purposes)
 * - OTP input field
 * - Countdown timer
 * - Remaining attempts
 * - Verify and Resend buttons
 * 
 * @param state Current OtpEntry state containing OTP, timer, attempts info
 * @param onOtpChanged Callback when OTP input changes
 * @param onValidateOtp Callback when user taps "Verify" button
 * @param onResendOtp Callback when user taps "Resend OTP" button
 * @param onBack Callback when user navigates back
 * @param modifier Optional modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    state: AuthState.OtpEntry,
    onOtpChanged: (String) -> Unit,
    onValidateOtp: () -> Unit,
    onResendOtp: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify OTP") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info text
            Text(
                text = "Enter the 6-digit code sent to",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Demo OTP display card (remove in production)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Demo: Your OTP is",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = state.generatedOtp,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // OTP input field
            OutlinedTextField(
                value = state.otp,
                onValueChange = onOtpChanged,
                label = { Text("Enter OTP") },
                placeholder = { Text("000000") },
                singleLine = true,
                enabled = !state.isLoading && state.attemptsRemaining > 0,
                isError = state.errorMessage != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        onValidateOtp()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Error message
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timer and attempts info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Timer
                val timerColor = if (state.remainingTimeSeconds <= 10) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Text(
                    text = "Expires in: ${formatTime(state.remainingTimeSeconds)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = timerColor
                )
                
                // Attempts
                val attemptsColor = if (state.attemptsRemaining <= 1) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Text(
                    text = "${state.attemptsRemaining} attempts left",
                    style = MaterialTheme.typography.bodyMedium,
                    color = attemptsColor
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Verify button
            Button(
                onClick = {
                    keyboardController?.hide()
                    onValidateOtp()
                },
                enabled = !state.isLoading && 
                         state.otp.length == 6 && 
                         state.attemptsRemaining > 0 &&
                         state.remainingTimeSeconds > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Verify OTP")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Resend button
            OutlinedButton(
                onClick = onResendOtp,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Resend OTP")
            }
        }
    }
}

/**
 * Formats seconds into mm:ss format.
 * 
 * @param seconds Total seconds to format
 * @return Formatted time string
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@Preview(showBackground = true)
@Composable
private fun OtpScreenPreview() {
    OtpAuthComposeTheme {
        OtpScreen(
            state = AuthState.OtpEntry(
                email = "test@example.com",
                generatedOtp = "123456",
                remainingTimeSeconds = 45,
                attemptsRemaining = 3
            ),
            onOtpChanged = {},
            onValidateOtp = {},
            onResendOtp = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OtpScreenWithErrorPreview() {
    OtpAuthComposeTheme {
        OtpScreen(
            state = AuthState.OtpEntry(
                email = "test@example.com",
                generatedOtp = "123456",
                remainingTimeSeconds = 30,
                attemptsRemaining = 1,
                errorMessage = "Incorrect OTP. 1 attempt remaining."
            ),
            onOtpChanged = {},
            onValidateOtp = {},
            onResendOtp = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OtpScreenExpiredPreview() {
    OtpAuthComposeTheme {
        OtpScreen(
            state = AuthState.OtpEntry(
                email = "test@example.com",
                generatedOtp = "123456",
                remainingTimeSeconds = 0,
                attemptsRemaining = 3,
                errorMessage = "OTP has expired. Please request a new one."
            ),
            onOtpChanged = {},
            onValidateOtp = {},
            onResendOtp = {},
            onBack = {}
        )
    }
}
