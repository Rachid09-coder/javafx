package com.edusmart.controller.shared;

import com.edusmart.model.User;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private Label nameTitleLabel;
    @FXML private Label roleLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField emailAssocField;
    @FXML private PasswordField passwordField;

    private final UserService userService = new UserServiceImpl(new JdbcUserDao());
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUser = SceneManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadUserData();
        }
    }

    private void loadUserData() {
        nameTitleLabel.setText(currentUser.getFullName());
        roleLabel.setText(currentUser.getRoleValue());
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
        emailAssocField.setText(currentUser.getEmailAssoc() != null ? currentUser.getEmailAssoc() : "");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (currentUser == null) return;

        currentUser.setFirstName(firstNameField.getText().trim());
        currentUser.setLastName(lastNameField.getText().trim());
        currentUser.setEmailAssoc(emailAssocField.getText().trim());

        String newPassword = passwordField.getText();
        if (newPassword != null && !newPassword.isEmpty()) {
            currentUser.setPassword(newPassword);
        }

        try {
            if (userService.updateUser(currentUser)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Profil mis à jour");
                alert.setHeaderText(null);
                alert.setContentText("Vos informations ont été enregistrées avec succès.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'enregistrer les modifications.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Return to previous scene based on role or just close
        if (currentUser.getRole() == User.Role.TEACHER) {
            SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD);
        } else {
            SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES);
        }
    }
}
