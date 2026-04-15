package com.edusmart.controller.auth;

import com.edusmart.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * LoginController - Handles user authentication (Login Screen).
 *
 * Team member: Implement the authenticate() method and connect to your
 * database/service layer.
 */
public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signUpButton;
    @FXML private Label errorLabel;
    @FXML private CheckBox rememberMeCheckbox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Clear any previous error messages
        if (errorLabel != null) errorLabel.setVisible(false);
    }

    /**
     * Handles the login button click.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Adresse email invalide.");
            return;
        }

        try {
            com.edusmart.service.UserService userService = new com.edusmart.service.impl.UserServiceImpl(new com.edusmart.dao.jdbc.JdbcUserDao());
            java.util.Optional<com.edusmart.model.User> optUser = userService.getUserByEmail(email);
            if (optUser.isPresent() && optUser.get().getPasswordHash().equals(password)) {
                 if (optUser.get().getRole() == com.edusmart.model.User.Role.TEACHER) {
                     SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD);
                 } else {
                     SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES);
                 }
            } else {
                showError("Email ou mot de passe incorrect.");
            }
        } catch (Exception ex) {
            showError("Erreur système.");
        }
    }

    /**
     * Navigates to the sign-up screen.
     */
    @FXML
    private void handleSignUp(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.SIGNUP);
    }

    /**
     * Handles the "Forgot Password" link.
     * TODO: Implement password reset flow.
     */
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        // TODO: Navigate to password reset screen or open dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe oublié");
        alert.setHeaderText(null);
        alert.setContentText("Veuillez contacter l'administrateur pour réinitialiser votre mot de passe.");
        alert.showAndWait();
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
