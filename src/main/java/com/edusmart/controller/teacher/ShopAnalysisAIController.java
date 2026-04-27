package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcOrderDao;
import com.edusmart.model.Order;
import com.edusmart.model.OrderItem;
import com.edusmart.service.GeminiAIService;
import com.edusmart.service.OrderService;
import com.edusmart.service.impl.OrderServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopAnalysisAIController implements Initializable {

    @FXML private TextArea analysisTextArea;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label messageLabel;

    private final OrderService orderService = new OrderServiceImpl(new JdbcOrderDao());
    private final GeminiAIService geminiAIService = new GeminiAIService();

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
        messageLabel.setStyle("-fx-text-fill: #10B981;"); // green
        analysisTextArea.clear();

        // Run API call in a background thread to prevent UI freezing
        new Thread(() -> {
            try {
                List<Order> orders = orderService.getAllOrders();
                List<OrderItem> items = orderService.getAllOrderItems();

                // Build a simple string representation of the data
                StringBuilder ordersData = new StringBuilder();
                for (Order o : orders) {
                    ordersData.append(String.format("Commande #%d: total=%.2f, status=%s, date=%s\n",
                            o.getId(), o.getTotalAfterDiscount(), o.getStatus(), o.getCreatedAt()));
                }

                StringBuilder itemsData = new StringBuilder();
                for (OrderItem it : items) {
                    itemsData.append(String.format("- Produit: '%s' (ID: %d), Quantité: %d, Prix unitaire: %.2f\n",
                            it.getProductName(), it.getProductId(), it.getQuantity(), it.getUnitPrice()));
                }

                if (orders.isEmpty() && items.isEmpty()) {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        messageLabel.setText("Aucune donnée disponible pour l'analyse.");
                        messageLabel.setStyle("-fx-text-fill: #EF4444;");
                    });
                    return;
                }

                String analysisResult = geminiAIService.generateShopAnalysis(ordersData.toString(), itemsData.toString());

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

    // ── Navigation handlers ──────────────────────────────────────────────
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
    @FXML private void handleStudentManagement(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT); }
    @FXML private void handleLogout(ActionEvent event) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
