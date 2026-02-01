package com.example.otpauthcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otpauthcompose.analytics.AnalyticsLogger
import com.example.otpauthcompose.data.OtpManager
import com.example.otpauthcompose.data.OtpValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel handles all authentication business logic.
 * 
 * Responsibilities:
 * - Manage authentication flow state (EmailEntry -> OtpEntry -> LoggedIn)
 * - Coordinate with OtpManager for OTP operations
 * - Manage OTP countdown timer
 * - Manage session duration timer
 * - Log events via AnalyticsLogger
 * 
 * Architecture:
 * - One-way data flow: UI sends events -> ViewModel processes -> State emits
 * - No UI or navigation logic in ViewModel
 * - Exposes immutable StateFlow for UI observation
 */
class AuthViewModel : ViewModel() {
    
    // OTP manager handles OTP generation and validation logic
    private val otpManager = OtpManager()
    
    // Mutable state - private to ViewModel
    private val _uiState = MutableStateFlow<AuthState>(AuthState.EmailEntry())
    
    // Immutable state exposed to UI
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()
    
    // Timer jobs - must be cancelled properly to avoid leaks
    private var otpCountdownJob: Job? = null
    private var sessionTimerJob: Job? = null
    
    // Current session data - cleared on logout
    private var currentSession: SessionData? = null
    
    /**
     * Updates the email input value.
     * Only valid in EmailEntry state.
     * 
     * @param email New email value
     */
    fun onEmailChanged(email: String) {
        val currentState = _uiState.value
        if (currentState is AuthState.EmailEntry) {
            _uiState.value = currentState.copy(
                email = email,
                errorMessage = null // Clear error on input change
            )
        }
    }
    
    /**
     * Initiates OTP generation and sends OTP to the email.
     * Transitions from EmailEntry to OtpEntry state.
     */
    fun onSendOtp() {
        val currentState = _uiState.value
        if (currentState !is AuthState.EmailEntry) return
        
        val email = currentState.email.trim()
        
        // Validate email format
        if (!isValidEmail(email)) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a valid email address"
            )
            return
        }
        
        // Show loading state
        _uiState.value = currentState.copy(isLoading = true)
        
        viewModelScope.launch {
            // Generate OTP (in real app, this would be async network call)
            val generatedOtp = otpManager.generateOtp(email)
            
            // Log OTP generation event
            AnalyticsLogger.logOtpGenerated(email)
            
            // Transition to OTP entry state
            _uiState.value = AuthState.OtpEntry(
                email = email,
                remainingTimeSeconds = 60,
                attemptsRemaining = OtpManager.MAX_ATTEMPTS,
                generatedOtp = generatedOtp // For demo - shows OTP to user
            )
            
            // Start countdown timer
            startOtpCountdown(email)
        }
    }
    
    /**
     * Updates the OTP input value.
     * Only valid in OtpEntry state.
     * 
     * @param otp New OTP value
     */
    fun onOtpChanged(otp: String) {
        val currentState = _uiState.value
        if (currentState is AuthState.OtpEntry) {
            // Only allow numeric input, max 6 digits
            val filteredOtp = otp.filter { it.isDigit() }.take(OtpManager.OTP_LENGTH)
            _uiState.value = currentState.copy(
                otp = filteredOtp,
                errorMessage = null // Clear error on input change
            )
        }
    }
    
    /**
     * Validates the entered OTP.
     * On success, transitions to LoggedIn state.
     * On failure, shows appropriate error message.
     */
    fun onValidateOtp() {
        val currentState = _uiState.value
        if (currentState !is AuthState.OtpEntry) return
        
        val email = currentState.email
        val otp = currentState.otp
        
        // Validate OTP length
        if (otp.length != OtpManager.OTP_LENGTH) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a 6-digit OTP"
            )
            return
        }
        
        // Show loading state
        _uiState.value = currentState.copy(isLoading = true)
        
        viewModelScope.launch {
            // Validate OTP
            when (val result = otpManager.validateOtp(email, otp)) {
                is OtpValidationResult.Success -> {
                    // Log success
                    AnalyticsLogger.logOtpValidationSuccess(email)
                    
                    // Cancel countdown timer
                    otpCountdownJob?.cancel()
                    
                    // Start session
                    val sessionStartTime = System.currentTimeMillis()
                    currentSession = SessionData(email, sessionStartTime)
                    
                    // Transition to logged in state
                    _uiState.value = AuthState.LoggedIn(
                        email = email,
                        sessionStartTimeMs = sessionStartTime,
                        sessionDurationSeconds = 0
                    )
                    
                    // Start session timer
                    startSessionTimer()
                }
                
                is OtpValidationResult.Invalid -> {
                    // Log failure
                    AnalyticsLogger.logOtpValidationFailure(email, "Invalid OTP")
                    
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        otp = "", // Clear OTP input
                        attemptsRemaining = result.attemptsRemaining,
                        errorMessage = "Incorrect OTP. ${result.attemptsRemaining} attempts remaining."
                    )
                }
                
                is OtpValidationResult.Expired -> {
                    // Log failure
                    AnalyticsLogger.logOtpValidationFailure(email, "OTP expired")
                    
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        otp = "",
                        remainingTimeSeconds = 0,
                        errorMessage = "OTP has expired. Please request a new one."
                    )
                }
                
                is OtpValidationResult.MaxAttemptsExceeded -> {
                    // Log failure
                    AnalyticsLogger.logOtpValidationFailure(email, "Max attempts exceeded")
                    
                    // Cancel countdown
                    otpCountdownJob?.cancel()
                    
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        otp = "",
                        attemptsRemaining = 0,
                        errorMessage = "Maximum attempts exceeded. Please request a new OTP."
                    )
                }
                
                is OtpValidationResult.NoOtpFound -> {
                    // Log failure
                    AnalyticsLogger.logOtpValidationFailure(email, "No OTP found")
                    
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "No OTP found. Please request a new one."
                    )
                }
            }
        }
    }
    
    /**
     * Resends OTP to the current email.
     * Resets attempt count and countdown timer.
     */
    fun onResendOtp() {
        val currentState = _uiState.value
        if (currentState !is AuthState.OtpEntry) return
        
        val email = currentState.email
        
        // Cancel existing countdown
        otpCountdownJob?.cancel()
        
        // Generate new OTP
        val generatedOtp = otpManager.generateOtp(email)
        
        // Log OTP generation
        AnalyticsLogger.logOtpGenerated(email)
        
        // Update state with new OTP
        _uiState.value = AuthState.OtpEntry(
            email = email,
            remainingTimeSeconds = 60,
            attemptsRemaining = OtpManager.MAX_ATTEMPTS,
            generatedOtp = generatedOtp
        )
        
        // Restart countdown
        startOtpCountdown(email)
    }
    
    /**
     * Navigates back to email entry from OTP screen.
     * Clears OTP data for the current email.
     */
    fun onBackToEmail() {
        val currentState = _uiState.value
        if (currentState is AuthState.OtpEntry) {
            // Cancel countdown
            otpCountdownJob?.cancel()
            
            // Clear OTP data
            otpManager.clearOtp(currentState.email)
            
            // Return to email entry
            _uiState.value = AuthState.EmailEntry(email = currentState.email)
        }
    }
    
    /**
     * Logs out the current user.
     * Clears session data and returns to email entry.
     */
    fun onLogout() {
        val currentState = _uiState.value
        if (currentState is AuthState.LoggedIn) {
            // Log logout event
            AnalyticsLogger.logLogout(currentState.email)
            
            // Cancel session timer
            sessionTimerJob?.cancel()
            
            // Clear session
            currentSession = null
            
            // Return to email entry
            _uiState.value = AuthState.EmailEntry()
        }
    }
    
    /**
     * Starts the OTP countdown timer.
     * Updates remaining time every second until expired.
     * 
     * @param email The email address for this OTP
     */
    private fun startOtpCountdown(email: String) {
        otpCountdownJob?.cancel()
        otpCountdownJob = viewModelScope.launch {
            var remainingSeconds = 60
            
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
                
                val currentState = _uiState.value
                if (currentState is AuthState.OtpEntry && currentState.email == email) {
                    _uiState.value = currentState.copy(
                        remainingTimeSeconds = remainingSeconds
                    )
                } else {
                    // State changed, stop countdown
                    break
                }
            }
        }
    }
    
    /**
     * Starts the session duration timer.
     * Updates duration every second while logged in.
     */
    private fun startSessionTimer() {
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            val session = currentSession ?: return@launch
            
            while (true) {
                delay(1000L)
                
                val currentState = _uiState.value
                if (currentState is AuthState.LoggedIn) {
                    val durationSeconds = (System.currentTimeMillis() - session.startTimeMs) / 1000
                    _uiState.value = currentState.copy(
                        sessionDurationSeconds = durationSeconds
                    )
                } else {
                    // State changed (logged out), stop timer
                    break
                }
            }
        }
    }
    
    /**
     * Simple email validation.
     * Checks for basic email format: contains @ and has text before and after.
     * 
     * @param email Email to validate
     * @return True if email format is valid
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && 
               email.contains("@") && 
               email.indexOf("@") > 0 &&
               email.indexOf("@") < email.length - 1
    }
    
    /**
     * Clean up when ViewModel is destroyed.
     * Cancels all running coroutines.
     */
    override fun onCleared() {
        super.onCleared()
        otpCountdownJob?.cancel()
        sessionTimerJob?.cancel()
    }
}
