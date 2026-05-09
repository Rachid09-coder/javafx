package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCategoryDao;
import com.edusmart.dao.jdbc.JdbcProductDao;
import com.edusmart.model.Category;
import com.edusmart.model.Product;
import com.edusmart.service.CategoryService;
import com.edusmart.service.ProductService;
import com.edusmart.service.impl.CategoryServiceImpl;
import com.edusmart.service.impl.ProductServiceImpl;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Teacher CRUD for the {@code product} table (boutique).
 */
public class ShopManagementController implements Initializable {

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> imageColumn;
    @FXML private TableColumn<Product, String> categoryNameColumn;

    @FXML private TextField searchField;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField imageField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private Label messageLabel;
    @FXML private Label totalProductsLabel;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoriesForCombo = FXCollections.observableArrayList();
    private final Map<Integer, String> categoryNameById = new HashMap<>();
    private Product selectedProduct;
    private final ProductService productService = new ProductServiceImpl(new JdbcProductDao());
    private final CategoryService categoryService = new CategoryServiceImpl(new JdbcCategoryDao());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupCategoryCombo();
        loadCategories();
        setupTable();
        loadProducts();
    }

    private void setupCategoryCombo() {
        if (categoryComboBox == null) return;
        categoryComboBox.setItems(categoriesForCombo);
        categoryComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        categoryComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    /** Reload categories (names map + combo) — call before listing products so labels stay correct. */
    private void loadCategories() {
        try {
            List<Category> all = categoryService.getAllCategories();
            categoriesForCombo.setAll(all);
            categoryNameById.clear();
            for (Category c : all) {
                categoryNameById.put(c.getId(), c.getName());
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement categories: " + rootCauseMessage(ex), true);
        }
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (priceColumn != null) priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (stockColumn != null) stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        if (imageColumn != null) {
            imageColumn.setCellValueFactory(data -> {
                String img = data.getValue().getImage();
                return new SimpleStringProperty(img != null ? img : "-");
            });
        }
        if (categoryNameColumn != null) {
            categoryNameColumn.setCellValueFactory(data -> {
                int cid = data.getValue().getCategoryId();
                String n = categoryNameById.get(cid);
                return new SimpleStringProperty(n != null ? n : ("#" + cid));
            });
        }
        if (productsTable != null) {
            productsTable.setItems(productList);
            productsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void loadProducts() {
        loadCategories();
        try {
            productList.setAll(productService.getAllProducts());
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement produits: " + rootCauseMessage(ex), true);
        }
        if (productsTable != null) {
            productsTable.refresh();
        }
        updateCount();
    }

    private void populateForm(Product product) {
        selectedProduct = product;
        if (product == null) return;
        if (nameField != null) nameField.setText(product.getName());
        if (priceField != null) priceField.setText(String.valueOf(product.getPrice()));
        if (stockField != null) stockField.setText(String.valueOf(product.getStock()));
        if (imageField != null) imageField.setText(product.getImage() != null ? product.getImage() : "");
        if (categoryComboBox != null) {
            Category match = categoriesForCombo.stream()
                    .filter(c -> c.getId() == product.getCategoryId())
                    .findFirst()
                    .orElse(null);
            categoryComboBox.setValue(match);
        }
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        try {
            if (productService.createProduct(buildProductFromForm(false))) {
                showMessage("Produit cree avec succes!", false);
                clearForm();
                selectedProduct = null;
                if (productsTable != null) productsTable.getSelectionModel().clearSelection();
                loadProducts();
            } else {
                showMessage("Creation du produit echouee.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur creation: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedProduct == null) {
            showMessage("Selectionnez un produit.", true);
            return;
        }
        if (!validateForm()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la modification du produit \"" + selectedProduct.getName() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    Product p = buildProductFromForm(true);
                    p.setId(selectedProduct.getId());
                    if (productService.updateProduct(p)) {
                        showMessage("Produit mis a jour!", false);
                        loadProducts();
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
        if (selectedProduct == null) {
            showMessage("Selectionnez un produit.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le produit \"" + selectedProduct.getName() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (productService.deleteProduct(selectedProduct.getId())) {
                        productList.remove(selectedProduct);
                        clearForm();
                        selectedProduct = null;
                        updateCount();
                        showMessage("Produit supprime.", false);
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
        selectedProduct = null;
        if (productsTable != null) productsTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        loadCategories();
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) {
            loadProducts();
            return;
        }
        List<Product> filtered = productService.getAllProducts().stream()
                .filter(p -> {
                    if (p.getName() != null && p.getName().toLowerCase().contains(query)) {
                        return true;
                    }
                    String catName = categoryNameById.get(p.getCategoryId());
                    return catName != null && catName.toLowerCase().contains(query);
                })
                .collect(Collectors.toList());
        productList.setAll(filtered);
        if (productsTable != null) {
            productsTable.refresh();
        }
        updateCount();
    }

    private boolean validateForm() {
        if (nameField == null || nameField.getText().trim().isEmpty()) {
            showMessage("Le nom du produit est obligatoire.", true);
            return false;
        }
        try {
            double price = Double.parseDouble(priceField.getText().trim().replace(",", "."));
            if (price < 0) {
                showMessage("Le prix doit etre positif ou nul.", true);
                return false;
            }
        } catch (NumberFormatException | NullPointerException e) {
            showMessage("Prix invalide.", true);
            return false;
        }
        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                showMessage("Le stock doit etre positif ou nul.", true);
                return false;
            }
        } catch (NumberFormatException | NullPointerException e) {
            showMessage("Stock invalide (entier).", true);
            return false;
        }
        if (categoryComboBox == null || categoryComboBox.getValue() == null) {
            showMessage("Selectionnez une categorie.", true);
            return false;
        }
        if (categoriesForCombo.isEmpty()) {
            showMessage("Aucune categorie en base. Creez-en une via le bouton Categories.", true);
            return false;
        }
        return true;
    }

    private Product buildProductFromForm(boolean forUpdate) {
        Product p = new Product();
        p.setName(nameField.getText().trim());
        p.setPrice(Double.parseDouble(priceField.getText().trim().replace(",", ".")));
        p.setStock(Integer.parseInt(stockField.getText().trim()));
        String img = imageField != null ? imageField.getText().trim() : "";
        p.setImage(img.isEmpty() ? null : img);
        p.setCategoryId(categoryComboBox.getValue().getId());
        return p;
    }

    private void clearForm() {
        if (nameField != null) nameField.clear();
        if (priceField != null) priceField.clear();
        if (stockField != null) stockField.clear();
        if (imageField != null) imageField.clear();
        if (categoryComboBox != null) categoryComboBox.setValue(null);
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

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : throwable.getMessage();
    }

    public ObservableList<Product> getProductList() {
        return productList;
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

    /**
     * Opens the category CRUD screen (liste + formulaire).
     */
    @FXML
    private void handleManageCategories(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CATEGORY_MANAGEMENT);
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
