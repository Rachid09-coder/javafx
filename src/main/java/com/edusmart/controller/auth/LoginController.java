package com.edusmart.controller.auth;

import com.edusmart.util.SceneManager;
import com.edusmart.util.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Optional;
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

    private final com.edusmart.service.UserService userService =
            new com.edusmart.service.impl.UserServiceImpl(new com.edusmart.dao.jdbc.JdbcUserDao());

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
            java.util.Optional<com.edusmart.model.User> optUser = userService.getUserByEmail(email);
            if (optUser.isPresent() && PasswordUtil.matches(password, optUser.get().getPassword())) {
                 navigateAfterLogin(optUser.get());
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
    private void handleGoogleSignIn(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog(emailField != null ? emailField.getText().trim() : "");
        dialog.setTitle("Connexion Google");
        dialog.setHeaderText("Se connecter avec Google");
        dialog.setContentText("Adresse email Google :");

        dialog.showAndWait().ifPresent(rawEmail -> {
            String googleEmail = rawEmail.trim();
            if (!isValidEmail(googleEmail)) {
                showError("Adresse Google invalide.");
                return;
            }

            String googleId = toGoogleId(googleEmail);
            try {
                Optional<com.edusmart.model.User> optUser = userService.getUserByGoogleId(googleId);
                if (optUser.isEmpty()) {
                    optUser = userService.getUserByEmail(googleEmail);
                }

                if (optUser.isPresent()) {
                    com.edusmart.model.User user = optUser.get();
                    if (user.getGoogleId() == null || user.getGoogleId().isBlank()) {
                        user.setGoogleId(googleId);
                        userService.updateUser(user);
                    }
                    navigateAfterLogin(user);
                } else {
                    showError("Aucun compte EduSmart n'est associe a ce compte Google.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Erreur lors de la connexion Google.");
            }
        });
    }

    @FXML
    private void handleFaceSignIn(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Connexion Face ID");
        dialog.setHeaderText("Reconnaissance Face ID");
        dialog.setContentText("Code Face ID enregistre :");

        dialog.showAndWait().ifPresent(rawDescriptor -> {
            String descriptor = normalizeFaceDescriptor(rawDescriptor);
            if (descriptor.isEmpty()) {
                showError("Code Face ID obligatoire.");
                return;
            }

            try {
                Optional<com.edusmart.model.User> optUser = userService.getUserByFaceDescriptor(descriptor);
                if (optUser.isPresent()) {
                    navigateAfterLogin(optUser.get());
                } else {
                    showError("Face ID non reconnu.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Erreur lors de la connexion Face ID.");
            }
        });
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Récupération de mot de passe");
        dialog.setHeaderText("Mot de passe oublié");
        dialog.setContentText("Saisissez votre e-mail de RÉCUPÉRATION (E-mail associé) :");

        dialog.showAndWait().ifPresent(emailAssoc -> {
            if (emailAssoc.trim().isEmpty() || !isValidEmail(emailAssoc)) {
                showError("Veuillez saisir une adresse e-mail valide.");
                return;
            }

            try {
                com.edusmart.service.UserService userService = new com.edusmart.service.impl.UserServiceImpl(new com.edusmart.dao.jdbc.JdbcUserDao());
                java.util.Optional<com.edusmart.model.User> optUser = userService.getUserByEmailAssoc(emailAssoc);

                if (optUser.isPresent()) {
                    com.edusmart.model.User user = optUser.get();
                    String subject = "Récupération de votre compte EduSmart";
                    String body = "Bonjour " + user.getFirstName() + ",\n\n" +
                            "Vous avez utilisé cet e-mail pour récupérer votre compte EduSmart (" + user.getEmail() + ").\n" +
                            "Votre mot de passe actuel est : " + user.getPassword() + "\n\n" +
                            "Cordialement,\nL'équipe EduSmart";

                    com.edusmart.util.MailSender.sendEmailWithAttachment(emailAssoc, subject, body, null);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("E-mail envoyé");
                    alert.setHeaderText(null);
                    alert.setContentText("Votre mot de passe a été envoyé à votre adresse de récupération.");
                    alert.showAndWait();
                } else {
                    showError("Aucun compte n'est associé à cette adresse de récupération.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Erreur lors de l'envoi de l'e-mail.");
            }
        });
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void navigateAfterLogin(com.edusmart.model.User user) {
        SceneManager.getInstance().setCurrentUser(user);

        if (user.getRole() == com.edusmart.model.User.Role.TEACHER) {
            SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD);
        } else {
            SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES);
        }
    }

    private String toGoogleId(String email) {
        return "google:" + email.trim().toLowerCase();
    }

    private String normalizeFaceDescriptor(String descriptor) {
        return descriptor == null ? "" : descriptor.trim().toLowerCase();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
