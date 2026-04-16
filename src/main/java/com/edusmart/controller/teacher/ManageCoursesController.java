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
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * ManageCoursesController - Gestion de liste des cours (version table + boutons).
 */
public class ManageCoursesController implements Initializable {

    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, Integer> idColumn;
    @FXML private TableColumn<Course, String> titleColumn;
    @FXML private TableColumn<Course, String> moduleColumn;
    @FXML private TableColumn<Course, Double> priceColumn;
    @FXML private TableColumn<Course, Double> coefficientColumn;
    @FXML private TableColumn<Course, String> statusColumn;
    @FXML private TableColumn<Course, String> createdAtColumn;

    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    private final CourseService courseService = new CourseServiceImpl(new JdbcCourseDao());
    private final ModuleService moduleService = new ModuleServiceImpl(new JdbcModuleDao());

    private final ObservableList<Course> courseList = FXCollections.observableArrayList();
    private Map<Integer, String> moduleTitles = new HashMap<>();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (moduleColumn != null) {
            moduleColumn.setCellValueFactory(cellData -> {
                Integer mId = cellData.getValue().getModuleId();
                if (mId != null && mId > 0) {
                    return new SimpleStringProperty(moduleTitles.getOrDefault(mId, "Module #" + mId));
                }
                return new SimpleStringProperty("-");
            });
        }
        if (priceColumn != null) priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (coefficientColumn != null) coefficientColumn.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusValue"));
        if (createdAtColumn != null) {
            createdAtColumn.setCellValueFactory(cellData -> {
                LocalDateTime cd = cellData.getValue().getCreatedAt();
                return new SimpleStringProperty(cd != null ? cd.format(FORMATTER) : "");
            });
        }
        if (coursesTable != null) coursesTable.setItems(courseList);
    }

    private void loadData() {
        try {
            // Chargement des modules pour le lookup
            moduleTitles = moduleService.getAllModules().stream()
                    .collect(Collectors.toMap(Module::getId, Module::getTitle, (a, b) -> a));
            
            courseList.setAll(courseService.getAllCourses());
            if (coursesTable != null) coursesTable.refresh();
        } catch (Exception ex) {
            showMessage("Erreur de chargement: " + rootCause(ex), true);
        }
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        Stage owner = (Stage) coursesTable.getScene().getWindow();
        if (CourseFormController.openDialog(owner, null)) {
            loadData();
            showMessage("Cours ajouté avec succès !", false);
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        Course selected = coursesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un cours à modifier.", true);
            return;
        }
        Stage owner = (Stage) coursesTable.getScene().getWindow();
        if (CourseFormController.openDialog(owner, selected)) {
            loadData();
            showMessage("Cours modifié avec succès !", false);
        }
    }

    @FXML
    private void handleDelete(ActionEvent e) {
        Course selected = coursesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un cours à supprimer.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer le cours \"" + selected.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (courseService.deleteCourse(selected.getId())) {
                        courseList.remove(selected);
                        showMessage("Cours supprimé avec succès.", false);
                    } else {
                        showMessage("La suppression a échoué.", true);
                    }
                } catch (Exception ex) {
                    showMessage("Erreur : " + rootCause(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent e) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) {
            loadData();
            return;
        }
        List<Course> filtered = courseService.getAllCourses().stream()
                .filter(c -> (c.getTitle() != null && c.getTitle().toLowerCase().contains(query))
                          || (c.getDescription() != null && c.getDescription().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        courseList.setAll(filtered);
        if (coursesTable != null) coursesTable.refresh();
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : "Erreur inconnue";
    }

    // ── Navigation ────────────────────────────────────────────────────────
    @FXML private void handleDashboard(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD); }
    @FXML private void handleManageCourses(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES); }
    @FXML private void handleManageModules(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES); }
    @FXML private void handleManageExams(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS); }
    @FXML private void handleGradeManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_GRADE_MANAGEMENT); }
    @FXML private void handleShopManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT); }
    @FXML private void handleBulletins(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS); }
    @FXML private void handleCertifications(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS); }
    @FXML private void handleAnalysisAI(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI); }
    @FXML private void handleStudentManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT); }
    @FXML private void handleProfile(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
