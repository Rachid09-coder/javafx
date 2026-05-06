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
 * ManageModulesController - Gestion de liste des modules (version table + boutons).
 */
public class ManageModulesController implements Initializable {

    @FXML private TableView<Module> modulesTable;
    @FXML private TableColumn<Module, Integer> idColumn;
    @FXML private TableColumn<Module, String> titleColumn;
    @FXML private TableColumn<Module, String> descriptionColumn;
    @FXML private TableColumn<Module, Integer> durationColumn;
    @FXML private TableColumn<Module, String> courseColumn;
    @FXML private TableColumn<Module, String> createdAtColumn;

    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    private final ModuleService moduleService = new ModuleServiceImpl(new JdbcModuleDao());
    private final CourseService courseService = new CourseServiceImpl(new JdbcCourseDao());

    private final ObservableList<Module> moduleList = FXCollections.observableArrayList();
    private Map<Integer, String> courseTitles = new HashMap<>();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (descriptionColumn != null) descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (durationColumn != null) durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationHours"));
        if (courseColumn != null) {
            courseColumn.setCellValueFactory(cellData -> {
                int cId = cellData.getValue().getCourseId();
                if (cId > 0) {
                    return new SimpleStringProperty(courseTitles.getOrDefault(cId, "Cours #" + cId));
                }
                return new SimpleStringProperty("-");
            });
        }
        if (createdAtColumn != null) {
            createdAtColumn.setCellValueFactory(cellData -> {
                LocalDateTime cd = cellData.getValue().getCreatedAt();
                return new SimpleStringProperty(cd != null ? cd.format(FORMATTER) : "");
            });
        }
        if (modulesTable != null) modulesTable.setItems(moduleList);
    }

    private void loadData() {
        try {
            courseTitles = courseService.getAllCourses().stream()
                    .collect(Collectors.toMap(Course::getId, Course::getTitle, (a, b) -> a));
            
            moduleList.setAll(moduleService.getAllModules());
            if (modulesTable != null) modulesTable.refresh();
        } catch (Exception ex) {
            showMessage("Erreur de chargement: " + rootCause(ex), true);
        }
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        Stage owner = (Stage) modulesTable.getScene().getWindow();
        if (ModuleFormController.openDialog(owner, null)) {
            loadData();
            showMessage("Module ajouté avec succès !", false);
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        Module selected = modulesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un module à modifier.", true);
            return;
        }
        Stage owner = (Stage) modulesTable.getScene().getWindow();
        if (ModuleFormController.openDialog(owner, selected)) {
            loadData();
            showMessage("Module modifié avec succès !", false);
        }
    }

    @FXML
    private void handleDelete(ActionEvent e) {
        Module selected = modulesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un module à supprimer.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer le module \"" + selected.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (moduleService.deleteModule(selected.getId())) {
                        moduleList.remove(selected);
                        showMessage("Module supprimé avec succès.", false);
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
        List<Module> filtered = moduleService.getAllModules().stream()
                .filter(m -> (m.getTitle() != null && m.getTitle().toLowerCase().contains(query))
                          || (m.getDescription() != null && m.getDescription().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        moduleList.setAll(filtered);
        if (modulesTable != null) modulesTable.refresh();
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

    // Navigation
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
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
