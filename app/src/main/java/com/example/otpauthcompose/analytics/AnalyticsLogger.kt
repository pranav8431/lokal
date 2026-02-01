package com.example.otpauthcompose.analytics

import timber.log.Timber

/**
 * AnalyticsLogger is a wrapper around Timber for logging authentication events.
 * 
 * This abstraction provides:
 * - Consistent log formatting across the app
 * - Single point of change if logging library changes
 * - Easy to extend with additional analytics providers (Firebase, Mixpanel, etc.)
 * 
 * All methods are static (object companion) for easy access throughout the app.
 */
object AnalyticsLogger {
    
    private const val TAG = "AuthAnalytics"
    
    /**
     * Logs when a new OTP is generated for an email.
     * 
     * @param email The email address OTP was generated for
     */
    fun logOtpGenerated(email: String) {
        Timber.tag(TAG).i("OTP generated for email: ${maskEmail(email)}")
    }
    
    /**
     * Logs successful OTP validation.
     * 
     * @param email The email address that was validated
     */
    fun logOtpValidationSuccess(email: String) {
        Timber.tag(TAG).i("OTP validation SUCCESS for email: ${maskEmail(email)}")
    }
    
    /**
     * Logs failed OTP validation.
     * 
     * @param email The email address that failed validation
     * @param reason The reason for failure (expired, invalid, max attempts, etc.)
     */
    fun logOtpValidationFailure(email: String, reason: String) {
        Timber.tag(TAG).w("OTP validation FAILED for email: ${maskEmail(email)}, reason: $reason")
    }
    
    /**
     * Logs user logout event.
     * 
     * @param email The email address of the user who logged out
     */
    fun logLogout(email: String) {
        Timber.tag(TAG).i("User LOGOUT: ${maskEmail(email)}")
    }
    
    /**
     * Logs generic authentication events.
     * 
     * @param event The event name
     * @param details Additional details about the event
     */
    fun logEvent(event: String, details: String? = null) {
        if (details != null) {
            Timber.tag(TAG).d("Event: $event - $details")
        } else {
            Timber.tag(TAG).d("Event: $event")
        }
    }
    
    /**
     * Logs errors that occur during authentication.
     * 
     * @param message Error message
     * @param throwable Optional exception that caused the error
     */
    fun logError(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(TAG).e(throwable, message)
        } else {
            Timber.tag(TAG).e(message)
        }
    }
    
    /**
     * Masks email for privacy in logs.
     * Example: "user@example.com" becomes "u***@example.com"
     * 
     * @param email The email to mask
     * @return Masked email string
     */
    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return email
        
        val localPart = email.substring(0, atIndex)
        val domainPart = email.substring(atIndex)
        
        val maskedLocal = if (localPart.length > 2) {
            "${localPart.first()}${"*".repeat(localPart.length - 2)}${localPart.last()}"
        } else {
            "${localPart.first()}*"
        }
        
        return "$maskedLocal$domainPart"
    }
}
