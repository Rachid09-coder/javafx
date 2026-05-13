package com.edusmart.util;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * SceneManager - Centralized scene/navigation management for EduSmart.
 *
 * Usage (from any controller):
 *   SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
 */
public class SceneManager {

    public enum Scene {
        LOGIN,
        SIGNUP,
        // Student
        STUDENT_DASHBOARD,
        STUDENT_COURSES,
        STUDENT_EXAMS,
        STUDENT_BULLETIN,
        STUDENT_CERTIFICATION,
        STUDENT_SHOP,
        STUDENT_ORDER_CHECKOUT,
        STUDENT_AI,
        STUDENT_TAKE_EXAM,
        // Teacher
        TEACHER_DASHBOARD,
        TEACHER_MANAGE_COURSES,
        TEACHER_MANAGE_MODULES,
        TEACHER_MANAGE_EXAMS,
        TEACHER_SHOP_MANAGEMENT,
        TEACHER_CATEGORY_MANAGEMENT,
        TEACHER_BULLETINS,
        TEACHER_CERTIFICATIONS,
        TEACHER_ANALYSIS_AI,
        TEACHER_SHOP_ANALYSIS_AI,
        TEACHER_STUDENT_MANAGEMENT,
        TEACHER_CALENDAR,
        TEACHER_GRADE_MANAGEMENT,
        TEACHER_EXAM_GRADING,
        TEACHER_METIER_MANAGEMENT,
        PROFILE
    }

    private static SceneManager instance;
    private Stage primaryStage;
    private javafx.scene.Scene mainScene;
    private BorderPane root;
    private com.edusmart.model.User currentUser;
    private java.util.Stack<Scene> history = new java.util.Stack<>();
    private Scene currentScene;
    private javafx.scene.layout.HBox globalHeader;
    private javafx.scene.control.Button backButton;

    private SceneManager() {}

    public com.edusmart.model.User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(com.edusmart.model.User currentUser) {
        this.currentUser = currentUser;
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
        root = new BorderPane();

        // Setup Global Header
        backButton = new javafx.scene.control.Button("⬅ Retour");
        backButton.getStyleClass().add("btn-back");
        backButton.setOnAction(e -> goBack());
        
        globalHeader = new javafx.scene.layout.HBox(backButton);
        globalHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        globalHeader.setPadding(new javafx.geometry.Insets(10, 24, 10, 24));
        globalHeader.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
        globalHeader.setVisible(false);
        globalHeader.setManaged(false);

        root.setTop(globalHeader);

        mainScene = new javafx.scene.Scene(root, 1200, 750);

        // Load global stylesheet
        URL cssUrl = getClass().getResource("/css/style.css");
        if (cssUrl != null) {
            mainScene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(mainScene);
    }

    public void navigateTo(Scene scene) {
        navigateTo(scene, true);
    }

    private void navigateTo(Scene scene, boolean saveToHistory) {
        String fxmlPath = getFxmlPath(scene);
        if (fxmlPath == null) return;
        try {
            // Track current scene before changing
            if (saveToHistory && currentScene != null) {
                if (history.isEmpty() || history.peek() != currentScene) {
                    history.push(currentScene);
                }
            }

            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML not found: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();
            this.currentScene = scene;

            // Manage global header visibility
            boolean isAuth = (scene == Scene.LOGIN || scene == Scene.SIGNUP);
            boolean usesOwnHeader = scene == Scene.TEACHER_MANAGE_EXAMS;
            if (globalHeader != null) {
                globalHeader.setVisible(!isAuth && !usesOwnHeader);
                globalHeader.setManaged(!isAuth && !usesOwnHeader);
                backButton.setDisable(history.isEmpty());
            }

            if (isAuth) {
                root.setLeft(null);
                root.setCenter(view);
                history.clear();
            } else {
                root.setCenter(view);
            }
        } catch (IOException e) {
            System.err.println("Failed to load scene: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public void goBack() {
        if (!history.isEmpty()) {
            navigateTo(history.pop(), false);
        }
    }

    public void setCurrentScene(Scene scene) {
        this.currentScene = scene;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private String getFxmlPath(Scene scene) {
        return switch (scene) {
            case LOGIN                        -> "/fxml/auth/login.fxml";
            case SIGNUP                       -> "/fxml/auth/signup.fxml";
            case STUDENT_DASHBOARD            -> "/fxml/student/dashboard.fxml";
            case STUDENT_COURSES              -> "/fxml/student/courses.fxml";
            case STUDENT_EXAMS                -> "/fxml/student/exams.fxml";
            case STUDENT_BULLETIN             -> "/fxml/student/bulletin.fxml";
            case STUDENT_CERTIFICATION        -> "/fxml/student/certification.fxml";
            case STUDENT_SHOP                 -> "/fxml/student/shop.fxml";
            case STUDENT_ORDER_CHECKOUT       -> "/fxml/student/order-checkout.fxml";
            case STUDENT_AI                   -> "/fxml/student/student-ai.fxml";
            case STUDENT_TAKE_EXAM            -> "/fxml/student/take-exam.fxml";
            case TEACHER_DASHBOARD            -> "/fxml/teacher/dashboard.fxml";
            case TEACHER_MANAGE_COURSES       -> "/fxml/teacher/manage-courses.fxml";
            case TEACHER_MANAGE_MODULES       -> "/fxml/teacher/manage-modules.fxml";
            case TEACHER_MANAGE_EXAMS         -> "/fxml/teacher/manage-exams.fxml";
            case TEACHER_SHOP_MANAGEMENT      -> "/fxml/teacher/shop-management.fxml";
            case TEACHER_CATEGORY_MANAGEMENT  -> "/fxml/teacher/category-management.fxml";
            case TEACHER_BULLETINS            -> "/fxml/teacher/bulletins.fxml";
            case TEACHER_CERTIFICATIONS       -> "/fxml/teacher/certifications.fxml";
            case TEACHER_METIER_MANAGEMENT    -> "/fxml/teacher/metier-management.fxml";
            case TEACHER_ANALYSIS_AI          -> "/fxml/teacher/analysis-ai.fxml";
            case TEACHER_SHOP_ANALYSIS_AI     -> "/fxml/teacher/shop-analysis-ai.fxml";
            case TEACHER_STUDENT_MANAGEMENT   -> "/fxml/teacher/student-management.fxml";
            case TEACHER_CALENDAR             -> "/fxml/teacher/calendar.fxml";
            case TEACHER_GRADE_MANAGEMENT     -> "/fxml/teacher/grade-management.fxml";
            case TEACHER_EXAM_GRADING         -> "/fxml/teacher/exam-grading.fxml";
            case PROFILE                     -> "/fxml/shared/profile.fxml";
        };
    }
}
