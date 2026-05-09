package com.edusmart.controller.student;

import com.edusmart.model.Exam;
import com.edusmart.model.ExamSubmission;
import com.edusmart.dao.jdbc.JdbcExamSubmissionDao;
import com.edusmart.util.SceneManager;
import com.edusmart.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

public class UploadExamController {

    @FXML private Label examTitleLabel;
    @FXML private TextField filePathField;

    private Exam exam;
    private File selectedFile;
    private final JdbcExamSubmissionDao submissionDao = new JdbcExamSubmissionDao();

    public void setExam(Exam exam) {
        this.exam = exam;
        if (exam != null) {
            examTitleLabel.setText("Examen : " + exam.getTitle());
        }
    }

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sélectionner votre copie PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        
        selectedFile = chooser.showOpenDialog(null);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un fichier avant de soumettre.");
            return;
        }

        try {
            // 1. Create uploads directory if it doesn't exist
            File uploadsDir = new File("uploads");
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }

            // 2. Copy file to local storage
            String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
            File destFile = new File(uploadsDir, fileName);
            Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 3. Save submission in database
            ExamSubmission sub = new ExamSubmission();
            sub.setExamId(exam.getId());
            sub.setStudentId(SessionManager.getCurrentUser().getId());
            sub.setFilePath(destFile.getAbsolutePath());
            sub.setStudentAnswer("Copie PDF soumise");
            sub.setStatus(ExamSubmission.SubmissionStatus.SUBMITTED);
            sub.setSubmittedAt(LocalDateTime.now());
            
            // Set dummy question ID to avoid NOT NULL constraint if no questions exist
            sub.setQuestionId(0); 

            submissionDao.create(sub);

            // 4. Success message and redirect
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Examen soumis avec succès !");
            SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de copier le fichier : " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur DB", "Une erreur est survenue lors de la sauvegarde : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
