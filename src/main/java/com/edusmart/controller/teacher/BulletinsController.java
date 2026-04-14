package com.edusmart.controller.teacher;

import com.edusmart.model.Grade;
import com.edusmart.util.SceneManager;
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
 * BulletinsController - Teacher interface for managing student grades/bulletins.
 *
 * Team member: Implement grade CRUD operations via service layer.
 */
public class BulletinsController implements Initializable {

    @FXML private TableView<Grade> gradesTable;
    @FXML private TableColumn<Grade, Integer> studentIdColumn;
    @FXML private TableColumn<Grade, String> subjectColumn;
    @FXML private TableColumn<Grade, Double> scoreColumn;
    @FXML private TableColumn<Grade, Double> maxScoreColumn;
    @FXML private TableColumn<Grade, String> semesterColumn;
    @FXML private TableColumn<Grade, String> commentColumn;

    @FXML private ComboBox<String> studentComboBox;
    @FXML private ComboBox<String> subjectComboBox;
    @FXML private TextField scoreField;
    @FXML private TextField maxScoreField;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private TextField commentField;
    @FXML private Label messageLabel;

    private ObservableList<Grade> gradeList = FXCollections.observableArrayList();
    private Grade selectedGrade;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupForm();
        loadGrades();
    }

    private void setupTable() {
        if (studentIdColumn != null) studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        if (subjectColumn != null) subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        if (scoreColumn != null) scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        if (maxScoreColumn != null) maxScoreColumn.setCellValueFactory(new PropertyValueFactory<>("maxScore"));
        if (semesterColumn != null) semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        if (commentColumn != null) commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        if (gradesTable != null) {
            gradesTable.setItems(gradeList);
            gradesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void setupForm() {
        // TODO: Populate studentComboBox from StudentService.getAllStudents()
        if (studentComboBox != null)
            studentComboBox.getItems().addAll("Étudiant 1", "Étudiant 2", "Étudiant 3");
        if (subjectComboBox != null)
            subjectComboBox.getItems().addAll("Mathématiques", "Informatique", "Physique", "Anglais");
        if (semesterComboBox != null)
            semesterComboBox.getItems().addAll("Semestre 1", "Semestre 2");
    }

    /**
     * Loads all grades.
     * TODO: Replace with service call.
     */
    private void loadGrades() {
        // TODO: gradeList.setAll(GradeService.getAllGrades());

        Grade g1 = new Grade(1, 1, 1, "Mathématiques", 16.5, 20.0, "Semestre 1");
        Grade g2 = new Grade(2, 1, 2, "Informatique", 18.0, 20.0, "Semestre 1");
        Grade g3 = new Grade(3, 2, 1, "Mathématiques", 14.0, 20.0, "Semestre 1");
        gradeList.setAll(g1, g2, g3);
    }

    private void populateForm(Grade grade) {
        selectedGrade = grade;
        if (grade == null) return;
        if (scoreField != null) scoreField.setText(String.valueOf(grade.getScore()));
        if (maxScoreField != null) maxScoreField.setText(String.valueOf(grade.getMaxScore()));
        if (semesterComboBox != null) semesterComboBox.setValue(grade.getSemester());
        if (commentField != null) commentField.setText(grade.getComment());
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        // TODO: GradeService.createGrade(buildGradeFromForm());
        showMessage("Note ajoutée avec succès!", false);
        clearForm();
        loadGrades();
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedGrade == null) { showMessage("Sélectionnez une note.", true); return; }
        // TODO: GradeService.updateGrade(selectedGrade.getId(), buildGradeFromForm());
        showMessage("Note mise à jour!", false);
        loadGrades();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedGrade == null) { showMessage("Sélectionnez une note.", true); return; }
        // TODO: GradeService.deleteGrade(selectedGrade.getId());
        gradeList.remove(selectedGrade);
        clearForm();
        showMessage("Note supprimée.", false);
    }

    @FXML
    private void handleClear(ActionEvent event) { clearForm(); selectedGrade = null; }

    private boolean validateForm() {
        if (scoreField != null) {
            try { Double.parseDouble(scoreField.getText()); }
            catch (NumberFormatException e) { showMessage("Note invalide.", true); return false; }
        }
        return true;
    }

    private void clearForm() {
        if (scoreField != null) scoreField.clear();
        if (maxScoreField != null) maxScoreField.clear();
        if (commentField != null) commentField.clear();
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    public ObservableList<Grade> getGradeList() {
        return gradeList;
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
