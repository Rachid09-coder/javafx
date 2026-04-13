package com.edusmart.controller.teacher;

import com.edusmart.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * TeacherDashboardController - Teacher's main dashboard with key statistics.
 *
 * Team member: Implement loadStatistics() to fetch real data from services.
 */
public class TeacherDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label studentsCountLabel;
    @FXML private Label coursesCountLabel;
    @FXML private Label examsCountLabel;
    @FXML private Label revenueLabel;
    @FXML private Label recentActivityLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setWelcomeMessage();
        loadStatistics();
    }

    private void setWelcomeMessage() {
        if (welcomeLabel != null) {
            // TODO: Use SessionManager.getCurrentUser().getFullName()
            welcomeLabel.setText("Bienvenue, Enseignant!");
        }
    }

    /**
     * Loads KPI statistics for the dashboard.
     * TODO: Replace with actual service calls.
     */
    private void loadStatistics() {
        // TODO: Load from services
        // int studentCount = StudentService.getStudentCount();
        // int courseCount = CourseService.getCourseCountByTeacher(teacherId);
        // int examCount = ExamService.getExamCountByTeacher(teacherId);
        // double revenue = RevenueService.getMonthlyRevenue(teacherId);

        // Demo statistics
        if (studentsCountLabel != null) studentsCountLabel.setText("245");
        if (coursesCountLabel != null) coursesCountLabel.setText("12");
        if (examsCountLabel != null) examsCountLabel.setText("38");
        if (revenueLabel != null) revenueLabel.setText("4 820 €");
    }

    // ── Navigation handlers ──────────────────────────────────────────────

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
}
