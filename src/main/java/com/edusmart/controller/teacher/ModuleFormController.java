package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.dao.jdbc.JdbcModuleDao;
import com.edusmart.model.Course;
import com.edusmart.model.Module;
import com.edusmart.service.ModuleService;
import com.edusmart.service.CourseService;
import com.edusmart.service.impl.ModuleServiceImpl;
import com.edusmart.service.impl.CourseServiceImpl;
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
 * Contrôleur du formulaire Module (dialog séparé).
 */
public class ModuleFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField titleField;
    @FXML private Label titleError;
    @FXML private TextArea descriptionArea;
    @FXML private TextField durationField;
    @FXML private Label durationError;
    @FXML private ComboBox<Course> courseComboBox;
    @FXML private Label globalError;

    private final ModuleService moduleService = new ModuleServiceImpl(new JdbcModuleDao());
    private final CourseService courseService = new CourseServiceImpl(new JdbcCourseDao());

    private Module moduleToEdit;
    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCourseCombo();
        titleField.textProperty().addListener((o, ov, nv) -> clearError(titleField, titleError));
        durationField.textProperty().addListener((o, ov, nv) -> clearError(durationField, durationError));
    }

    private void setupCourseCombo() {
        courseComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Course c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getTitle());
            }
        });
        courseComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Course c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getTitle());
            }
        });
        List<Course> items = new ArrayList<>();
        Course none = new Course(); none.setId(0); none.setTitle("(Aucun cours)");
        items.add(none);
        try { items.addAll(courseService.getAllCourses()); } catch (Exception ignored) {}
        courseComboBox.setItems(FXCollections.observableArrayList(items));
        courseComboBox.setValue(items.get(0));
    }

    public void setAddMode() {
        titleLabel.setText("Nouveau Module");
        moduleToEdit = null;
    }

    public void setEditMode(Module module) {
        titleLabel.setText("Modifier le Module");
        moduleToEdit = module;
        titleField.setText(module.getTitle());
        descriptionArea.setText(module.getDescription() != null ? module.getDescription() : "");
        durationField.setText(module.getDurationHours() > 0 ? String.valueOf(module.getDurationHours()) : "");
        if (module.getCourseId() > 0) {
            courseComboBox.getItems().stream()
                .filter(c -> c.getId() == module.getCourseId())
                .findFirst().ifPresent(courseComboBox::setValue);
        }
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave(ActionEvent e) {
        if (!validateForm()) return;
        try {
            Module mod = buildModule();
            boolean ok;
            if (moduleToEdit == null) {
                ok = moduleService.createModule(mod);
            } else {
                mod.setId(moduleToEdit.getId());
                ok = moduleService.updateModule(mod);
            }
            if (ok) { saved = true; closeStage(); }
            else showGlobalError("Opération échouée.");
        } catch (IllegalArgumentException ex) {
            showGlobalError(ex.getMessage());
        } catch (Exception ex) {
            showGlobalError("Erreur : " + rootCause(ex));
        }
    }

    @FXML
    private void handleCancel(ActionEvent e) { closeStage(); }

    private boolean validateForm() {
        boolean valid = true;
        if (titleField.getText().trim().isEmpty()) {
            showFieldError(titleField, titleError, "Le titre est obligatoire."); valid = false;
        } else if (titleField.getText().trim().length() < 2) {
            showFieldError(titleField, titleError, "Minimum 2 caractères."); valid = false;
        }
        String dur = durationField.getText().trim();
        if (!dur.isEmpty()) {
            try {
                int d = Integer.parseInt(dur);
                if (d < 0) { showFieldError(durationField, durationError, "Durée doit être ≥ 0."); valid = false; }
            } catch (NumberFormatException ex) {
                showFieldError(durationField, durationError, "Nombre entier invalide."); valid = false;
            }
        }
        return valid;
    }

    private Module buildModule() {
        Module m = new Module();
        m.setTitle(titleField.getText().trim());
        m.setDescription(descriptionArea.getText().trim());
        String dur = durationField.getText().trim();
        m.setDurationHours(dur.isEmpty() ? 0 : Integer.parseInt(dur));
        m.setCreatedAt(LocalDateTime.now());
        Course c = courseComboBox.getValue();
        m.setCourseId(c != null && c.getId() != 0 ? c.getId() : 0);
        return m;
    }

    private void showFieldError(TextField f, Label l, String msg) {
        f.setStyle("-fx-border-color:#EF4444;-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;");
        l.setText(msg); l.setVisible(true); l.setManaged(true);
    }
    private void clearError(TextField f, Label l) {
        f.setStyle(""); l.setVisible(false); l.setManaged(false);
    }
    private void showGlobalError(String msg) {
        globalError.setText(msg); globalError.setVisible(true); globalError.setManaged(true);
    }
    private void closeStage() { ((Stage) titleLabel.getScene().getWindow()).close(); }
    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : "Erreur inconnue";
    }

    public static boolean openDialog(Stage owner, Module module) {
        try {
            URL fxmlUrl = ModuleFormController.class.getResource("/fxml/teacher/module-form.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();
            ModuleFormController ctrl = loader.getController();
            if (module == null) ctrl.setAddMode();
            else ctrl.setEditMode(module);
            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(module == null ? "Ajouter un module" : "Modifier le module");
            stage.setResizable(false);
            Scene scene = new Scene(root);
            URL cssUrl = ModuleFormController.class.getResource("/css/style.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            return ctrl.isSaved();
        } catch (IOException ex) { ex.printStackTrace(); return false; }
    }
}
