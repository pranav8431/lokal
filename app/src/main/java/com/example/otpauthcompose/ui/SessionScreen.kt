package com.example.otpauthcompose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.otpauthcompose.ui.theme.OtpAuthComposeTheme
import com.example.otpauthcompose.viewmodel.AuthState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SessionScreen displays the logged-in session information.
 * 
 * Shows:
 * - Welcome message with user's email
 * - Session start time
 * - Live session duration (updates every second)
 * - Logout button
 * 
 * The session timer is managed by the ViewModel and survives recompositions.
 * 
 * @param state Current LoggedIn state containing session info
 * @param onLogout Callback when user taps "Logout" button
 * @param modifier Optional modifier for the composable
 */
@Composable
fun SessionScreen(
    state: AuthState.LoggedIn,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome message
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // User email
        Text(
            text = state.email,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Session info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Session start time
                Text(
                    text = "Session Started",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatDateTime(state.sessionStartTimeMs),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Session duration
                Text(
                    text = "Session Duration",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Large duration display
                Text(
                    text = formatDuration(state.sessionDurationSeconds),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Logout button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "Logout",
                color = MaterialTheme.colorScheme.onError
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info text
        Text(
            text = "Your session is active and secure.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Formats a timestamp into a human-readable date/time string.
 * 
 * @param timestampMs Timestamp in milliseconds
 * @return Formatted date/time string
 */
private fun formatDateTime(timestampMs: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm:ss a", Locale.getDefault())
    return dateFormat.format(Date(timestampMs))
}

/**
 * Formats duration in seconds to mm:ss format.
 * 
 * @param seconds Total seconds
 * @return Formatted duration string
 */
private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@Preview(showBackground = true)
@Composable
private fun SessionScreenPreview() {
    OtpAuthComposeTheme {
        SessionScreen(
            state = AuthState.LoggedIn(
                email = "test@example.com",
                sessionStartTimeMs = System.currentTimeMillis() - 125_000, // 2m 5s ago
                sessionDurationSeconds = 125
            ),
            onLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SessionScreenNewSessionPreview() {
    OtpAuthComposeTheme {
        SessionScreen(
            state = AuthState.LoggedIn(
                email = "user@company.org",
                sessionStartTimeMs = System.currentTimeMillis(),
                sessionDurationSeconds = 0
            ),
            onLogout = {}
        )
    }
}
