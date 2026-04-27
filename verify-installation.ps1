# Advanced Forgot Password Feature - Installation & Verification (PowerShell)
# Run this script to verify all files are in place and correctly configured

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Advanced Forgot Password - Verification" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Counter
$PASSED = 0
$FAILED = 0
$WARNINGS = 0

# Function to check if file exists
function Check-File {
    param(
        [string]$Path,
        [string]$Description
    )
    
    if (Test-Path $Path -PathType Leaf) {
        Write-Host "✓ $Description" -ForegroundColor Green
        $global:PASSED++
        return $true
    } else {
        Write-Host "✗ $Description - NOT FOUND: $Path" -ForegroundColor Red
        $global:FAILED++
        return $false
    }
}

# Function to check if directory exists
function Check-Directory {
    param(
        [string]$Path,
        [string]$Description
    )
    
    if (Test-Path $Path -PathType Container) {
        Write-Host "✓ $Description" -ForegroundColor Green
        $global:PASSED++
        return $true
    } else {
        Write-Host "✗ $Description - NOT FOUND: $Path" -ForegroundColor Red
        $global:FAILED++
        return $false
    }
}

# Function to warn about configuration
function Add-Warning {
    param([string]$Message)
    Write-Host "⚠ $Message" -ForegroundColor Yellow
    $global:WARNINGS++
}

# Project root (current directory)
$PROJECT_ROOT = "."

Write-Host "1. Checking Service Files..." -ForegroundColor Yellow
Write-Host "---" -ForegroundColor Gray
Check-File "$PROJECT_ROOT\src\main\java\com\edusmart\service\PasswordResetService.java" `
    "PasswordResetService interface"
Check-File "$PROJECT_ROOT\src\main\java\com\edusmart\service\impl\PasswordResetServiceImpl.java" `
    "PasswordResetServiceImpl implementation"
Write-Host ""

Write-Host "2. Checking Controller Files..." -ForegroundColor Yellow
Write-Host "---" -ForegroundColor Gray
Check-File "$PROJECT_ROOT\src\main\java\com\edusmart\controller\auth\ForgotPasswordController.java" `
    "ForgotPasswordController"
Check-File "$PROJECT_ROOT\src\main\java\com\edusmart\controller\auth\ResetPasswordController.java" `
    "ResetPasswordController"
Write-Host ""

Write-Host "3. Checking FXML View Files..." -ForegroundColor Yellow
Write-Host "---" -ForegroundColor Gray
Check-File "$PROJECT_ROOT\src\main\resources\fxml\auth\forgot-password.fxml" `
    "forgot-password.fxml"
Check-File "$PROJECT_ROOT\src\main\resources\fxml\auth\reset-password.fxml" `
    "reset-password.fxml"
Write-Host ""

Write-Host "4. Checking Modified Files..." -ForegroundColor Yellow
Write-Host "---" -ForegroundColor Gray
Check-File "$PROJECT_ROOT\src\main\java\com\edusmart\util\SceneManager.java" `
    "SceneManager.java (should contain FORGOT_PASSWORD and RESET_PASSWORD)"
Check-File "$PROJECT_ROOT\src\main\java\com\edusmart\controller\auth\LoginController.java" `
    "LoginController.java (should contain updated handleForgotPassword)"
Write-Host ""

Write-Host "5. Checking Documentation Files..." -ForegroundColor Yellow
Write-Host "---" -ForegroundColor Gray
Check-File "$PROJECT_ROOT\ADVANCED_FORGOT_PASSWORD_GUIDE.md" `
    "Complete feature documentation"
Check-File "$PROJECT_ROOT\FORGOT_PASSWORD_QUICK_START.md" `
    "Quick start guide"
Check-File "$PROJECT_ROOT\FILES_SUMMARY.md" `
    "Files summary"
Write-Host ""

Write-Host "6. Checking Directory Structure..." -ForegroundColor Yellow
Write-Host "---" -ForegroundColor Gray
Check-Directory "$PROJECT_ROOT\src\main\java\com\edusmart\service" `
    "Service directory exists"
Check-Directory "$PROJECT_ROOT\src\main\java\com\edusmart\controller\auth" `
    "Controller/auth directory exists"
Check-Directory "$PROJECT_ROOT\src\main\resources\fxml\auth" `
    "FXML/auth directory exists"
Write-Host ""

Write-Host "7. Configuration Checks..." -ForegroundColor Yellow
Write-Host "---" -ForegroundColor Gray
Add-Warning "Verify MailSender.java has correct SMTP credentials"
Add-Warning "Verify MySQL User table has reset_token and reset_token_expires_at columns"
Add-Warning "Verify Database connection is working"
Write-Host ""

Write-Host "8. Dependency Checks..." -ForegroundColor Yellow
Write-Host "---" -ForegroundColor Gray
$pomContent = Get-Content "$PROJECT_ROOT\pom.xml" -ErrorAction SilentlyContinue
if ($pomContent -match "jakarta\.mail|jakarta-mail") {
    Write-Host "✓ Jakarta Mail dependency found" -ForegroundColor Green
    $PASSED++
} else {
    Add-Warning "Jakarta Mail - verify in pom.xml"
}

if ($pomContent -match "openjfx|javafx") {
    Write-Host "✓ JavaFX dependency found" -ForegroundColor Green
    $PASSED++
} else {
    Add-Warning "JavaFX - verify in pom.xml"
}
Write-Host ""

# Summary
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "VERIFICATION SUMMARY" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Passed: $PASSED" -ForegroundColor Green
Write-Host "Failed: $FAILED" -ForegroundColor Red
Write-Host "Warnings: $WARNINGS" -ForegroundColor Yellow
Write-Host ""

if ($FAILED -eq 0) {
    Write-Host "✓ All files are in place!" -ForegroundColor Green
    if ($WARNINGS -eq 0) {
        Write-Host "✓ No configuration issues detected" -ForegroundColor Green
        Write-Host ""
        Write-Host "You can now compile and run the application:" -ForegroundColor Green
        Write-Host "  mvn clean compile" -ForegroundColor Cyan
        Write-Host "  mvn clean javafx:run" -ForegroundColor Cyan
    } else {
        Write-Host ""
        Write-Host "⚠ Please review the warnings above before running" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ Some files are missing!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please ensure all new files were copied to the correct locations." -ForegroundColor Yellow
    Write-Host "Refer to: FILES_SUMMARY.md" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
