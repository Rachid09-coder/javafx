#!/bin/bash
# Advanced Forgot Password Feature - Installation & Verification Script
# Run this to verify all files are in place and correctly configured

echo "======================================"
echo "Advanced Forgot Password - Verification"
echo "======================================"
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counter
PASSED=0
FAILED=0
WARNINGS=0

# Function to check if file exists
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗${NC} $2 - NOT FOUND: $1"
        ((FAILED++))
        return 1
    fi
}

# Function to check if directory exists
check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗${NC} $2 - NOT FOUND: $1"
        ((FAILED++))
        return 1
    fi
}

# Function to warn about configuration
check_config() {
    echo -e "${YELLOW}⚠${NC} $1"
    ((WARNINGS++))
}

# Project root
PROJECT_ROOT="."

echo "1. Checking Service Files..."
echo "---"
check_file "$PROJECT_ROOT/src/main/java/com/edusmart/service/PasswordResetService.java" \
    "PasswordResetService interface"
check_file "$PROJECT_ROOT/src/main/java/com/edusmart/service/impl/PasswordResetServiceImpl.java" \
    "PasswordResetServiceImpl implementation"
echo ""

echo "2. Checking Controller Files..."
echo "---"
check_file "$PROJECT_ROOT/src/main/java/com/edusmart/controller/auth/ForgotPasswordController.java" \
    "ForgotPasswordController"
check_file "$PROJECT_ROOT/src/main/java/com/edusmart/controller/auth/ResetPasswordController.java" \
    "ResetPasswordController"
echo ""

echo "3. Checking FXML View Files..."
echo "---"
check_file "$PROJECT_ROOT/src/main/resources/fxml/auth/forgot-password.fxml" \
    "forgot-password.fxml"
check_file "$PROJECT_ROOT/src/main/resources/fxml/auth/reset-password.fxml" \
    "reset-password.fxml"
echo ""

echo "4. Checking Modified Files..."
echo "---"
check_file "$PROJECT_ROOT/src/main/java/com/edusmart/util/SceneManager.java" \
    "SceneManager.java (should contain FORGOT_PASSWORD and RESET_PASSWORD)"
check_file "$PROJECT_ROOT/src/main/java/com/edusmart/controller/auth/LoginController.java" \
    "LoginController.java (should contain updated handleForgotPassword)"
echo ""

echo "5. Checking Documentation Files..."
echo "---"
check_file "$PROJECT_ROOT/ADVANCED_FORGOT_PASSWORD_GUIDE.md" \
    "Complete feature documentation"
check_file "$PROJECT_ROOT/FORGOT_PASSWORD_QUICK_START.md" \
    "Quick start guide"
check_file "$PROJECT_ROOT/FILES_SUMMARY.md" \
    "Files summary"
echo ""

echo "6. Checking Directory Structure..."
echo "---"
check_dir "$PROJECT_ROOT/src/main/java/com/edusmart/service" \
    "Service directory exists"
check_dir "$PROJECT_ROOT/src/main/java/com/edusmart/controller/auth" \
    "Controller/auth directory exists"
check_dir "$PROJECT_ROOT/src/main/resources/fxml/auth" \
    "FXML/auth directory exists"
echo ""

echo "7. Configuration Checks..."
echo "---"
check_config "Verify MailSender.java has correct SMTP credentials"
check_config "Verify MySQL User table has reset_token and reset_token_expires_at columns"
check_config "Verify Database connection is working"
echo ""

echo "8. Dependency Checks..."
echo "---"
if grep -q "jakarta.mail" "$PROJECT_ROOT/pom.xml" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} Jakarta Mail dependency found"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠${NC} Jakarta Mail - verify in pom.xml"
    ((WARNINGS++))
fi

if grep -q "openjfx\|javafx" "$PROJECT_ROOT/pom.xml" 2>/dev/null; then
    echo -e "${GREEN}✓${NC} JavaFX dependency found"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠${NC} JavaFX - verify in pom.xml"
    ((WARNINGS++))
fi
echo ""

echo "======================================"
echo "VERIFICATION SUMMARY"
echo "======================================"
echo -e "${GREEN}Passed:${NC} $PASSED"
echo -e "${RED}Failed:${NC} $FAILED"
echo -e "${YELLOW}Warnings:${NC} $WARNINGS"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All files are in place!${NC}"
    if [ $WARNINGS -eq 0 ]; then
        echo -e "${GREEN}✓ No configuration issues detected${NC}"
        echo ""
        echo "You can now compile and run the application:"
        echo "  mvn clean compile"
        echo "  mvn clean javafx:run"
    else
        echo ""
        echo "⚠ Please review the warnings above before running"
    fi
else
    echo -e "${RED}✗ Some files are missing!${NC}"
    echo ""
    echo "Please ensure all new files were copied to the correct locations."
    echo "Refer to: FILES_SUMMARY.md"
fi

echo ""
echo "======================================"
