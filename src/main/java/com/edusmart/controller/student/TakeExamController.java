package com.edusmart.controller.student;

import com.edusmart.dao.jdbc.JdbcExamQuestionDao;
import com.edusmart.dao.jdbc.JdbcExamSubmissionDao;
import com.edusmart.model.Exam;
import com.edusmart.model.ExamQuestion;
import com.edusmart.model.ExamSubmission;
import com.edusmart.util.SceneManager;
import com.edusmart.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeExamController {

    @FXML private Label examTitleLabel;
    @FXML private Label examInfoLabel;
    @FXML private Label timerLabel;
    @FXML private VBox qcmContainer;
    @FXML private VBox questionsList;
    @FXML private VBox pdfContainer;
    @FXML private VBox successContainer;
    @FXML private TextField submissionPathField;

    private Exam exam;
    private static Exam selectedExamForTake;
    private final JdbcExamQuestionDao questionDao = new JdbcExamQuestionDao();
    private final JdbcExamSubmissionDao submissionDao = new JdbcExamSubmissionDao();
    private final Map<Integer, ToggleGroup> qcmToggles = new HashMap<>();

    public void setExamToTake(Exam exam) {
        this.exam = exam;
        initData(exam);
    }

    public void initialize() {
        // No longer relying on static state for new navigation
    }

    public void initData(Exam exam) {
        this.exam = exam;
        examTitleLabel.setText(exam.getTitle());
        examInfoLabel.setText(exam.getSubject() + " | " + (exam.getDuration() != null ? exam.getDuration() : 0) + " minutes");
        
        if ("QCM".equals(exam.getType())) {
            showQCM();
        } else {
            showPDF();
        }
    }

    private void showQCM() {
        qcmContainer.setVisible(true);
        qcmContainer.setManaged(true);
        loadQCMQuestions();
    }

    private void showPDF() {
        pdfContainer.setVisible(true);
        pdfContainer.setManaged(true);
    }

    private void loadQCMQuestions() {
        questionsList.getChildren().clear();
        List<ExamQuestion> questions = questionDao.findByExamId(exam.getId());
        
        int i = 1;
        for (ExamQuestion q : questions) {
            VBox qBox = new VBox(10);
            qBox.getStyleClass().add("card");
            qBox.setStyle("-fx-padding: 15;");

            Label qText = new Label(i + ". " + q.getQuestionText());
            qText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
            qBox.getChildren().add(qText);

            ToggleGroup group = new ToggleGroup();
            String[] options = q.getOptions().split("\\|");
            for (String opt : options) {
                RadioButton rb = new RadioButton(opt);
                rb.setToggleGroup(group);
                qBox.getChildren().add(rb);
            }
            
            qcmToggles.put(q.getId(), group);
            questionsList.getChildren().add(qBox);
            i++;
        }
    }

    @FXML
    private void handleSubmitQCM(ActionEvent event) {
        int studentId = SessionManager.getCurrentUser().getId();
        int examId = exam.getId();

        try {
            for (Map.Entry<Integer, ToggleGroup> entry : qcmToggles.entrySet()) {
                RadioButton selected = (RadioButton) entry.getValue().getSelectedToggle();
                ExamSubmission sub = new ExamSubmission();
                sub.setExamId(examId);
                sub.setStudentId(studentId);
                sub.setQuestionId(entry.getKey());
                sub.setStudentAnswer(selected != null ? selected.getText() : "");
                sub.setStatus(ExamSubmission.SubmissionStatus.PENDING);
                sub.setSubmittedAt(LocalDateTime.now());
                
                submissionDao.create(sub);
            }
            showSuccess();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la soumission de l'examen: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBrowseSubmission(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sélectionner votre copie PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            submissionPathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSubmitPDF(ActionEvent event) {
        if (submissionPathField.getText().isEmpty()) return;
        
        try {
            ExamSubmission sub = new ExamSubmission();
            sub.setExamId(exam.getId());
            sub.setStudentId(SessionManager.getCurrentUser().getId());
            sub.setStudentAnswer("Fichier déposé");
            sub.setFilePath(submissionPathField.getText());
            sub.setStatus(ExamSubmission.SubmissionStatus.SUBMITTED);
            sub.setSubmittedAt(LocalDateTime.now());
            
            submissionDao.create(sub);
            showSuccess();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la soumission du PDF: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDownloadSujet(ActionEvent event) {
        if (exam == null || exam.getFilePath() == null || exam.getFilePath().isBlank()) {
            System.err.println("Aucun fichier PDF n'est associé à cet examen.");
            return;
        }
        try {
            java.awt.Desktop.getDesktop().open(new java.io.File(exam.getFilePath()));
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture du PDF : " + e.getMessage());
        }
    }

    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Exam submitted successfully!");
        alert.showAndWait();

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
        pause.setOnFinished(e -> {
            SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS);
        });
        pause.play();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS);
    }
}
