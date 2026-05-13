package com.edusmart.controller.student;

import com.edusmart.model.Product;
import com.edusmart.util.MailSender;
import com.edusmart.util.PdfGenerator;
import com.edusmart.util.SceneManager;
import com.edusmart.util.StudentShopCart;
import com.edusmart.util.StripeCheckoutClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

public class OrderCheckoutController implements Initializable {

    @FXML private ListView<String> orderSummaryListView;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;
    @FXML private TextField emailField;
    @FXML private TextField promoCodeField;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private Label statusLabel;

    private double discountRate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        paymentMethodComboBox.getItems().setAll("Paiement sur place", "Stripe");
        paymentMethodComboBox.getSelectionModel().selectFirst();
        refreshSummary();
    }

    private void refreshSummary() {
        Map<Product, Integer> cart = StudentShopCart.snapshot();
        ObservableList<String> lines = FXCollections.observableArrayList();

        cart.forEach((product, quantity) -> lines.add(product.getName() + " x" + quantity + " = " + String.format("%.2f €", product.getPrice() * quantity)));

        if (cart.isEmpty()) {
            lines.add("Le panier est vide.");
        }

        orderSummaryListView.setItems(lines);

        double subtotal = StudentShopCart.getTotal();
        double discount = subtotal * discountRate;
        double total = subtotal - discount;

        subtotalLabel.setText(String.format("Sous-total: %.2f €", subtotal));
        discountLabel.setText(String.format("Réduction: %.2f €", discount));
        totalLabel.setText(String.format("Total: %.2f €", total));
        statusLabel.setText("Prêt pour la validation.");
    }

    @FXML
    private void handleApplyPromo(ActionEvent event) {
        String code = promoCodeField.getText() == null ? "" : promoCodeField.getText().trim().toUpperCase();
        switch (code) {
            case "EDUSMART10" -> discountRate = 0.10;
            case "WELCOME5" -> discountRate = 0.05;
            case "" -> discountRate = 0.0;
            default -> {
                discountRate = 0.0;
                showAlert("Code promo invalide", "Le code saisi n'est pas reconnu.");
            }
        }
        refreshSummary();
    }

    @FXML
    private void handleGenerateInvoice(ActionEvent event) {
        try {
            Path invoicePath = Path.of("generated_pdfs", "Facture_Commande_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            double subtotal = StudentShopCart.getTotal();
            double discount = subtotal * discountRate;
            double total = subtotal - discount;
            PdfGenerator.generateInvoice(invoicePath, StudentShopCart.snapshot(), subtotal, discount, total, emailField.getText());
            statusLabel.setText("Facture générée: " + invoicePath.toAbsolutePath());
        } catch (Exception ex) {
            showAlert("Erreur facture", ex.getMessage());
        }
    }

    @FXML
    private void handleConfirmOrder(ActionEvent event) {
        if (StudentShopCart.snapshot().isEmpty()) {
            showAlert("Panier vide", "Ajoutez des produits avant de confirmer la commande.");
            return;
        }

        double subtotal = StudentShopCart.getTotal();
        double discount = subtotal * discountRate;
        double total = subtotal - discount;

        if ("Stripe".equals(paymentMethodComboBox.getValue())) {
            String checkoutUrl = new StripeCheckoutClient().createCheckoutUrl(total);
            if (checkoutUrl == null) {
                showAlert("Stripe non configuré", "Aucune clé Stripe n'est configurée. La commande est validée en mode simulé.");
            } else {
                statusLabel.setText("Paiement Stripe simulé: " + checkoutUrl);
            }
        }

        MailSender.sendOrderConfirmation(emailField.getText(), "Confirmation de commande EduSmart", StudentShopCart.buildSummary());
        StudentShopCart.clear();
        showAlert("Commande validée", String.format("Votre commande a été confirmée. Total: %.2f €", total));
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP);
    }

    @FXML
    private void handleBackToShop(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}