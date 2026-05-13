package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcProductDao;
import com.edusmart.model.Product;
import com.edusmart.service.ProductService;
import com.edusmart.service.impl.ProductServiceImpl;
import com.edusmart.util.OpenAIService;
import com.edusmart.util.SceneManager;
import com.edusmart.util.StudentShopCart;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ShopAnalysisAIController implements Initializable {

    @FXML private TextArea analysisTextArea;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label messageLabel;
    @FXML private PieChart salesPieChart;
    @FXML private BarChart<String, Number> stockBarChart;

    private final ProductService productService = new ProductServiceImpl(new JdbcProductDao());
    private final OpenAIService openAIService = new OpenAIService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        analysisTextArea.setEditable(false);
        analysisTextArea.setWrapText(true);
        loadingIndicator.setVisible(false);
        messageLabel.setText("");
    }

    @FXML
    private void handleGenerateAnalysis(ActionEvent event) {
        loadingIndicator.setVisible(true);
        messageLabel.setText("Génération de l'analyse en cours...");
        messageLabel.setStyle("-fx-text-fill: #10B981;");
        analysisTextArea.clear();

        new Thread(() -> {
            try {
                List<Product> products = productService.getAllProducts();
                Map<String, Double> salesData = new LinkedHashMap<>();
                StudentShopCart.snapshot().forEach((product, quantity) -> salesData.merge(product.getName(), product.getPrice() * quantity, (v1, v2) -> v1 + v2));

                Platform.runLater(() -> {
                    salesPieChart.getData().clear();
                    if (salesData.isEmpty()) {
                        salesPieChart.getData().add(new PieChart.Data("Aucune vente", 1));
                    } else {
                        salesData.forEach((name, value) -> salesPieChart.getData().add(new PieChart.Data(name, value)));
                    }

                    stockBarChart.getData().clear();
                    XYChart.Series<String, Number> stockSeries = new XYChart.Series<>();
                    stockSeries.setName("Stock Disponible");
                    for (Product p : products) {
                        stockSeries.getData().add(new XYChart.Data<>(p.getName(), p.getStock()));
                    }
                    stockBarChart.getData().add(stockSeries);
                });

                StringBuilder ordersData = new StringBuilder();
                ordersData.append("Panier courant: ").append(StudentShopCart.buildSummary()).append('\n');

                StringBuilder productsData = new StringBuilder();
                for (Product p : products) {
                    productsData.append(String.format("- Produit: '%s' (ID: %d), Prix: %.2f, Stock Restant: %d\n",
                        p.getName(), p.getId(), p.getPrice(), p.getStock()));
                }

                String analysisResult = openAIService.generateShopAnalysis(ordersData.toString(), productsData.toString());

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    messageLabel.setText("Analyse générée avec succès.");
                    messageLabel.setStyle("-fx-text-fill: #10B981;");
                    analysisTextArea.setText(analysisResult);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    messageLabel.setText("Erreur: " + e.getMessage());
                    messageLabel.setStyle("-fx-text-fill: #EF4444;");
                });
            }
        }).start();
    }

    @FXML private void handleDashboard(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD); }
    @FXML private void handleManageCourses(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES); }
    @FXML private void handleManageModules(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES); }
    @FXML private void handleManageExams(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS); }
    @FXML private void handleGradeManagement(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_GRADE_MANAGEMENT); }
    @FXML private void handleShopManagement(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT); }
    @FXML private void handleCategoryManagement(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CATEGORY_MANAGEMENT); }
    @FXML private void handleBulletins(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS); }
    @FXML private void handleCertifications(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS); }
    @FXML private void handleAnalysisAI(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI); }
    @FXML private void handleShopAnalysisAI(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_ANALYSIS_AI); }
    @FXML private void handleStudentManagement(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT); }
    @FXML private void handleLogout(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}