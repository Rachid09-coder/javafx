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
import javafx.application.Platform;

import com.edusmart.util.ThemeManager;
import com.edusmart.util.ActivityLogger;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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

    @FXML private TableColumn<Module, String> tagsColumn;
    @FXML private TableColumn<Module, Integer> courseCountColumn;
    @FXML private TableColumn<Module, String> avgPriceColumn;

    @FXML private Label totalModulesStat;
    @FXML private Label totalLinkedCoursesStat;
    @FXML private Label avgModulePriceStat;
    @FXML private Label popularTagStat;
    @FXML private ComboBox<String> filterTagBox;

    @FXML private TextField titleField;
    @FXML private TextField thumbnailField;
    @FXML private TextField tagsField;
    @FXML private TextArea descriptionArea;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private VBox formPanel;
    @FXML private Label formPanelTitle;
    @FXML private Label countLabel;
    @FXML private ListView<String> activityList;

    private ObservableList<Module> moduleList = FXCollections.observableArrayList();
    private Module selectedModule;
    private final ModuleService moduleService = new ModuleServiceImpl(new com.edusmart.dao.jdbc.JdbcModuleDao());
    private final com.edusmart.service.CourseService courseService = new com.edusmart.service.impl.CourseServiceImpl(new com.edusmart.dao.jdbc.JdbcCourseDao());
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        if (sortComboBox != null) {
            sortComboBox.getItems().addAll(
                "Titre (A-Z)", "Titre (Z-A)",
                "Plus récent", "Plus ancien"
            );
            sortComboBox.setValue("Titre (A-Z)");
            sortComboBox.setOnAction(e -> applyFiltersAndSort());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFiltersAndSort());
        }
        if (filterTagBox != null) {
            filterTagBox.getItems().addAll("Tous les tags", "Programmation", "Design", "Business", "Débutant", "Avancé");
            filterTagBox.setValue("Tous les tags");
            filterTagBox.setOnAction(e -> applyFiltersAndSort());
        }
        loadModules();
        // Panel starts hidden — slides in on demand
        if (formPanel != null) {
            formPanel.setVisible(false);
            formPanel.setManaged(false);
            formPanel.setTranslateX(350);
            formPanel.setOpacity(0);
        }
        if (activityList != null) {
            activityList.setItems(ActivityLogger.getActivities());
        }
        Platform.runLater(() -> {
            if (modulesTable != null && modulesTable.getScene() != null) {
                ThemeManager.applyTheme(modulesTable.getScene());
            }
        });
    }

    @FXML
    private void handleToggleTheme(ActionEvent event) {
        ThemeManager.setDarkMode(!ThemeManager.isDarkMode());
        if (modulesTable != null && modulesTable.getScene() != null) {
            ThemeManager.applyTheme(modulesTable.getScene());
        }
        Button btn = (Button) event.getSource();
        btn.setText(ThemeManager.isDarkMode() ? "☀️ Mode Clair" : "🌙 Mode Sombre");
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
        if (tagsColumn != null) tagsColumn.setCellValueFactory(new PropertyValueFactory<>("tags"));
        if (courseCountColumn != null) {
            courseCountColumn.setCellValueFactory(new PropertyValueFactory<>("courseCount"));
            courseCountColumn.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); setText(null); return; }
                    Label l = new Label(item + " cours");
                    l.setStyle("-fx-background-color: rgba(79,70,229,0.1); -fx-text-fill: #4F46E5; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-weight: bold;");
                    setGraphic(l);
                    setText(null);
                }
            });
        }
        if (avgPriceColumn != null) {
            avgPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f €", cellData.getValue().getAveragePrice())
            ));
        }
        if (createdAtColumn != null) {
            createdAtColumn.setCellValueFactory(cellData -> {
                LocalDateTime createdAt = cellData.getValue().getCreatedAt();
                return new javafx.beans.property.SimpleStringProperty(
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
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        try {
            List<Module> all = moduleService.getAllModules();
            
            // Fetch all courses once to compute analytics per module
            List<com.edusmart.model.Course> allCourses = courseService.getAllCourses();
            
            for (Module m : all) {
                // Generate fake deterministic tags if none
                if (m.getTags() == null) {
                    String[] sampleTags = {"Programmation", "Design", "Business", "Débutant", "Avancé"};
                    m.setTags(sampleTags[m.getId() % sampleTags.length]);
                }
                
                List<com.edusmart.model.Course> mCourses = allCourses.stream()
                        .filter(c -> c.getModuleId() != null && c.getModuleId() == m.getId())
                        .collect(Collectors.toList());
                m.setCourseCount(mCourses.size());
                double avg = mCourses.stream().mapToDouble(com.edusmart.model.Course::getPrice).average().orElse(0.0);
                m.setAveragePrice(avg);
            }
            
            // Search & Tag filter
            String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
            String tagFilter = filterTagBox != null ? filterTagBox.getValue() : "Tous les tags";
            
            if (!query.isEmpty() || !"Tous les tags".equals(tagFilter)) {
                all = all.stream().filter(m -> {
                    if (!"Tous les tags".equals(tagFilter)) {
                        if (m.getTags() == null || !m.getTags().contains(tagFilter)) return false;
                    }
                    if (!query.isEmpty() && !matchesSearch(m, query)) return false;
                    return true;
                }).collect(Collectors.toList());
            }

            // Sorting
            String sortMode = sortComboBox != null ? sortComboBox.getValue() : "Titre (A-Z)";
            if (sortMode == null) sortMode = "Titre (A-Z)";
            
            java.util.Comparator<Module> cmp;
            switch (sortMode) {
                case "Titre (Z-A)":
                    cmp = java.util.Comparator.comparing((Module m) -> m.getTitle() == null ? "" : m.getTitle().toLowerCase()).reversed();
                    break;
                case "Plus récent":
                    cmp = java.util.Comparator.comparing(Module::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()));
                    break;
                case "Plus ancien":
                    cmp = java.util.Comparator.comparing(Module::getCreatedAt, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder()));
                    break;
                case "Titre (A-Z)":
                default:
                    cmp = java.util.Comparator.comparing((Module m) -> m.getTitle() == null ? "" : m.getTitle().toLowerCase());
                    break;
            }
            all.sort(cmp);
            
            moduleList.setAll(all);
            if (countLabel != null) countLabel.setText(moduleList.size() + " modules");
            updateAnalytics(all);
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement modules: " + rootCauseMessage(ex), true);
        }
    }
    
    private void updateAnalytics(List<Module> list) {
        if (totalModulesStat != null) totalModulesStat.setText(String.valueOf(list.size()));
        
        if (totalLinkedCoursesStat != null) {
            int total = list.stream().mapToInt(Module::getCourseCount).sum();
            totalLinkedCoursesStat.setText(String.valueOf(total));
        }
        
        if (avgModulePriceStat != null) {
            double avg = list.stream().filter(m -> m.getAveragePrice() > 0)
                .mapToDouble(Module::getAveragePrice).average().orElse(0.0);
            avgModulePriceStat.setText(String.format("%.2f €", avg));
        }
        
        if (popularTagStat != null) {
            Map<String, Long> tagCounts = list.stream()
                .filter(m -> m.getTags() != null)
                .flatMap(m -> java.util.Arrays.stream(m.getTags().split(",")))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
            
            String pop = tagCounts.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("Aucun");
            popularTagStat.setText(pop);
        }
    }

    private void populateForm(Module module) {
        selectedModule = module;
        if (module == null) return;
        if (titleField != null) titleField.setText(module.getTitle());
        if (thumbnailField != null) {
            thumbnailField.setText(module.getThumbnail() != null ? module.getThumbnail() : "");
        }
        if (tagsField != null) {
            tagsField.setText(module.getTags() != null ? module.getTags() : "");
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
                ActivityLogger.log("Module", "créé", module.getTitle());
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
                        ActivityLogger.log("Module", "modifié", module.getTitle());
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
        if (selectedModule.getCourseCount() > 0) {
            showMessage("Impossible de supprimer : " + selectedModule.getCourseCount() + " cours sont liés à ce module.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le module \"" + selectedModule.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (moduleService.deleteModule(selectedModule.getId())) {
                        ActivityLogger.log("Module", "supprimé", selectedModule.getTitle());
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
        applyFiltersAndSort();
    }

    @FXML
    private void handleExport(ActionEvent event) {
        try {
            java.io.File file = new java.io.File("modules_export.csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("ID,Titre,Tags,Cours,Prix Moyen,Date");
                for (Module m : moduleList) {
                    String date = m.getCreatedAt() != null ? m.getCreatedAt().format(DATE_TIME_FORMATTER) : "";
                    String tags = m.getTags() != null ? m.getTags().replace("\"", "\"\"") : "";
                    pw.printf("%d,\"%s\",\"%s\",%d,%.2f,%s%n",
                            m.getId(), m.getTitle().replace("\"", "\"\""), tags,
                            m.getCourseCount(), m.getAveragePrice(), date);
                }
            }
            showMessage("Export réussi : " + file.getAbsolutePath(), false);
        } catch (Exception e) {
            showMessage("Erreur d'exportation : " + e.getMessage(), true);
        }
    }

    private boolean matchesSearch(Module m, String q) {
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
        if (tagsField != null) module.setTags(tagsField.getText().trim());

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
        if (tagsField != null) tagsField.clear();
        if (descriptionArea != null) descriptionArea.clear();
    }

    private void showMessage(String message, boolean isError) {
        showToast(message, isError);
    }
    
    private void showToast(String message, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            if (isError) {
                messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-background-color: rgba(239,68,68,0.1); -fx-background-radius: 8; -fx-padding: 8 14;");
            } else {
                messageLabel.setStyle("-fx-text-fill: #059669; -fx-background-color: rgba(16,185,129,0.1); -fx-background-radius: 8; -fx-padding: 8 14;");
            }
            messageLabel.setVisible(true);
            messageLabel.setOpacity(0);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messageLabel);
            fadeIn.setToValue(1);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), messageLabel);
            fadeOut.setDelay(Duration.seconds(3));
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> messageLabel.setVisible(false));
            
            fadeIn.setOnFinished(e -> fadeOut.play());
            fadeIn.play();
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
