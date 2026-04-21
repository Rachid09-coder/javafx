package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCategoryDao;
import com.edusmart.dao.jdbc.JdbcProductDao;
import com.edusmart.model.Category;
import com.edusmart.model.Product;
import com.edusmart.service.CategoryService;
import com.edusmart.service.ProductService;
import com.edusmart.service.impl.CategoryServiceImpl;
import com.edusmart.service.impl.ProductServiceImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProductFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private Label nameError;
    @FXML private TextField priceField;
    @FXML private Label priceError;
    @FXML private TextField stockField;
    @FXML private Label stockError;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private Label categoryError;
    @FXML private TextField imageField;
    @FXML private Label globalError;

    private final ProductService productService = new ProductServiceImpl(new JdbcProductDao());
    private final CategoryService categoryService = new CategoryServiceImpl(new JdbcCategoryDao());

    private Product productToEdit;
    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCategoryCombo();
        nameField.textProperty().addListener((o, ov, nv) -> clearError(nameField, nameError));
        priceField.textProperty().addListener((o, ov, nv) -> clearError(priceField, priceError));
        stockField.textProperty().addListener((o, ov, nv) -> clearError(stockField, stockError));
    }

    private void setupCategoryCombo() {
        categoryComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName());
            }
        });
        categoryComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName());
            }
        });
        List<Category> cats = new ArrayList<>();
        try { cats.addAll(categoryService.getAllCategories()); } catch (Exception ignored) {}
        categoryComboBox.setItems(FXCollections.observableArrayList(cats));
    }

    public void setAddMode() { titleLabel.setText("Nouveau Produit"); productToEdit = null; }

    public void setEditMode(Product product) {
        titleLabel.setText("Modifier le Produit");
        productToEdit = product;
        nameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        stockField.setText(String.valueOf(product.getStock()));
        imageField.setText(product.getImage() != null ? product.getImage() : "");
        categoryComboBox.getItems().stream()
            .filter(c -> c.getId() == product.getCategoryId())
            .findFirst().ifPresent(categoryComboBox::setValue);
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave(ActionEvent e) {
        if (!validateForm()) return;
        try {
            Product p = buildProduct();
            boolean ok;
            if (productToEdit == null) ok = productService.createProduct(p);
            else { p.setId(productToEdit.getId()); ok = productService.updateProduct(p); }
            if (ok) { saved = true; closeStage(); }
            else showGlobalError("Opération échouée.");
        } catch (IllegalArgumentException ex) { showGlobalError(ex.getMessage()); }
        catch (Exception ex) { showGlobalError("Erreur : " + rootCause(ex)); }
    }

    @FXML
    private void handleCancel(ActionEvent e) { closeStage(); }

    private boolean validateForm() {
        boolean valid = true;
        if (nameField.getText().trim().isEmpty()) {
            showFieldError(nameField, nameError, "Nom obligatoire."); valid = false;
        }
        String priceText = priceField.getText().trim();
        if (priceText.isEmpty()) {
            showFieldError(priceField, priceError, "Prix obligatoire."); valid = false;
        } else { try { double p = Double.parseDouble(priceText); if (p < 0) { showFieldError(priceField, priceError, "Prix doit être ≥ 0."); valid = false; } } catch (NumberFormatException ex) { showFieldError(priceField, priceError, "Nombre invalide."); valid = false; } }
        String stockText = stockField.getText().trim();
        if (stockText.isEmpty()) {
            showFieldError(stockField, stockError, "Stock obligatoire."); valid = false;
        } else { try { int s = Integer.parseInt(stockText); if (s < 0) { showFieldError(stockField, stockError, "Stock doit être ≥ 0."); valid = false; } } catch (NumberFormatException ex) { showFieldError(stockField, stockError, "Entier invalide."); valid = false; } }
        if (categoryComboBox.getValue() == null) {
            categoryError.setText("Catégorie obligatoire."); categoryError.setVisible(true); categoryError.setManaged(true);
            categoryComboBox.setStyle("-fx-border-color:#EF4444;"); valid = false;
        }
        return valid;
    }

    private Product buildProduct() {
        Category cat = categoryComboBox.getValue();
        return new Product(0, nameField.getText().trim(),
                Double.parseDouble(priceField.getText().trim()),
                Integer.parseInt(stockField.getText().trim()),
                imageField.getText().trim(),
                cat != null ? cat.getId() : 0);
    }

    private void showFieldError(TextField f, Label l, String msg) {
        f.setStyle("-fx-border-color:#EF4444;-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;");
        l.setText(msg); l.setVisible(true); l.setManaged(true);
    }
    private void clearError(TextField f, Label l) { f.setStyle(""); l.setVisible(false); l.setManaged(false); }
    private void showGlobalError(String msg) { globalError.setText(msg); globalError.setVisible(true); globalError.setManaged(true); }
    private void closeStage() { ((Stage) titleLabel.getScene().getWindow()).close(); }
    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : "Erreur";
    }

    public static boolean openDialog(Stage owner, Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(ProductFormController.class.getResource("/fxml/teacher/product-form.fxml"));
            javafx.scene.Parent root = loader.load();
            ProductFormController ctrl = loader.getController();
            if (product == null) ctrl.setAddMode(); else ctrl.setEditMode(product);
            Stage stage = new Stage();
            stage.initOwner(owner); stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(product == null ? "Ajouter un produit" : "Modifier le produit");
            stage.setResizable(false);
            Scene scene = new Scene(root);
            URL css = ProductFormController.class.getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene); stage.showAndWait();
            return ctrl.isSaved();
        } catch (IOException ex) { ex.printStackTrace(); return false; }
    }
}
