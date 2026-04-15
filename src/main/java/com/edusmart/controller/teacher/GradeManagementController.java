package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcGradeDao;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.model.Grade;
import com.edusmart.model.User;
import com.edusmart.model.Course;
import com.edusmart.service.GradeService;
import com.edusmart.service.UserService;
import com.edusmart.service.CourseService;
import com.edusmart.service.impl.GradeServiceImpl;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.service.impl.CourseServiceImpl;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * GradeManagementController - Gestion des notes (page liste avec dialogs séparés).
 */
public class GradeManagementController implements Initializable {

    @FXML private TableView<Grade> gradesTable;
    @FXML private TableColumn<Grade, Integer> idColumn;
    @FXML private TableColumn<Grade, String> studentColumn;
    @FXML private TableColumn<Grade, String> subjectColumn;
    @FXML private TableColumn<Grade, Double> scoreColumn;
    @FXML private TableColumn<Grade, Double> maxScoreColumn;
    @FXML private TableColumn<Grade, String> percentageColumn;
    @FXML private TableColumn<Grade, String> semesterColumn;
    @FXML private TableColumn<Grade, String> courseColumn;
    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    private final GradeService gradeService = new GradeServiceImpl(new JdbcGradeDao());
    private final UserService userService = new UserServiceImpl(new JdbcUserDao());
    private final CourseService courseService = new CourseServiceImpl(new JdbcCourseDao());

    private final ObservableList<Grade> gradeList = FXCollections.observableArrayList();
    private Map<Integer, String> studentNames = new HashMap<>();
    private Map<Integer, String> courseTitles = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (studentColumn != null) studentColumn.setCellValueFactory(cd -> {
            String name = studentNames.getOrDefault(cd.getValue().getStudentId(), "#" + cd.getValue().getStudentId());
            return new SimpleStringProperty(name);
        });
        if (subjectColumn != null) subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        if (scoreColumn != null) scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        if (maxScoreColumn != null) maxScoreColumn.setCellValueFactory(new PropertyValueFactory<>("maxScore"));
        if (percentageColumn != null) percentageColumn.setCellValueFactory(cd -> {
            double pct = cd.getValue().getPercentage();
            return new SimpleStringProperty(String.format("%.1f%%", pct));
        });
        if (semesterColumn != null) semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        if (courseColumn != null) courseColumn.setCellValueFactory(cd -> {
            int cid = cd.getValue().getCourseId();
            String title = cid > 0 ? courseTitles.getOrDefault(cid, "#" + cid) : "-";
            return new SimpleStringProperty(title);
        });
        if (gradesTable != null) gradesTable.setItems(gradeList);
    }

    private void loadData() {
        try {
            // Load lookup maps
            studentNames = userService.getAllUsers().stream()
                    .collect(Collectors.toMap(User::getId, User::getFullName, (a, b) -> a));
            courseTitles = courseService.getAllCourses().stream()
                    .collect(Collectors.toMap(Course::getId, Course::getTitle, (a, b) -> a));
            gradeList.setAll(gradeService.getAllGrades());
            if (gradesTable != null) gradesTable.refresh();
        } catch (Exception ex) {
            showMessage("Erreur chargement : " + rootCause(ex), true);
        }
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        Stage owner = (Stage) gradesTable.getScene().getWindow();
        if (GradeFormController.openDialog(owner, null)) {
            loadData();
            showMessage("Note ajoutée avec succès !", false);
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        Grade selected = gradesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Sélectionnez une note à modifier.", true); return; }
        Stage owner = (Stage) gradesTable.getScene().getWindow();
        if (GradeFormController.openDialog(owner, selected)) {
            loadData();
            showMessage("Note mise à jour avec succès !", false);
        }
    }

    @FXML
    private void handleDelete(ActionEvent e) {
        Grade selected = gradesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Sélectionnez une note à supprimer.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la note de \"" + selected.getSubject() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    if (gradeService.deleteGrade(selected.getId())) {
                        gradeList.remove(selected);
                        showMessage("Note supprimée.", false);
                    } else showMessage("Suppression échouée.", true);
                } catch (Exception ex) { showMessage("Erreur : " + rootCause(ex), true); }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent e) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) { loadData(); return; }
        List<Grade> filtered = gradeService.getAllGrades().stream()
                .filter(g -> (g.getSubject() != null && g.getSubject().toLowerCase().contains(query))
                        || studentNames.getOrDefault(g.getStudentId(), "").toLowerCase().contains(query)
                        || (g.getSemester() != null && g.getSemester().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        gradeList.setAll(filtered);
        if (gradesTable != null) gradesTable.refresh();
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill:#EF4444;" : "-fx-text-fill:#10B981;");
            messageLabel.setVisible(true);
        }
    }

    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : "Erreur inconnue";
    }

    // ── Navigation ────────────────────────────────────────────────────────
    @FXML private void handleDashboard(ActionEvent e)        { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD); }
    @FXML private void handleManageCourses(ActionEvent e)    { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES); }
    @FXML private void handleManageModules(ActionEvent e)    { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES); }
    @FXML private void handleManageExams(ActionEvent e)      { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS); }
    @FXML private void handleGradeManagement(ActionEvent e)  { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_GRADE_MANAGEMENT); }
    @FXML private void handleShopManagement(ActionEvent e)   { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT); }
    @FXML private void handleBulletins(ActionEvent e)        { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS); }
    @FXML private void handleCertifications(ActionEvent e)   { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS); }
    @FXML private void handleAnalysisAI(ActionEvent e)       { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI); }
    @FXML private void handleStudentManagement(ActionEvent e){ SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT); }
    @FXML private void handleLogout(ActionEvent e)           { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
