package com.edusmart.controller.student;

import com.edusmart.dao.ExamSubmissionDao;
import com.edusmart.dao.jdbc.JdbcExamDao;
import com.edusmart.dao.jdbc.JdbcExamSubmissionDao;
import com.edusmart.model.Exam;
import com.edusmart.model.ExamSubmission;
import com.edusmart.service.ExamService;
import com.edusmart.service.impl.ExamServiceImpl;
import com.edusmart.util.ModernActionCell;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

    @FXML private TableColumn<Exam, Void> actionsColumn;

    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> subjectFilter;
    @FXML private TextField searchField;
    @FXML private Label totalExamsLabel;

    private final ExamService examService = new ExamServiceImpl(new JdbcExamDao());
    private final ExamSubmissionDao submissionDao = new JdbcExamSubmissionDao();
    private final ObservableList<Exam> examList = FXCollections.observableArrayList();
    private FilteredList<Exam> filteredExams;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupFilters();
        loadExams();
    }

    private void setupTable() {
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (subjectColumn != null) subjectColumn.setCellValueFactory(new PropertyValueFactory<>("moduleName"));
        if (dateColumn != null) {
            dateColumn.setCellValueFactory(cd -> {
                return new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getDate());
            });
        }
        if (durationColumn != null) durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (scoreColumn != null) {
            scoreColumn.setCellValueFactory(cd -> {
                Double s = cd.getValue().getStudentScore();
                return new javafx.beans.property.SimpleObjectProperty<>(s != null && s > 0 ? s : null);
            });
        }

        if (actionsColumn != null) {
            actionsColumn.setCellFactory(col -> new ModernActionCell.Builder<Exam>()
                .addCustomAction("📝", this::handlePassExam, "Passer l'examen")
                .addCustomAction("👁️", this::handleViewCorrection, "Voir la correction")
                .build());
        }

        filteredExams = new FilteredList<>(examList, p -> true);
        examsTable.setItems(filteredExams);
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
        try {
            java.util.List<Exam> all = examService.getAllExams();
            int studentId = 2; // Mock current student ID
            
            for (Exam ex : all) {
                ExamSubmission sub = submissionDao.findByStudentAndExam(studentId, ex.getId());
                if (sub != null) {
                    ex.setStudentScore(sub.getScore() != null ? sub.getScore() : 0.0);
                    ex.setStatus(Exam.Status.COMPLETED);
                } else {
                    ex.setStatus(Exam.Status.UPCOMING);
                }
            }
            examList.setAll(all);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateTotalLabel();
    }

    private void handlePassExam(Exam exam) {
        if (exam.getStatus() == Exam.Status.COMPLETED) {
            showAlert("Déjà fait", "Vous avez déjà passé cet examen.");
            return;
        }
        TakeExamController.setExam(exam);
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_TAKE_EXAM);
    }

    private void handleViewCorrection(Exam exam) {
        if (!exam.isCorrectionPublished()) {
            showAlert("Non disponible", "La correction n'est pas encore publiée par l'enseignant.");
            return;
        }
        showAlert("Correction", "Affichage de la correction pour : " + exam.getTitle());
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
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

    @FXML private void handleDashboard(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_DASHBOARD);
    }

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

    @FXML private void handleShop(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP); }
    @FXML private void handleStudentAI(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_AI); }
    @FXML private void handleProfile(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE); }
    @FXML private void handleLogout(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
