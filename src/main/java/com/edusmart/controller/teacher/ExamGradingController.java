package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcExamDao;
import com.edusmart.dao.jdbc.JdbcExamQuestionDao;
import com.edusmart.dao.jdbc.JdbcExamSubmissionDao;
import com.edusmart.model.*;
import com.edusmart.service.AIGradingService;
import com.edusmart.service.ExamService;
import com.edusmart.service.PlagiarismDetectionService;
import com.edusmart.service.impl.AIGradingServiceImpl;
import com.edusmart.service.impl.EmailServiceImpl;
import com.edusmart.service.impl.ExamServiceImpl;
import com.edusmart.service.impl.PlagiarismDetectionServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * ExamGradingController — Advanced AI-assisted exam grading panel.
 *
 * Features:
 *  - Browse exams and their questions
 *  - View all student submissions per question
 *  - AI-grade a single answer or all at once (async, non-blocking)
 *  - Run plagiarism detection across all submissions
 *  - Manually override the AI score
 *  - Send email notification to a student with their result
 */
public class ExamGradingController implements Initializable {

    // ── Exam selector ────────────────────────────────────────────────────
    @FXML private ComboBox<Exam>         examComboBox;
    @FXML private ComboBox<ExamQuestion> questionComboBox;

    // ── Submissions table ────────────────────────────────────────────────
    @FXML private TableView<ExamSubmission>         submissionsTable;
    @FXML private TableColumn<ExamSubmission, String>                     colStudentName;
    @FXML private TableColumn<ExamSubmission, ExamSubmission.SubmissionStatus> colStatus;
    @FXML private TableColumn<ExamSubmission, Double>                     colScore;
    @FXML private TableColumn<ExamSubmission, Double>                     colPlagiarism;

    // ── Detail panel ─────────────────────────────────────────────────────
    @FXML private Label     lblStudentName;
    @FXML private Label     lblQuestionText;
    @FXML private TextArea  taStudentAnswer;
    @FXML private TextArea  taAiFeedback;
    @FXML private TextArea  taStrengths;
    @FXML private TextArea  taImprovements;
    @FXML private TextField tfManualScore;
    @FXML private Label     lblAiScore;
    @FXML private Label     lblConfidence;
    @FXML private Label     lblPlagiarismScore;
    @FXML private ProgressBar pbPlagiarism;
    @FXML private ProgressBar pbAiConfidence;

    // ── Status ───────────────────────────────────────────────────────────
    @FXML private Label   statusLabel;
    @FXML private VBox    loadingOverlay;

    private final JdbcExamQuestionDao  questionDao    = new JdbcExamQuestionDao();
    private final JdbcExamSubmissionDao submissionDao  = new JdbcExamSubmissionDao();
    private final ExamService          examService     = new ExamServiceImpl(new JdbcExamDao());
    private final AIGradingService     aiService       = new AIGradingServiceImpl();
    private final PlagiarismDetectionService plagiarismService = new PlagiarismDetectionServiceImpl();
    private final EmailServiceImpl     emailService    = new EmailServiceImpl();

    private final ObservableList<ExamSubmission> submissionList = FXCollections.observableArrayList();
    private ExamSubmission selectedSubmission;
    private ExamQuestion   selectedQuestion;

    // ── Initialise ────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadExams();
        if (loadingOverlay != null) loadingOverlay.setVisible(false);
    }

    private void setupTable() {
        if (colStudentName != null)
            colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        if (colStatus != null) {
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colStatus.setCellFactory(col ->
                new TableCell<ExamSubmission, ExamSubmission.SubmissionStatus>() {
                    @Override
                    protected void updateItem(ExamSubmission.SubmissionStatus v, boolean empty) {
                        super.updateItem(v, empty);
                        if (empty || v == null) { setText(null); setStyle(""); return; }
                        setText(v.name());
                        String color = switch (v) {
                            case AI_GRADED          -> "#3B82F6";
                            case MANUALLY_GRADED    -> "#10B981";
                            case PLAGIARISM_FLAGGED -> "#EF4444";
                            default                 -> "#F59E0B";
                        };
                        setStyle("-fx-text-fill: " + color + ";");
                    }
                }
            );
        }
        if (colScore != null)      colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        if (colPlagiarism != null) colPlagiarism.setCellValueFactory(new PropertyValueFactory<>("plagiarismScore"));
        if (submissionsTable != null) {
            submissionsTable.setItems(submissionList);
            submissionsTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, o, n) -> populateDetail(n));
        }
    }

    private void loadExams() {
        try {
            List<Exam> exams = examService.getAllExams();
            if (examComboBox != null) {
                examComboBox.setItems(FXCollections.observableArrayList(exams));
                examComboBox.setConverter(new javafx.util.StringConverter<>() {
                    @Override public String toString(Exam e)  { return e == null ? "" : e.getTitle(); }
                    @Override public Exam fromString(String s){ return null; }
                });
            }
        } catch (Exception ex) {
            showStatus("Erreur chargement examens: " + ex.getMessage(), true);
        }
    }

    // ── FXML handlers ─────────────────────────────────────────────────────

    @FXML
    private void handleExamSelected(ActionEvent event) {
        Exam exam = examComboBox != null ? examComboBox.getValue() : null;
        if (exam == null) return;
        try {
            List<ExamQuestion> questions = questionDao.findByExamId(exam.getId());
            if (questionComboBox != null) {
                questionComboBox.setItems(FXCollections.observableArrayList(questions));
                questionComboBox.setConverter(new javafx.util.StringConverter<>() {
                    @Override public String toString(ExamQuestion q) { return q == null ? "" : q.toString(); }
                    @Override public ExamQuestion fromString(String s){ return null; }
                });
            }
            submissionList.clear();
            clearDetail();
        } catch (Exception ex) {
            showStatus("Erreur chargement questions: " + ex.getMessage(), true);
        }
    }

    @FXML
    private void handleQuestionSelected(ActionEvent event) {
        selectedQuestion = questionComboBox != null ? questionComboBox.getValue() : null;
        if (selectedQuestion == null) return;
        if (lblQuestionText != null) lblQuestionText.setText(selectedQuestion.getQuestionText());
        try {
            List<ExamSubmission> subs = submissionDao.findByQuestionId(selectedQuestion.getId());
            submissionList.setAll(subs);
        } catch (Exception ex) {
            showStatus("Erreur chargement soumissions: " + ex.getMessage(), true);
        }
    }

    /** AI-grade the currently selected student answer. */
    @FXML
    private void handleAiGradeSingle(ActionEvent event) {
        if (selectedSubmission == null || selectedQuestion == null) {
            showStatus("Sélectionnez une soumission.", true); return;
        }
        setLoading(true);
        showStatus("IA en cours de correction…", false);
        aiService.gradeAnswerAsync(selectedQuestion, selectedSubmission)
                .whenComplete((result, ex) -> Platform.runLater(() -> {
                    setLoading(false);
                    if (ex != null || !result.isSuccess()) {
                        showStatus("Erreur IA: " + (ex != null ? ex.getMessage() : result.getErrorMessage()), true);
                        return;
                    }
                    applyAiResult(selectedSubmission, result);
                    populateDetail(selectedSubmission);
                    showStatus("Correction IA terminée!", false);
                }));
    }

    /** AI-grade ALL submissions for the selected question at once. */
    @FXML
    private void handleAiGradeAll(ActionEvent event) {
        if (selectedQuestion == null || submissionList.isEmpty()) {
            showStatus("Sélectionnez une question avec des soumissions.", true); return;
        }
        setLoading(true);
        showStatus("Correction IA de toutes les copies…", false);
        List<ExamSubmission> subs = List.copyOf(submissionList);
        aiService.gradeAllAnswersAsync(selectedQuestion, subs)
                .whenComplete((results, ex) -> Platform.runLater(() -> {
                    setLoading(false);
                    if (ex != null) {
                        showStatus("Erreur IA: " + ex.getMessage(), true); return;
                    }
                    for (int i = 0; i < results.size(); i++) {
                        if (results.get(i).isSuccess()) applyAiResult(subs.get(i), results.get(i));
                    }
                    submissionsTable.refresh();
                    showStatus("Correction IA de " + results.size() + " copies terminée!", false);
                }));
    }

    /** Run plagiarism detection on all submissions for the selected question. */
    @FXML
    private void handlePlagiarismCheck(ActionEvent event) {
        if (selectedQuestion == null || submissionList.isEmpty()) {
            showStatus("Sélectionnez une question avec des soumissions.", true); return;
        }
        setLoading(true);
        showStatus("Détection de plagiat en cours…", false);
        List<ExamSubmission> subs = List.copyOf(submissionList);
        plagiarismService.detectPlagiarismAsync(subs)
                .whenComplete((results, ex) -> Platform.runLater(() -> {
                    setLoading(false);
                    if (ex != null) {
                        showStatus("Erreur plagiat: " + ex.getMessage(), true); return;
                    }
                    long flagged = 0;
                    for (int i = 0; i < results.size(); i++) {
                        PlagiarismResult pr = results.get(i);
                        ExamSubmission s = subs.get(i);
                        s.setPlagiarismScore(pr.getMaxSimilarityScore());
                        if (pr.isFlagged()) {
                            s.setStatus(ExamSubmission.SubmissionStatus.PLAGIARISM_FLAGGED);
                            flagged++;
                        }
                        try { submissionDao.update(s); } catch (Exception ignored) {}
                    }
                    submissionsTable.refresh();
                    showStatus("Plagiat: " + flagged + " copie(s) signalée(s) (seuil 70%)", flagged > 0);
                }));
    }

    /** Save manual score override. */
    @FXML
    private void handleSaveManualScore(ActionEvent event) {
        if (selectedSubmission == null) { showStatus("Sélectionnez une soumission.", true); return; }
        try {
            double score = Double.parseDouble(tfManualScore.getText().trim());
            double max   = selectedQuestion != null ? selectedQuestion.getMaxPoints() : 20;
            if (score < 0 || score > max) {
                showStatus("Score invalide (0–" + max + ").", true); return;
            }
            selectedSubmission.setScore(score);
            selectedSubmission.setStatus(ExamSubmission.SubmissionStatus.MANUALLY_GRADED);
            selectedSubmission.setGradedAt(LocalDateTime.now());
            submissionDao.update(selectedSubmission);
            submissionsTable.refresh();
            showStatus("Score sauvegardé: " + score + "/" + max, false);
        } catch (NumberFormatException e) {
            showStatus("Entrez un nombre valide.", true);
        }
    }

    /** Send email notification to the selected student. */
    @FXML
    private void handleSendEmail(ActionEvent event) {
        if (selectedSubmission == null) { showStatus("Sélectionnez une soumission.", true); return; }
        String email = selectedSubmission.getStudentEmail();
        if (email == null || email.isBlank()) { showStatus("Email étudiant introuvable.", true); return; }

        String html = buildResultEmail(selectedSubmission, selectedQuestion);
        new Thread(() -> {
            emailService.sendEmail(email, "Résultat de votre examen — EduSmart", html);
            Platform.runLater(() -> showStatus("Email envoyé à " + email, false));
        }, "email-notify").start();
        showStatus("Envoi de l'email en cours…", false);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void applyAiResult(ExamSubmission s, AIGradingResult r) {
        s.setScore(r.getEstimatedScore());
        s.setMaxScore(r.getMaxScore());
        s.setAiFeedback(r.getFeedback());
        s.setAiConfidence(r.getConfidenceLevel());
        s.setStatus(ExamSubmission.SubmissionStatus.AI_GRADED);
        s.setGradedAt(LocalDateTime.now());
        try { submissionDao.update(s); } catch (Exception ignored) {}
    }

    private void populateDetail(ExamSubmission s) {
        selectedSubmission = s;
        if (s == null) return;
        if (lblStudentName != null) lblStudentName.setText(s.getStudentName() != null ? s.getStudentName() : "Étudiant #" + s.getStudentId());
        if (taStudentAnswer != null) taStudentAnswer.setText(s.getStudentAnswer());
        if (taAiFeedback != null) taAiFeedback.setText(s.getAiFeedback());
        if (lblAiScore != null)   lblAiScore.setText(String.format("%.1f / %.1f", s.getScore(), s.getMaxScore()));
        if (lblConfidence != null) lblConfidence.setText(String.format("Confiance IA: %.0f%%", s.getAiConfidence() * 100));
        if (pbAiConfidence != null) pbAiConfidence.setProgress(s.getAiConfidence());
        if (lblPlagiarismScore != null) lblPlagiarismScore.setText(String.format("Plagiat: %.1f%%", s.getPlagiarismScore()));
        if (pbPlagiarism != null) {
            pbPlagiarism.setProgress(s.getPlagiarismScore() / 100.0);
            pbPlagiarism.setStyle(s.getPlagiarismScore() >= 70
                    ? "-fx-accent: #EF4444;" : "-fx-accent: #10B981;");
        }
        if (tfManualScore != null) tfManualScore.setText(String.valueOf(s.getScore()));
    }

    private void clearDetail() {
        if (lblStudentName != null) lblStudentName.setText("—");
        if (taStudentAnswer != null) taStudentAnswer.clear();
        if (taAiFeedback != null) taAiFeedback.clear();
        if (lblAiScore != null) lblAiScore.setText("—");
        if (lblConfidence != null) lblConfidence.setText("—");
        if (lblPlagiarismScore != null) lblPlagiarismScore.setText("—");
        if (tfManualScore != null) tfManualScore.clear();
    }

    private void showStatus(String msg, boolean isError) {
        if (statusLabel == null) return;
        statusLabel.setText(msg);
        statusLabel.setStyle(isError
                ? "-fx-text-fill:#EF4444;-fx-background-color:rgba(239,68,68,0.1);-fx-background-radius:6;-fx-padding:6 12;"
                : "-fx-text-fill:#10B981;-fx-background-color:rgba(16,185,129,0.1);-fx-background-radius:6;-fx-padding:6 12;");
        statusLabel.setVisible(true);
    }

    private void setLoading(boolean loading) {
        if (loadingOverlay != null) loadingOverlay.setVisible(loading);
    }

    private String buildResultEmail(ExamSubmission s, ExamQuestion q) {
        String name  = s.getStudentName() != null ? s.getStudentName() : "Étudiant";
        String score = String.format("%.1f / %.1f", s.getScore(), s.getMaxScore());
        String pct   = String.format("%.0f%%", s.getScorePercentage());
        String feedback = s.getAiFeedback() != null ? s.getAiFeedback() : "Aucun feedback disponible.";
        return "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:24px;border:1px solid #e2e8f0;border-radius:12px'>"
             + "<h2 style='color:#4F46E5'>📋 Résultat de votre examen — EduSmart</h2>"
             + "<p>Bonjour <strong>" + name + "</strong>,</p>"
             + "<p>Voici le résultat de votre évaluation :</p>"
             + "<div style='background:#f8fafc;padding:16px;border-radius:8px;margin:16px 0'>"
             + "<p><strong>Question :</strong> " + (q != null ? q.getQuestionText() : "—") + "</p>"
             + "<p style='font-size:24px;font-weight:bold;color:#4F46E5'>Score : " + score + " (" + pct + ")</p>"
             + "</div>"
             + "<h3>Feedback :</h3><p>" + feedback + "</p>"
             + "<p style='font-size:11px;color:#94a3b8;margin-top:24px'>EduSmart Educational Platform</p>"
             + "</div>";
    }

    // ── Navigation ────────────────────────────────────────────────────────

    @FXML private void handleDashboard(ActionEvent e)         { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD); }
    @FXML private void handleManageCourses(ActionEvent e)     { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES); }
    @FXML private void handleManageModules(ActionEvent e)     { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES); }
    @FXML private void handleManageExams(ActionEvent e)       { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS); }
    @FXML private void handleExamGrading(ActionEvent e)       { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_EXAM_GRADING); }
    @FXML private void handleBulletins(ActionEvent e)         { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS); }
    @FXML private void handleCertifications(ActionEvent e)    { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS); }
    @FXML private void handleAnalysisAI(ActionEvent e)        { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI); }
    @FXML private void handleStudentManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT); }
    @FXML private void handleShopManagement(ActionEvent e)    { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT); }
    @FXML private void handleLogout(ActionEvent e)            { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
