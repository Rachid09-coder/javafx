package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcModuleDao;
import com.edusmart.model.Module;
import com.edusmart.service.ModuleService;
import com.edusmart.service.impl.ModuleServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Teacher UI for CRUD on the {@code module} table (id, title, description, thumbnail, created_at).
 */
public class ManageModulesController implements Initializable {

    @FXML private TableView<Module> modulesTable;
    @FXML private TableColumn<Module, Integer> idColumn;
    @FXML private TableColumn<Module, String> titleColumn;
    @FXML private TableColumn<Module, String> thumbnailColumn;
    @FXML private TableColumn<Module, String> createdAtColumn;

    @FXML private TextField titleField;
    @FXML private TextField thumbnailField;
    @FXML private TextArea descriptionArea;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;
    @FXML private VBox formPanel;
    @FXML private Label formPanelTitle;
    @FXML private Label countLabel;

    private ObservableList<Module> moduleList = FXCollections.observableArrayList();
    private Module selectedModule;
    private final ModuleService moduleService = new ModuleServiceImpl(new JdbcModuleDao());
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadModules();
        // Panel starts hidden — slides in on demand
        if (formPanel != null) {
            formPanel.setVisible(false);
            formPanel.setManaged(false);
            formPanel.setTranslateX(350);
            formPanel.setOpacity(0);
        }
    }

    // ── Drawer helpers ──────────────────────────────────────────

    private void showFormPanel(String title) {
        if (formPanel == null) return;
        if (formPanelTitle != null) formPanelTitle.setText(title);
        formPanel.setManaged(true);
        formPanel.setVisible(true);
        TranslateTransition tt = new TranslateTransition(Duration.millis(320), formPanel);
        tt.setFromX(350);
        tt.setToX(0);
        FadeTransition ft = new FadeTransition(Duration.millis(320), formPanel);
        ft.setFromValue(0);
        ft.setToValue(1);
        tt.play();
        ft.play();
    }

    private void hideFormPanel() {
        if (formPanel == null) return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), formPanel);
        tt.setFromX(0);
        tt.setToX(350);
        FadeTransition ft = new FadeTransition(Duration.millis(280), formPanel);
        ft.setFromValue(1);
        ft.setToValue(0);
        tt.setOnFinished(e -> {
            formPanel.setVisible(false);
            formPanel.setManaged(false);
        });
        tt.play();
        ft.play();
    }

    @FXML
    private void handleOpenAdd(ActionEvent event) {
        clearForm();
        selectedModule = null;
        if (modulesTable != null) modulesTable.getSelectionModel().clearSelection();
        showFormPanel("Nouveau Module");
    }

    @FXML
    private void handleOpenEdit(ActionEvent event) {
        if (selectedModule == null) {
            showMessage("Sélectionnez un module à modifier.", true);
            return;
        }
        showFormPanel("Modifier le Module");
    }

    @FXML
    private void handleClosePanel(ActionEvent event) {
        hideFormPanel();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (thumbnailColumn != null) {
            thumbnailColumn.setCellValueFactory(cellData -> {
                String t = cellData.getValue().getThumbnail();
                return new SimpleStringProperty(t != null && !t.isBlank() ? t : "-");
            });
        }
        if (createdAtColumn != null) {
            createdAtColumn.setCellValueFactory(cellData -> {
                LocalDateTime createdAt = cellData.getValue().getCreatedAt();
                return new SimpleStringProperty(
                        createdAt != null ? createdAt.format(DATE_TIME_FORMATTER) : "-"
                );
            });
        }
        if (modulesTable != null) {
            modulesTable.setItems(moduleList);
            // Single-click row: populate form and slide in drawer
            modulesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        populateForm(newVal);
                        showFormPanel("Modifier le Module");
                    }
                });
        }
    }

    private void loadModules() {
        try {
            moduleList.setAll(moduleService.getAllModules());
            if (countLabel != null) countLabel.setText(moduleList.size() + " modules");
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement modules: " + rootCauseMessage(ex), true);
        }
    }

    private void populateForm(Module module) {
        selectedModule = module;
        if (module == null) return;
        if (titleField != null) titleField.setText(module.getTitle());
        if (thumbnailField != null) {
            thumbnailField.setText(module.getThumbnail() != null ? module.getThumbnail() : "");
        }
        if (descriptionArea != null) {
            descriptionArea.setText(module.getDescription() != null ? module.getDescription() : "");
        }
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        try {
            Module module = buildModuleFromForm(false);
            if (moduleService.createModule(module)) {
                showMessage("Module créé avec succès!", false);
                clearForm();
                loadModules();
            } else {
                showMessage("Création du module échouée.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur création module: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedModule == null) {
            showMessage("Sélectionnez un module.", true);
            return;
        }
        if (!validateForm()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la modification du module \"" + selectedModule.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    Module module = buildModuleFromForm(true);
                    if (moduleService.updateModule(module)) {
                        showMessage("Module mis à jour!", false);
                        loadModules();
                    } else {
                        showMessage("Mise à jour du module échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur mise à jour module: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedModule == null) {
            showMessage("Sélectionnez un module.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le module \"" + selectedModule.getTitle() + "\" ? "
                        + "Tous les cours associés à ce module seront supprimés, ainsi que les examens liés à ces cours.",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (moduleService.deleteModule(selectedModule.getId())) {
                        clearForm();
                        loadModules();
                        showMessage("Module supprimé.", false);
                    } else {
                        showMessage("Suppression du module échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur suppression module: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        selectedModule = null;
        if (modulesTable != null) modulesTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) {
            loadModules();
            return;
        }
        List<Module> filtered = moduleService.getAllModules().stream()
                .filter(this::matchesSearch)
                .collect(Collectors.toList());
        moduleList.setAll(filtered);
    }

    private boolean matchesSearch(Module m) {
        String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (q.isEmpty()) return true;
        return (m.getTitle() != null && m.getTitle().toLowerCase().contains(q))
                || (m.getDescription() != null && m.getDescription().toLowerCase().contains(q))
                || (m.getThumbnail() != null && m.getThumbnail().toLowerCase().contains(q));
    }

    private boolean validateForm() {
        if (titleField != null && titleField.getText().trim().isEmpty()) {
            showMessage("Le titre du module est obligatoire.", true);
            return false;
        }
        return true;
    }

    private Module buildModuleFromForm(boolean updating) {
        Module module = new Module();
        String title = titleField != null ? titleField.getText().trim() : "";
        String descRaw = descriptionArea != null ? descriptionArea.getText() : "";
        String desc = descRaw != null && !descRaw.isBlank() ? descRaw.trim() : null;
        String thumbRaw = thumbnailField != null ? thumbnailField.getText().trim() : "";
        String thumb = thumbRaw != null && !thumbRaw.isBlank() ? thumbRaw : null;

        module.setTitle(title);
        module.setDescription(desc);
        module.setThumbnail(thumb);

        if (updating && selectedModule != null) {
            module.setId(selectedModule.getId());
            module.setCreatedAt(selectedModule.getCreatedAt());
        } else {
            module.setCreatedAt(LocalDateTime.now());
        }
        return module;
    }

    private void clearForm() {
        if (titleField != null) titleField.clear();
        if (thumbnailField != null) thumbnailField.clear();
        if (descriptionArea != null) descriptionArea.clear();
    }

    private void showMessage(String message, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            if (isError) {
                messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-background-color: rgba(239,68,68,0.1); -fx-background-radius: 8; -fx-padding: 8 14;");
            } else {
                messageLabel.setStyle("-fx-text-fill: #059669; -fx-background-color: rgba(16,185,129,0.1); -fx-background-radius: 8; -fx-padding: 8 14;");
            }
            messageLabel.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(300), messageLabel);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : throwable.getMessage();
    }

    public ObservableList<Module> getModuleList() {
        return moduleList;
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
