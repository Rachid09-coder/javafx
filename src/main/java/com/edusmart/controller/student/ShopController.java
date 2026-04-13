package com.edusmart.controller.student;

import com.edusmart.model.Product;
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
     * Loads available products from the shop.
     * TODO: Replace with actual service call.
     */
    private void loadProducts() {
        // TODO: Load from service
        // productList.setAll(ProductService.getAllProducts());

        // Demo data
        productList.setAll(
            new Product(1, "Java pour débutants", 29.99, 50, Product.Category.BOOK),
            new Product(2, "Cahier de Mathématiques", 12.50, 100, Product.Category.COURSE_MATERIAL),
            new Product(3, "IDE IntelliJ IDEA License", 99.00, 20, Product.Category.SOFTWARE),
            new Product(4, "Calculatrice Scientifique", 45.00, 30, Product.Category.EQUIPMENT)
        );
    }

    /**
     * Adds a product to the shopping cart.
     * TODO: Implement cart persistence.
     */
    public void addToCart(Product product) {
        cartItems.merge(product, 1, Integer::sum);
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
     * Proceeds to checkout.
     * TODO: Implement payment integration.
     */
    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showAlert("Panier vide", "Ajoutez des articles à votre panier avant de procéder au paiement.");
            return;
        }
        // TODO: Navigate to checkout/payment screen
        showAlert("Commande", "Votre commande a été passée avec succès!\nTotal: " + getCartTotal() + " €");
        cartItems.clear();
        updateCartUI();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        // TODO: Filter products by search query
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        // TODO: Apply category filter
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
}
