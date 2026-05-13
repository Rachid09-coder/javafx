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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Hazem-style exam management screen: Gestion, publication, correction.
 */
public class ManageExamsController implements Initializable {

    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, Integer> idColumn;
    @FXML private TableColumn<Exam, String> titleColumn;
    @FXML private TableColumn<Exam, String> typeColumn;
    @FXML private TableColumn<Exam, String> subjectColumn;
    @FXML private TableColumn<Exam, String> statusColumn;
    @FXML private TableColumn<Exam, String> durationColumn;
    @FXML private TableColumn<Exam, Integer> semesterColumn;
    @FXML private TableColumn<Exam, Double> coefficientColumn;

    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TextField searchField;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField durationField;
    @FXML private TextField filePathField;
    @FXML private TextField externalLinkField;
    @FXML private ComboBox<String> gradeCategoryComboBox;
    @FXML private TextField academicYearField;
    @FXML private TextField semesterField;
    @FXML private TextField coefficientField;
    @FXML private TextArea descriptionArea;
    @FXML private Label messageLabel;

    private final ExamService examService = new ExamServiceImpl(new JdbcExamDao());
    private final ObservableList<Exam> examList = FXCollections.observableArrayList();
    private FilteredList<Exam> filteredExams;
    private Exam selectedExam;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        setupForm();
        setupTable();
        loadExams();
    }

    private void setupFilters() {
        if (statusFilterComboBox != null) {
            statusFilterComboBox.setItems(FXCollections.observableArrayList(
                    "Tous", "UPCOMING", "IN_PROGRESS", "COMPLETED", "CANCELLED"));
            statusFilterComboBox.setValue("Tous");
            statusFilterComboBox.setOnAction(event -> applyFilters());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        }
    }

    private void setupForm() {
        if (typeComboBox != null) {
            typeComboBox.setItems(FXCollections.observableArrayList("QUIZ", "ORAL", "WRITTEN", "PROJECT", "PRACTICAL"));
            typeComboBox.setValue("WRITTEN");
        }
        if (gradeCategoryComboBox != null) {
            gradeCategoryComboBox.setItems(FXCollections.observableArrayList("CC", "EXAM", "TP", "PROJECT"));
            gradeCategoryComboBox.setValue("EXAM");
        }
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (typeColumn != null) typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (subjectColumn != null) subjectColumn.setCellValueFactory(cell -> new SimpleStringProperty(subjectOf(cell.getValue())));
        if (statusColumn != null) statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(statusOf(cell.getValue())));
        if (durationColumn != null) durationColumn.setCellValueFactory(cell -> new SimpleStringProperty(durationOf(cell.getValue())));
        if (semesterColumn != null) semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        if (coefficientColumn != null) coefficientColumn.setCellValueFactory(new PropertyValueFactory<>("coefficient"));

        filteredExams = new FilteredList<>(examList, exam -> true);
        SortedList<Exam> sortedExams = new SortedList<>(filteredExams);
        if (examsTable != null) {
            sortedExams.comparatorProperty().bind(examsTable.comparatorProperty());
            examsTable.setItems(sortedExams);
            examsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldValue, newValue) -> populateForm(newValue));
        }
    }

    private void loadExams() {
        try {
            List<Exam> exams = examService.getAllExams();
            examList.setAll(exams);
            applyFilters();
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement examens: " + rootCauseMessage(ex), true);
        }
    }

    private void applyFilters() {
        if (filteredExams == null) {
            return;
        }

        String query = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();
        String selectedStatus = statusFilterComboBox == null ? "Tous" : statusFilterComboBox.getValue();

        filteredExams.setPredicate(exam -> {
            boolean matchesStatus = selectedStatus == null
                    || "Tous".equals(selectedStatus)
                    || selectedStatus.equalsIgnoreCase(statusOf(exam));
            boolean matchesSearch = query.isEmpty()
                    || contains(exam.getTitle(), query)
                    || contains(exam.getType(), query)
                    || contains(subjectOf(exam), query)
                    || contains(statusOf(exam), query);
            return matchesStatus && matchesSearch;
        });
    }

    private void populateForm(Exam exam) {
        selectedExam = exam;
        if (exam == null) {
            return;
        }
        if (titleField != null) titleField.setText(nullToEmpty(exam.getTitle()));
        if (typeComboBox != null) typeComboBox.setValue(exam.getType() != null ? exam.getType() : "WRITTEN");
        if (durationField != null) durationField.setText(exam.getDuration() != null ? String.valueOf(exam.getDuration()) : "");
        if (filePathField != null) filePathField.setText(nullToEmpty(exam.getFilePath()));
        if (externalLinkField != null) externalLinkField.setText(nullToEmpty(exam.getExternalLink()));
        if (gradeCategoryComboBox != null) gradeCategoryComboBox.setValue(exam.getGradeCategory() != null ? exam.getGradeCategory() : "EXAM");
        if (academicYearField != null) academicYearField.setText(nullToEmpty(exam.getAcademicYear()));
        if (semesterField != null) semesterField.setText(exam.getSemester() != null ? String.valueOf(exam.getSemester()) : "");
        if (coefficientField != null) coefficientField.setText(exam.getCoefficient() != null ? String.valueOf(exam.getCoefficient()) : "");
        if (descriptionArea != null) descriptionArea.setText(nullToEmpty(exam.getDescription()));
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        if (ExamFormController.openDialog(SceneManager.getInstance().getPrimaryStage(), null)) {
            loadExams();
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        Exam selected = getSelectedExam();
        if (selected == null) {
            showMessage("Sélectionnez un examen à modifier.", true);
            return;
        }
        if (ExamFormController.openDialog(SceneManager.getInstance().getPrimaryStage(), selected)) {
            loadExams();
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Exam selected = getSelectedExam();
        if (selected == null) {
            showMessage("Sélectionnez un examen à supprimer.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'examen \"" + selected.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (examService.deleteExam(selected.getId())) {
                        examList.remove(selected);
                        showMessage("Examen supprimé.", false);
                    } else {
                        showMessage("Suppression échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur suppression examen: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleViewPdf(ActionEvent event) {
        Exam selected = getSelectedExam();
        if (selected == null) {
            showMessage("Sélectionnez un examen.", true);
            return;
        }

        try {
            String filePath = selected.getFilePath();
            if (filePath != null && !filePath.isBlank()) {
                File file = new File(filePath);
                if (file.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                    return;
                }
            }

            String externalLink = selected.getExternalLink();
            if (externalLink != null && !externalLink.isBlank() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(externalLink));
                return;
            }

            showMessage("Aucun PDF ou lien externe disponible.", true);
        } catch (Exception ex) {
            showMessage("Impossible d'ouvrir le document: " + ex.getMessage(), true);
        }
    }

    @FXML
    private void handlePublish(ActionEvent event) {
        Exam selected = getSelectedExam();
        if (selected == null) {
            showMessage("Sélectionnez un examen à publier.", true);
            return;
        }

        try {
            selected.setCorrectionPublished(true);
            if (examService.updateExam(selected)) {
                showMessage("Examen publié.", false);
                examsTable.refresh();
            } else {
                showMessage("Publication échouée.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur publication: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleCorrection(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_EXAM_GRADING);
    }

    @FXML
    private void handleExamGrading(ActionEvent event) {
        handleCorrection(event);
    }

    @FXML
    private void handleGenerateAiQuiz(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_EXAM_GRADING);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadExams();
        showMessage("Examens actualisés.", false);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        try {
            if (examService.createExam(buildExamFromForm(null))) {
                showMessage("Examen créé avec succès.", false);
                clearForm();
                loadExams();
            } else {
                showMessage("Création de l'examen échouée.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur création examen: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedExam == null) {
            showMessage("Sélectionnez un examen.", true);
            return;
        }
        if (!validateForm()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la modification de l'examen \"" + selectedExam.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    Exam exam = buildExamFromForm(selectedExam);
                    exam.setId(selectedExam.getId());
                    if (examService.updateExam(exam)) {
                        showMessage("Examen mis à jour.", false);
                        loadExams();
                    } else {
                        showMessage("Mise à jour de l'examen échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur mise à jour examen: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        selectedExam = null;
        if (examsTable != null) {
            examsTable.getSelectionModel().clearSelection();
        }
    }

    private Exam getSelectedExam() {
        return examsTable == null ? null : examsTable.getSelectionModel().getSelectedItem();
    }

    private String subjectOf(Exam exam) {
        if (exam == null) {
            return "";
        }
        if (exam.getModuleName() != null && !exam.getModuleName().isBlank()) {
            return exam.getModuleName();
        }
        return exam.getSubject() == null ? "" : exam.getSubject();
    }

    private String statusOf(Exam exam) {
        if (exam == null || exam.getStatus() == null) {
            return "UPCOMING";
        }
        return exam.getStatus().name();
    }

    private String durationOf(Exam exam) {
        if (exam == null) {
            return "";
        }
        if (exam.getDuration() != null) {
            return String.valueOf(exam.getDuration());
        }
        return exam.getDurationMinutes() > 0 ? String.valueOf(exam.getDurationMinutes()) : "";
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private boolean validateForm() {
        if (titleField != null && titleField.getText().trim().isEmpty()) {
            showMessage("Le titre de l'examen est obligatoire.", true);
            return false;
        }
        if (typeComboBox != null && (typeComboBox.getValue() == null || typeComboBox.getValue().isBlank())) {
            showMessage("Le type de l'examen est obligatoire.", true);
            return false;
        }
        if (!isValidInteger(durationField, "La durée doit être un entier positif.")) {
            return false;
        }
        if (!isValidInteger(semesterField, "Le semestre doit être un entier valide.")) {
            return false;
        }
        if (!isValidDouble(coefficientField, "Le coefficient doit être un nombre positif.")) {
            return false;
        }
        return true;
    }

    private boolean isValidInteger(TextField field, String message) {
        if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
            return true;
        }
        try {
            if (Integer.parseInt(field.getText().trim()) <= 0) {
                showMessage(message, true);
                return false;
            }
            return true;
        } catch (NumberFormatException ex) {
            showMessage(message, true);
            return false;
        }
    }

    private boolean isValidDouble(TextField field, String message) {
        if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
            return true;
        }
        try {
            if (Double.parseDouble(field.getText().trim()) <= 0) {
                showMessage(message, true);
                return false;
            }
            return true;
        } catch (NumberFormatException ex) {
            showMessage(message, true);
            return false;
        }
    }

    private Exam buildExamFromForm(Exam existing) {
        Exam exam = new Exam();
        exam.setTitle(titleField != null ? titleField.getText().trim() : "");
        exam.setDescription(descriptionArea != null ? emptyToNull(descriptionArea.getText()) : null);
        exam.setType(typeComboBox != null ? typeComboBox.getValue() : "WRITTEN");
        exam.setFilePath(filePathField != null ? emptyToNull(filePathField.getText()) : null);
        exam.setExternalLink(externalLinkField != null ? emptyToNull(externalLinkField.getText()) : null);
        exam.setDuration(parseInteger(durationField));
        exam.setGradeCategory(gradeCategoryComboBox != null ? emptyToNull(gradeCategoryComboBox.getValue()) : null);
        exam.setAcademicYear(academicYearField != null ? emptyToNull(academicYearField.getText()) : null);
        exam.setSemester(parseInteger(semesterField));
        exam.setCoefficient(parseDouble(coefficientField));

        if (existing != null) {
            exam.setModuleName(existing.getModuleName());
            exam.setSubject(existing.getSubject());
            exam.setCourseIdNullable(existing.getCourseIdNullable());
            exam.setCorrectionPublished(existing.isCorrectionPublished());
            exam.setStatus(existing.getStatus());
            exam.setDate(existing.getDate());
            exam.setMaxScore(existing.getMaxScore());
            exam.setStudentScore(existing.getStudentScore());
        }
        return exam;
    }

    private void clearForm() {
        if (titleField != null) titleField.clear();
        if (typeComboBox != null) typeComboBox.setValue("WRITTEN");
        if (durationField != null) durationField.clear();
        if (filePathField != null) filePathField.clear();
        if (externalLinkField != null) externalLinkField.clear();
        if (gradeCategoryComboBox != null) gradeCategoryComboBox.setValue("EXAM");
        if (academicYearField != null) academicYearField.clear();
        if (semesterField != null) semesterField.clear();
        if (coefficientField != null) coefficientField.clear();
        if (descriptionArea != null) descriptionArea.clear();
    }

    private Integer parseInteger(TextField field) {
        if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
            return null;
        }
        return Integer.parseInt(field.getText().trim());
    }

    private Double parseDouble(TextField field) {
        if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
            return null;
        }
        return Double.parseDouble(field.getText().trim());
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showMessage(String message, boolean error) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setManaged(true);
            messageLabel.setVisible(true);
            messageLabel.setStyle(error ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : "Erreur";
    }

    @FXML private void handleDashboard(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD); }
    @FXML private void handleManageCourses(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES); }
    @FXML private void handleManageModules(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES); }
    @FXML private void handleManageExams(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS); }
    @FXML private void handleGradeManagement(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_GRADE_MANAGEMENT); }
    @FXML private void handleShopManagement(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT); }
    @FXML private void handleBulletins(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS); }
    @FXML private void handleCertifications(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS); }
    @FXML private void handleAnalysisAI(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI); }
    @FXML private void handleStudentManagement(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT); }
    @FXML private void handleLogout(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
