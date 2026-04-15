package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcExamDao;
import com.edusmart.model.Exam;
import com.edusmart.service.ExamService;
import com.edusmart.service.impl.ExamServiceImpl;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur du formulaire Examen (dialog séparé).
 */
public class ExamFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField titleField;
    @FXML private Label titleError;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField durationField;
    @FXML private Label durationError;
    @FXML private TextField coefficientField;
    @FXML private Label coefficientError;
    @FXML private TextField academicYearField;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private TextField moduleNameField;
    @FXML private Label globalError;

    private final ExamService examService = new ExamServiceImpl(new JdbcExamDao());

    private Exam examToEdit;
    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Examen Final", "Contrôle Continu", "TP", "Projet", "Devoir", "Rattrapage"));
        semesterComboBox.setItems(FXCollections.observableArrayList("1", "2", "3", "4"));
        semesterComboBox.setValue("1");
        titleField.textProperty().addListener((o, ov, nv) -> clearError(titleField, titleError));
        durationField.textProperty().addListener((o, ov, nv) -> clearError(durationField, durationError));
        coefficientField.textProperty().addListener((o, ov, nv) -> clearError(coefficientField, coefficientError));
    }

    public void setAddMode() { titleLabel.setText("Nouvel Examen"); examToEdit = null; }

    public void setEditMode(Exam exam) {
        titleLabel.setText("Modifier l'Examen");
        examToEdit = exam;
        titleField.setText(exam.getTitle());
        descriptionArea.setText(exam.getDescription() != null ? exam.getDescription() : "");
        if (exam.getType() != null) typeComboBox.setValue(exam.getType());
        durationField.setText(exam.getDuration() != null ? String.valueOf(exam.getDuration()) : "");
        coefficientField.setText(exam.getCoefficient() != null ? String.valueOf(exam.getCoefficient()) : "");
        academicYearField.setText(exam.getAcademicYear() != null ? exam.getAcademicYear() : "");
        if (exam.getSemester() != null) semesterComboBox.setValue(String.valueOf(exam.getSemester()));
        moduleNameField.setText(exam.getModuleName() != null ? exam.getModuleName() : "");
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave(ActionEvent e) {
        if (!validateForm()) return;
        try {
            Exam exam = buildExam();
            boolean ok;
            if (examToEdit == null) ok = examService.createExam(exam);
            else { exam.setId(examToEdit.getId()); ok = examService.updateExam(exam); }
            if (ok) { saved = true; closeStage(); }
            else showGlobalError("Opération échouée.");
        } catch (IllegalArgumentException ex) {
            showGlobalError(ex.getMessage());
        } catch (Exception ex) {
            showGlobalError("Erreur : " + rootCause(ex));
        }
    }

    @FXML
    private void handleCancel(ActionEvent e) { closeStage(); }

    private boolean validateForm() {
        boolean valid = true;
        if (titleField.getText().trim().isEmpty()) {
            showFieldError(titleField, titleError, "Le titre est obligatoire."); valid = false;
        } else if (titleField.getText().trim().length() < 3) {
            showFieldError(titleField, titleError, "Minimum 3 caractères."); valid = false;
        }
        String dur = durationField.getText().trim();
        if (!dur.isEmpty()) {
            try {
                int d = Integer.parseInt(dur);
                if (d <= 0) { showFieldError(durationField, durationError, "Durée doit être > 0."); valid = false; }
            } catch (NumberFormatException ex) {
                showFieldError(durationField, durationError, "Nombre entier invalide."); valid = false;
            }
        }
        String coeff = coefficientField.getText().trim();
        if (!coeff.isEmpty()) {
            try {
                double c = Double.parseDouble(coeff);
                if (c <= 0) { showFieldError(coefficientField, coefficientError, "Coefficient doit être > 0."); valid = false; }
            } catch (NumberFormatException ex) {
                showFieldError(coefficientField, coefficientError, "Nombre invalide."); valid = false;
            }
        }
        return valid;
    }

    private Exam buildExam() {
        Exam ex = new Exam();
        ex.setTitle(titleField.getText().trim());
        ex.setDescription(descriptionArea.getText().trim());
        ex.setType(typeComboBox.getValue());
        String dur = durationField.getText().trim();
        ex.setDuration(dur.isEmpty() ? null : Integer.parseInt(dur));
        String coeff = coefficientField.getText().trim();
        ex.setCoefficient(coeff.isEmpty() ? null : Double.parseDouble(coeff));
        ex.setAcademicYear(academicYearField.getText().trim());
        String sem = semesterComboBox.getValue();
        ex.setSemester(sem != null ? Integer.parseInt(sem) : null);
        ex.setModuleName(moduleNameField.getText().trim());
        return ex;
    }

    private void showFieldError(TextField f, Label l, String msg) {
        f.setStyle("-fx-border-color:#EF4444;-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;");
        l.setText(msg); l.setVisible(true); l.setManaged(true);
    }
    private void clearError(TextField f, Label l) { f.setStyle(""); l.setVisible(false); l.setManaged(false); }
    private void showGlobalError(String msg) { globalError.setText(msg); globalError.setVisible(true); globalError.setManaged(true); }
    private void closeStage() { ((Stage) titleLabel.getScene().getWindow()).close(); }
    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : "Erreur";
    }

    public static boolean openDialog(Stage owner, Exam exam) {
        try {
            FXMLLoader loader = new FXMLLoader(ExamFormController.class.getResource("/fxml/teacher/exam-form.fxml"));
            javafx.scene.Parent root = loader.load();
            ExamFormController ctrl = loader.getController();
            if (exam == null) ctrl.setAddMode(); else ctrl.setEditMode(exam);
            Stage stage = new Stage();
            stage.initOwner(owner); stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(exam == null ? "Ajouter un examen" : "Modifier l'examen");
            stage.setResizable(false);
            Scene scene = new Scene(root);
            URL css = ExamFormController.class.getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene); stage.showAndWait();
            return ctrl.isSaved();
        } catch (IOException ex) { ex.printStackTrace(); return false; }
    }
}
