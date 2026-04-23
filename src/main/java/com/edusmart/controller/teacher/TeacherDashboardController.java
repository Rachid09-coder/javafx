package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.dao.jdbc.JdbcExamDao;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.service.CourseService;
import com.edusmart.service.ExamService;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.CourseServiceImpl;
import com.edusmart.service.impl.ExamServiceImpl;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * TeacherDashboardController - Teacher's main dashboard with key statistics.
 */
public class TeacherDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label studentsCountLabel;
    @FXML private Label coursesCountLabel;
    @FXML private Label examsCountLabel;
    @FXML private Label revenueLabel;
    @FXML private Label recentActivityLabel;

    private final UserService userService = new UserServiceImpl(new JdbcUserDao());
    private final CourseService courseService = new CourseServiceImpl(new JdbcCourseDao());
    private final ExamService examService = new ExamServiceImpl(new JdbcExamDao());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setWelcomeMessage();
        loadStatistics();
    }

    private void setWelcomeMessage() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, Enseignant!");
        }
    }

    private void loadStatistics() {
        try {
            int studentCount = userService.getAllUsers().size();
            int courseCount = courseService.getAllCourses().size();
            int examCount = examService.getAllExams().size();

            if (studentsCountLabel != null) studentsCountLabel.setText(String.valueOf(studentCount));
            if (coursesCountLabel != null) coursesCountLabel.setText(String.valueOf(courseCount));
            if (examsCountLabel != null) examsCountLabel.setText(String.valueOf(examCount));
            if (revenueLabel != null) revenueLabel.setText("4 820 DT");
        } catch (Exception e) {
            System.err.println("Failed to load statistics: " + e.getMessage());
        }
    }

    // ── Navigation handlers ──────────────────────────────────────────────

    @FXML private void handleDashboard(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD);
    }

    @FXML private void handleManageCourses(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES);
    }

    @FXML private void handleManageModules(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES);
    }

    @FXML private void handleManageExams(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS);
    }

    @FXML private void handleShopManagement(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT);
    }

    @FXML private void handleBulletins(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS);
    }

    @FXML private void handleCertifications(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS);
    }

    @FXML private void handleAnalysisAI(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI);
    }

    @FXML private void handleStudentManagement(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT);
    }

    @FXML private void handleMetierManagement(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_METIER_MANAGEMENT);
    }

    @FXML private void handleProfile(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE);
    }

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
