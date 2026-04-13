package com.edusmart.controller.teacher;

import com.edusmart.model.Exam;
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
 * ManageExamsController - Teacher interface for creating and managing exams.
 *
 * Team member: Implement CRUD operations via service layer.
 */
public class ManageExamsController implements Initializable {

    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, Integer> idColumn;
    @FXML private TableColumn<Exam, String> titleColumn;
    @FXML private TableColumn<Exam, String> subjectColumn;
    @FXML private TableColumn<Exam, LocalDate> dateColumn;
    @FXML private TableColumn<Exam, Integer> durationColumn;
    @FXML private TableColumn<Exam, Exam.Status> statusColumn;

    @FXML private TextField titleField;
    @FXML private TextField subjectField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;

    private ObservableList<Exam> examList = FXCollections.observableArrayList();
    private Exam selectedExam;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupForm();
        loadExams();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (subjectColumn != null) subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        if (dateColumn != null) dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (durationColumn != null) durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (examsTable != null) {
            examsTable.setItems(examList);
            examsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void setupForm() {
        if (statusComboBox != null)
            statusComboBox.getItems().addAll("UPCOMING", "IN_PROGRESS", "COMPLETED", "CANCELLED");
        if (durationSpinner != null)
            durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 300, 60));
    }

    /**
     * Loads all exams created by this teacher.
     * TODO: Replace with service call.
     */
    private void loadExams() {
        // TODO: examList.setAll(ExamService.getExamsByTeacher(teacherId));

        examList.setAll(
            new Exam(1, "Examen Final Java", "Informatique", LocalDate.now().plusDays(7), 120, Exam.Status.UPCOMING),
            new Exam(2, "Contrôle Mathématiques", "Mathématiques", LocalDate.now().minusDays(3), 60, Exam.Status.COMPLETED),
            new Exam(3, "Quiz Sciences", "Sciences", LocalDate.now().plusDays(14), 30, Exam.Status.UPCOMING)
        );
    }

    private void populateForm(Exam exam) {
        selectedExam = exam;
        if (exam == null) return;
        if (titleField != null) titleField.setText(exam.getTitle());
        if (subjectField != null) subjectField.setText(exam.getSubject());
        if (datePicker != null) datePicker.setValue(exam.getDate());
        if (durationSpinner != null) durationSpinner.getValueFactory().setValue(exam.getDurationMinutes());
        if (statusComboBox != null && exam.getStatus() != null) statusComboBox.setValue(exam.getStatus().name());
        if (descriptionArea != null) descriptionArea.setText(exam.getDescription());
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        // TODO: ExamService.createExam(buildExamFromForm());
        showMessage("Examen créé avec succès!", false);
        clearForm();
        loadExams();
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedExam == null) { showMessage("Sélectionnez un examen.", true); return; }
        // TODO: ExamService.updateExam(selectedExam.getId(), buildExamFromForm());
        showMessage("Examen mis à jour!", false);
        loadExams();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedExam == null) { showMessage("Sélectionnez un examen.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer l'examen \"" + selectedExam.getTitle() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // TODO: ExamService.deleteExam(selectedExam.getId());
                examList.remove(selectedExam);
                clearForm();
            }
        });
    }

    @FXML
    private void handleClear(ActionEvent event) { clearForm(); selectedExam = null; }

    private boolean validateForm() {
        if (titleField != null && titleField.getText().trim().isEmpty()) {
            showMessage("Le titre de l'examen est obligatoire.", true);
            return false;
        }
        return true;
    }

    private void clearForm() {
        if (titleField != null) titleField.clear();
        if (subjectField != null) subjectField.clear();
        if (datePicker != null) datePicker.setValue(null);
        if (descriptionArea != null) descriptionArea.clear();
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    public ObservableList<Exam> getExamList() {
        return examList;
    }
}
