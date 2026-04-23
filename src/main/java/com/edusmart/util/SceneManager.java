package com.edusmart.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

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
        STUDENT_COURSES,
        STUDENT_EXAMS,
        STUDENT_BULLETIN,
        STUDENT_CERTIFICATION,
        STUDENT_SHOP,
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
        TEACHER_STUDENT_MANAGEMENT,
        TEACHER_GRADE_MANAGEMENT,
        TEACHER_METIER_MANAGEMENT,
        PROFILE
    }

    private static SceneManager instance;
    private Stage primaryStage;
    private javafx.scene.Scene mainScene;
    private BorderPane root;
    private com.edusmart.model.User currentUser;

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
        mainScene = new javafx.scene.Scene(root, 1200, 750);

        // Load global stylesheet
        URL cssUrl = getClass().getResource("/css/style.css");
        if (cssUrl != null) {
            mainScene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(mainScene);
    }

    public void navigateTo(Scene scene) {
        String fxmlPath = getFxmlPath(scene);
        if (fxmlPath == null) return;
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML not found: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            if (scene == Scene.LOGIN || scene == Scene.SIGNUP) {
                // Full-screen auth views (no sidebar)
                root.setLeft(null);
                root.setCenter(view);
            } else {
                // Application views keep sidebar in left, content in center
                root.setCenter(view);
            }
        } catch (IOException e) {
            System.err.println("Failed to load scene: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private String getFxmlPath(Scene scene) {
        return switch (scene) {
            case LOGIN                        -> "/fxml/auth/login.fxml";
            case SIGNUP                       -> "/fxml/auth/signup.fxml";
            case STUDENT_COURSES              -> "/fxml/student/courses.fxml";
            case STUDENT_EXAMS                -> "/fxml/student/exams.fxml";
            case STUDENT_BULLETIN             -> "/fxml/student/bulletin.fxml";
            case STUDENT_CERTIFICATION        -> "/fxml/student/certification.fxml";
            case STUDENT_SHOP                 -> "/fxml/student/shop.fxml";
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
            case TEACHER_STUDENT_MANAGEMENT   -> "/fxml/teacher/student-management.fxml";
            case TEACHER_GRADE_MANAGEMENT     -> "/fxml/teacher/grade-management.fxml";
            case PROFILE                     -> "/fxml/shared/profile.fxml";
        };
    }
}
