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
 * ForgotPasswordController - Handles password recovery initiation
 * User enters email to receive reset token
 */
public class ForgotPasswordController implements Initializable {

    @FXML private TextField emailField;
    @FXML private Button sendCodeButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;
    @FXML private Label infoLabel;
    @FXML private Label successLabel;
    @FXML private ProgressBar progressBar;

    private PasswordResetService resetService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resetService = new PasswordResetServiceImpl(new JdbcUserDao());
        
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
        infoLabel.setVisible(true);
        progressBar.setVisible(false);

        sendCodeButton.setOnAction(this::handleSendCode);
        backButton.setOnAction(event -> SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN));
    }

    @FXML
    private void handleSendCode(ActionEvent event) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez entrer votre adresse email.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Adresse email invalide.");
            return;
        }

        // Show loading state
        progressBar.setVisible(true);
        sendCodeButton.setDisable(true);
        errorLabel.setVisible(false);
        successLabel.setVisible(false);

        // Send email in background thread
        Thread emailThread = new Thread(() -> {
            try {
                boolean success = resetService.initiatePasswordReset(email);
                
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    sendCodeButton.setDisable(false);
                    
                    if (success) {
                        showSuccess("Un code de réinitialisation a été envoyé à votre email.\n" +
                                  "Vérifiez votre boîte de réception (et spam si nécessaire).");
                        
                        // Navigate to reset password screen after 2 seconds
                        Thread navigateThread = new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                Platform.runLater(() -> 
                                    SceneManager.getInstance().navigateTo(
                                        SceneManager.Scene.RESET_PASSWORD,
                                        email
                                    )
                                );
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        navigateThread.setDaemon(true);
                        navigateThread.start();
                    } else {
                        showError("Impossible d'envoyer le code. Vérifiez votre email ou réessayez plus tard.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    sendCodeButton.setDisable(false);
                    showError("Erreur système : " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
        emailThread.setDaemon(true);
        emailThread.start();
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

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
