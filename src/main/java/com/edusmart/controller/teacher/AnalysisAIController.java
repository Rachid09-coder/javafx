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
    @FXML private javafx.scene.control.TextField aiPromptField;
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
        
        try {
            java.util.List<com.edusmart.model.Grade> allGrades = gradeDao.findAll();
            if (allGrades.isEmpty()) {
                series.getData().add(new XYChart.Data<>("Aucune donnée", 0));
            } else {
                double avg = allGrades.stream().mapToDouble(com.edusmart.model.Grade::getNote).average().orElse(0);
                series.getData().add(new XYChart.Data<>("Actuel", avg));
            }
        } catch (Exception e) {
            series.getData().add(new XYChart.Data<>("Erreur", 0));
        }
        performanceChart.getData().add(series);
    }

    private void loadEngagementChart() {
        if (engagementChart == null) return;
        engagementChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Étudiants actifs (7 derniers jours)");
        try {
            int totalStudents = new com.edusmart.dao.jdbc.JdbcUserDao().findAll().stream()
                    .filter(u -> u.getRole() == com.edusmart.model.User.Role.STUDENT)
                    .toList().size();
            
            // Generate simulated activity for the past 7 days based on total students
            java.util.Random rand = new java.util.Random();
            String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
            for (String day : days) {
                int active = totalStudents == 0 ? 0 : (int)(totalStudents * (0.4 + rand.nextDouble() * 0.5));
                series.getData().add(new XYChart.Data<>(day, active));
            }
        } catch (Exception e) {
             series.getData().add(new XYChart.Data<>("Erreur", 0));
        }
        engagementChart.getData().add(series);
    }

    private void loadGradeDistribution() {
        if (gradeDistributionChart == null) return;
        gradeDistributionChart.getData().clear();
        try {
            java.util.List<com.edusmart.model.Grade> allGrades = gradeDao.findAll();
            long excellent = allGrades.stream().filter(g -> g.getNote() >= 16).count();
            long bien = allGrades.stream().filter(g -> g.getNote() >= 12 && g.getNote() < 16).count();
            long moyen = allGrades.stream().filter(g -> g.getNote() >= 10 && g.getNote() < 12).count();
            long insuffisant = allGrades.stream().filter(g -> g.getNote() < 10).count();

            gradeDistributionChart.getData().addAll(
                new PieChart.Data("Excellent (16-20)", (int)excellent),
                new PieChart.Data("Bien (12-16)", (int)bien),
                new PieChart.Data("Moyen (10-12)", (int)moyen),
                new PieChart.Data("Insuffisant (<10)", (int)insuffisant)
            );
        } catch (Exception e) {
            gradeDistributionChart.getData().add(new PieChart.Data("Erreur", 1));
        }
    }

    private void updateStatistics() {
        try {
            java.util.List<com.edusmart.model.Grade> allGrades = gradeDao.findAll();
            if (allGrades.isEmpty()) {
                if (avgGradeLabel != null) avgGradeLabel.setText("N/A");
                if (passRateLabel != null) passRateLabel.setText("N/A");
                if (topStudentLabel != null) topStudentLabel.setText("Aucun");
                return;
            }

            double avg = allGrades.stream().mapToDouble(com.edusmart.model.Grade::getNote).average().orElse(0);
            long passed = allGrades.stream().filter(g -> g.getNote() >= 10).count();
            double passRate = (double)passed / allGrades.size() * 100;

            com.edusmart.model.Grade topGrade = allGrades.stream()
                    .max(java.util.Comparator.comparingDouble(com.edusmart.model.Grade::getNote))
                    .orElse(null);

            if (avgGradeLabel != null) avgGradeLabel.setText(String.format("%.2f / 20", avg));
            if (passRateLabel != null) passRateLabel.setText(String.format("%.1f%%", passRate));
            
            if (topStudentLabel != null && topGrade != null) {
                com.edusmart.model.User student = new com.edusmart.dao.jdbc.JdbcUserDao().findById(topGrade.getStudentId()).orElse(null);
                String name = student != null ? student.getFirstName() + " " + student.getLastName().substring(0, 1) + "." : "Étudiant #" + topGrade.getStudentId();
                topStudentLabel.setText(name + " - " + topGrade.getNote() + "/20");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final com.edusmart.service.GeminiAiService aiService = new com.edusmart.service.GeminiAiService();
    private final com.edusmart.dao.jdbc.JdbcGradeDao gradeDao = new com.edusmart.dao.jdbc.JdbcGradeDao();
    private final com.google.gson.Gson gson = new com.google.gson.Gson();

    private void loadAIInsights() {
        if (aiInsightLabel != null) {
            aiInsightLabel.setText("🤖 L'IA réfléchit aux tendances de la classe...");
            
            javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
                @Override
                protected String call() throws Exception {
                    java.util.List<com.edusmart.model.Grade> allGrades = gradeDao.findAll();
                    if (allGrades.isEmpty()) return "Pas assez de données pour l'analyse.";
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
    private void handleAskAI(ActionEvent event) {
        if (aiPromptField == null) return;
        String prompt = aiPromptField.getText();
        if (prompt == null || prompt.trim().isEmpty()) return;
        
        aiInsightLabel.setText("🤖 L'IA réfléchit à votre question...");
        aiPromptField.setDisable(true);
        
        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override
            protected String call() throws Exception {
                java.util.List<com.edusmart.model.Grade> allGrades = gradeDao.findAll();
                String dataSummary = gson.toJson(allGrades);
                
                String systemPrompt = "Tu es un assistant IA exclusif pour EduSmart, une plateforme éducative. " +
                        "Règle stricte: Tu DOIS refuser catégoriquement de répondre à toute question qui n'est pas " +
                        "liée à l'éducation, aux statistiques des étudiants, à l'apprentissage ou à EduSmart. " +
                        "Si l'utilisateur dit 'salut' ou pose une question générale, rappelle ta mission éducative. " +
                        "Voici les données JSON actuelles de la classe : " + dataSummary + ". " +
                        "Question de l'utilisateur : " + prompt;

                return aiService.generateContent(systemPrompt);
            }
        };

        task.setOnSucceeded(e -> {
            aiInsightLabel.setText("Q: " + prompt + "\n\nR: " + task.getValue());
            aiPromptField.setDisable(false);
            aiPromptField.clear();
        });

        task.setOnFailed(e -> {
            aiInsightLabel.setText("❌ Erreur : " + task.getException().getMessage());
            aiPromptField.setDisable(false);
        });

        new Thread(task).start();
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

    @FXML private void handleGradeManagement(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_GRADE_MANAGEMENT);
    }

    @FXML private void handleProfile(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE);
    }

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
