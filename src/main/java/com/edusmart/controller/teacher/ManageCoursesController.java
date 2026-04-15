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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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

    @FXML private TextField titleField;
    @FXML private TextField priceField;
    @FXML private TextField coefficientField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<Module> moduleComboBox;
    @FXML private TextField searchField;
    @FXML private Label messageLabel;

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
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (priceColumn != null) priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (coefficientColumn != null) coefficientColumn.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        if (createdAtColumn != null) {
            createdAtColumn.setCellValueFactory(cellData -> {
                LocalDateTime createdAt = cellData.getValue().getCreatedAt();
                return new javafx.beans.property.SimpleStringProperty(
                        createdAt != null ? createdAt.format(DATE_TIME_FORMATTER) : "-"
                );
            });
        }
        if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusValue"));
        if (moduleColumn != null) {
            moduleColumn.setCellValueFactory(cellData -> {
                Integer mid = cellData.getValue().getModuleId();
                if (mid == null) {
                    return new SimpleStringProperty("-");
                }
                String title = moduleTitleById.get(mid);
                return new SimpleStringProperty(title != null ? title : "#" + mid);
            });
        }
        if (coursesTable != null) {
            coursesTable.setItems(courseList);
            coursesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void setupForm() {
        if (statusComboBox != null) {
            statusComboBox.getItems().addAll("ACTIVE", "DRAFT", "INACTIVE", "ARCHIVED");
            statusComboBox.setValue("ACTIVE");
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
        try {
            refreshModuleTitles();
            courseList.setAll(courseService.getAllCourses());
            if (coursesTable != null) {
                coursesTable.refresh();
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement cours: " + rootCauseMessage(ex), true);
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
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) {
            loadCourses();
            return;
        }
        refreshModuleTitles();
        List<Course> filtered = courseService.getAllCourses().stream()
                .filter(c -> {
                    boolean titleMatch = c.getTitle() != null && c.getTitle().toLowerCase().contains(query);
                    if (titleMatch) {
                        return true;
                    }
                    Integer mid = c.getModuleId();
                    if (mid == null) {
                        return false;
                    }
                    String mt = moduleTitleById.getOrDefault(mid, "").toLowerCase();
                    return mt.contains(query);
                })
                .collect(Collectors.toList());
        courseList.setAll(filtered);
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
        course.setStatusValue(statusComboBox != null ? statusComboBox.getValue() : "DRAFT");

        String coefficientText = coefficientField != null ? coefficientField.getText().trim() : "";
        course.setCoefficient(coefficientText.isEmpty() ? null : Double.parseDouble(coefficientText));

        Module modulePick = moduleComboBox != null ? moduleComboBox.getSelectionModel().getSelectedItem() : null;
        if (modulePick == null && moduleComboBox != null) {
            modulePick = moduleComboBox.getValue();
        }
        course.setModuleId(modulePick != null && modulePick.getId() != 0 ? modulePick.getId() : null);

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
        if (messageLabel != null) {
            messageLabel.setText(message);
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
