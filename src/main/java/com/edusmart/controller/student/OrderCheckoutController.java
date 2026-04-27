package com.edusmart.controller.student;

import com.edusmart.dao.jdbc.JdbcOrderDao;
import com.edusmart.model.CartItem;
import com.edusmart.model.Order;
import com.edusmart.model.OrderItem;
import com.edusmart.model.PromoCode;
import com.edusmart.model.User;
import com.edusmart.service.OrderService;
import com.edusmart.service.PromoCodeService;
import com.edusmart.service.impl.OrderServiceImpl;
import com.edusmart.service.impl.PromoCodeServiceImpl;
import com.edusmart.dao.jdbc.JdbcPromoCodeDao;
import com.edusmart.util.SceneManager;
import com.edusmart.util.StripeCheckoutClient;
import com.edusmart.util.StripeKeys;
import com.edusmart.util.StudentShopCart;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OrderCheckoutController implements Initializable {

    @FXML private Label publishableKeyHint;
    @FXML private TableView<CartItem> itemsTable;
    @FXML private TableColumn<CartItem, String> nameCol;
    @FXML private TableColumn<CartItem, Integer> qtyCol;
    @FXML private TableColumn<CartItem, String> unitCol;
    @FXML private TableColumn<CartItem, String> lineCol;
    @FXML private TextField promoCodeField;
    @FXML private Button applyPromoButton;
    @FXML private Label promoStatusLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;
    @FXML private Button payButton;

    private final StudentShopCart shopCart = StudentShopCart.getInstance();
    private final PromoCodeService promoCodeService = new PromoCodeServiceImpl(new JdbcPromoCodeDao());
    private final OrderService orderService = new OrderServiceImpl(new JdbcOrderDao());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupPublishableHint();
        setupTable();
        if (promoCodeField != null) {
            // reflect existing promo, if any
            PromoCode p = shopCart.getAppliedPromo();
            if (p != null) promoCodeField.setText(p.getCode());
        }
        shopCart.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends CartItem> c) -> refresh());
        refresh();
    }

    private void setupPublishableHint() {
        if (publishableKeyHint == null) return;
        String pk = StripeKeys.publishableKey();
        if (pk == null || pk.isBlank()) {
            publishableKeyHint.setText("Clé publique Stripe (optionnelle) : non définie. Pour Checkout hébergé, la clé secrète suffit côté serveur (voir variable STRIPE_SECRET_KEY).");
        } else {
            publishableKeyHint.setText("Clé publique chargée (optionnel) : " + mask(pk));
        }
    }

    private String mask(String key) {
        if (key.length() <= 10) return "****";
        return key.substring(0, 7) + "…" + key.substring(key.length() - 4);
    }

    private void setupTable() {
        if (itemsTable == null) return;
        itemsTable.setItems(shopCart.getItems());

        if (nameCol != null) {
            nameCol.setCellValueFactory(cd -> new SimpleStringProperty(
                Objects.toString(cd.getValue().getProduct().getName(), "Produit")
            ));
        }
        if (qtyCol != null) qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        if (unitCol != null) {
            unitCol.setCellValueFactory(cd -> Bindings.createStringBinding(
                () -> String.format("%.2f €", cd.getValue().getProduct().getPrice()),
                cd.getValue().quantityProperty()
            ));
        }
        if (lineCol != null) {
            lineCol.setCellValueFactory(cd -> Bindings.createStringBinding(
                () -> String.format("%.2f €", cd.getValue().getProduct().getPrice() * cd.getValue().getQuantity()),
                cd.getValue().quantityProperty()
            ));
        }
    }

    private void refresh() {
        if (itemsTable != null) itemsTable.refresh();
        updateTotals();
    }

    private void updateTotals() {
        double sub = shopCart.getSubtotalEur();
        double disc = shopCart.computeDiscountEur();
        double tot = shopCart.getTotalEur();
        if (subtotalLabel != null) subtotalLabel.setText("Sous-total: " + shopCart.formatMoneyEur(sub));
        if (discountLabel != null) discountLabel.setText("Remise: -" + shopCart.formatMoneyEur(disc));
        if (totalLabel != null) totalLabel.setText("Total: " + shopCart.formatMoneyEur(tot));
        if (payButton != null) payButton.setDisable(shopCart.getItems().isEmpty() || tot <= 0);
    }

    @FXML
    private void handleApplyPromo(ActionEvent e) {
        User user = SceneManager.getInstance().getCurrentUser();
        if (user == null || user.getId() <= 0) {
            showPromoStatus("Veuillez vous connecter pour utiliser un code promo.", true);
            return;
        }
        String code = promoCodeField != null ? promoCodeField.getText() : null;
        if (code == null || code.trim().isEmpty()) {
            shopCart.clearPromo();
            showPromoStatus("Code promo retiré.", false);
            refresh();
            return;
        }
        try {
            PromoCode promo = promoCodeService.getActivePromoByCode(code.trim()).orElse(null);
            if (promo == null) {
                shopCart.clearPromo();
                showPromoStatus("Code invalide ou inactif.", true);
                refresh();
                return;
            }
            if (promoCodeService.hasStudentUsedPromo(promo.getId(), user.getId())) {
                shopCart.clearPromo();
                showPromoStatus("Ce code promo a déjà été utilisé.", true);
                refresh();
                return;
            }
            shopCart.setAppliedPromo(promo);
            showPromoStatus("Code appliqué: -" + shopCart.formatMoneyEur(shopCart.computeDiscountEur()), false);
            refresh();
        } catch (Exception ex) {
            shopCart.clearPromo();
            showPromoStatus("Erreur lors de la validation du code.", true);
            refresh();
        }
    }

    private void showPromoStatus(String msg, boolean isErr) {
        if (promoStatusLabel == null) return;
        promoStatusLabel.setText(msg);
        promoStatusLabel.setStyle(isErr ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
        promoStatusLabel.setVisible(true);
        promoStatusLabel.setManaged(true);
    }

    @FXML
    private void handlePayStripe(ActionEvent e) {
        User user = SceneManager.getInstance().getCurrentUser();
        if (user == null || user.getId() <= 0) {
            alert(Alert.AlertType.WARNING, "Connexion requise", "Veuillez vous connecter.");
            return;
        }
        if (shopCart.getItems().isEmpty()) {
            alert(Alert.AlertType.INFORMATION, "Panier vide", "Ajoutez des articles d’abord.");
            return;
        }
        if (shopCart.getTotalEur() <= 0) {
            alert(Alert.AlertType.INFORMATION, "Total invalide", "Le total doit être supérieur à 0.");
            return;
        }

        // Persist order in DB (PENDING) before redirecting to Stripe
        try {
            double sub = shopCart.getSubtotalEur();
            double disc = shopCart.computeDiscountEur();
            double tot = shopCart.getTotalEur();
            PromoCode promo = shopCart.getAppliedPromo();

            Order order = new Order();
            order.setStudentId(user.getId());
            order.setPromoCodeId(promo != null ? promo.getId() : null);
            order.setTotalBeforeDiscount(sub);
            order.setDiscountAmount(disc);
            order.setTotalAfterDiscount(tot);
            order.setStatus("PENDING");

            List<OrderItem> items = shopCart.getItems().stream().map(ci -> {
                OrderItem it = new OrderItem();
                it.setProductId(ci.getProduct().getId());
                it.setProductName(Objects.toString(ci.getProduct().getName(), "Produit"));
                it.setQuantity(ci.getQuantity());
                it.setUnitPrice(ci.getProduct().getPrice());
                it.setTotalPrice(ci.getProduct().getPrice() * ci.getQuantity());
                return it;
            }).collect(Collectors.toList());

            int orderId = orderService.createOrderWithItems(order, items);
            if (promo != null) {
                promoCodeService.markUsed(promo.getId(), user.getId());
            }

            long cents = shopCart.getTotalCentsEur();
            StripeCheckoutClient.CheckoutSession session = StripeCheckoutClient.createPaymentSessionEur(
                cents,
                "EduSmart - Commande #" + orderId,
                "http://localhost:8080/success",
                "http://localhost:8080/cancel",
                String.valueOf(orderId)
            );

            if (session.id() != null) {
                orderService.updateStripeSessionId(orderId, session.id());
            } else {
                // still store something if only url present
                orderService.updateStripeSessionId(orderId, session.url());
            }

            openInBrowser(session.url());
            shopCart.clear();
            SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP);
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Paiement", rootCause(ex));
        }
    }

    private void openInBrowser(String url) throws Exception {
        if (url == null || url.isBlank()) return;
        if (!Desktop.isDesktopSupported()) {
            throw new IllegalStateException("Impossible d’ouvrir le navigateur (Desktop non supporté). URL: " + url);
        }
        Desktop d = Desktop.getDesktop();
        if (!d.isSupported(Desktop.Action.BROWSE)) {
            throw new IllegalStateException("Action BROWSE non supportée. URL: " + url);
        }
        d.browse(URI.create(url));
    }

    @FXML
    private void handleBackToShop(ActionEvent e) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP);
    }

    @FXML private void handleShop(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP); }
    @FXML private void handleProfile(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }

    private void alert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : t.toString();
    }
}
