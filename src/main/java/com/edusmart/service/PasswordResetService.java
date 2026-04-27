package com.edusmart.service;

/**
 * Advanced Password Reset Service
 * Handles token generation, validation, and password reset operations
 */
public interface PasswordResetService {
    
    /**
     * Generates a reset token and sends it to the user's email
     * @param email User's email address
     * @return true if email exists and token sent successfully
     */
    boolean initiatePasswordReset(String email);
    
    /**
     * Validates a reset token
     * @param email User's email
     * @param token Reset token
     * @return true if token is valid and not expired
     */
    boolean validateResetToken(String email, String token);
    
    /**
     * Resets the password using a valid token
     * @param email User's email
     * @param token Reset token
     * @param newPassword New password
     * @return true if password reset successfully
     */
    boolean resetPassword(String email, String token, String newPassword);
    
    /**
     * Gets remaining attempts for a user
     * @param email User's email
     * @return Number of remaining attempts (0-3)
     */
    int getRemainingAttempts(String email);
    
    /**
     * Checks if user is locked due to too many attempts
     * @param email User's email
     * @return true if locked
     */
    boolean isLockedOutFromReset(String email);
    
    /**
     * Clears reset attempts for a user after successful reset
     * @param email User's email
     */
    void clearResetAttempts(String email);
}
