package com.edusmart.controller.student;

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
 * BulletinController - Student view for displaying report cards / grades.
 *
 * Team member: Implement loadGrades() to fetch data from your service layer.
 */
public class BulletinController implements Initializable {

    @FXML private TableView<Grade> gradesTable;
    @FXML private TableColumn<Grade, String> subjectColumn;
    @FXML private TableColumn<Grade, Double> scoreColumn;
    @FXML private TableColumn<Grade, Double> maxScoreColumn;
    @FXML private TableColumn<Grade, String> semesterColumn;
    @FXML private TableColumn<Grade, String> commentColumn;

    @FXML private ComboBox<String> semesterFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private Label averageLabel;
    @FXML private Label rankLabel;
    @FXML private Button printButton;
    @FXML private Button downloadButton;

    private ObservableList<Grade> gradeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupFilters();
        loadGrades();
    }

    private void setupTable() {
        if (subjectColumn != null) subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        if (scoreColumn != null) scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        if (maxScoreColumn != null) maxScoreColumn.setCellValueFactory(new PropertyValueFactory<>("maxScore"));
        if (semesterColumn != null) semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        if (commentColumn != null) commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        if (gradesTable != null) gradesTable.setItems(gradeList);
    }

    private void setupFilters() {
        if (semesterFilter != null) {
            semesterFilter.getItems().addAll("Semestre 1", "Semestre 2", "Annuel");
            semesterFilter.setValue("Semestre 1");
        }
        if (yearFilter != null) {
            yearFilter.getItems().addAll("2023-2024", "2024-2025");
            yearFilter.setValue("2024-2025");
        }
    }

    /**
     * Loads grades for the current student.
     */
    private void loadGrades() {

        // Demo data
        Grade g1 = new Grade(1, 1, 1, "Mathématiques", 16.5, 20.0, "Semestre 1");
        Grade g2 = new Grade(2, 1, 2, "Informatique", 18.0, 20.0, "Semestre 1");
        Grade g3 = new Grade(3, 1, 3, "Physique", 14.0, 20.0, "Semestre 1");
        Grade g4 = new Grade(4, 1, 4, "Anglais", 15.5, 20.0, "Semestre 1");
        g1.setComment("Très bien");
        g2.setComment("Excellent");
        g3.setComment("Bien");
        g4.setComment("Bien");
        gradeList.setAll(g1, g2, g3, g4);

        updateStatistics();
    }

    private void updateStatistics() {
        if (averageLabel != null) {
            double avg = gradeList.stream().mapToDouble(Grade::getScore).average().orElse(0);
            averageLabel.setText(String.format("Moyenne: %.2f/20", avg));
        if (rankLabel != null) {
            rankLabel.setText("Rang: 3/35");
        }
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        System.out.println("Filtres appliqués au bulletin.");
    }

    /**
     * Prints the bulletin.
     */
    @FXML
    private void handlePrint(ActionEvent event) {
        System.out.println("Impression du bulletin en cours...");
    }

    /**
     * Downloads the bulletin as PDF.
     */
    @FXML
    private void handleDownload(ActionEvent event) {
        System.out.println("Téléchargement du PDF...");
    }

    public ObservableList<Grade> getGradeList() {
        return gradeList;
    }

    // ── Navigation handlers ──────────────────────────────────────────────

    @FXML private void handleCourses(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES);
    }

    @FXML private void handleExams(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS);
    }

    @FXML private void handleBulletin(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_BULLETIN);
    }

    @FXML private void handleCertification(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_CERTIFICATION);
    }

    @FXML private void handleShop(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP);
    }

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
