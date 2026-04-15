package com.edusmart.controller.student;

import com.edusmart.model.Exam;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * ExamsController - Student view for browsing exams and evaluations.
 *
 * Team member: Implement loadExams() to fetch data from your service layer.
 */
public class ExamsController implements Initializable {

    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, String> titleColumn;
    @FXML private TableColumn<Exam, String> subjectColumn;
    @FXML private TableColumn<Exam, LocalDate> dateColumn;
    @FXML private TableColumn<Exam, Integer> durationColumn;
    @FXML private TableColumn<Exam, Exam.Status> statusColumn;
    @FXML private TableColumn<Exam, Double> scoreColumn;

    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> subjectFilter;
    @FXML private TextField searchField;
    @FXML private Label totalExamsLabel;

    private ObservableList<Exam> examList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupFilters();
        loadExams();
    }

    private void setupTable() {
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (subjectColumn != null) subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        if (dateColumn != null) dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (durationColumn != null) durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (scoreColumn != null) scoreColumn.setCellValueFactory(new PropertyValueFactory<>("studentScore"));
        if (examsTable != null) examsTable.setItems(examList);
    }

    private void setupFilters() {
        if (statusFilter != null) {
            statusFilter.getItems().addAll("Tous", "À venir", "En cours", "Terminé", "Annulé");
            statusFilter.setValue("Tous");
        }
        if (subjectFilter != null) {
            subjectFilter.getItems().addAll("Tous", "Mathématiques", "Informatique", "Sciences", "Langues");
            subjectFilter.setValue("Tous");
        }
    }

    /**
     * Loads exams for the current student.
     */
    private void loadExams() {

        // Demo data
        examList.setAll(
            new Exam(1, "Examen Final Java", "Informatique", LocalDate.now().plusDays(7), 120, Exam.Status.UPCOMING),
            new Exam(2, "Contrôle Mathématiques", "Mathématiques", LocalDate.now().minusDays(3), 60, Exam.Status.COMPLETED),
            new Exam(3, "Quiz Sciences", "Sciences", LocalDate.now().plusDays(14), 30, Exam.Status.UPCOMING),
            new Exam(4, "Examen Anglais", "Langues", LocalDate.now().minusDays(10), 90, Exam.Status.COMPLETED)
        );

        updateTotalLabel();
    }

    private void updateTotalLabel() {
        if (totalExamsLabel != null) {
            totalExamsLabel.setText(examList.size() + " examens");
        }
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        System.out.println("Filtre appliqué.");
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        System.out.println("Recherche: " + searchField.getText());
    }

    /**
     * Opens the detail view for a selected exam.
     */
    @FXML
    private void handleViewExam(ActionEvent event) {
        Exam selected = examsTable != null ? examsTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) return;
        System.out.println("Ouvrir l'examen: " + selected.getId());
    }

    public ObservableList<Exam> getExamList() {
        return examList;
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
