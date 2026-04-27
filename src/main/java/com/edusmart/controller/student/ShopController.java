package com.edusmart.controller.student;

import com.edusmart.dao.jdbc.JdbcProductDao;
import com.edusmart.model.Product;
import com.edusmart.service.ProductService;
import com.edusmart.service.impl.ProductServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * ShopController - Student view for browsing and purchasing products.
 *
 * Team member: Implement loadProducts(), addToCart(), checkout() methods.
 */
public class ShopController implements Initializable {

    @FXML private FlowPane productsContainer;
    @FXML private VBox cartPanel;
    @FXML private ListView<String> cartListView;
    @FXML private Label cartTotalLabel;
    @FXML private Label cartItemCountLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Button checkoutButton;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private Map<Product, Integer> cartItems = new HashMap<>();
    private final ProductService productService = new ProductServiceImpl(new JdbcProductDao());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        loadProducts();
        updateCartUI();
    }

    private void setupFilters() {
        if (categoryFilter != null) {
            categoryFilter.getItems().addAll("Tous", "Livres", "Logiciels", "Matériel de cours", "Équipement");
            categoryFilter.setValue("Tous");
        }
    }

    /**
     * Loads products from the connected service.
     */
    private void loadProducts() {
        try {
            productList.setAll(productService.getAllProducts());
        } catch (RuntimeException ex) {
            productList.clear();
        }
    }

    /**
     * Adds a product to the cart.
     */
    public void addToCart(Product product) {
        cartItems.merge(product, 1, (a, b) -> a + b);
        updateCartUI();
    }

    /**
     * Removes a product from the cart.
     */
    public void removeFromCart(Product product) {
        cartItems.remove(product);
        updateCartUI();
    }

    /**
     * Clears the entire cart.
     */
    @FXML
    private void handleClearCart(ActionEvent event) {
        cartItems.clear();
        updateCartUI();
    }

    /**
     * Initiates the checkout process for items in the cart.
     */
    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showAlert("Panier vide", "Ajoutez des articles à votre panier avant de procéder au paiement.");
            return;
        }
        System.out.println("Navigation vers la page de paiement...");
        showAlert("Commande", "Votre commande a été passée avec succès!\nTotal: " + getCartTotal() + " €");
        cartItems.clear();
        updateCartUI();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText();
        System.out.println("Recherche: " + query);
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        String category = categoryFilter.getValue();
        System.out.println("Filtre: " + category);
    }

    private void updateCartUI() {
        int itemCount = cartItems.values().stream().mapToInt(Integer::intValue).sum();
        double total = getCartTotal();

        if (cartItemCountLabel != null) cartItemCountLabel.setText(itemCount + " article(s)");
        if (cartTotalLabel != null) cartTotalLabel.setText(String.format("Total: %.2f €", total));

        if (cartListView != null) {
            ObservableList<String> cartLines = FXCollections.observableArrayList();
            cartItems.forEach((product, qty) ->
                cartLines.add(product.getName() + " x" + qty + " = " + String.format("%.2f €", product.getPrice() * qty)));
            cartListView.setItems(cartLines);
        }
    }

    private double getCartTotal() {
        return cartItems.entrySet().stream()
            .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
            .sum();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public ObservableList<Product> getProductList() {
        return productList;
    }

    public Map<Product, Integer> getCartItems() {
        return cartItems;
    }

    // ── Navigation handlers ──────────────────────────────────────────────

    @FXML private void handleCourses(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES);
    }

    @FXML private void handleExams(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS);
    }

    @FXML private void handleBulletin(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_BULLETIN);
    }

    @FXML private void handleCertification(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_CERTIFICATION);
    }

    @FXML private void handleShop(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP);
    }

    @FXML private void handleProfile(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE);
    }

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
