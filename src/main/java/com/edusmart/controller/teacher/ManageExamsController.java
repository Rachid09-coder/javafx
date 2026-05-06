package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcExamDao;
import com.edusmart.model.Exam;
import com.edusmart.service.ExamService;
import com.edusmart.service.impl.ExamServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ManageExamsController - Gestion des examens avec dialogs.
 */
public class ManageExamsController implements Initializable {

    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, Integer> idColumn;
    @FXML private TableColumn<Exam, String> titleColumn;
    @FXML private TableColumn<Exam, String> typeColumn;
    @FXML private TableColumn<Exam, String> moduleColumn;
    @FXML private TableColumn<Exam, Integer> durationColumn;
    @FXML private TableColumn<Exam, Double> coeffColumn;
    @FXML private TableColumn<Exam, String> yearColumn;
    @FXML private TableColumn<Exam, String> semesterColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Label messageLabel;

    private final ExamService examService = new ExamServiceImpl(new JdbcExamDao());
    private final ObservableList<Exam> examList = FXCollections.observableArrayList();
    private FilteredList<Exam> filteredExams;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupFilters() {
        if (typeFilter != null) {
            typeFilter.setItems(FXCollections.observableArrayList("Tous", "Examen Final", "DS", "CC", "Quiz", "TP", "Projet"));
            typeFilter.setValue("Tous");
            typeFilter.setOnAction(e -> applyFilters());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (typeColumn != null) typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (moduleColumn != null) moduleColumn.setCellValueFactory(new PropertyValueFactory<>("moduleName"));
        if (durationColumn != null) durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        if (coeffColumn != null) coeffColumn.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        if (yearColumn != null) yearColumn.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        if (semesterColumn != null) {
            semesterColumn.setCellValueFactory(cd -> {
                Integer s = cd.getValue().getSemester();
                return new SimpleStringProperty(s != null ? "S" + s : "");
            });
        }
        
        filteredExams = new FilteredList<>(examList, p -> true);
        SortedList<Exam> sortedExams = new SortedList<>(filteredExams);
        sortedExams.comparatorProperty().bind(examsTable.comparatorProperty());
        examsTable.setItems(sortedExams);
    }

    private void loadData() {
        try {
            examList.setAll(examService.getAllExams());
        } catch (Exception ex) {
            showMessage("Erreur chargement: " + rootCause(ex), true);
        }
    }

    private void applyFilters() {
        if (filteredExams == null) return;
        String query = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String type = typeFilter != null ? typeFilter.getValue() : "Tous";

        filteredExams.setPredicate(ex -> {
            boolean matchesSearch = query.isEmpty() ||
                    (ex.getTitle() != null && ex.getTitle().toLowerCase().contains(query)) ||
                    (ex.getModuleName() != null && ex.getModuleName().toLowerCase().contains(query));

            boolean matchesType = type == null || type.equals("Tous") ||
                    (ex.getType() != null && ex.getType().equalsIgnoreCase(type));

            return matchesSearch && matchesType;
        });
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        Stage owner = (Stage) examsTable.getScene().getWindow();
        if (ExamFormController.openDialog(owner, null)) {
            loadData();
            showMessage("Examen ajouté avec succès !", false);
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        Exam selected = examsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un examen à modifier.", true);
            return;
        }
        Stage owner = (Stage) examsTable.getScene().getWindow();
        if (ExamFormController.openDialog(owner, selected)) {
            loadData();
            showMessage("Examen modifié avec succès !", false);
        }
    }

    @FXML
    private void handleDelete(ActionEvent e) {
        Exam selected = examsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un examen à supprimer.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'examen \"" + selected.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (examService.deleteExam(selected.getId())) {
                        examList.remove(selected);
                        showMessage("Examen supprimé.", false);
                    } else showMessage("Suppression échouée.", true);
                } catch (Exception ex) { showMessage("Erreur : " + rootCause(ex), true); }
            }
        });
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
        return t.getMessage() != null ? t.getMessage() : "Erreur";
    }

    // Navigation
    @FXML private void handleSearch(ActionEvent e) { applyFilters(); }
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
