package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.User;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.UserServiceImpl;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Contrôleur du formulaire Étudiant/Utilisateur (dialog séparé).
 */
public class StudentFormController implements Initializable {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[\\d\\s\\-]{8,15}$");

    @FXML private Label titleLabel;
    @FXML private TextField firstNameField;
    @FXML private Label firstNameError;
    @FXML private TextField lastNameField;
    @FXML private Label lastNameError;
    @FXML private TextField emailField;
    @FXML private Label emailError;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordError;
    @FXML private TextField phoneField;
    @FXML private Label phoneError;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label roleError;
    @FXML private CheckBox activeCheckBox;
    @FXML private Label globalError;

    private final UserService userService = new UserServiceImpl(new JdbcUserDao());

    private User userToEdit;
    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleComboBox.setItems(FXCollections.observableArrayList("STUDENT", "TEACHER", "ADMIN"));
        roleComboBox.setValue("STUDENT");
        // Live validation
        firstNameField.textProperty().addListener((o, ov, nv) -> clearError(firstNameField, firstNameError));
        lastNameField.textProperty().addListener((o, ov, nv) -> clearError(lastNameField, lastNameError));
        emailField.textProperty().addListener((o, ov, nv) -> clearError(emailField, emailError));
        passwordField.textProperty().addListener((o, ov, nv) -> clearError(passwordField, passwordError));
        phoneField.textProperty().addListener((o, ov, nv) -> clearError(phoneField, phoneError));
    }

    public void setAddMode() { titleLabel.setText("Nouvel Utilisateur"); userToEdit = null; }

    public void setEditMode(User user) {
        titleLabel.setText("Modifier l'Utilisateur");
        userToEdit = user;
        firstNameField.setText(user.getFirstName() != null ? user.getFirstName() : "");
        lastNameField.setText(user.getLastName() != null ? user.getLastName() : "");
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        phoneField.setText(user.getNumtel() != null ? user.getNumtel() : "");
        if (user.getRoleValue() != null) roleComboBox.setValue(user.getRoleValue().toUpperCase());
        activeCheckBox.setSelected(user.isActive());
        // Password not shown for edit (kept as is unless changed)
        passwordField.setPromptText("Laisser vide pour ne pas changer");
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave(ActionEvent e) {
        if (!validateForm()) return;
        try {
            User user = buildUser();
            boolean ok;
            if (userToEdit == null) {
                ok = userService.createUser(user);
            } else {
                user.setId(userToEdit.getId());
                // Keep existing password if field left empty
                if (passwordField.getText().isBlank()) {
                    user.setPassword(userToEdit.getPassword());
                }
                ok = userService.updateUser(user);
            }
            if (ok) { saved = true; closeStage(); }
            else showGlobalError("Opération échouée. Email peut-être déjà utilisé.");
        } catch (IllegalArgumentException ex) {
            showGlobalError(ex.getMessage());
        } catch (Exception ex) {
            showGlobalError("Erreur : " + rootCause(ex));
        }
    }

    @FXML
    private void handleCancel(ActionEvent e) { closeStage(); }

    private boolean validateForm() {
        boolean valid = true;
        if (firstNameField.getText().trim().isEmpty()) {
            showFieldError(firstNameField, firstNameError, "Prénom obligatoire."); valid = false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showFieldError(lastNameField, lastNameError, "Nom obligatoire."); valid = false;
        }
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showFieldError(emailField, emailError, "Email obligatoire."); valid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            showFieldError(emailField, emailError, "Format invalide (ex: nom@domaine.com)."); valid = false;
        }
        // Password required only for new user
        if (userToEdit == null && passwordField.getText().isBlank()) {
            showFieldError(passwordField, passwordError, "Mot de passe obligatoire."); valid = false;
        } else if (!passwordField.getText().isBlank() && passwordField.getText().length() < 6) {
            showFieldError(passwordField, passwordError, "Minimum 6 caractères."); valid = false;
        }
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showFieldError(phoneField, phoneError, "Format invalide (ex: 0612345678)."); valid = false;
        }
        if (roleComboBox.getValue() == null) {
            roleComboBox.setStyle("-fx-border-color:#EF4444;");
            roleError.setText("Rôle obligatoire."); roleError.setVisible(true); roleError.setManaged(true);
            valid = false;
        }
        return valid;
    }

    private User buildUser() {
        User u = new User();
        u.setFirstName(firstNameField.getText().trim());
        u.setLastName(lastNameField.getText().trim());
        u.setEmail(emailField.getText().trim());
        u.setNumtel(phoneField.getText().trim());
        u.setRoleValue(roleComboBox.getValue());
        u.setActive(activeCheckBox.isSelected());
        if (!passwordField.getText().isBlank()) u.setPassword(passwordField.getText());
        return u;
    }

    private void showFieldError(TextField f, Label l, String msg) {
        f.setStyle("-fx-border-color:#EF4444;-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;");
        l.setText(msg); l.setVisible(true); l.setManaged(true);
    }
    private void showFieldError(PasswordField f, Label l, String msg) {
        f.setStyle("-fx-border-color:#EF4444;-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;");
        l.setText(msg); l.setVisible(true); l.setManaged(true);
    }
    private void clearError(TextField f, Label l) { f.setStyle(""); l.setVisible(false); l.setManaged(false); }
    private void clearError(PasswordField f, Label l) { f.setStyle(""); l.setVisible(false); l.setManaged(false); }
    private void showGlobalError(String msg) { globalError.setText(msg); globalError.setVisible(true); globalError.setManaged(true); }
    private void closeStage() { ((Stage) titleLabel.getScene().getWindow()).close(); }
    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : "Erreur";
    }

    public static boolean openDialog(Stage owner, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(StudentFormController.class.getResource("/fxml/teacher/student-form.fxml"));
            javafx.scene.Parent root = loader.load();
            StudentFormController ctrl = loader.getController();
            if (user == null) ctrl.setAddMode(); else ctrl.setEditMode(user);
            Stage stage = new Stage();
            stage.initOwner(owner); stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(user == null ? "Ajouter un utilisateur" : "Modifier l'utilisateur");
            stage.setResizable(false);
            Scene scene = new Scene(root);
            URL css = StudentFormController.class.getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene); stage.showAndWait();
            return ctrl.isSaved();
        } catch (IOException ex) { ex.printStackTrace(); return false; }
    }
}
