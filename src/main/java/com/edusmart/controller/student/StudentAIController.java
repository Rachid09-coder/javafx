package com.edusmart.controller.student;

import com.edusmart.service.GeminiAiService;
import com.edusmart.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class StudentAIController implements Initializable {

    @FXML private TextField queryField;
    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScroll;

    private final GeminiAiService aiService = new GeminiAiService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initial greeting
        addMessageToChat("🤖 TutorIA", "Bonjour ! Je suis votre assistant personnel d'étude. Avez-vous besoin d'aide avec un exercice ou pour réviser un cours ?", "#F1F5F9");
    }

    @FXML
    private void handleAskAI(ActionEvent event) {
        String query = queryField.getText().trim();
        if (query.isEmpty()) return;

        // Add user message
        addMessageToChat("Vous", query, "#E0E7FF");
        queryField.clear();

        // Add loading placeholder
        Label loadingLabel = new Label("L'IA réfléchit...");
        loadingLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
        chatContainer.getChildren().add(loadingLabel);
        scrollToBottom();

        // Run AI request in background
        new Thread(() -> {
            try {
                String prompt = "Tu es un tuteur personnel pour un étudiant. " +
                        "Réponds à cette question de manière pédagogique et encourageante, en français : " + query;
                String response = aiService.generateContent(prompt);
                
                Platform.runLater(() -> {
                    chatContainer.getChildren().remove(loadingLabel);
                    addMessageToChat("🤖 TutorIA", response, "#F1F5F9");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatContainer.getChildren().remove(loadingLabel);
                    addMessageToChat("❌ Erreur", "Je n'ai pas pu vous répondre. (Peut-être utiliser le mode simulation?)", "#FEE2E2");
                });
            }
        }).start();
    }

    private void addMessageToChat(String sender, String message, String bgColor) {
        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10));
        bubble.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 8;");
        
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #1E293B;");
        
        bubble.getChildren().addAll(senderLabel, messageLabel);
        chatContainer.getChildren().add(bubble);
        scrollToBottom();
    }

    private void scrollToBottom() {
        chatScroll.layout();
        chatScroll.setVvalue(1.0);
    }

    // ── Navigation handlers ──────────────────────────────────────────────
    @FXML private void handleCourses(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES); }
    @FXML private void handleExams(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS); }
    @FXML private void handleBulletin(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_BULLETIN); }
    @FXML private void handleCertification(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_CERTIFICATION); }
    @FXML private void handleShop(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP); }
    @FXML private void handleStudentAI(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_AI); }
    @FXML private void handleProfile(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE); }
    @FXML private void handleLogout(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
