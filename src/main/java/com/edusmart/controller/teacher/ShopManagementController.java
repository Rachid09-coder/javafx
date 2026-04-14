package com.edusmart.controller.teacher;

import com.edusmart.model.Product;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ShopManagementController - Teacher interface for managing the shop/boutique.
 *
 * Team member: Implement CRUD operations via service layer.
 */
public class ShopManagementController implements Initializable {

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Product.Category> categoryColumn;

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private Spinner<Integer> stockSpinner;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private Label messageLabel;
    @FXML private Label totalProductsLabel;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private Product selectedProduct;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupForm();
        loadProducts();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (priceColumn != null) priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (stockColumn != null) stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        if (categoryColumn != null) categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        if (productsTable != null) {
            productsTable.setItems(productList);
            productsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void setupForm() {
        if (categoryComboBox != null)
            categoryComboBox.getItems().addAll("BOOK", "SOFTWARE", "COURSE_MATERIAL", "EQUIPMENT", "OTHER");
        if (stockSpinner != null)
            stockSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 1));
    }

    /**
     * Loads all products in the shop.
     * TODO: Replace with service call.
     */
    private void loadProducts() {
        // TODO: productList.setAll(ProductService.getAllProducts());

        productList.setAll(
            new Product(1, "Java pour débutants", 29.99, 50, Product.Category.BOOK),
            new Product(2, "Cahier de Mathématiques", 12.50, 100, Product.Category.COURSE_MATERIAL),
            new Product(3, "IDE IntelliJ IDEA License", 99.00, 20, Product.Category.SOFTWARE)
        );
        updateCount();
    }

    private void populateForm(Product product) {
        selectedProduct = product;
        if (product == null) return;
        if (nameField != null) nameField.setText(product.getName());
        if (descriptionArea != null) descriptionArea.setText(product.getDescription());
        if (priceField != null) priceField.setText(String.valueOf(product.getPrice()));
        if (stockSpinner != null) stockSpinner.getValueFactory().setValue(product.getStock());
        if (categoryComboBox != null && product.getCategory() != null)
            categoryComboBox.setValue(product.getCategory().name());
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        // TODO: ProductService.createProduct(buildProductFromForm());
        showMessage("Produit créé avec succès!", false);
        clearForm();
        loadProducts();
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedProduct == null) { showMessage("Sélectionnez un produit.", true); return; }
        // TODO: ProductService.updateProduct(selectedProduct.getId(), buildProductFromForm());
        showMessage("Produit mis à jour!", false);
        loadProducts();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedProduct == null) { showMessage("Sélectionnez un produit.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer le produit \"" + selectedProduct.getName() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // TODO: ProductService.deleteProduct(selectedProduct.getId());
                productList.remove(selectedProduct);
                updateCount();
                clearForm();
            }
        });
    }

    @FXML
    private void handleClear(ActionEvent event) { clearForm(); selectedProduct = null; }

    private boolean validateForm() {
        if (nameField != null && nameField.getText().trim().isEmpty()) {
            showMessage("Le nom du produit est obligatoire.", true);
            return false;
        }
        if (priceField != null) {
            try { Double.parseDouble(priceField.getText()); }
            catch (NumberFormatException e) { showMessage("Prix invalide.", true); return false; }
        }
        return true;
    }

    private void clearForm() {
        if (nameField != null) nameField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        if (priceField != null) priceField.clear();
    }

    private void updateCount() {
        if (totalProductsLabel != null)
            totalProductsLabel.setText(productList.size() + " produit(s)");
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    public ObservableList<Product> getProductList() {
        return productList;
    }

    // ── Navigation handlers ──────────────────────────────────────────────

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
