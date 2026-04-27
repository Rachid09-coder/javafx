package com.edusmart.service.impl;

import com.edusmart.dao.UserDao;
import com.edusmart.model.User;
import com.edusmart.service.PasswordResetService;
import com.edusmart.util.MailSender;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;

/**
 * Implementation of Password Reset Service
 * Provides secure token generation and password reset functionality
 */
public class PasswordResetServiceImpl implements PasswordResetService {
    
    private static final int TOKEN_EXPIRATION_MINUTES = 15;
    private static final int MAX_RESET_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_MINUTES = 10;
    
    // In-memory tracking of reset attempts (in production, use database)
    private final Map<String, ResetAttemptTracker> attemptTrackers = new HashMap<>();
    
    private final UserDao userDao;
    
    public PasswordResetServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }
    
    @Override
    public boolean initiatePasswordReset(String email) {
        // Check if user exists
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            // Don't reveal if email exists (security best practice)
            return false;
        }
        
        // Check if user is locked out
        if (isLockedOutFromReset(email)) {
            return false;
        }
        
        User user = userOpt.get();
        
        try {
            // Generate secure token
            String token = generateSecureToken();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES);
            
            // Update user with token
            user.setResetToken(token);
            user.setResetTokenExpiresAt(expiresAt);
            
            if (!userDao.update(user)) {
                return false;
            }
            
            // Send email with reset link
            return sendPasswordResetEmail(user, token);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean validateResetToken(String email, String token) {
        Optional<User> userOpt = userDao.findByEmail(email);
        
        if (!userOpt.isPresent()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Check if token matches
        if (user.getResetToken() == null || !user.getResetToken().equals(token)) {
            return false;
        }
        
        // Check if token is expired
        if (user.getResetTokenExpiresAt() == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(user.getResetTokenExpiresAt())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean resetPassword(String email, String token, String newPassword) {
        // Validate token first
        if (!validateResetToken(email, token)) {
            trackFailedAttempt(email);
            return false;
        }
        
        if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 6) {
            trackFailedAttempt(email);
            return false;
        }
        
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            return false;
        }
        
        User user = userOpt.get();
        
        try {
            // Update password
            user.setPassword(newPassword);
            
            // Clear reset token
            user.setResetToken(null);
            user.setResetTokenExpiresAt(null);
            
            boolean success = userDao.update(user);
            
            if (success) {
                clearResetAttempts(email);
                // Send confirmation email
                sendPasswordResetConfirmationEmail(user);
            }
            
            return success;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public int getRemainingAttempts(String email) {
        ResetAttemptTracker tracker = attemptTrackers.get(email);
        if (tracker == null) {
            return MAX_RESET_ATTEMPTS;
        }
        
        // Check if lockout period expired
        if (tracker.isLockoutExpired()) {
            attemptTrackers.remove(email);
            return MAX_RESET_ATTEMPTS;
        }
        
        return Math.max(0, MAX_RESET_ATTEMPTS - tracker.failedAttempts);
    }
    
    @Override
    public boolean isLockedOutFromReset(String email) {
        ResetAttemptTracker tracker = attemptTrackers.get(email);
        if (tracker == null) {
            return false;
        }
        
        if (tracker.isLockoutExpired()) {
            attemptTrackers.remove(email);
            return false;
        }
        
        return tracker.failedAttempts >= MAX_RESET_ATTEMPTS;
    }
    
    @Override
    public void clearResetAttempts(String email) {
        attemptTrackers.remove(email);
    }
    
    // ========== Private Helper Methods ==========
    
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    private boolean sendPasswordResetEmail(User user, String token) {
        try {
            String subject = "Réinitialisation de votre mot de passe - EduSmart";
            String body = "Bonjour " + user.getFirstName() + ",\n\n" +
                         "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                         "Token de réinitialisation : " + token + "\n" +
                         "Ce token expire dans 15 minutes.\n\n" +
                         "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                         "Cordialement,\nL'équipe EduSmart";
            
            MailSender.sendEmailWithAttachment(user.getEmail(), subject, body, null);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void sendPasswordResetConfirmationEmail(User user) {
        try {
            String subject = "Mot de passe réinitialisé - EduSmart";
            String body = "Bonjour " + user.getFirstName() + ",\n\n" +
                         "Votre mot de passe a été réinitialisé avec succès.\n\n" +
                         "Si vous n'avez pas effectué cette action, contactez immédiatement le support.\n\n" +
                         "Cordialement,\nL'équipe EduSmart";
            
            MailSender.sendEmailWithAttachment(user.getEmail(), subject, body, null);
            
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }
    
    private void trackFailedAttempt(String email) {
        ResetAttemptTracker tracker = attemptTrackers.computeIfAbsent(email, 
            k -> new ResetAttemptTracker());
        tracker.recordFailedAttempt();
    }
    
    // ========== Inner Class for Tracking ==========
    
    private static class ResetAttemptTracker {
        int failedAttempts;
        LocalDateTime firstAttemptTime;
        
        void recordFailedAttempt() {
            if (firstAttemptTime == null) {
                firstAttemptTime = LocalDateTime.now();
            }
            failedAttempts++;
        }
        
        boolean isLockoutExpired() {
            if (firstAttemptTime == null) {
                return true;
            }
            return LocalDateTime.now().isAfter(
                firstAttemptTime.plusMinutes(LOCKOUT_DURATION_MINUTES)
            );
        }
    }
}
