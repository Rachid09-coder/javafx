package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCategoryDao;
import com.edusmart.model.Category;
import com.edusmart.service.CategoryService;
import com.edusmart.service.impl.CategoryServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CategoryManagementController implements Initializable {

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Integer> idColumn;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TableColumn<Category, String> iconColumn;
    @FXML private TableColumn<Category, String> colorColumn;

    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    private final CategoryService categoryService = new CategoryServiceImpl(new JdbcCategoryDao());
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (iconColumn != null) iconColumn.setCellValueFactory(new PropertyValueFactory<>("icon"));
        if (colorColumn != null) colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        if (categoriesTable != null) categoriesTable.setItems(categoryList);
    }

    private void loadData() {
        try {
            categoryList.setAll(categoryService.getAllCategories());
            if (categoriesTable != null) categoriesTable.refresh();
        } catch (Exception ex) { showMessage("Erreur: " + rootCause(ex), true); }
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        Stage owner = (Stage) categoriesTable.getScene().getWindow();
        if (CategoryFormController.openDialog(owner, null)) {
            loadData();
            showMessage("Catégorie ajoutée.", false);
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        Category sel = categoriesTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez une catégorie.", true); return; }
        Stage owner = (Stage) categoriesTable.getScene().getWindow();
        if (CategoryFormController.openDialog(owner, sel)) {
            loadData();
            showMessage("Catégorie modifiée.", false);
        }
    }

    @FXML
    private void handleDelete(ActionEvent e) {
        Category sel = categoriesTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez une catégorie.", true); return; }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + sel.getName() + " ?", ButtonType.YES, ButtonType.NO);
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    if (categoryService.deleteCategory(sel.getId())) {
                        categoryList.remove(sel); showMessage("Catégorie supprimée.", false);
                    } else showMessage("Échec.", true);
                } catch (Exception ex) { showMessage("Erreur : " + rootCause(ex), true); }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent e) {
        String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (q.isEmpty()) { loadData(); return; }
        List<Category> filtered = categoryService.getAllCategories().stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
        categoryList.setAll(filtered);
    }

    private void showMessage(String msg, boolean isErr) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isErr ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }
    private String rootCause(Throwable t) { while(t.getCause()!=null) t=t.getCause(); return t.getMessage(); }

    @FXML private void handleDashboard(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD); }
    @FXML private void handleManageCourses(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES); }
    @FXML private void handleManageModules(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES); }
    @FXML private void handleManageExams(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS); }
    @FXML private void handleGradeManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_GRADE_MANAGEMENT); }
    @FXML private void handleShopManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT); }
    @FXML private void handleCategoryManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CATEGORY_MANAGEMENT); }
    @FXML private void handleBulletins(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS); }
    @FXML private void handleCertifications(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS); }
    @FXML private void handleAnalysisAI(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI); }
    @FXML private void handleStudentManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
