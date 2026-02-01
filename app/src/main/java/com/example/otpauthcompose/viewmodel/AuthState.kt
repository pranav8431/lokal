package com.example.otpauthcompose.viewmodel

/**
 * Sealed class representing all possible UI states in the authentication flow.
 * 
 * Uses one-way data flow: ViewModel emits states, UI observes and renders.
 * Each state contains all the data needed to render that screen.
 */
sealed class AuthState {
    
    /**
     * Initial state: User enters their email address.
     * 
     * @property email Current email input value
     * @property isLoading True while OTP is being generated
     * @property errorMessage Error message to display, if any
     */
    data class EmailEntry(
        val email: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : AuthState()
    
    /**
     * OTP entry state: User has received OTP and needs to enter it.
     * 
     * @property email The email address OTP was sent to
     * @property otp Current OTP input value
     * @property remainingTimeSeconds Seconds until OTP expires
     * @property attemptsRemaining Number of validation attempts left
     * @property isLoading True while OTP is being validated
     * @property errorMessage Error message to display, if any
     * @property generatedOtp The actual OTP (for demo purposes only - remove in production)
     */
    data class OtpEntry(
        val email: String,
        val otp: String = "",
        val remainingTimeSeconds: Int = 60,
        val attemptsRemaining: Int = 3,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val generatedOtp: String = "" // For demo purposes - shows the OTP to user
    ) : AuthState()
    
    /**
     * Logged in state: User has successfully authenticated.
     * 
     * @property email The authenticated user's email
     * @property sessionStartTimeMs Timestamp when session started (milliseconds)
     * @property sessionDurationSeconds Current session duration in seconds
     */
    data class LoggedIn(
        val email: String,
        val sessionStartTimeMs: Long,
        val sessionDurationSeconds: Long = 0
    ) : AuthState()
    
    /**
     * Error state: An unrecoverable error occurred.
     * 
     * @property message Error description
     * @property canRetry True if user can retry the operation
     */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : AuthState()
}

/**
 * Data class for session information.
 * Used internally by ViewModel to track session state.
 * 
 * @property email The authenticated user's email
 * @property startTimeMs Session start timestamp in milliseconds
 */
data class SessionData(
    val email: String,
    val startTimeMs: Long
)
