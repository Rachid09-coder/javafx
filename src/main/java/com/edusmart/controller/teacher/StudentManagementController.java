package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.User;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Teacher UI — CRUD on the {@code user} table.
 */
public class StudentManagementController implements Initializable {

    @FXML private TableView<User> studentsTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> numtelColumn;
    @FXML private TableColumn<User, String> activeColumn;

    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField passwordField;
    @FXML private TextField numtelField;
    @FXML private CheckBox activeCheckBox;
    @FXML private TextField resetTokenField;
    @FXML private TextField resetTokenExpiresField;
    @FXML private TextField googleIdField;
    @FXML private TextArea faceDescriptorArea;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private User selectedUser;
    private final UserService userService = new UserServiceImpl(new JdbcUserDao());

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupForm();
        loadUsers();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (firstNameColumn != null) firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        if (lastNameColumn != null) lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        if (emailColumn != null) emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (roleColumn != null) roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleValue"));
        if (numtelColumn != null) numtelColumn.setCellValueFactory(new PropertyValueFactory<>("numtel"));
        if (activeColumn != null) {
            activeColumn.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().isActive() ? "Oui" : "Non"));
        }
        if (studentsTable != null) {
            studentsTable.setItems(userList);
            studentsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        selectedUser = newVal;
                        populateForm(newVal);
                    });
        }
    }

    private void setupForm() {
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("STUDENT", "TEACHER", "ADMIN");
            roleComboBox.setValue("STUDENT");
        }
        if (activeCheckBox != null) activeCheckBox.setSelected(true);
    }

    private void loadUsers() {
        try {
            userList.setAll(userService.getAllUsers());
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement utilisateurs: " + rootCauseMessage(ex), true);
        }
    }

    private void populateForm(User u) {
        if (u == null) return;
        if (firstNameField != null) firstNameField.setText(u.getFirstName());
        if (lastNameField != null) lastNameField.setText(u.getLastName());
        if (emailField != null) emailField.setText(u.getEmail());
        if (roleComboBox != null && u.getRoleValue() != null) roleComboBox.setValue(u.getRoleValue().toUpperCase());
        if (passwordField != null) passwordField.clear();
        if (numtelField != null) numtelField.setText(u.getNumtel());
        if (activeCheckBox != null) activeCheckBox.setSelected(u.isActive());
        if (resetTokenField != null) resetTokenField.setText(u.getResetToken() != null ? u.getResetToken() : "");
        if (resetTokenExpiresField != null) {
            resetTokenExpiresField.setText(u.getResetTokenExpiresAt() != null ? u.getResetTokenExpiresAt().format(DT) : "");
        }
        if (googleIdField != null) googleIdField.setText(u.getGoogleId() != null ? u.getGoogleId() : "");
        if (faceDescriptorArea != null) {
            faceDescriptorArea.setText(u.getFaceDescriptor() != null ? u.getFaceDescriptor() : "");
        }
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm(false)) return;
        try {
            User u = buildUserFromForm(false);
            if (userService.createUser(u)) {
                showMessage("Utilisateur créé avec succès!", false);
                clearForm();
                selectedUser = null;
                if (studentsTable != null) studentsTable.getSelectionModel().clearSelection();
                loadUsers();
            } else {
                showMessage("Création échouée.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur création: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedUser == null) {
            showMessage("Sélectionnez un utilisateur.", true);
            return;
        }
        if (!validateForm(true)) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la modification de " + selectedUser.getFullName().trim() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    User u = buildUserFromForm(true);
                    u.setId(selectedUser.getId());
                    if (userService.updateUser(u)) {
                        showMessage("Utilisateur mis à jour.", false);
                        loadUsers();
                    } else {
                        showMessage("Mise à jour échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur mise à jour: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedUser == null) {
            showMessage("Sélectionnez un utilisateur.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'utilisateur \"" + selectedUser.getFullName().trim() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (userService.deleteUser(selectedUser.getId())) {
                        userList.remove(selectedUser);
                        clearForm();
                        selectedUser = null;
                        showMessage("Utilisateur supprimé.", false);
                    } else {
                        showMessage("Suppression échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur suppression: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) {
            loadUsers();
            return;
        }
        List<User> filtered = userService.getAllUsers().stream()
                .filter(u ->
                        (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(query))
                                || (u.getLastName() != null && u.getLastName().toLowerCase().contains(query))
                                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(query))
                                || (u.getNumtel() != null && u.getNumtel().toLowerCase().contains(query))
                                || (u.getRoleValue() != null && u.getRoleValue().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        userList.setAll(filtered);
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        selectedUser = null;
        if (studentsTable != null) studentsTable.getSelectionModel().clearSelection();
    }

    private boolean validateForm(boolean isUpdate) {
        if (firstNameField == null || firstNameField.getText().trim().isEmpty()) {
            showMessage("Le prénom est obligatoire.", true);
            return false;
        }
        if (lastNameField == null || lastNameField.getText().trim().isEmpty()) {
            showMessage("Le nom est obligatoire.", true);
            return false;
        }
        if (emailField == null || emailField.getText().trim().isEmpty()) {
            showMessage("L'email est obligatoire.", true);
            return false;
        }
        if (roleComboBox == null || roleComboBox.getValue() == null || roleComboBox.getValue().isBlank()) {
            showMessage("Le rôle est obligatoire.", true);
            return false;
        }
        if (numtelField == null || numtelField.getText().trim().isEmpty()) {
            showMessage("Le numéro de téléphone est obligatoire.", true);
            return false;
        }
        if (!isUpdate && (passwordField == null || passwordField.getText().isEmpty())) {
            showMessage("Le mot de passe est obligatoire à la création.", true);
            return false;
        }
        if (passwordField != null && !passwordField.getText().isEmpty() && passwordField.getText().length() < 6) {
            showMessage("Le mot de passe doit contenir au moins 6 caractères.", true);
            return false;
        }
        if (resetTokenExpiresField != null && !resetTokenExpiresField.getText().trim().isEmpty()) {
            if (parseDateTime(resetTokenExpiresField.getText().trim()) == null) {
                showMessage("Date expiration token invalide (yyyy-MM-dd HH:mm:ss).", true);
                return false;
            }
        }
        return true;
    }

    private User buildUserFromForm(boolean isUpdate) {
        User u = new User();
        u.setFirstName(firstNameField.getText().trim());
        u.setLastName(lastNameField.getText().trim());
        u.setEmail(emailField.getText().trim());
        u.setRoleValue(roleComboBox.getValue().trim().toUpperCase());
        u.setNumtel(numtelField.getText().trim());
        u.setActive(activeCheckBox != null && activeCheckBox.isSelected());

        if (isUpdate) {
            String pwd = passwordField != null ? passwordField.getText() : "";
            if (pwd.isEmpty()) {
                u.setPassword(selectedUser.getPassword());
            } else {
                u.setPassword(pwd);
            }
        } else {
            u.setPassword(passwordField.getText());
        }

        u.setResetToken(emptyToNull(resetTokenField));
        u.setResetTokenExpiresAt(parseDateTimeField(resetTokenExpiresField));
        u.setGoogleId(emptyToNull(googleIdField));
        u.setFaceDescriptor(emptyToNull(faceDescriptorArea));
        return u;
    }

    private static String emptyToNull(TextField tf) {
        if (tf == null || tf.getText() == null) return null;
        String t = tf.getText().trim();
        return t.isEmpty() ? null : t;
    }

    private static String emptyToNull(TextArea ta) {
        if (ta == null || ta.getText() == null) return null;
        String t = ta.getText().trim();
        return t.isEmpty() ? null : t;
    }

    private LocalDateTime parseDateTimeField(TextField tf) {
        if (tf == null || tf.getText() == null) return null;
        return parseDateTime(tf.getText().trim());
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s.trim(), DT);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(s.trim());
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    private void clearForm() {
        if (firstNameField != null) firstNameField.clear();
        if (lastNameField != null) lastNameField.clear();
        if (emailField != null) emailField.clear();
        if (roleComboBox != null) roleComboBox.setValue("STUDENT");
        if (passwordField != null) passwordField.clear();
        if (numtelField != null) numtelField.clear();
        if (activeCheckBox != null) activeCheckBox.setSelected(true);
        if (resetTokenField != null) resetTokenField.clear();
        if (resetTokenExpiresField != null) resetTokenExpiresField.clear();
        if (googleIdField != null) googleIdField.clear();
        if (faceDescriptorArea != null) faceDescriptorArea.clear();
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : throwable.getMessage();
    }

    public ObservableList<User> getStudentList() {
        return userList;
    }

    // ── Navigation ──────────────────────────────────────────────────────

    @FXML private void handleDashboard(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD);
    }

    @FXML private void handleManageCourses(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES);
    }

    @FXML private void handleManageModules(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES);
    }

    @FXML private void handleManageExams(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS);
    }

    @FXML private void handleShopManagement(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT);
    }

    @FXML private void handleBulletins(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS);
    }

    @FXML private void handleCertifications(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS);
    }

    @FXML private void handleAnalysisAI(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI);
    }

    @FXML private void handleStudentManagement(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT);
    }

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
