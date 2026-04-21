package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.User;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.util.ModernActionCell;
import com.edusmart.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * StudentManagementController - Gestion des utilisateurs avec dialogs.
 */
public class StudentManagementController implements Initializable {

    @FXML private TableView<User> studentsTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> activeColumn;
    @FXML private TableColumn<User, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserServiceImpl(new JdbcUserDao());
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (firstNameColumn != null) firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        if (lastNameColumn != null) lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        if (emailColumn != null) emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (phoneColumn != null) phoneColumn.setCellValueFactory(new PropertyValueFactory<>("numtel"));
        if (roleColumn != null) roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleValue"));
        if (activeColumn != null) {
            activeColumn.setCellValueFactory(cd -> 
                new SimpleStringProperty(cd.getValue().isActive() ? "Oui" : "Non")
            );
        }
        // Use Modern Action Cell for edit/delete buttons
        if (actionsColumn != null) {
            actionsColumn.setCellFactory(col -> 
                new ModernActionCell.Builder<User>()
                    .onEdit(this::handleEditRow)
                    .onDelete(this::handleDeleteRow)
                    .editTooltip("Modifier l'utilisateur")
                    .deleteTooltip("Supprimer l'utilisateur")
                    .build()
            );
        }
        if (studentsTable != null) studentsTable.setItems(userList);
    }

    private void loadData() {
        try {
            userList.setAll(userService.getAllUsers());
            if (studentsTable != null) studentsTable.refresh();
        } catch (Exception ex) {
            showMessage("Erreur chargement: " + rootCause(ex), true);
        }
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        Stage owner = (Stage) studentsTable.getScene().getWindow();
        if (StudentFormController.openDialog(owner, null)) {
            loadData();
            showMessage("Utilisateur ajouté avec succès !", false);
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        User selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un utilisateur à modifier.", true);
            return;
        }
        editUser(selected);
    }

    private void handleEditRow(User user) {
        editUser(user);
    }

    private void editUser(User user) {
        Stage owner = (Stage) studentsTable.getScene().getWindow();
        if (StudentFormController.openDialog(owner, user)) {
            loadData();
            showMessage("Utilisateur modifié avec succès !", false);
        }
    }

    @FXML
    private void handleDelete(ActionEvent e) {
        User selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un utilisateur à supprimer.", true);
            return;
        }
        confirmAndDelete(selected);
    }

    private void handleDeleteRow(User user) {
        confirmAndDelete(user);
    }

    private void confirmAndDelete(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'utilisateur \"" + user.getFullName() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (userService.deleteUser(user.getId())) {
                        userList.remove(user);
                        showMessage("Utilisateur supprimé.", false);
                    } else showMessage("Suppression échouée.", true);
                } catch (Exception ex) { showMessage("Erreur : " + rootCause(ex), true); }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent e) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) { loadData(); return; }
        List<User> filtered = userService.getAllUsers().stream()
                .filter(u -> (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(query))
                        || (u.getLastName() != null && u.getLastName().toLowerCase().contains(query))
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        userList.setAll(filtered);
        if (studentsTable != null) studentsTable.refresh();
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : "Erreur";
    }

    // Navigation
    @FXML private void handleDashboard(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD); }
    @FXML private void handleManageCourses(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES); }
    @FXML private void handleManageModules(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES); }
    @FXML private void handleManageExams(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS); }
    @FXML private void handleGradeManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_GRADE_MANAGEMENT); }
    @FXML private void handleShopManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT); }
    @FXML private void handleBulletins(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS); }
    @FXML private void handleCertifications(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS); }
    @FXML private void handleAnalysisAI(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI); }
    @FXML private void handleStudentManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
