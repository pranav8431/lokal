package com.example.otpauthcompose.data

/**
 * Data class representing OTP information for a specific email.
 * Stores the OTP code, generation timestamp, and attempt count.
 *
 * @property otp The 6-digit OTP code
 * @property generatedAt Timestamp when OTP was generated (milliseconds)
 * @property attemptCount Number of validation attempts made
 */
data class OtpData(
    val otp: String,
    val generatedAt: Long,
    val attemptCount: Int = 0
)

/**
 * Sealed class representing the result of OTP validation.
 * Provides exhaustive handling of all possible validation outcomes.
 */
sealed class OtpValidationResult {
    /** OTP validation succeeded */
    data object Success : OtpValidationResult()
    
    /** OTP has expired (older than 60 seconds) */
    data object Expired : OtpValidationResult()
    
    /** OTP code was incorrect */
    data class Invalid(val attemptsRemaining: Int) : OtpValidationResult()
    
    /** Maximum attempts (3) exceeded */
    data object MaxAttemptsExceeded : OtpValidationResult()
    
    /** No OTP exists for this email */
    data object NoOtpFound : OtpValidationResult()
}

/**
 * OtpManager handles all OTP-related business logic.
 * 
 * Responsibilities:
 * - Generate 6-digit numeric OTPs
 * - Store OTP data per email using Map<String, OtpData>
 * - Handle OTP expiry (60 seconds)
 * - Track and limit validation attempts (max 3)
 * - Reset state on OTP resend
 * 
 * Note: This class is not thread-safe. In a production app,
 * consider using synchronization or a thread-safe data structure.
 */
class OtpManager {
    
    companion object {
        /** OTP expiry duration in milliseconds (60 seconds) */
        const val OTP_EXPIRY_MS = 60_000L
        
        /** Maximum number of validation attempts allowed */
        const val MAX_ATTEMPTS = 3
        
        /** Length of generated OTP */
        const val OTP_LENGTH = 6
    }
    
    // Storage for OTP data, keyed by email address
    private val otpStorage: MutableMap<String, OtpData> = mutableMapOf()
    
    /**
     * Generates a new 6-digit OTP for the given email.
     * 
     * If an OTP already exists for this email, it will be invalidated
     * and replaced with the new one. Attempt count is reset to 0.
     * 
     * @param email The email address to generate OTP for
     * @return The generated 6-digit OTP string
     */
    fun generateOtp(email: String): String {
        // Generate random 6-digit OTP (100000-999999)
        val otp = (100_000..999_999).random().toString()
        
        // Store OTP with current timestamp, resetting attempts
        otpStorage[email] = OtpData(
            otp = otp,
            generatedAt = System.currentTimeMillis(),
            attemptCount = 0
        )
        
        return otp
    }
    
    /**
     * Validates the provided OTP for the given email.
     * 
     * Validation checks (in order):
     * 1. OTP exists for email
     * 2. OTP has not expired (60 seconds)
     * 3. Maximum attempts not exceeded (3)
     * 4. OTP matches
     * 
     * @param email The email address to validate OTP for
     * @param inputOtp The OTP entered by the user
     * @return OtpValidationResult indicating success or type of failure
     */
    fun validateOtp(email: String, inputOtp: String): OtpValidationResult {
        val otpData = otpStorage[email]
            ?: return OtpValidationResult.NoOtpFound
        
        // Check if OTP has expired
        val currentTime = System.currentTimeMillis()
        if (currentTime - otpData.generatedAt > OTP_EXPIRY_MS) {
            return OtpValidationResult.Expired
        }
        
        // Check if max attempts exceeded BEFORE this attempt
        if (otpData.attemptCount >= MAX_ATTEMPTS) {
            return OtpValidationResult.MaxAttemptsExceeded
        }
        
        // Validate OTP
        return if (inputOtp == otpData.otp) {
            // Success - clear the OTP data
            otpStorage.remove(email)
            OtpValidationResult.Success
        } else {
            // Increment attempt count
            val newAttemptCount = otpData.attemptCount + 1
            otpStorage[email] = otpData.copy(attemptCount = newAttemptCount)
            
            if (newAttemptCount >= MAX_ATTEMPTS) {
                OtpValidationResult.MaxAttemptsExceeded
            } else {
                OtpValidationResult.Invalid(attemptsRemaining = MAX_ATTEMPTS - newAttemptCount)
            }
        }
    }
    
    /**
     * Gets the remaining time before OTP expires for the given email.
     * 
     * @param email The email address to check
     * @return Remaining time in milliseconds, or 0 if expired/not found
     */
    fun getRemainingTimeMs(email: String): Long {
        val otpData = otpStorage[email] ?: return 0
        val elapsed = System.currentTimeMillis() - otpData.generatedAt
        val remaining = OTP_EXPIRY_MS - elapsed
        return if (remaining > 0) remaining else 0
    }
    
    /**
     * Gets the number of remaining validation attempts for the given email.
     * 
     * @param email The email address to check
     * @return Number of remaining attempts, or 0 if not found
     */
    fun getRemainingAttempts(email: String): Int {
        val otpData = otpStorage[email] ?: return 0
        return MAX_ATTEMPTS - otpData.attemptCount
    }
    
    /**
     * Clears OTP data for the given email.
     * Called on successful validation or manual logout.
     * 
     * @param email The email address to clear OTP data for
     */
    fun clearOtp(email: String) {
        otpStorage.remove(email)
    }
    
    /**
     * Checks if an active (non-expired) OTP exists for the given email.
     * 
     * @param email The email address to check
     * @return True if active OTP exists, false otherwise
     */
    fun hasActiveOtp(email: String): Boolean {
        val otpData = otpStorage[email] ?: return false
        val elapsed = System.currentTimeMillis() - otpData.generatedAt
        return elapsed <= OTP_EXPIRY_MS && otpData.attemptCount < MAX_ATTEMPTS
    }
}
