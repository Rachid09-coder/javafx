package com.edusmart.controller.auth;

import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.User;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * SignUpController - Handles new user registration (Sign Up Screen).
 *
 * Team member: Implement the registerUser() method to persist data via
 * your database/service layer.
 */
public class SignUpController implements Initializable {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private CheckBox termsCheckbox;
    @FXML private Button registerButton;
    @FXML private Button backToLoginButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final UserService userService = new UserServiceImpl(new JdbcUserDao());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (errorLabel != null) errorLabel.setVisible(false);
        if (successLabel != null) successLabel.setVisible(false);

        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("Étudiant", "Enseignant");
            roleComboBox.setValue("Étudiant");
        }
    }

    /**
     * Handles the registration form submission.
     * TODO: Connect to user registration service/database.
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        if (!validateForm()) return;

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String numtel = phoneField != null ? phoneField.getText().trim() : "";
        String password = passwordField.getText();
        String roleLabel = roleComboBox.getValue();

        User.Role role = "Enseignant".equals(roleLabel) ? User.Role.TEACHER : User.Role.STUDENT;
        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole(role);
        newUser.setNumtel(numtel);
        newUser.setActive(true);

        try {
            if (userService.getUserByEmail(email).isPresent()) {
                showError("Cet email est deja utilise.");
                return;
            }
            if (userService.createUser(newUser)) {
                showSuccess("Compte cree avec succes! Vous pouvez maintenant vous connecter.");
            } else {
                showError("Echec de la creation du compte.");
            }
        } catch (RuntimeException ex) {
            Throwable c = ex;
            while (c.getCause() != null) c = c.getCause();
            showError(c.getMessage() != null ? c.getMessage() : "Erreur lors de l'inscription.");
        }
    }

    /**
     * Navigates back to the login screen.
     */
    @FXML
    private void handleBackToLogin(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }

    private boolean validateForm() {
        clearMessages();

        if (firstNameField.getText().trim().isEmpty()) {
            showError("Le prénom est obligatoire.");
            return false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Le nom est obligatoire.");
            return false;
        }
        if (!isValidEmail(emailField.getText().trim())) {
            showError("Adresse email invalide.");
            return false;
        }
        if (phoneField == null || phoneField.getText().trim().isEmpty()) {
            showError("Le numero de telephone est obligatoire.");
            return false;
        }
        if (passwordField.getText().length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas.");
            return false;
        }
        if (termsCheckbox != null && !termsCheckbox.isSelected()) {
            showError("Vous devez accepter les conditions d'utilisation.");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void showSuccess(String message) {
        if (successLabel != null) {
            successLabel.setText(message);
            successLabel.setVisible(true);
        }
    }

    private void clearMessages() {
        if (errorLabel != null) errorLabel.setVisible(false);
        if (successLabel != null) successLabel.setVisible(false);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
