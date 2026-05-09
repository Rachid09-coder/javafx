package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCategoryDao;
import com.edusmart.dao.jdbc.JdbcProductDao;
import com.edusmart.model.Category;
import com.edusmart.service.CategoryService;
import com.edusmart.service.ProductService;
import com.edusmart.service.impl.CategoryServiceImpl;
import com.edusmart.service.impl.ProductServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * CRUD for the {@code category} table (boutique categories).
 */
public class CategoryManagementController implements Initializable {

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Integer> idColumn;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TableColumn<Category, String> descriptionColumn;
    @FXML private TableColumn<Category, String> iconColumn;
    @FXML private TableColumn<Category, String> colorColumn;

    @FXML private TextField searchField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField iconField;
    @FXML private TextField colorField;
    @FXML private Label messageLabel;
    @FXML private Label totalCategoriesLabel;

    private ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private Category selectedCategory;
    private final CategoryService categoryService = new CategoryServiceImpl(new JdbcCategoryDao());
    private final ProductService productService = new ProductServiceImpl(new JdbcProductDao());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadCategories();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (descriptionColumn != null) {
            descriptionColumn.setCellValueFactory(data -> {
                String d = data.getValue().getDescription();
                if (d == null) return new javafx.beans.property.SimpleStringProperty("-");
                String shortD = d.length() > 60 ? d.substring(0, 57) + "..." : d;
                return new javafx.beans.property.SimpleStringProperty(shortD);
            });
        }
        if (iconColumn != null) iconColumn.setCellValueFactory(new PropertyValueFactory<>("icon"));
        if (colorColumn != null) colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        if (categoriesTable != null) {
            categoriesTable.setItems(categoryList);
            categoriesTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void loadCategories() {
        try {
            categoryList.setAll(categoryService.getAllCategories());
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement categories: " + rootCauseMessage(ex), true);
        }
        updateCount();
    }

    private void populateForm(Category c) {
        selectedCategory = c;
        if (c == null) return;
        if (nameField != null) nameField.setText(c.getName());
        if (descriptionArea != null) descriptionArea.setText(c.getDescription());
        if (iconField != null) iconField.setText(c.getIcon());
        if (colorField != null) colorField.setText(c.getColor());
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        try {
            if (categoryService.createCategory(buildFromForm())) {
                showMessage("Categorie creee avec succes!", false);
                clearForm();
                selectedCategory = null;
                if (categoriesTable != null) categoriesTable.getSelectionModel().clearSelection();
                loadCategories();
            } else {
                showMessage("Creation echouee.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur creation: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedCategory == null) {
            showMessage("Selectionnez une categorie.", true);
            return;
        }
        if (!validateForm()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la modification de \"" + selectedCategory.getName() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    Category c = buildFromForm();
                    c.setId(selectedCategory.getId());
                    if (categoryService.updateCategory(c)) {
                        showMessage("Categorie mise a jour.", false);
                        loadCategories();
                    } else {
                        showMessage("Mise a jour echouee.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur mise a jour: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedCategory == null) {
            showMessage("Selectionnez une categorie.", true);
            return;
        }
        int productCount = productService.countProductsByCategory(selectedCategory.getId());
        String confirmText = productCount > 0
                ? "Supprimer la categorie \"" + selectedCategory.getName() + "\" ?\n\n"
                + productCount + " produit(s) seront egalement supprimes."
                : "Supprimer la categorie \"" + selectedCategory.getName() + "\" ?";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, confirmText, ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                int deletedProductCount = productCount;
                try {
                    if (categoryService.deleteCategoryAndProducts(selectedCategory.getId())) {
                        categoryList.remove(selectedCategory);
                        clearForm();
                        selectedCategory = null;
                        updateCount();
                        showMessage(deletedProductCount > 0
                                ? "Categorie supprimee (" + deletedProductCount + " produit(s) supprime(s))."
                                : "Categorie supprimee.", false);
                    } else {
                        showMessage("Suppression echouee.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur suppression: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        selectedCategory = null;
        if (categoriesTable != null) categoriesTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) {
            loadCategories();
            return;
        }
        List<Category> filtered = categoryService.getAllCategories().stream()
                .filter(c ->
                        (c.getName() != null && c.getName().toLowerCase().contains(query))
                                || (c.getDescription() != null && c.getDescription().toLowerCase().contains(query))
                                || (c.getIcon() != null && c.getIcon().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        categoryList.setAll(filtered);
        updateCount();
    }

    private boolean validateForm() {
        if (nameField == null || nameField.getText().trim().isEmpty()) {
            showMessage("Le nom est obligatoire.", true);
            return false;
        }
        if (nameField.getText().trim().length() > 120) {
            showMessage("Nom: maximum 120 caracteres.", true);
            return false;
        }
        if (descriptionArea == null || descriptionArea.getText().trim().isEmpty()) {
            showMessage("La description est obligatoire.", true);
            return false;
        }
        if (iconField == null || iconField.getText().trim().isEmpty()) {
            showMessage("L'icone est obligatoire.", true);
            return false;
        }
        if (colorField == null || colorField.getText().trim().isEmpty()) {
            showMessage("La couleur est obligatoire.", true);
            return false;
        }
        if (iconField.getText().trim().length() > 50) {
            showMessage("Icone: maximum 50 caracteres.", true);
            return false;
        }
        if (colorField.getText().trim().length() > 20) {
            showMessage("Couleur: maximum 20 caracteres.", true);
            return false;
        }
        return true;
    }

    private Category buildFromForm() {
        Category c = new Category();
        c.setName(nameField.getText().trim());
        c.setDescription(descriptionArea.getText().trim());
        c.setIcon(iconField.getText().trim());
        c.setColor(colorField.getText().trim());
        return c;
    }

    private void clearForm() {
        if (nameField != null) nameField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        if (iconField != null) iconField.clear();
        if (colorField != null) colorField.clear();
    }

    private void updateCount() {
        if (totalCategoriesLabel != null) {
            totalCategoriesLabel.setText(categoryList.size() + " categorie(s)");
        }
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

    @FXML
    private void handleBackToShop(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT);
    }

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
