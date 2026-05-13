package com.edusmart.controller.student;

import com.edusmart.dao.jdbc.JdbcExamQuestionDao;
import com.edusmart.dao.jdbc.JdbcExamSubmissionDao;
import com.edusmart.model.Exam;
import com.edusmart.model.ExamQuestion;
import com.edusmart.model.ExamSubmission;
import com.edusmart.util.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TakeExamController implements Initializable {

    @FXML private Label timerLabel;
    @FXML private Label examTitleLabel;
    @FXML private Label moduleLabel;
    @FXML private Label questionNumberLabel;
    @FXML private Label pointsLabel;
    @FXML private Text questionText;
    @FXML private VBox optionsContainer;
    @FXML private TextArea openAnswerArea;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressText;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button submitButton;
    @FXML private VBox questionListNav;

    private static Exam currentExam;
    private List<ExamQuestion> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private Map<Integer, String> answers = new HashMap<>(); // QuestionIndex -> Answer
    private int remainingSeconds;
    private Timeline timeline;

    private final JdbcExamQuestionDao questionDao = new JdbcExamQuestionDao();
    private final JdbcExamSubmissionDao submissionDao = new JdbcExamSubmissionDao();

    public static void setExam(Exam exam) {
        currentExam = exam;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (currentExam == null) {
            handleExit(null);
            return;
        }

        examTitleLabel.setText(currentExam.getTitle());
        moduleLabel.setText("Module : " + currentExam.getModuleName());
        
        loadQuestions();
        startTimer();
        showQuestion(0);
        updateProgress();
    }

    private void loadQuestions() {
        questions = questionDao.findByExamId(currentExam.getId());
        if (questions.isEmpty()) {
            // Mock questions if empty for testing
            mockQuestions();
        }
    }

    private void mockQuestions() {
        // This is just for demonstration if no questions exist in DB
        ExamQuestion q1 = new ExamQuestion();
        q1.setQuestionText("Quelle est la définition d'un algorithme ?");
        q1.setQuestionType(ExamQuestion.QuestionType.OPEN_ENDED);
        q1.setMaxPoints(2.0);
        questions.add(q1);

        ExamQuestion q2 = new ExamQuestion();
        q2.setQuestionText("Quel langage est utilisé pour le style en JavaFX ?");
        q2.setQuestionType(ExamQuestion.QuestionType.MCQ);
        q2.setOptions("Fxml|Java|CSS|Json");
        q2.setMaxPoints(1.0);
        questions.add(q2);
    }

    private void startTimer() {
        remainingSeconds = (currentExam.getDuration() != null ? currentExam.getDuration() : 60) * 60;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerLabel();
            if (remainingSeconds <= 0) {
                timeline.stop();
                handleSubmit(null);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimerLabel() {
        int h = remainingSeconds / 3600;
        int m = (remainingSeconds % 3600) / 60;
        int s = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d:%02d", h, m, s));
        if (remainingSeconds < 300) { // Less than 5 mins
            timerLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 24px; -fx-font-weight: bold;");
        }
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        
        // Save current answer before switching
        saveCurrentAnswer();

        currentQuestionIndex = index;
        ExamQuestion q = questions.get(index);
        
        questionNumberLabel.setText("Question " + (index + 1) + "/" + questions.size());
        pointsLabel.setText(q.getMaxPoints() + " pts");
        questionText.setText(q.getQuestionText());

        optionsContainer.getChildren().clear();
        if (q.getQuestionType() == ExamQuestion.QuestionType.MCQ) {
            openAnswerArea.setVisible(false);
            openAnswerArea.setManaged(false);
            optionsContainer.setVisible(true);
            optionsContainer.setManaged(true);
            
            ToggleGroup group = new ToggleGroup();
            String[] opts = q.getOptions() != null ? q.getOptions().split("\\|") : new String[0];
            String savedAnswer = answers.get(index);

            for (String opt : opts) {
                RadioButton rb = new RadioButton(opt);
                rb.setToggleGroup(group);
                rb.setStyle("-fx-font-size: 16px;");
                if (opt.equals(savedAnswer)) rb.setSelected(true);
                optionsContainer.getChildren().add(rb);
            }
        } else {
            optionsContainer.setVisible(false);
            optionsContainer.setManaged(false);
            openAnswerArea.setVisible(true);
            openAnswerArea.setManaged(true);
            openAnswerArea.setText(answers.getOrDefault(index, ""));
        }

        prevButton.setDisable(index == 0);
        nextButton.setVisible(index < questions.size() - 1);
        submitButton.setVisible(index == questions.size() - 1);
        
        updateProgress();
    }

    private void saveCurrentAnswer() {
        if (questions.isEmpty()) return;
        ExamQuestion q = questions.get(currentQuestionIndex);
        if (q.getQuestionType() == ExamQuestion.QuestionType.MCQ) {
            for (javafx.scene.Node node : optionsContainer.getChildren()) {
                if (node instanceof RadioButton && ((RadioButton) node).isSelected()) {
                    answers.put(currentQuestionIndex, ((RadioButton) node).getText());
                    break;
                }
            }
        } else {
            answers.put(currentQuestionIndex, openAnswerArea.getText());
        }
    }

    @FXML
    private void handlePrev(ActionEvent event) {
        showQuestion(currentQuestionIndex - 1);
    }

    @FXML
    private void handleNext(ActionEvent event) {
        showQuestion(currentQuestionIndex + 1);
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        saveCurrentAnswer();
        if (timeline != null) timeline.stop();

        // Save submissions to DB
        int studentId = 2; // Mock
        for (int i = 0; i < questions.size(); i++) {
            ExamSubmission sub = new ExamSubmission();
            sub.setStudentId(studentId);
            sub.setExamId(currentExam.getId());
            sub.setQuestionId(questions.get(i).getId());
            sub.setStudentAnswer(answers.getOrDefault(i, ""));
            sub.setSubmissionDate(LocalDateTime.now());
            sub.setStatus(ExamSubmission.Status.SUBMITTED);
            submissionDao.create(sub);
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Examen Terminé");
        alert.setHeaderText("Votre examen a été soumis avec succès.");
        alert.setContentText("Vous allez être redirigé vers la liste des examens.");
        alert.showAndWait();

        handleExit(null);
    }

    @FXML
    private void handleExit(ActionEvent event) {
        if (timeline != null) timeline.stop();
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS);
    }

    private void updateProgress() {
        double progress = (double) (currentQuestionIndex + 1) / questions.size();
        progressBar.setProgress(progress);
        progressText.setText((int)(progress * 100) + "% complété");
    }
}
