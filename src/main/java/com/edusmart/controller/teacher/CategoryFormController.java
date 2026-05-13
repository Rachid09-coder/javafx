package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCategoryDao;
import com.edusmart.model.Category;
import com.edusmart.service.CategoryService;
import com.edusmart.service.impl.CategoryServiceImpl;
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

public class CategoryFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private Label nameError;
    @FXML private TextArea descriptionArea;
    @FXML private TextField iconField;
    @FXML private TextField colorField;
    @FXML private Label globalError;

    private final CategoryService categoryService = new CategoryServiceImpl(new JdbcCategoryDao());

    private Category categoryToEdit;
    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nameField.textProperty().addListener((o, ov, nv) -> clearError(nameField, nameError));
    }

    public void setAddMode() { titleLabel.setText("Nouvelle Catégorie"); categoryToEdit = null; }

    public void setEditMode(Category cat) {
        titleLabel.setText("Modifier la Catégorie");
        categoryToEdit = cat;
        nameField.setText(cat.getName() != null ? cat.getName() : "");
        descriptionArea.setText(cat.getDescription() != null ? cat.getDescription() : "");
        iconField.setText(cat.getIcon() != null ? cat.getIcon() : "");
        colorField.setText(cat.getColor() != null ? cat.getColor() : "");
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave(ActionEvent e) {
        if (!validateForm()) return;
        try {
            Category cat = buildCategory();
            boolean ok;
            if (categoryToEdit == null) ok = categoryService.createCategory(cat);
            else { cat.setId(categoryToEdit.getId()); ok = categoryService.updateCategory(cat); }
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
        } else if (nameField.getText().trim().length() < 2) {
            showFieldError(nameField, nameError, "Minimum 2 caractères."); valid = false;
        }
        return valid;
    }

    private Category buildCategory() {
        return new Category(0, nameField.getText().trim(),
                descriptionArea.getText().trim(),
                iconField.getText().trim(), colorField.getText().trim());
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

    public static boolean openDialog(Stage owner, Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(CategoryFormController.class.getResource("/fxml/teacher/category-form.fxml"));
            javafx.scene.Parent root = loader.load();
            CategoryFormController ctrl = loader.getController();
            if (category == null) ctrl.setAddMode(); else ctrl.setEditMode(category);
            Stage stage = new Stage();
            stage.initOwner(owner); stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(category == null ? "Ajouter une catégorie" : "Modifier la catégorie");
            stage.setResizable(false);
            Scene scene = new Scene(root);
            URL css = CategoryFormController.class.getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene); stage.showAndWait();
            return ctrl.isSaved();
        } catch (IOException ex) { ex.printStackTrace(); return false; }
    }
}
