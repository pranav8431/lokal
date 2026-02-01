package com.example.otpauthcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otpauthcompose.ui.LoginScreen
import com.example.otpauthcompose.ui.OtpScreen
import com.example.otpauthcompose.ui.SessionScreen
import com.example.otpauthcompose.ui.theme.OtpAuthComposeTheme
import com.example.otpauthcompose.viewmodel.AuthState
import com.example.otpauthcompose.viewmodel.AuthViewModel

/**
 * MainActivity is the single entry point of the app.
 * 
 * Uses Jetpack Compose for UI with a single-activity architecture.
 * Navigation between screens is handled by observing AuthState from ViewModel.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OtpAuthComposeTheme {
                AuthApp()
            }
        }
    }
}

/**
 * Root composable that manages navigation based on authentication state.
 * 
 * Observes AuthState from ViewModel and renders the appropriate screen:
 * - EmailEntry → LoginScreen
 * - OtpEntry → OtpScreen
 * - LoggedIn → SessionScreen
 * - Error → Error display
 * 
 * All state hoisting and callbacks are properly connected to ViewModel.
 */
@Composable
fun AuthApp(
    viewModel: AuthViewModel = viewModel()
) {
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        // Navigate based on current state
        when (val state = uiState) {
            is AuthState.EmailEntry -> {
                LoginScreen(
                    state = state,
                    onEmailChanged = viewModel::onEmailChanged,
                    onSendOtp = viewModel::onSendOtp,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            is AuthState.OtpEntry -> {
                OtpScreen(
                    state = state,
                    onOtpChanged = viewModel::onOtpChanged,
                    onValidateOtp = viewModel::onValidateOtp,
                    onResendOtp = viewModel::onResendOtp,
                    onBack = viewModel::onBackToEmail,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            is AuthState.LoggedIn -> {
                SessionScreen(
                    state = state,
                    onLogout = viewModel::onLogout,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            is AuthState.Error -> {
                // For now, show error on login screen
                // In a production app, this could be a dedicated error screen
                LoginScreen(
                    state = AuthState.EmailEntry(
                        errorMessage = state.message
                    ),
                    onEmailChanged = viewModel::onEmailChanged,
                    onSendOtp = viewModel::onSendOtp,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}