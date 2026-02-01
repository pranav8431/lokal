package com.example.otpauthcompose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.otpauthcompose.ui.theme.OtpAuthComposeTheme
import com.example.otpauthcompose.viewmodel.AuthState

/**
 * LoginScreen displays the email entry form for authentication.
 * 
 * This is a stateless composable that receives state and callbacks from parent.
 * All business logic is handled by the ViewModel through callbacks.
 * 
 * @param state Current EmailEntry state containing email, loading, and error info
 * @param onEmailChanged Callback when email input changes
 * @param onSendOtp Callback when user taps "Send OTP" button
 * @param modifier Optional modifier for the composable
 */
@Composable
fun LoginScreen(
    state: AuthState.EmailEntry,
    onEmailChanged: (String) -> Unit,
    onSendOtp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = "Enter your email to receive a one-time password",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Email input field
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            label = { Text("Email Address") },
            placeholder = { Text("you@example.com") },
            singleLine = true,
            enabled = !state.isLoading,
            isError = state.errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onSendOtp()
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
                color = MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Send OTP button
        Button(
            onClick = {
                keyboardController?.hide()
                onSendOtp()
            },
            enabled = !state.isLoading && state.email.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Send OTP")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    OtpAuthComposeTheme {
        LoginScreen(
            state = AuthState.EmailEntry(email = "test@example.com"),
            onEmailChanged = {},
            onSendOtp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    OtpAuthComposeTheme {
        LoginScreen(
            state = AuthState.EmailEntry(
                email = "invalid",
                errorMessage = "Please enter a valid email address"
            ),
            onEmailChanged = {},
            onSendOtp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenLoadingPreview() {
    OtpAuthComposeTheme {
        LoginScreen(
            state = AuthState.EmailEntry(
                email = "test@example.com",
                isLoading = true
            ),
            onEmailChanged = {},
            onSendOtp = {}
        )
    }
}
