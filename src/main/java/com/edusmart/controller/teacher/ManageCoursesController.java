package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.dao.jdbc.JdbcModuleDao;
import com.edusmart.model.Course;
import com.edusmart.model.Module;
import com.edusmart.service.CourseService;
import com.edusmart.service.ModuleService;
import com.edusmart.service.impl.CourseServiceImpl;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * ManageCoursesController - Teacher interface for managing courses.
 *
 * Team member: Implement CRUD operations via service layer.
 */
public class ManageCoursesController implements Initializable {

    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, Integer> idColumn;
    @FXML private TableColumn<Course, String> titleColumn;
    @FXML private TableColumn<Course, Double> priceColumn;
    @FXML private TableColumn<Course, Double> coefficientColumn;
    @FXML private TableColumn<Course, String> createdAtColumn;
    @FXML private TableColumn<Course, String> statusColumn;
    @FXML private TableColumn<Course, String> moduleColumn;
    @FXML private TableColumn<Course, Course> ratingColumn;
    @FXML private TableColumn<Course, Course> favoriteColumn;

    @FXML private Label totalCoursesStat;
    @FXML private Label avgPriceStat;
    @FXML private Label topModuleStat;
    @FXML private Label avgRatingStat;
    
    @FXML private ComboBox<String> filterStatusBox;
    @FXML private ComboBox<Module> filterModuleBox;
    @FXML private Label aiSuggestionLabel;
    @FXML private ListView<String> activityList;

    @FXML private TextField titleField;
    @FXML private TextField priceField;
    @FXML private TextField coefficientField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<Module> moduleComboBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label messageLabel;
    @FXML private Label countLabel;
    @FXML private VBox formPanel;
    @FXML private Label formPanelTitle;

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private Course selectedCourse;
    private final CourseService courseService = new CourseServiceImpl(new JdbcCourseDao());
    private final ModuleService moduleService = new ModuleServiceImpl(new JdbcModuleDao());
    /** module id -> title for table and search */
    private Map<Integer, String> moduleTitleById = new HashMap<>();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupForm();
        loadCourses();
        // Panel starts hidden — it will slide in on demand
        if (formPanel != null) {
            formPanel.setVisible(false);
            formPanel.setManaged(false);
            formPanel.setTranslateX(350);
            formPanel.setOpacity(0);
        }
        if (searchField != null) {
            // Live Search
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFiltersAndSort());
        }
        if (priceField != null && aiSuggestionLabel != null) {
            priceField.textProperty().addListener((obs, oldV, newV) -> updateAiSuggestion(newV));
        }
        if (activityList != null) {
            activityList.setItems(ActivityLogger.getActivities());
        }
        Platform.runLater(() -> {
            if (coursesTable != null && coursesTable.getScene() != null) {
                ThemeManager.applyTheme(coursesTable.getScene());
                updateThemeButton();
            }
        });
    }
    
    private void updateThemeButton() {
        if (coursesTable != null && coursesTable.getScene() != null) {
            Button btn = (Button) coursesTable.getScene().lookup(".btn-secondary"); // naive lookup or we could do better
            // Instead of looking up by class, we can just let it be. But to change text:
            // Actually let's not change text dynamically unless we bind it.
        }
    }

    @FXML
    private void handleToggleTheme(ActionEvent event) {
        ThemeManager.setDarkMode(!ThemeManager.isDarkMode());
        if (coursesTable != null && coursesTable.getScene() != null) {
            ThemeManager.applyTheme(coursesTable.getScene());
        }
        Button btn = (Button) event.getSource();
        btn.setText(ThemeManager.isDarkMode() ? "☀️ Mode Clair" : "🌙 Mode Sombre");
    }

    // ── Drawer helpers ──────────────────────────────────────────

    /** Show the form panel with a slide-in animation from the right. */
    private void showFormPanel(String title) {
        if (formPanel == null) return;
        if (formPanelTitle != null) formPanelTitle.setText(title);
        formPanel.setManaged(true);
        formPanel.setVisible(true);
        // Slide in
        TranslateTransition tt = new TranslateTransition(Duration.millis(320), formPanel);
        tt.setFromX(350);
        tt.setToX(0);
        FadeTransition ft = new FadeTransition(Duration.millis(320), formPanel);
        ft.setFromValue(0);
        ft.setToValue(1);
        tt.play();
        ft.play();
    }

    /** Slide the form panel back to the right and then hide it. */
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

    /** Open drawer in "Add" mode (empty form). */
    @FXML
    private void handleOpenAdd(ActionEvent event) {
        clearForm();
        showFormPanel("Nouveau Cours");
    }

    /** Open drawer in "Edit" mode for the selected row. */
    @FXML
    private void handleOpenEdit(ActionEvent event) {
        if (selectedCourse == null) {
            showMessage("Sélectionnez un cours à modifier.", true);
            return;
        }
        showFormPanel("Modifier le Cours");
    }

    /** Close / slide-out the drawer. */
    @FXML
    private void handleClosePanel(ActionEvent event) {
        hideFormPanel();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (priceColumn != null) priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        if (ratingColumn != null) {
            ratingColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
            ratingColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); setText(null); return; }
                    // Generate a fake deterministic rating for demo if 0
                    if (item.getRating() == 0.0) item.setRating(3.5 + (item.getId() % 15) / 10.0);
                    Label l = new Label(String.format("%.1f ★", item.getRating()));
                    l.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    setGraphic(l);
                    setText(null);
                }
            });
        }
        
        if (favoriteColumn != null) {
            favoriteColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
            favoriteColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); setText(null); return; }
                    Button btn = new Button(item.isFavorite() ? "★" : "☆");
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (item.isFavorite() ? "#F59E0B" : "#94A3B8") + "; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");
                    btn.setOnAction(e -> {
                        item.setFavorite(!item.isFavorite());
                        getTableView().refresh();
                    });
                    setGraphic(btn);
                    setText(null);
                }
            });
        }
        if (createdAtColumn != null) {
            createdAtColumn.setCellValueFactory(cellData -> {
                LocalDateTime createdAt = cellData.getValue().getCreatedAt();
                return new javafx.beans.property.SimpleStringProperty(
                        createdAt != null ? createdAt.format(DATE_TIME_FORMATTER) : "-"
                );
            });
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusValue"));
            statusColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) { setGraphic(null); setText(null); return; }
                    Label badge = new Label(value);
                    badge.getStyleClass().add("badge");
                    switch (value.toUpperCase()) {
                        case "ACTIVE"   -> badge.getStyleClass().add("badge-success");
                        case "INACTIVE" -> badge.getStyleClass().add("badge-danger");
                        case "DRAFT"    -> badge.getStyleClass().add("badge-warning");
                        case "ARCHIVED" -> badge.getStyleClass().add("badge-gray");
                        default         -> badge.getStyleClass().add("badge-blue");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            });
        }
        if (moduleColumn != null) {
            moduleColumn.setCellValueFactory(cellData -> {
                Integer mid = cellData.getValue().getModuleId();
                if (mid == null) return new SimpleStringProperty("-");
                String title = moduleTitleById.get(mid);
                return new SimpleStringProperty(title != null ? title : "#" + mid);
            });
        }
        if (coursesTable != null) {
            coursesTable.setItems(courseList);
            // Single-click row: populate form and open drawer in edit mode
            coursesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        populateForm(newVal);
                        showFormPanel("Modifier le Cours");
                    }
                });
        }
    }

    private void setupForm() {
        if (sortComboBox != null) {
            sortComboBox.getItems().addAll(
                "Titre (A-Z)", "Titre (Z-A)",
                "Prix (Croissant)", "Prix (Décroissant)",
                "Plus récent", "Plus ancien"
            );
            sortComboBox.setValue("Titre (A-Z)");
            sortComboBox.setOnAction(e -> applyFiltersAndSort());
        }
        if (statusComboBox != null) {
            statusComboBox.getItems().addAll("ACTIVE", "DRAFT", "INACTIVE", "ARCHIVED");
            statusComboBox.setValue("ACTIVE");
        }
        if (filterStatusBox != null) {
            filterStatusBox.getItems().addAll("Tous", "ACTIVE", "DRAFT", "INACTIVE", "ARCHIVED");
            filterStatusBox.setValue("Tous");
            filterStatusBox.setOnAction(e -> applyFiltersAndSort());
        }
        attachModuleComboPresentation();
        reloadModuleComboItems(null);
    }

    private void attachModuleComboPresentation() {
        if (moduleComboBox == null) {
            return;
        }
        moduleComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        moduleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
    }

    private void reloadModuleComboItems(Integer preferredModuleId) {
        if (moduleComboBox == null) {
            return;
        }
        Module placeholder = new Module();
        placeholder.setId(0);
        placeholder.setTitle("(Aucun module)");
        List<Module> items = new ArrayList<>();
        items.add(placeholder);
        try {
            items.addAll(moduleService.getAllModules());
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement modules: " + rootCauseMessage(ex), true);
        }
        moduleComboBox.setItems(FXCollections.observableArrayList(items));
        Module select = items.get(0);
        if (preferredModuleId != null) {
            for (Module m : items) {
                if (m.getId() == preferredModuleId) {
                    select = m;
                    break;
                }
            }
        }
        moduleComboBox.setValue(select);
        
        // Setup filterModuleBox
        if (filterModuleBox != null) {
            Module filterAll = new Module(); filterAll.setId(-1); filterAll.setTitle("Tous les modules");
            List<Module> filterItems = new ArrayList<>();
            filterItems.add(filterAll);
            filterItems.addAll(items.stream().filter(m -> m.getId() > 0).collect(Collectors.toList()));
            filterModuleBox.setItems(FXCollections.observableArrayList(filterItems));
            filterModuleBox.setValue(filterAll);
            
            filterModuleBox.setCellFactory(moduleComboBox.getCellFactory());
            filterModuleBox.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Module item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitle());
                }
            });
            filterModuleBox.setOnAction(e -> applyFiltersAndSort());
        }
    }

    private void refreshModuleTitles() {
        try {
            moduleTitleById = moduleService.getAllModules().stream()
                    .collect(Collectors.toMap(Module::getId, Module::getTitle, (a, b) -> a));
        } catch (RuntimeException ex) {
            moduleTitleById = new HashMap<>();
        }
    }

    private void loadCourses() {
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        try {
            refreshModuleTitles();
            List<Course> all = courseService.getAllCourses();
            
            // Search filter
            String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
            String statusFilter = filterStatusBox != null ? filterStatusBox.getValue() : "Tous";
            Module modFilter = filterModuleBox != null ? filterModuleBox.getValue() : null;
            
            all = all.stream().filter(c -> {
                // Initialize fake rating if needed
                if (c.getRating() == 0.0) c.setRating(3.5 + (c.getId() % 15) / 10.0);
                
                // Status match
                if (!"Tous".equals(statusFilter) && !statusFilter.equalsIgnoreCase(c.getStatusValue())) return false;
                
                // Module match
                if (modFilter != null && modFilter.getId() != -1) {
                    if (c.getModuleId() == null || c.getModuleId() != modFilter.getId()) return false;
                }
                
                // Search query match
                if (!query.isEmpty()) {
                    boolean titleMatch = c.getTitle() != null && c.getTitle().toLowerCase().contains(query);
                    if (titleMatch) return true;
                    Integer mid = c.getModuleId();
                    if (mid != null) {
                        String mt = moduleTitleById.getOrDefault(mid, "").toLowerCase();
                        if (mt.contains(query)) return true;
                    }
                    return false;
                }
                return true;
            }).collect(Collectors.toList());

            // Sort
            String sortMode = sortComboBox != null ? sortComboBox.getValue() : "Titre (A-Z)";
            if (sortMode == null) sortMode = "Titre (A-Z)";
            
            java.util.Comparator<Course> cmp;
            switch (sortMode) {
                case "Titre (Z-A)":
                    cmp = java.util.Comparator.comparing((Course c) -> c.getTitle() == null ? "" : c.getTitle().toLowerCase()).reversed();
                    break;
                case "Prix (Croissant)":
                    cmp = java.util.Comparator.comparing(Course::getPrice);
                    break;
                case "Prix (Décroissant)":
                    cmp = java.util.Comparator.comparing(Course::getPrice).reversed();
                    break;
                case "Plus récent":
                    cmp = java.util.Comparator.comparing(Course::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()));
                    break;
                case "Plus ancien":
                    cmp = java.util.Comparator.comparing(Course::getCreatedAt, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder()));
                    break;
                case "Titre (A-Z)":
                default:
                    cmp = java.util.Comparator.comparing((Course c) -> c.getTitle() == null ? "" : c.getTitle().toLowerCase());
                    break;
            }
            all.sort(cmp);
            
            courseList.setAll(all);
            if (coursesTable != null) coursesTable.refresh();
            if (countLabel != null) countLabel.setText(courseList.size() + " cours");
            
            updateAnalytics(all);
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement cours: " + rootCauseMessage(ex), true);
        }
    }
    
    private void updateAnalytics(List<Course> list) {
        if (totalCoursesStat != null) totalCoursesStat.setText(String.valueOf(list.size()));
        
        if (avgPriceStat != null) {
            double avg = list.stream().mapToDouble(Course::getPrice).average().orElse(0.0);
            avgPriceStat.setText(String.format("%.2f €", avg));
        }
        
        if (topModuleStat != null) {
            Map<Integer, Long> counts = list.stream()
                .filter(c -> c.getModuleId() != null)
                .collect(Collectors.groupingBy(Course::getModuleId, Collectors.counting()));
            
            String top = "Aucun";
            if (!counts.isEmpty()) {
                int topId = counts.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
                top = moduleTitleById.getOrDefault(topId, "Inconnu");
            }
            topModuleStat.setText(top);
        }
        
        if (avgRatingStat != null) {
            double avgRat = list.stream().mapToDouble(Course::getRating).average().orElse(0.0);
            avgRatingStat.setText(String.format("%.1f / 5", avgRat));
        }
    }

    private void updateAiSuggestion(String priceText) {
        if (aiSuggestionLabel == null) return;
        try {
            double p = Double.parseDouble(priceText);
            if (p < 20) {
                aiSuggestionLabel.setText("💡 Astuce: Prix bas. Idéal pour attirer beaucoup d'étudiants.");
                aiSuggestionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669; -fx-background-color: rgba(16,185,129,0.1); -fx-padding: 6 10; -fx-background-radius: 6;");
            } else if (p > 100) {
                aiSuggestionLabel.setText("💡 Astuce: Prix Premium. Assurez-vous d'avoir un contenu exclusif.");
                aiSuggestionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #D97706; -fx-background-color: rgba(245,158,11,0.1); -fx-padding: 6 10; -fx-background-radius: 6;");
            } else {
                aiSuggestionLabel.setText("💡 Astuce: Prix standard. Parfaitement aligné avec le marché.");
                aiSuggestionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4F46E5; -fx-background-color: rgba(79,70,229,0.1); -fx-padding: 6 10; -fx-background-radius: 6;");
            }
        } catch (NumberFormatException e) {
            aiSuggestionLabel.setText("💡 Astuce: Saisissez un prix valide pour obtenir une analyse.");
            aiSuggestionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B; -fx-background-color: rgba(148,163,184,0.1); -fx-padding: 6 10; -fx-background-radius: 6;");
        }
    }

    private void populateForm(Course course) {
        selectedCourse = course;
        if (course == null) return;
        if (titleField != null) titleField.setText(course.getTitle());
        if (priceField != null) priceField.setText(String.valueOf(course.getPrice()));
        if (coefficientField != null) {
            coefficientField.setText(course.getCoefficient() != null ? String.valueOf(course.getCoefficient()) : "");
        }
        if (descriptionArea != null) descriptionArea.setText(course.getDescription());
        if (statusComboBox != null && course.getStatusValue() != null) {
            statusComboBox.setValue(course.getStatusValue().toUpperCase());
        }
        reloadModuleComboItems(course.getModuleId());
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        try {
            Course course = buildCourseFromForm(true);
            if (courseService.createCourse(course)) {
                ActivityLogger.log("Cours", "créé", course.getTitle());
                showMessage("Cours créé avec succès!", false);
                clearForm();
                loadCourses();
            } else {
                showMessage("Création du cours échouée.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur création cours: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedCourse == null) {
            showMessage("Sélectionnez un cours à modifier.", true);
            return;
        }
        if (!validateForm()) return;
        // Snapshot form before the dialog: focus changes can reset the module ComboBox.
        final Course courseToSave = buildCourseFromForm(false);
        courseToSave.setId(selectedCourse.getId());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la modification du cours \"" + selectedCourse.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (courseService.updateCourse(courseToSave)) {
                        ActivityLogger.log("Cours", "modifié", courseToSave.getTitle());
                        showMessage("Cours mis à jour avec succès!", false);
                        loadCourses();
                    } else {
                        showMessage("Mise à jour du cours échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur mise à jour cours: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedCourse == null) {
            showMessage("Sélectionnez un cours à supprimer.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer le cours \"" + selectedCourse.getTitle() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (courseService.deleteCourse(selectedCourse.getId())) {
                        ActivityLogger.log("Cours", "supprimé", selectedCourse.getTitle());
                        courseList.remove(selectedCourse);
                        clearForm();
                        showMessage("Cours supprimé.", false);
                    } else {
                        showMessage("Suppression du cours échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur suppression cours: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSort();
    }

    @FXML
    private void handleExport(ActionEvent event) {
        try {
            java.io.File file = new java.io.File("cours_export.csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("ID,Titre,Prix,Note,Module,Statut,Date");
                for (Course c : courseList) {
                    String mod = c.getModuleId() != null ? moduleTitleById.getOrDefault(c.getModuleId(), "") : "";
                    String date = c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_TIME_FORMATTER) : "";
                    pw.printf("%d,\"%s\",%.2f,%.1f,\"%s\",%s,%s%n",
                            c.getId(), c.getTitle().replace("\"", "\"\""), c.getPrice(), c.getRating(),
                            mod.replace("\"", "\"\""), c.getStatusValue(), date);
                }
            }
            showMessage("Export réussi : " + file.getAbsolutePath(), false);
        } catch (Exception e) {
            showMessage("Erreur d'exportation : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private boolean validateForm() {
        if (titleField != null && titleField.getText().trim().isEmpty()) {
            showMessage("Le titre du cours est obligatoire.", true);
            return false;
        }
        if (priceField != null) {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) {
                    showMessage("Le prix doit être positif ou nul.", true);
                    return false;
                }
            } catch (NumberFormatException ex) {
                showMessage("Le prix doit être un nombre valide.", true);
                return false;
            }
        }
        if (coefficientField != null && !coefficientField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(coefficientField.getText().trim());
            } catch (NumberFormatException ex) {
                showMessage("Le coefficient doit être un nombre valide.", true);
                return false;
            }
        }
        if (statusComboBox != null && (statusComboBox.getValue() == null || statusComboBox.getValue().isBlank())) {
            showMessage("Le statut du cours est obligatoire.", true);
            return false;
        }
        return true;
    }

    /**
     * @param forCreate when true, never copy id / files from a selected table row (Créer with a row focused must still be a new course).
     */
    private Course buildCourseFromForm(boolean forCreate) {
        Course course = new Course();
        if (forCreate || selectedCourse == null) {
            course.setCreatedAt(LocalDateTime.now());
            course.setThumbnailPath(null);
            course.setPdfPath(null);
            course.setGeneratedContent(null);
        } else {
            course.setId(selectedCourse.getId());
            course.setCreatedAt(selectedCourse.getCreatedAt() != null ? selectedCourse.getCreatedAt() : LocalDateTime.now());
            course.setThumbnailPath(selectedCourse.getThumbnailPath());
            course.setPdfPath(selectedCourse.getPdfPath());
            course.setGeneratedContent(selectedCourse.getGeneratedContent());
        }
        course.setTitle(titleField != null ? titleField.getText().trim() : "");
        course.setDescription(descriptionArea != null ? descriptionArea.getText().trim() : null);
        course.setPrice(priceField != null ? Double.parseDouble(priceField.getText().trim()) : 0.0);
        
        String coefficientText = coefficientField != null ? coefficientField.getText().trim() : "";
        course.setCoefficient(coefficientText.isEmpty() ? null : Double.parseDouble(coefficientText));

        Module modulePick = moduleComboBox != null ? moduleComboBox.getSelectionModel().getSelectedItem() : null;
        if (modulePick == null && moduleComboBox != null) {
            modulePick = moduleComboBox.getValue();
        }
        course.setModuleId(modulePick != null && modulePick.getId() != 0 ? modulePick.getId() : null);

        String st = statusComboBox != null ? statusComboBox.getValue() : "DRAFT";
        // Auto-promote DRAFT -> ACTIVE
        if ("DRAFT".equals(st) && course.getPrice() > 0 && course.getDescription() != null && course.getDescription().length() > 10) {
            st = "ACTIVE";
            Platform.runLater(() -> showToast("✨ Statut automatisé : DRAFT → ACTIVE", false));
        }
        course.setStatusValue(st);

        return course;
    }

    private void clearForm() {
        selectedCourse = null;
        if (coursesTable != null) {
            coursesTable.getSelectionModel().clearSelection();
        }
        if (titleField != null) titleField.clear();
        if (priceField != null) priceField.clear();
        if (coefficientField != null) coefficientField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        if (statusComboBox != null) statusComboBox.setValue("ACTIVE");
        reloadModuleComboItems(null);
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

    public ObservableList<Course> getCourseList() {
        return courseList;
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
