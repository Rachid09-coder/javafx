package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.dao.jdbc.JdbcModuleDao;
import com.edusmart.model.Course;
import com.edusmart.model.Module;
import com.edusmart.service.CourseService;
import com.edusmart.service.ModuleService;
import com.edusmart.service.impl.CourseServiceImpl;
import com.edusmart.service.impl.ModuleServiceImpl;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur du formulaire Cours (dialog séparé).
 * Utilisé aussi bien pour Ajouter que pour Modifier.
 */
public class CourseFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField titleField;
    @FXML private Label titleError;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private Label priceError;
    @FXML private TextField coefficientField;
    @FXML private Label coefficientError;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<Module> moduleComboBox;
    @FXML private Label globalError;

    private final CourseService courseService = new CourseServiceImpl(new JdbcCourseDao());
    private final ModuleService moduleService = new ModuleServiceImpl(new JdbcModuleDao());

    private Course courseToEdit;
    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusComboBox.setItems(FXCollections.observableArrayList("ACTIVE", "DRAFT", "INACTIVE", "ARCHIVED"));
        statusComboBox.setValue("ACTIVE");
        setupModuleCombo();
        // Validate price live
        priceField.textProperty().addListener((obs, o, n) -> clearError(priceField, priceError));
        titleField.textProperty().addListener((obs, o, n) -> clearError(titleField, titleError));
        // Auto-update mention preview is not needed here, but validate coeff live
        coefficientField.textProperty().addListener((obs, o, n) -> clearError(coefficientField, coefficientError));
    }

    private void setupModuleCombo() {
        moduleComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Module m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? null : m.getTitle());
            }
        });
        moduleComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Module m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? null : m.getTitle());
            }
        });
        List<Module> items = new ArrayList<>();
        Module none = new Module(); none.setId(0); none.setTitle("(Aucun module)");
        items.add(none);
        try { items.addAll(moduleService.getAllModules()); } catch (Exception ignored) {}
        moduleComboBox.setItems(FXCollections.observableArrayList(items));
        moduleComboBox.setValue(items.get(0));
    }

    /** Prépare le formulaire en mode ajout. */
    public void setAddMode() {
        titleLabel.setText("Nouveau Cours");
        courseToEdit = null;
    }

    /** Prépare le formulaire en mode modification. */
    public void setEditMode(Course course) {
        titleLabel.setText("Modifier le Cours");
        courseToEdit = course;
        titleField.setText(course.getTitle());
        descriptionArea.setText(course.getDescription() != null ? course.getDescription() : "");
        priceField.setText(String.valueOf(course.getPrice()));
        coefficientField.setText(course.getCoefficient() != null ? String.valueOf(course.getCoefficient()) : "");
        if (course.getStatusValue() != null) statusComboBox.setValue(course.getStatusValue().toUpperCase());
        // Set module
        if (course.getModuleId() != null) {
            moduleComboBox.getItems().stream()
                .filter(m -> m.getId() == course.getModuleId())
                .findFirst()
                .ifPresent(moduleComboBox::setValue);
        }
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave(ActionEvent e) {
        if (!validateForm()) return;
        try {
            Course course = buildCourse();
            boolean ok;
            if (courseToEdit == null) {
                ok = courseService.createCourse(course);
            } else {
                course.setId(courseToEdit.getId());
                course.setCreatedAt(courseToEdit.getCreatedAt());
                course.setThumbnailPath(courseToEdit.getThumbnailPath());
                course.setPdfPath(courseToEdit.getPdfPath());
                ok = courseService.updateCourse(course);
            }
            if (ok) {
                saved = true;
                closeStage();
            } else {
                showGlobalError("Opération échouée. Vérifiez les données.");
            }
        } catch (IllegalArgumentException ex) {
            showGlobalError(ex.getMessage());
        } catch (Exception ex) {
            showGlobalError("Erreur : " + rootCause(ex));
        }
    }

    @FXML
    private void handleCancel(ActionEvent e) {
        closeStage();
    }

    // ── Validation ────────────────────────────────────────────────────────
    private boolean validateForm() {
        boolean valid = true;
        // Titre
        if (titleField.getText().trim().isEmpty()) {
            showFieldError(titleField, titleError, "Le titre est obligatoire.");
            valid = false;
        } else if (titleField.getText().trim().length() < 3) {
            showFieldError(titleField, titleError, "Minimum 3 caractères.");
            valid = false;
        }
        // Prix
        String priceText = priceField.getText().trim();
        if (priceText.isEmpty()) {
            showFieldError(priceField, priceError, "Le prix est obligatoire.");
            valid = false;
        } else {
            try {
                double p = Double.parseDouble(priceText);
                if (p < 0) { showFieldError(priceField, priceError, "Le prix doit être ≥ 0."); valid = false; }
            } catch (NumberFormatException ex) {
                showFieldError(priceField, priceError, "Nombre invalide."); valid = false;
            }
        }
        // Coefficient (optionnel)
        String coeffText = coefficientField.getText().trim();
        if (!coeffText.isEmpty()) {
            try {
                double c = Double.parseDouble(coeffText);
                if (c <= 0) { showFieldError(coefficientField, coefficientError, "Le coefficient doit être > 0."); valid = false; }
            } catch (NumberFormatException ex) {
                showFieldError(coefficientField, coefficientError, "Nombre invalide."); valid = false;
            }
        }
        return valid;
    }

    private Course buildCourse() {
        Course c = new Course();
        c.setTitle(titleField.getText().trim());
        c.setDescription(descriptionArea.getText().trim());
        c.setPrice(Double.parseDouble(priceField.getText().trim()));
        String coeff = coefficientField.getText().trim();
        c.setCoefficient(coeff.isEmpty() ? null : Double.parseDouble(coeff));
        c.setStatusValue(statusComboBox.getValue());
        c.setCreatedAt(LocalDateTime.now());
        Module mod = moduleComboBox.getValue();
        c.setModuleId(mod != null && mod.getId() != 0 ? mod.getId() : null);
        return c;
    }

    // ── UI helpers ────────────────────────────────────────────────────────
    private void showFieldError(TextField field, Label errorLabel, String msg) {
        field.setStyle("-fx-border-color: #EF4444; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8;");
        errorLabel.setText(msg); errorLabel.setVisible(true); errorLabel.setManaged(true);
        hideGlobalError();
    }

    private void clearError(TextField field, Label errorLabel) {
        field.setStyle("");
        errorLabel.setVisible(false); errorLabel.setManaged(false);
    }

    private void showGlobalError(String msg) {
        globalError.setText(msg); globalError.setVisible(true); globalError.setManaged(true);
    }

    private void hideGlobalError() {
        globalError.setVisible(false); globalError.setManaged(false);
    }

    private void closeStage() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : "Erreur inconnue";
    }

    // ── Static factory method ─────────────────────────────────────────────
    /**
     * Ouvre le formulaire Cours dans une fenêtre modale.
     * @param owner la fenêtre parente
     * @param course null pour Ajouter, non-null pour Modifier
     * @return true si l'utilisateur a enregistré
     */
    public static boolean openDialog(Stage owner, Course course) {
        try {
            URL fxmlUrl = CourseFormController.class.getResource("/fxml/teacher/course-form.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();
            CourseFormController ctrl = loader.getController();
            if (course == null) ctrl.setAddMode();
            else ctrl.setEditMode(course);

            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(course == null ? "Ajouter un cours" : "Modifier le cours");
            stage.setResizable(false);
            URL cssUrl = CourseFormController.class.getResource("/css/style.css");
            Scene scene = new Scene(root);
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            return ctrl.isSaved();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
