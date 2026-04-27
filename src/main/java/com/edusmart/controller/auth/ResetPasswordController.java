package com.edusmart.controller.auth;

import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.service.PasswordResetService;
import com.edusmart.service.impl.PasswordResetServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ResetPasswordController - Handles password reset with token validation
 * User enters reset token and new password
 */
public class ResetPasswordController implements Initializable {

    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Label attemptsLabel;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private Label req1, req2, req3;

    private PasswordResetService resetService;
    private String userEmail;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resetService = new PasswordResetServiceImpl(new JdbcUserDao());
        
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
        strengthBar.setStyle("-fx-accent: #ff9800;");

        resetButton.setOnAction(this::handleResetPassword);
        backButton.setOnAction(event -> SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN));

        // Add password strength listener
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));
        
        // Add key listener to update requirements in real-time
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> updateRequirements(newVal));
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
        updateAttempts();
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String token = tokenField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (token.isEmpty()) {
            showError("Veuillez entrer le code de réinitialisation.");
            return;
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez entrer et confirmer votre nouveau mot de passe.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        if (newPassword.length() < 6) {
            showError("Le mot de passe doit contenir au minimum 6 caractères.");
            return;
        }

        // Check if locked out
        if (resetService.isLockedOutFromReset(userEmail)) {
            showError("Trop de tentatives. Réessayez dans 10 minutes.");
            return;
        }

        resetButton.setDisable(true);

        // Reset password in background
        Thread resetThread = new Thread(() -> {
            try {
                boolean success = resetService.resetPassword(userEmail, token, newPassword);
                
                Platform.runLater(() -> {
                    resetButton.setDisable(false);
                    
                    if (success) {
                        showSuccess("Mot de passe réinitialisé avec succès!\nVous pouvez maintenant vous connecter.");
                        
                        // Navigate to login after 2 seconds
                        Thread navigateThread = new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                Platform.runLater(() -> 
                                    SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN)
                                );
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        navigateThread.setDaemon(true);
                        navigateThread.start();
                    } else {
                        updateAttempts();
                        showError("Impossible de réinitialiser le mot de passe.\nVérifiez le code et réessayez.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    resetButton.setDisable(false);
                    showError("Erreur système : " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
        resetThread.setDaemon(true);
        resetThread.start();
    }

    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            strengthBar.setProgress(0);
            strengthLabel.setText("Vide");
            strengthLabel.setStyle("-fx-text-fill: #999;");
            return;
        }

        double strength = calculateStrength(password);
        strengthBar.setProgress(strength);

        if (strength <= 0.33) {
            strengthLabel.setText("Faible");
            strengthLabel.setStyle("-fx-text-fill: #cc0000;");
        } else if (strength <= 0.66) {
            strengthLabel.setText("Moyen");
            strengthLabel.setStyle("-fx-text-fill: #ff9800;");
        } else {
            strengthLabel.setText("Fort");
            strengthLabel.setStyle("-fx-text-fill: #00aa00;");
        }
    }

    private double calculateStrength(String password) {
        if (password == null) return 0;
        
        double strength = 0;
        
        // Length (max 0.4)
        strength += Math.min(password.length() / 20.0, 0.4);
        
        // Uppercase (max 0.2)
        if (password.matches(".*[A-Z].*")) strength += 0.2;
        
        // Lowercase (max 0.2)
        if (password.matches(".*[a-z].*")) strength += 0.2;
        
        // Numbers (max 0.1)
        if (password.matches(".*[0-9].*")) strength += 0.1;
        
        // Special characters (max 0.1)
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/].*")) strength += 0.1;
        
        return Math.min(strength, 1.0);
    }

    private void updateRequirements(String password) {
        // Length requirement
        if (password != null && password.length() >= 6) {
            req1.setText("✓ Minimum 6 caractères");
            req1.setStyle("-fx-text-fill: #00aa00;");
        } else {
            req1.setText("✗ Minimum 6 caractères");
            req1.setStyle("-fx-text-fill: #cc0000;");
        }

        // Uppercase requirement
        if (password != null && password.matches(".*[A-Z].*")) {
            req2.setText("✓ Au moins une majuscule");
            req2.setStyle("-fx-text-fill: #00aa00;");
        } else {
            req2.setText("✗ Au moins une majuscule");
            req2.setStyle("-fx-text-fill: #cc0000;");
        }

        // Number requirement
        if (password != null && password.matches(".*[0-9].*")) {
            req3.setText("✓ Au moins un chiffre");
            req3.setStyle("-fx-text-fill: #00aa00;");
        } else {
            req3.setText("✗ Au moins un chiffre");
            req3.setStyle("-fx-text-fill: #cc0000;");
        }
    }

    private void updateAttempts() {
        int remaining = resetService.getRemainingAttempts(userEmail);
        attemptsLabel.setText("Tentatives restantes : " + remaining);
        
        if (remaining <= 1) {
            attemptsLabel.setStyle("-fx-text-fill: #cc0000;");
        } else if (remaining <= 2) {
            attemptsLabel.setStyle("-fx-text-fill: #ff9800;");
        } else {
            attemptsLabel.setStyle("-fx-text-fill: #00aa00;");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
}
