package com.edusmart.controller.student;

import com.edusmart.model.Course;
import com.edusmart.util.SceneManager;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import com.edusmart.service.AIService;
import com.edusmart.service.impl.AIServiceImpl;

/**
 * CoursesController - Student view for browsing available courses.
 *
 * Team member: Implement loadCourses() to fetch data from your service layer.
 */
public class CoursesController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private FlowPane coursesContainer;
    @FXML private Label coursesCountLabel;
    @FXML private ProgressIndicator loadingIndicator;

    // AI Chat UI Elements
    @FXML private VBox chatPanel;
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField chatInputField;
    @FXML private Label chatContextLabel;
    @FXML private HBox loadingIndicatorBox;

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private AIService aiService;
    private Course selectedCourseForChat;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        aiService = new AIServiceImpl();
        setupFilters();
        loadCourses();
    }

    private void setupFilters() {
        if (categoryFilter != null) {
            categoryFilter.getItems().addAll("Tous", "Mathématiques", "Informatique", "Sciences", "Langues", "Arts");
            categoryFilter.setValue("Tous");
        }
        if (statusFilter != null) {
            statusFilter.getItems().addAll("Tous", "Actif", "Brouillon", "Archivé");
            statusFilter.setValue("Tous");
        }
    }

    /**
     * Loads all courses available to the student.
     * TODO: Replace with actual service call.
     */
    private void loadCourses() {
        // TODO: Load from service
        // courseList.setAll(CourseService.getAllCourses());
        // renderCourseCards();

        // Demo data
        courseList.setAll(
            new Course(1, "Introduction à Java", "Prof. Martin", 8, 40, Course.Status.ACTIVE),
            new Course(2, "Mathématiques Avancées", "Prof. Dupont", 12, 60, Course.Status.ACTIVE),
            new Course(3, "Physique Quantique", "Prof. Bernard", 10, 50, Course.Status.ACTIVE),
            new Course(4, "Anglais Professionnel", "Prof. Smith", 6, 30, Course.Status.DRAFT)
        );

        updateCoursesCount();
    }

    private void updateCoursesCount() {
        if (coursesCountLabel != null) {
            coursesCountLabel.setText(courseList.size() + " cours disponibles");
        }
    }

    /**
     * Handles course search by title.
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        // TODO: Filter courseList by query and re-render
    }

    /**
     * Handles filter changes.
     */
    @FXML
    private void handleFilterChange(ActionEvent event) {
        // TODO: Apply category/status filters and re-render
    }

    /**
     * Opens the detail view for a selected course.
     * TODO: Implement course detail navigation.
     */
    @FXML
    private void handleTestViewCourse(ActionEvent event) {
        if (!courseList.isEmpty()) {
            handleViewCourse(courseList.get(0));
        }
    }

    public void handleViewCourse(Course course) {
        selectedCourseForChat = course;
        
        // Open the chat panel
        chatPanel.setVisible(true);
        chatPanel.setManaged(true);
        
        chatContextLabel.setText("Contexte: " + course.getTitle());
        chatMessagesContainer.getChildren().clear();
        addChatMessage("Système", "Vous consultez le cours : " + course.getTitle() + "\nComment puis-je vous aider ?", false);
    }
    
    @FXML
    private void handleCloseChat(ActionEvent event) {
        chatPanel.setVisible(false);
        chatPanel.setManaged(false);
        selectedCourseForChat = null;
    }

    @FXML
    private void handleAiSummarize(ActionEvent event) {
        sendToAI("Fais un résumé clair et concis de ce cours en 3 points.");
    }

    @FXML
    private void handleAiExercises(ActionEvent event) {
        sendToAI("Génère 2 petits exercices pratiques ou questions pour tester mes connaissances sur ce cours.");
    }

    @FXML
    private void handleAiExplain(ActionEvent event) {
        sendToAI("Explique-moi les concepts de ce cours comme si j'avais 10 ans, avec une analogie simple.");
    }

    @FXML
    private void handleSendChatMessage(ActionEvent event) {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            chatInputField.clear();
            sendToAI(message);
        }
    }

    private void sendToAI(String message) {
        if (selectedCourseForChat == null) {
            addChatMessage("Système", "Veuillez d'abord sélectionner un cours.", false);
            return;
        }

        addChatMessage("Vous", message, true);
        
        loadingIndicatorBox.setVisible(true);
        loadingIndicatorBox.setManaged(true);
        
        String context = "Titre: " + selectedCourseForChat.getTitle() + "\nDescription: " + 
                (selectedCourseForChat.getDescription() != null ? selectedCourseForChat.getDescription() : "Aucune description.");

        aiService.askAI(message, context).thenAccept(response -> {
            Platform.runLater(() -> {
                loadingIndicatorBox.setVisible(false);
                loadingIndicatorBox.setManaged(false);
                addChatMessage("IA", response, false);
            });
        });
    }

    private void addChatMessage(String sender, String text, boolean isUser) {
        VBox bubble = new VBox();
        bubble.setPadding(new Insets(10));
        bubble.setSpacing(4);
        
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");
        
        Label textLabel = new Label(text);
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (isUser ? "#FFFFFF" : "#1E293B") + ";");
        
        bubble.getChildren().addAll(senderLabel, textLabel);
        
        if (isUser) {
            bubble.setStyle("-fx-background-color: #4F46E5; -fx-background-radius: 12 12 0 12;");
            bubble.setAlignment(Pos.CENTER_RIGHT);
        } else {
            bubble.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 12 12 12 0;");
            bubble.setAlignment(Pos.CENTER_LEFT);
        }
        
        HBox row = new HBox(bubble);
        row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));
        
        chatMessagesContainer.getChildren().add(row);
    }

    public List<Course> getCourseList() {
        return courseList;
    }

    // ── Navigation handlers ──────────────────────────────────────────────

    @FXML private void handleCourses(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES);
    }

    @FXML private void handleExams(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS);
    }

    @FXML private void handleBulletin(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_BULLETIN);
    }

    @FXML private void handleCertification(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_CERTIFICATION);
    }

    @FXML private void handleShop(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP);
    }

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
