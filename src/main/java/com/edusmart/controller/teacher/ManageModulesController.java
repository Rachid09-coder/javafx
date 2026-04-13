package com.edusmart.controller.teacher;

import com.edusmart.model.Module;
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
 * ManageModulesController - Teacher interface for managing course modules.
 *
 * Team member: Implement CRUD operations via service layer.
 */
public class ManageModulesController implements Initializable {

    @FXML private TableView<Module> modulesTable;
    @FXML private TableColumn<Module, Integer> idColumn;
    @FXML private TableColumn<Module, String> titleColumn;
    @FXML private TableColumn<Module, Integer> courseIdColumn;
    @FXML private TableColumn<Module, Integer> orderColumn;
    @FXML private TableColumn<Module, Integer> durationColumn;

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> courseComboBox;
    @FXML private Spinner<Integer> orderSpinner;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;

    private ObservableList<Module> moduleList = FXCollections.observableArrayList();
    private Module selectedModule;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupForm();
        loadModules();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (courseIdColumn != null) courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        if (orderColumn != null) orderColumn.setCellValueFactory(new PropertyValueFactory<>("orderIndex"));
        if (durationColumn != null) durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationHours"));
        if (modulesTable != null) {
            modulesTable.setItems(moduleList);
            modulesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void setupForm() {
        // TODO: Populate courseComboBox from CourseService.getAllCourses()
        if (courseComboBox != null) {
            courseComboBox.getItems().addAll("Introduction à Java", "Mathématiques Avancées", "Physique Quantique");
        }
        if (orderSpinner != null) orderSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        if (durationSpinner != null) durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 2));
    }

    /**
     * Loads all modules.
     * TODO: Replace with service call.
     */
    private void loadModules() {
        // TODO: moduleList.setAll(ModuleService.getAllModules());

        moduleList.setAll(
            new Module(1, "Introduction et installation", 1, 1, 3),
            new Module(2, "Variables et types de données", 1, 2, 4),
            new Module(3, "Structures de contrôle", 1, 3, 5),
            new Module(4, "Algèbre linéaire", 2, 1, 6)
        );
    }

    private void populateForm(Module module) {
        selectedModule = module;
        if (module == null) return;
        if (titleField != null) titleField.setText(module.getTitle());
        if (descriptionArea != null) descriptionArea.setText(module.getDescription());
        if (orderSpinner != null) orderSpinner.getValueFactory().setValue(module.getOrderIndex());
        if (durationSpinner != null) durationSpinner.getValueFactory().setValue(module.getDurationHours());
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        // TODO: ModuleService.createModule(buildModuleFromForm());
        showMessage("Module créé avec succès!", false);
        clearForm();
        loadModules();
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedModule == null) { showMessage("Sélectionnez un module.", true); return; }
        if (!validateForm()) return;
        // TODO: ModuleService.updateModule(selectedModule.getId(), buildModuleFromForm());
        showMessage("Module mis à jour!", false);
        loadModules();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedModule == null) { showMessage("Sélectionnez un module.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer le module \"" + selectedModule.getTitle() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // TODO: ModuleService.deleteModule(selectedModule.getId());
                moduleList.remove(selectedModule);
                clearForm();
            }
        });
    }

    @FXML
    private void handleClear(ActionEvent event) { clearForm(); selectedModule = null; }

    private boolean validateForm() {
        if (titleField != null && titleField.getText().trim().isEmpty()) {
            showMessage("Le titre du module est obligatoire.", true);
            return false;
        }
        return true;
    }

    private void clearForm() {
        if (titleField != null) titleField.clear();
        if (descriptionArea != null) descriptionArea.clear();
    }

    private void showMessage(String message, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    public ObservableList<Module> getModuleList() {
        return moduleList;
    }
}
