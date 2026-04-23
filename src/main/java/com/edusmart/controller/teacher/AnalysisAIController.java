package com.edusmart.controller.teacher;

import com.edusmart.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * AnalysisAIController - Teacher interface for AI-powered analytics and insights.
 * AnalysisAIController - Teacher interface for AI-powered analytics and insights.
 */
public class AnalysisAIController implements Initializable {

    @FXML private LineChart<String, Number> performanceChart;
    @FXML private BarChart<String, Number> engagementChart;
    @FXML private PieChart gradeDistributionChart;
    @FXML private Label avgGradeLabel;
    @FXML private Label passRateLabel;
    @FXML private Label topStudentLabel;
    @FXML private Label aiInsightLabel;
    @FXML private ComboBox<String> courseFilter;
    @FXML private ComboBox<String> periodFilter;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        loadAnalytics();
        loadAIInsights();
    }

    private void setupFilters() {
        if (courseFilter != null)
            courseFilter.getItems().addAll("Tous les cours", "Introduction à Java", "Mathématiques Avancées");
        if (periodFilter != null) {
            periodFilter.getItems().addAll("Ce mois", "Ce semestre", "Cette année");
            periodFilter.setValue("Ce semestre");
        }
    }

    /**
     * Loads analytics data and populates charts.
     */
    private void loadAnalytics() {
        loadPerformanceChart();
        loadEngagementChart();
        loadGradeDistribution();
        updateStatistics();
    }

    private void loadPerformanceChart() {
        if (performanceChart == null) return;
        performanceChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Moyenne des Notes");
        series.getData().add(new XYChart.Data<>("Jan", 13.5));
        series.getData().add(new XYChart.Data<>("Fév", 14.2));
        series.getData().add(new XYChart.Data<>("Mar", 15.0));
        series.getData().add(new XYChart.Data<>("Avr", 14.8));
        series.getData().add(new XYChart.Data<>("Mai", 16.1));
        series.getData().add(new XYChart.Data<>("Jun", 15.7));
        performanceChart.getData().add(series);
    }

    private void loadEngagementChart() {
        if (engagementChart == null) return;
        engagementChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Connexions actives");
        series.getData().add(new XYChart.Data<>("Lun", 45));
        series.getData().add(new XYChart.Data<>("Mar", 52));
        series.getData().add(new XYChart.Data<>("Mer", 61));
        series.getData().add(new XYChart.Data<>("Jeu", 48));
        series.getData().add(new XYChart.Data<>("Ven", 38));
        engagementChart.getData().add(series);
    }

    private void loadGradeDistribution() {
        if (gradeDistributionChart == null) return;
        gradeDistributionChart.getData().clear();
        gradeDistributionChart.getData().addAll(
            new PieChart.Data("Excellent (16-20)", 25),
            new PieChart.Data("Bien (12-16)", 40),
            new PieChart.Data("Moyen (10-12)", 20),
            new PieChart.Data("Insuffisant (<10)", 15)
        );
    }

    private void updateStatistics() {
        if (avgGradeLabel != null) avgGradeLabel.setText("14.8 / 20");
        if (passRateLabel != null) passRateLabel.setText("85%");
        if (topStudentLabel != null) topStudentLabel.setText("Ahmed B. - 19.5/20");
    }

    private final com.edusmart.service.GeminiAiService aiService = new com.edusmart.service.GeminiAiService();
    private final com.edusmart.dao.jdbc.JdbcGradeDao gradeDao = new com.edusmart.dao.jdbc.JdbcGradeDao();
    private final com.google.gson.Gson gson = new com.google.gson.Gson();

    /**
     * Loads AI-generated insights about student performance.
     */
    private void loadAIInsights() {
        if (aiInsightLabel != null) {
            aiInsightLabel.setText("🤖 L'IA réfléchit aux tendances de la classe...");
            
            javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
                @Override
                protected String call() throws Exception {
                    // Gather some real data for the AI
                    java.util.List<com.edusmart.model.Grade> allGrades = gradeDao.findAll();
                    String dataSummary = gson.toJson(allGrades);
                    return aiService.analyzeClassTrends("Ma classe", dataSummary);
                }
            };

            task.setOnSucceeded(e -> aiInsightLabel.setText(task.getValue()));
            task.setOnFailed(e -> {
                aiInsightLabel.setText("❌ Échec de la récupération des insights : " + task.getException().getMessage());
                task.getException().printStackTrace();
            });

            new Thread(task).start();
        }
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        loadAnalytics();
    }

    /**
     * Exports the analytics report.
     */
    @FXML
    private void handleExportReport(ActionEvent event) {
        System.out.println("Exportation du rapport en cours...");
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

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
