package com.edusmart.controller.teacher;

import com.edusmart.model.ExamQuestion;
import com.edusmart.model.QuizResult;
import com.edusmart.service.GeminiAiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class GenerateQuizController implements Initializable {

    @FXML private TextField subjectField;
    @FXML private Spinner<Integer> numQuestionsSpinner;
    @FXML private ComboBox<String> difficultyComboBox;
    @FXML private ComboBox<String> levelComboBox;

    private final GeminiAiService aiService = new GeminiAiService();
    private final Gson gson = new Gson();
    private Consumer<QuizResult> onGeneratedCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        numQuestionsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5));
        difficultyComboBox.getItems().addAll("Facile", "Moyen", "Difficile");
        difficultyComboBox.setValue("Moyen");
        
        levelComboBox.getItems().addAll(
            "1ère année Ingénierie", "2ème année Ingénierie", "3ème année Ingénierie",
            "1ère année Licence", "2ème année Licence", "3ème année Licence",
            "Master 1", "Master 2"
        );
        levelComboBox.setValue("1ère année Ingénierie");
    }

    public void setOnGeneratedCallback(Consumer<QuizResult> callback) {
        this.onGeneratedCallback = callback;
    }

    @FXML
    private void handleGenerate(ActionEvent event) {
        String subject = subjectField.getText();
        if (subject == null || subject.isBlank()) {
            showAlert("Erreur", "Veuillez saisir une matière.");
            return;
        }

        int count = numQuestionsSpinner.getValue();
        String diff = difficultyComboBox.getValue();
        String level = levelComboBox.getValue();

        Stage stage = (Stage) subjectField.getScene().getWindow();
        
        // Visual feedback
        subjectField.setDisable(true);
        numQuestionsSpinner.setDisable(true);
        difficultyComboBox.setDisable(true);
        levelComboBox.setDisable(true);
        
        new Thread(() -> {
            try {
                String json = aiService.generateQuiz(subject, count, diff, level);
                
                // Robust JSON extraction
                if (json.contains("[") && json.contains("]")) {
                    json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
                }
                
                List<ExamQuestion> questions = gson.fromJson(json, new TypeToken<List<ExamQuestion>>(){}.getType());
                QuizResult result = new QuizResult(subject, count, diff, level, questions);
                
                Platform.runLater(() -> {
                    if (onGeneratedCallback != null) {
                        onGeneratedCallback.accept(result);
                    }
                    stage.close();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    subjectField.setDisable(false);
                    numQuestionsSpinner.setDisable(false);
                    difficultyComboBox.setDisable(false);
                    levelComboBox.setDisable(false);
                    showAlert("Erreur IA", "Désolé, l'IA n'a pas pu générer le quiz. \n\n" +
                             "Détails : " + ex.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        ((Stage) subjectField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
