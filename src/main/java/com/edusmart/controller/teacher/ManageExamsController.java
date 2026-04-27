package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcExamDao;
import com.edusmart.model.Exam;
import com.edusmart.service.ExamService;
import com.edusmart.service.impl.ExamServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * ManageExamsController - Teacher interface for creating and managing exams.
 *
 * Team member: Implement CRUD operations via service layer.
 */
public class ManageExamsController implements Initializable {

    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, Integer> idColumn;
    @FXML private TableColumn<Exam, String> titleColumn;
    @FXML private TableColumn<Exam, String> typeColumn;
    @FXML private TableColumn<Exam, Integer> durationColumn;
    @FXML private TableColumn<Exam, Integer> semesterColumn;
    @FXML private TableColumn<Exam, Double> coefficientColumn;

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
    @FXML private TextField searchField;
    @FXML private VBox formPanel;

    private ObservableList<Exam> examList = FXCollections.observableArrayList();
    private Exam selectedExam;
    private final ExamService examService = new ExamServiceImpl(new JdbcExamDao());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupForm();
        loadExams();
        if (formPanel != null) {
            formPanel.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(450), formPanel);
            ft.setToValue(1.0); ft.play();
        }
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (typeColumn != null) {
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            typeColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) { setGraphic(null); setText(null); return; }
                    Label badge = new Label(value);
                    badge.getStyleClass().add("badge");
                    switch (value.toUpperCase()) {
                        case "WRITTEN"   -> badge.getStyleClass().add("badge-blue");
                        case "QUIZ"      -> badge.getStyleClass().add("badge-success");
                        case "ORAL"      -> badge.getStyleClass().add("badge-purple");
                        case "PROJECT"   -> badge.getStyleClass().add("badge-warning");
                        case "PRACTICAL" -> badge.getStyleClass().add("badge-gray");
                        default          -> badge.getStyleClass().add("badge-blue");
                    }
                    setGraphic(badge); setText(null);
                }
            });
        }
        if (durationColumn != null) durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        if (semesterColumn != null) semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        if (coefficientColumn != null) coefficientColumn.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        if (examsTable != null) {
            examsTable.setItems(examList);
            examsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    populateForm(newVal);
                    if (newVal != null && formPanel != null) {
                        TranslateTransition tt = new TranslateTransition(Duration.millis(260), formPanel);
                        tt.setFromX(30); tt.setToX(0);
                        FadeTransition ft = new FadeTransition(Duration.millis(260), formPanel);
                        ft.setFromValue(0.6); ft.setToValue(1.0);
                        tt.play(); ft.play();
                    }
                });
        }
    }

    private void setupForm() {
        if (typeComboBox != null) {
            typeComboBox.getItems().addAll("QUIZ", "ORAL", "WRITTEN", "PROJECT", "PRACTICAL");
            typeComboBox.setValue("WRITTEN");
        }
        if (gradeCategoryComboBox != null) {
            gradeCategoryComboBox.getItems().addAll("CC", "EXAM", "TP", "PROJECT");
            gradeCategoryComboBox.setValue("EXAM");
        }
    }

    /**
     * Loads all exams created by this teacher.
     * TODO: Replace with service call.
     */
    private void loadExams() {
        try {
            examList.setAll(examService.getAllExams());
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement examens: " + rootCauseMessage(ex), true);
        }
    }

    private void populateForm(Exam exam) {
        selectedExam = exam;
        if (exam == null) return;
        if (titleField != null) titleField.setText(exam.getTitle());
        if (typeComboBox != null) typeComboBox.setValue(exam.getType());
        if (durationField != null) durationField.setText(exam.getDuration() != null ? String.valueOf(exam.getDuration()) : "");
        if (filePathField != null) filePathField.setText(exam.getFilePath());
        if (externalLinkField != null) externalLinkField.setText(exam.getExternalLink());
        if (gradeCategoryComboBox != null) gradeCategoryComboBox.setValue(exam.getGradeCategory());
        if (academicYearField != null) academicYearField.setText(exam.getAcademicYear());
        if (semesterField != null) semesterField.setText(exam.getSemester() != null ? String.valueOf(exam.getSemester()) : "");
        if (coefficientField != null) coefficientField.setText(exam.getCoefficient() != null ? String.valueOf(exam.getCoefficient()) : "");
        if (descriptionArea != null) descriptionArea.setText(exam.getDescription());
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        try {
            if (examService.createExam(buildExamFromForm())) {
                showMessage("Examen créé avec succès!", false);
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
        if (selectedExam == null) { showMessage("Sélectionnez un examen.", true); return; }
        if (!validateForm()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la modification de l'examen \"" + selectedExam.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    Exam exam = buildExamFromForm();
                    exam.setId(selectedExam.getId());
                    if (examService.updateExam(exam)) {
                        showMessage("Examen mis à jour!", false);
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
    private void handleDelete(ActionEvent event) {
        if (selectedExam == null) { showMessage("Sélectionnez un examen.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer l'examen \"" + selectedExam.getTitle() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (examService.deleteExam(selectedExam.getId())) {
                        examList.remove(selectedExam);
                        clearForm();
                        showMessage("Examen supprimé.", false);
                    } else {
                        showMessage("Suppression de l'examen échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur suppression examen: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) {
            loadExams();
            return;
        }
        List<Exam> filtered = examService.getAllExams().stream()
                .filter(e ->
                        (e.getTitle() != null && e.getTitle().toLowerCase().contains(query)) ||
                        (e.getType() != null && e.getType().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        examList.setAll(filtered);
    }

    @FXML
    private void handleClear(ActionEvent event) { clearForm(); selectedExam = null; }

    private boolean validateForm() {
        if (titleField != null && titleField.getText().trim().isEmpty()) {
            showMessage("Le titre de l'examen est obligatoire.", true);
            return false;
        }
        if (typeComboBox != null && (typeComboBox.getValue() == null || typeComboBox.getValue().isBlank())) {
            showMessage("Le type de l'examen est obligatoire.", true);
            return false;
        }
        if (durationField != null && !durationField.getText().trim().isEmpty()) {
            try {
                int duration = Integer.parseInt(durationField.getText().trim());
                if (duration < 0) {
                    showMessage("La durée doit être positive ou nulle.", true);
                    return false;
                }
            } catch (NumberFormatException ex) {
                showMessage("La durée doit être un nombre entier valide.", true);
                return false;
            }
        }
        if (semesterField != null && !semesterField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(semesterField.getText().trim());
            } catch (NumberFormatException ex) {
                showMessage("Le semestre doit être un entier valide.", true);
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
        return true;
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

    private Exam buildExamFromForm() {
        Exam exam = new Exam();
        exam.setTitle(titleField != null ? titleField.getText().trim() : "");
        exam.setDescription(descriptionArea != null ? descriptionArea.getText().trim() : null);
        exam.setType(typeComboBox != null ? typeComboBox.getValue() : "WRITTEN");
        exam.setFilePath(filePathField != null ? emptyToNull(filePathField.getText()) : null);
        exam.setExternalLink(externalLinkField != null ? emptyToNull(externalLinkField.getText()) : null);
        exam.setDuration(parseInteger(durationField));
        exam.setModuleName(null);
        exam.setGradeCategory(gradeCategoryComboBox != null ? emptyToNull(gradeCategoryComboBox.getValue()) : null);
        exam.setAcademicYear(academicYearField != null ? emptyToNull(academicYearField.getText()) : null);
        exam.setSemester(parseInteger(semesterField));
        exam.setCoefficient(parseDouble(coefficientField));
        exam.setCourseIdNullable(null);
        return exam;
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

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            if (isError) {
                messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-background-color: rgba(239,68,68,0.1); -fx-background-radius: 8; -fx-padding: 8 14;");
            } else {
                messageLabel.setStyle("-fx-text-fill: #059669; -fx-background-color: rgba(16,185,129,0.1); -fx-background-radius: 8; -fx-padding: 8 14;");
            }
            messageLabel.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(300), messageLabel);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        }
    }

    public ObservableList<Exam> getExamList() {
        return examList;
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : throwable.getMessage();
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
