package com.edusmart.controller.teacher;

import com.edusmart.dao.ExamQuestionDao;
import com.edusmart.dao.ExamSubmissionDao;
import com.edusmart.dao.jdbc.JdbcExamDao;
import com.edusmart.dao.jdbc.JdbcExamQuestionDao;
import com.edusmart.dao.jdbc.JdbcExamSubmissionDao;
import com.edusmart.model.*;
import com.edusmart.service.AIGradingService;
import com.edusmart.service.ExamService;
import com.edusmart.service.PlagiarismDetectionService;
import com.edusmart.service.impl.AIGradingServiceImpl;
import com.edusmart.service.impl.ExamServiceImpl;
import com.edusmart.service.impl.PlagiarismDetectionServiceImpl;
import com.edusmart.util.MailSender;
import com.edusmart.util.PdfGenerator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ExamGradingController implements Initializable {

    @FXML private ComboBox<Exam> examComboBox;
    @FXML private ComboBox<ExamQuestion> questionComboBox;
    @FXML private TableView<StudentSubmission> submissionsTable;
    @FXML private TableColumn<StudentSubmission, String> colStudentName;
    @FXML private TableColumn<StudentSubmission, String> colStatus;
    @FXML private TableColumn<StudentSubmission, Double> colScore;
    @FXML private TableColumn<StudentSubmission, Double> colPlagiarism;

    @FXML private Label lblStudentName;
    @FXML private Label lblQuestionText;
    @FXML private TextArea taStudentAnswer;
    @FXML private Label lblAiScore;
    @FXML private Label lblConfidence;
    @FXML private ProgressBar pbAiConfidence;
    @FXML private Label lblPlagiarismScore;
    @FXML private ProgressBar pbPlagiarism;
    @FXML private TextArea taAiFeedback;
    @FXML private TextField tfManualScore;
    @FXML private StackPane loadingOverlay;
    @FXML private Label statusLabel;

    private final ExamService examService = new ExamServiceImpl(new JdbcExamDao());
    private final ExamSubmissionDao submissionDao = new JdbcExamSubmissionDao();
    private final ExamQuestionDao questionDao = new JdbcExamQuestionDao();
    private final AIGradingService aiService = new AIGradingServiceImpl();
    private final PlagiarismDetectionService plagiarismService = new PlagiarismDetectionServiceImpl();

    private final ObservableList<StudentSubmission> submissionList = FXCollections.observableArrayList();
    private List<ExamSubmission> currentSubmissions;
    private ExamSubmission selectedSubmission;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        loadExams();

        // Enable multiple selection for plagiarism comparison
        submissionsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        submissionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displaySubmission(newVal.getSubmissionId());
            }
        });
    }

    private void setupColumns() {
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colPlagiarism.setCellValueFactory(new PropertyValueFactory<>("plagiarismScore"));
        submissionsTable.setItems(submissionList);
    }

    private void loadExams() {
        List<Exam> exams = examService.getAllExams();
        examComboBox.getItems().setAll(exams);

        // Show exam title in dropdown
        examComboBox.setCellFactory(lv -> new ListCell<Exam>() {
            @Override protected void updateItem(Exam item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle() + " (" + item.getModuleName() + ")");
            }
        });
        examComboBox.setButtonCell(new ListCell<Exam>() {
            @Override protected void updateItem(Exam item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Examen..." : item.getTitle());
            }
        });
    }

    @FXML
    private void handleExamSelected() {
        Exam selected = examComboBox.getValue();
        if (selected != null) {
            loadQuestions();
            loadSubmissions();
        }
    }

    private void loadQuestions() {
        Exam selected = examComboBox.getValue();
        if (selected == null) return;
        List<ExamQuestion> questions = questionDao.findByExamId(selected.getId());
        questionComboBox.getItems().clear();
        
        // Add a special item for 'All questions'
        ExamQuestion all = new ExamQuestion();
        all.setId(-1);
        all.setQuestionText("Toutes les questions");
        
        questionComboBox.getItems().add(all);
        questionComboBox.getItems().addAll(questions);
        questionComboBox.getSelectionModel().select(0);
        
        // Use a cell factory to show question text in dropdown
        questionComboBox.setCellFactory(lv -> new ListCell<ExamQuestion>() {
            @Override
            protected void updateItem(ExamQuestion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getQuestionText());
            }
        });
        questionComboBox.setButtonCell(new ListCell<ExamQuestion>() {
            @Override
            protected void updateItem(ExamQuestion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getQuestionText());
            }
        });
    }

    @FXML
    private void handleQuestionSelected() {
        loadSubmissions();
    }

    private void loadSubmissions() {
        Exam exam = examComboBox.getValue();
        ExamQuestion question = questionComboBox.getValue();
        if (exam == null) return;

        List<ExamSubmission> allSubmissions = submissionDao.findByExam(exam.getId());
        
        if (question != null && question.getId() != -1) {
            currentSubmissions = allSubmissions.stream()
                .filter(s -> s.getQuestionId() == question.getId())
                .collect(Collectors.toList());
        } else {
            currentSubmissions = allSubmissions;
        }

        submissionList.clear();
        for (ExamSubmission s : currentSubmissions) {
            submissionList.add(new StudentSubmission(
                s.getId(), "Student #" + s.getStudentId(), s.getStatus().name(), s.getScore(), s.getPlagiarismScore()
            ));
        }
    }

    private void displaySubmission(int id) {
        selectedSubmission = currentSubmissions.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
        if (selectedSubmission == null) return;

        lblStudentName.setText("Étudiant #" + selectedSubmission.getStudentId());
        
        // Find question text
        ExamQuestion q = null;
        if (selectedSubmission.getQuestionId() > 0) {
            q = questionDao.findById(selectedSubmission.getQuestionId());
        }
        
        if (q != null) {
            lblQuestionText.setText("Question : " + q.getQuestionText());
        } else {
            lblQuestionText.setText("Examen : " + examComboBox.getValue().getTitle());
        }
        
        taStudentAnswer.setText(selectedSubmission.getStudentAnswer());
        
        lblAiScore.setText(selectedSubmission.getScore() != null ? String.valueOf(selectedSubmission.getScore()) : "—");
        taAiFeedback.setText(selectedSubmission.getAiFeedback());
        
        if (selectedSubmission.getPlagiarismScore() != null) {
            lblPlagiarismScore.setText("Plagiat: " + (int)(selectedSubmission.getPlagiarismScore() * 100) + "%");
            pbPlagiarism.setProgress(selectedSubmission.getPlagiarismScore());
        } else {
            lblPlagiarismScore.setText("Plagiat: —");
            pbPlagiarism.setProgress(0);
        }
    }

    @FXML
    private void handleAiGradeSingle() {
        if (selectedSubmission == null) return;
        
        showLoading(true);
        new Thread(() -> {
            try {
                // We send the exam context (title/desc) instead of a specific question
                AIGradingResult result = aiService.gradeAnswer(null, selectedSubmission.getStudentAnswer()); // Service handles null question as full exam
                Platform.runLater(() -> {
                    showLoading(false);
                    lblAiScore.setText(String.format("%.1f", result.getSuggestedScore()));
                    taAiFeedback.setText(result.getFeedback());
                    pbAiConfidence.setProgress(result.getConfidence());
                    lblConfidence.setText(String.format("Confiance IA: %d%%", (int)(result.getConfidence() * 100)));
                    
                    selectedSubmission.setScore(result.getSuggestedScore());
                    selectedSubmission.setAiFeedback(result.getFeedback());
                    tfManualScore.setText(String.valueOf(result.getSuggestedScore()));
                    submissionDao.update(selectedSubmission);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    showStatus("Erreur correction : " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleCompareSelected() {
        ObservableList<StudentSubmission> selectedItems = submissionsTable.getSelectionModel().getSelectedItems();
        if (selectedItems.size() < 2) {
            showStatus("Sélectionnez au moins 2 copies pour comparer.");
            return;
        }
        
        showLoading(true);
        new Thread(() -> {
            try {
                for (StudentSubmission item : selectedItems) {
                    ExamSubmission s = currentSubmissions.stream().filter(sub -> sub.getId() == item.getSubmissionId()).findFirst().get();
                    List<String> others = selectedItems.stream()
                        .filter(other -> other.getSubmissionId() != item.getSubmissionId())
                        .map(other -> currentSubmissions.stream().filter(sub -> sub.getId() == other.getSubmissionId()).findFirst().get().getStudentAnswer())
                        .collect(Collectors.toList());
                    
                    PlagiarismResult res = plagiarismService.checkPlagiarism(s.getStudentAnswer(), others);
                    s.setPlagiarismScore(res.getSimilarityScore());
                    submissionDao.update(s);
                }
                Platform.runLater(() -> {
                    showLoading(false);
                    loadSubmissions();
                    showStatus("Détection de plagiat sur sélection terminée.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> { showLoading(false); showStatus("Erreur plagiat : " + e.getMessage()); });
            }
        }).start();
    }

    @FXML
    private void handleGeneratePdfReport() {
        if (selectedSubmission == null) {
            showStatus("Sélectionnez une copie d'abord.");
            return;
        }
        
        try {
            // Mock student for PDF (ideally fetch from DB)
            User student = new User();
            student.setFirstName("Étudiant");
            student.setLastName("#" + selectedSubmission.getStudentId());
            
            java.io.File file = PdfGenerator.generateExamGradingReport(examComboBox.getValue(), selectedSubmission, student);
            showStatus("Rapport PDF généré : " + file.getName());
            
            // Open the file
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Erreur PDF : " + e.getMessage());
        }
    }

    @FXML
    private void handleAiGradeAll() {
        if (currentSubmissions == null || currentSubmissions.isEmpty()) {
            showStatus("Aucune copie à corriger.");
            return;
        }
        
        showLoading(true);
        new Thread(() -> {
            int success = 0;
            for (ExamSubmission s : currentSubmissions) {
                try {
                    AIGradingResult result = aiService.gradeAnswer(null, s.getStudentAnswer());
                    s.setScore(result.getSuggestedScore());
                    s.setAiFeedback(result.getFeedback());
                    s.setStatus(ExamSubmission.Status.GRADED);
                    submissionDao.update(s);
                    success++;
                } catch (Exception e) { 
                    System.err.println("Error grading submission #" + s.getId() + ": " + e.getMessage());
                }
            }
            final int count = success;
            Platform.runLater(() -> {
                showLoading(false);
                loadSubmissions();
                showStatus("Correction IA terminée : " + count + " copies traitées.");
            });
        }).start();
    }

    @FXML
    private void handleSaveManualScore() {
        if (selectedSubmission == null) return;
        try {
            double score = Double.parseDouble(tfManualScore.getText());
            selectedSubmission.setScore(score);
            selectedSubmission.setStatus(ExamSubmission.Status.GRADED);
            if (submissionDao.update(selectedSubmission)) {
                loadSubmissions();
                showStatus("Note enregistrée.");
            }
        } catch (NumberFormatException e) {
            showStatus("Score invalide.");
        }
    }

    @FXML
    private void handleSendEmail() {
        if (selectedSubmission == null) return;
        
        String studentEmail = "student" + selectedSubmission.getStudentId() + "@edusmart.com"; // Placeholder
        String examTitle = examComboBox.getValue().getTitle();
        String score = selectedSubmission.getScore() != null ? String.valueOf(selectedSubmission.getScore()) : "N/A";
        String feedback = selectedSubmission.getTeacherFeedback() != null ? selectedSubmission.getTeacherFeedback() : selectedSubmission.getAiFeedback();
        
        showLoading(true);
        new Thread(() -> {
            try {
                String htmlBody = MailSender.buildExamGradingEmailBody("Étudiant #" + selectedSubmission.getStudentId(), examTitle, score, feedback);
                MailSender.sendEmailWithAttachment(studentEmail, "Résultat Examen : " + examTitle, htmlBody, null);
                Platform.runLater(() -> {
                    showLoading(false);
                    showStatus("Email envoyé avec succès !");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showLoading(false);
                    showStatus("Échec de l'envoi de l'email.");
                    ex.printStackTrace();
                });
            }
        }).start();
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
    }

    private void showStatus(String msg) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            Platform.runLater(() -> statusLabel.setVisible(false));
        }).start();
    }
}
