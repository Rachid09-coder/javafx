# Advanced Forgot Password Feature - EduSmart

## Overview
A complete implementation of a secure, multi-step password recovery system with email-based token verification, rate limiting, and password strength validation.

---

## Features

### 1. **Secure Token Generation**
- Uses `SecureRandom` to generate cryptographically secure tokens
- Base64 URL-encoded for safe transmission
- Tokens are 32 bytes (256 bits) for strong security
- Automatic expiration after 15 minutes

### 2. **Two-Stage Authentication**
- **Stage 1**: User provides email address
- **Stage 2**: User receives token and enters new password

### 3. **Email Notifications**
- Password reset token sent via email
- Confirmation email sent after successful reset
- Security: Never reveals if email exists (prevents user enumeration)

### 4. **Rate Limiting & Lockout**
- Maximum 3 failed reset attempts per user
- 10-minute lockout period after exceeding attempts
- Automatic lockout reset after timeout

### 5. **Password Strength Validation**
- Minimum 6 characters
- At least one uppercase letter
- At least one number
- Real-time strength indicator (Weak/Medium/Strong)
- Visual requirement checklist

### 6. **User-Friendly UI**
- Separate screens for forget password and reset password
- Loading indicators during email sending
- Clear error and success messages
- Attempt counter display
- Password visibility toggle

---

## Architecture Components

### Services
1. **PasswordResetService** (Interface)
   - `initiatePasswordReset(String email)` - Generates token and sends email
   - `validateResetToken(String email, String token)` - Validates token
   - `resetPassword(String email, String token, String newPassword)` - Updates password
   - `getRemainingAttempts(String email)` - Returns remaining attempts
   - `isLockedOutFromReset(String email)` - Checks lockout status
   - `clearResetAttempts(String email)` - Clears attempts after success

2. **PasswordResetServiceImpl** (Implementation)
   - Handles all business logic
   - Uses in-memory tracking for attempts (can be moved to DB)
   - Integrates with MailSender utility

### Controllers
1. **ForgotPasswordController**
   - Handles email input
   - Validates email format
   - Initiates token generation and sending
   - Navigates to reset screen on success

2. **ResetPasswordController**
   - Receives email parameter
   - Validates reset token
   - Validates password strength
   - Updates password in database
   - Shows remaining attempts

### FXML Views
1. **forgot-password.fxml**
   - Email input field
   - Error/Success labels
   - Send code button
   - Back to login button

2. **reset-password.fxml**
   - Token input field
   - New password field (PasswordField)
   - Confirm password field
   - Password strength indicator
   - Requirements checklist
   - Reset button

### Models
- **User** model already includes:
  - `resetToken` - Current reset token
  - `resetTokenExpiresAt` - Token expiration time

### Database
- No new tables required
- Uses existing User table columns:
  - `reset_token` - VARCHAR(255)
  - `reset_token_expires_at` - DATETIME

---

## Security Features

### 1. **Token Security**
- Non-sequential, cryptographically random tokens
- Tokens stored in database (not sent back to user on request)
- Time-limited tokens (15 minutes default)
- One-time use tokens (cleared after password update)

### 2. **Account Protection**
- Email verification before password reset
- Rate limiting to prevent brute force attacks
- Lockout mechanism after multiple failures
- Confirmation email notification

### 3. **Password Security**
- Minimum complexity requirements enforced
- Real-time strength feedback
- Password mismatch detection
- No password hints or recovery of old password

### 4. **Information Leakage Prevention**
- Non-revealing error messages (doesn't tell if email exists)
- Same response time for existing/non-existing emails
- No user enumeration vulnerability

---

## User Flow

```
1. User clicks "Mot de passe oublié?" on login screen
   ↓
2. ForgotPasswordController shown
   ↓
3. User enters email address
   ↓
4. System validates email format
   ↓
5. System generates secure token
   ↓
6. Token sent to user's email
   ↓
7. User receives email with reset token
   ↓
8. ResetPasswordController displayed
   ↓
9. User enters token + new password
   ↓
10. System validates token (exists, not expired, matches email)
    ↓
11. System validates password (format, strength)
    ↓
12. If all valid: Password updated in DB, token cleared
    ↓
13. Confirmation email sent
    ↓
14. Success message and redirect to login
```

---

## Implementation Details

### Token Validation Logic
```java
// Token must:
1. Match the stored reset_token in database
2. Not be null/empty
3. Not be expired (compared with reset_token_expires_at)
4. Be associated with the email provided
```

### Attempt Tracking
```java
// In-memory tracking structure:
{
    "user@email.com": {
        failedAttempts: 2,
        firstAttemptTime: LocalDateTime,
        isLockoutExpired() -> boolean
    }
}
```

### Password Strength Calculation
```
Score calculation:
- Length (0-0.4): Based on character count
- Uppercase (0.2): If contains [A-Z]
- Lowercase (0.2): If contains [a-z]
- Numbers (0.1): If contains [0-9]
- Special chars (0.1): If contains !@#$%^&*()...

Final strength = sum of all (max 1.0)
0.0-0.33: Faible (Red)
0.33-0.66: Moyen (Orange)
0.66-1.0: Fort (Green)
```

---

## Configuration

### Timing Settings (in PasswordResetServiceImpl)
```java
TOKEN_EXPIRATION_MINUTES = 15      // How long tokens are valid
MAX_RESET_ATTEMPTS = 3             // Max failed attempts
LOCKOUT_DURATION_MINUTES = 10      // Lockout period
```

### Email Configuration
Uses existing MailSender utility:
- SMTP: smtp.gmail.com
- Port: 587
- Authentication: OAuth/App Password

### Minimum Password Requirements
```java
- Minimum 6 characters
- At least 1 uppercase letter
- At least 1 number
- (Optional) Special characters recommended
```

---

## Database Schema

No migration needed - uses existing User table:

```sql
ALTER TABLE user ADD COLUMN reset_token VARCHAR(255) NULL DEFAULT NULL;
ALTER TABLE user ADD COLUMN reset_token_expires_at DATETIME NULL DEFAULT NULL;
```

---

## Integration Steps

1. **Copy Files**
   - PasswordResetService.java → service/
   - PasswordResetServiceImpl.java → service/impl/
   - ForgotPasswordController.java → controller/auth/
   - ResetPasswordController.java → controller/auth/
   - forgot-password.fxml → resources/fxml/auth/
   - reset-password.fxml → resources/fxml/auth/

2. **Update SceneManager**
   - Add FORGOT_PASSWORD and RESET_PASSWORD to enum
   - Update getFxmlPath() method
   - Add parameter support for ResetPasswordController

3. **Update LoginController**
   - Replace handleForgotPassword() method

4. **Verify Dependencies**
   - Jakarta Mail (already in pom.xml)
   - JavaFX (already configured)

---

## Error Handling

### Validation Errors
- Invalid email format
- Missing fields
- Password mismatch
- Weak password
- Invalid/expired token

### System Errors
- Email sending failures (graceful degradation)
- Database connection errors
- Unexpected exceptions (logged and user-friendly message)

---

## Testing Checklist

- [ ] Forgot password button navigates correctly
- [ ] Email validation works
- [ ] Token is generated and saved to database
- [ ] Email is sent with token
- [ ] Token expires after 15 minutes
- [ ] Invalid token is rejected
- [ ] Password strength indicator shows correctly
- [ ] Password requirements update in real-time
- [ ] Rate limiting works (3 attempts max)
- [ ] Lockout enforced for 10 minutes
- [ ] Confirmation email sent after success
- [ ] User redirected to login after reset
- [ ] Database updated with new password
- [ ] Old reset token cleared after use

---

## Future Enhancements

1. **Database-backed Attempt Tracking**
   - Create `password_reset_attempts` table
   - Better for distributed systems

2. **Multi-Channel Verification**
   - SMS OTP support
   - Security questions
   - Two-factor authentication

3. **Enhanced Analytics**
   - Track password reset attempts
   - Identify suspicious patterns
   - Generate security reports

4. **Customizable Token Expiration**
   - Admin configurable timeout
   - Different timeouts for different user roles

5. **Password History**
   - Prevent reusing recent passwords
   - Store old password hashes

6. **Email Templates**
   - HTML email templates
   - Localization support
   - Branded email design

---

## Troubleshooting

### Emails Not Sending
- Verify Gmail app password is set
- Check SMTP credentials in MailSender
- Verify email address is valid

### Token Not Working
- Check token hasn't expired (15 min limit)
- Verify email matches user record
- Clear browser cache/app cache

### Lock Out Issues
- Wait 10 minutes for automatic unlock
- Admin can manually clear attempts in service

### Password Requirements Not Showing
- Verify ResetPasswordController is loaded
- Check FXML binding for requirement labels
- Verify CSS styles are applied

---

## Support & Maintenance

For issues or enhancements:
1. Check error logs in console
2. Verify database connectivity
3. Test email configuration separately
4. Review token expiration settings
5. Check user table schema

---

**Last Updated**: April 2026
**Version**: 1.0
**Status**: Production Ready
