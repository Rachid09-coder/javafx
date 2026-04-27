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
    @FXML private Button googleLoginButton;
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
            if (optUser.isPresent() && optUser.get().getPassword() != null && optUser.get().getPassword().equals(password)) {
                 loginUser(optUser.get());
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

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        try {
            com.edusmart.service.GoogleAuthService googleService = new com.edusmart.service.GoogleAuthService();
            com.google.api.services.oauth2.model.Userinfo userInfo = googleService.authenticate();
            
            com.edusmart.service.UserService userService = new com.edusmart.service.impl.UserServiceImpl(new com.edusmart.dao.jdbc.JdbcUserDao());
            
            // Try to find user by Google ID or Email
            java.util.Optional<com.edusmart.model.User> optUser = userService.getUserByGoogleId(userInfo.getId());
            if (optUser.isEmpty()) {
                optUser = userService.getUserByEmail(userInfo.getEmail());
                if (optUser.isPresent()) {
                    // Update user to link Google ID
                    com.edusmart.model.User existingUser = optUser.get();
                    existingUser.setGoogleId(userInfo.getId());
                    userService.updateUser(existingUser);
                }
            }
            
            if (optUser.isPresent()) {
                loginUser(optUser.get());
            } else {
                // User doesn't exist, navigate to signup and pass info
                // We'll store it in a temporary static variable or redirect to signup
                // For simplicity, we just show error to ask them to signup first
                showError("Aucun compte associé à ce compte Google. Veuillez vous inscrire.");
                // In a complete flow, we would navigate to SignUp and prefill the form.
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur lors de l'authentification Google: " + ex.getMessage());
        }
    }

    private void loginUser(com.edusmart.model.User user) {
        SceneManager.getInstance().setCurrentUser(user);
        if (user.getRole() == com.edusmart.model.User.Role.TEACHER) {
            SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD);
        } else {
            SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES);
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.FORGOT_PASSWORD);
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
