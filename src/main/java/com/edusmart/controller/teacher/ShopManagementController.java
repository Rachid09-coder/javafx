package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcProductDao;
import com.edusmart.dao.jdbc.JdbcCategoryDao;
import com.edusmart.model.Product;
import com.edusmart.model.Category;
import com.edusmart.service.ProductService;
import com.edusmart.service.CategoryService;
import com.edusmart.service.impl.ProductServiceImpl;
import com.edusmart.service.impl.CategoryServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ShopManagementController implements Initializable {

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> categoryColumn;

    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    private final ProductService productService = new ProductServiceImpl(new JdbcProductDao());
    private final CategoryService categoryService = new CategoryServiceImpl(new JdbcCategoryDao());

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private Map<Integer, String> categoryNames = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (priceColumn != null) priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (stockColumn != null) stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        if (categoryColumn != null) {
            categoryColumn.setCellValueFactory(cd -> {
                int cId = cd.getValue().getCategoryId();
                return new SimpleStringProperty(categoryNames.getOrDefault(cId, "Catégorie #" + cId));
            });
        }
        if (productsTable != null) productsTable.setItems(productList);
    }

    private void loadData() {
        try {
            categoryNames = categoryService.getAllCategories().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a,b)->a));
            productList.setAll(productService.getAllProducts());
            if (productsTable != null) productsTable.refresh();
        } catch(Exception ex) { showMessage("Erreur chargement: " + rootCause(ex), true); }
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        Stage owner = (Stage) productsTable.getScene().getWindow();
        if (ProductFormController.openDialog(owner, null)) {
            loadData();
            showMessage("Produit ajouté.", false);
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        Product sel = productsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez un produit.", true); return; }
        Stage owner = (Stage) productsTable.getScene().getWindow();
        if (ProductFormController.openDialog(owner, sel)) {
            loadData();
            showMessage("Produit modifié.", false);
        }
    }

    @FXML
    private void handleDelete(ActionEvent e) {
        Product sel = productsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez un produit.", true); return; }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer produit ?", ButtonType.YES, ButtonType.NO);
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    if (productService.deleteProduct(sel.getId())) {
                        productList.remove(sel); showMessage("Supprimé.", false);
                    } else showMessage("Échec.", true);
                } catch (Exception ex) { showMessage("Erreur: "+rootCause(ex), true); }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent e) {
        String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (q.isEmpty()) { loadData(); return; }
        List<Product> filtered = productService.getAllProducts().stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
        productList.setAll(filtered);
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
