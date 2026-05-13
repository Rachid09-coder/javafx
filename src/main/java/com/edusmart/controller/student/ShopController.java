package com.edusmart.controller.student;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.edusmart.dao.jdbc.JdbcProductDao;
import com.edusmart.model.Product;
import com.edusmart.service.ProductService;
import com.edusmart.service.impl.ProductServiceImpl;
import com.edusmart.util.SceneManager;
import com.edusmart.util.StudentShopCart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * ShopController - Student view for browsing and purchasing products.
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

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final Map<Product, Integer> cartItems = new HashMap<>();
    private final ProductService productService = new ProductServiceImpl(new JdbcProductDao());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        loadProducts();
        cartItems.putAll(StudentShopCart.snapshot());
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
            refreshProductsView();
        } catch (RuntimeException ex) {
            productList.clear();
            if (productsContainer != null) {
                productsContainer.getChildren().clear();
            }
        }
    }

    private void refreshProductsView() {
        if (productsContainer == null) {
            return;
        }

        String query = searchField != null && searchField.getText() != null
            ? searchField.getText().trim().toLowerCase()
            : "";
        String category = categoryFilter != null && categoryFilter.getValue() != null
            ? categoryFilter.getValue()
            : "Tous";

        List<Product> visibleProducts = productList.stream()
            .filter(product -> query.isEmpty()
                || safeLower(product.getName()).contains(query)
                || safeLower(product.getImage()).contains(query))
            .filter(product -> matchesCategory(product, category))
            .toList();

        productsContainer.getChildren().clear();

        if (visibleProducts.isEmpty()) {
            VBox emptyCard = new VBox(8);
            emptyCard.setAlignment(Pos.CENTER_LEFT);
            emptyCard.getStyleClass().add("card");
            emptyCard.setPrefWidth(260);
            Label title = new Label("Aucun produit trouvé");
            title.getStyleClass().add("course-title");
            Label subtitle = new Label("Essayez un autre filtre ou une autre recherche.");
            subtitle.getStyleClass().add("course-meta");
            emptyCard.getChildren().addAll(title, subtitle);
            productsContainer.getChildren().add(emptyCard);
            return;
        }

        for (Product product : visibleProducts) {
            productsContainer.getChildren().add(createProductCard(product));
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(220);
        card.setPadding(new Insets(16));

        Label iconLabel = new Label(productIcon(product));
        iconLabel.setStyle("-fx-font-size: 36px;");

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("course-title");

        Label metaLabel = new Label(productCategoryLabel(product));
        metaLabel.getStyleClass().add("course-meta");

        Label priceLabel = new Label(String.format("%.2f €", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E3A8A;");

        Label stockLabel = new Label("Stock: " + product.getStock());
        stockLabel.getStyleClass().add("course-meta");

        Button addButton = new Button("Ajouter au panier 🛒");
        addButton.getStyleClass().add("btn-primary");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setDisable(product.getStock() <= 0);
        addButton.setOnAction(event -> addToCart(product));

        card.getChildren().addAll(iconLabel, nameLabel, metaLabel, priceLabel, stockLabel, addButton);
        return card;
    }

    private String productCategoryLabel(Product product) {
        return switch (product.getCategoryId()) {
            case 1 -> "Livre";
            case 2 -> "Logiciel";
            case 3 -> "Matériel de cours";
            case 4 -> "Équipement";
            default -> "Produit";
        };
    }

    private String productIcon(Product product) {
        return switch (product.getCategoryId()) {
            case 1 -> "📗";
            case 2 -> "💻";
            case 3 -> "📓";
            case 4 -> "🎧";
            default -> "🛒";
        };
    }

    private boolean matchesCategory(Product product, String category) {
        if (category == null || category.isBlank() || "Tous".equalsIgnoreCase(category)) {
            return true;
        }
        return category.equalsIgnoreCase(productCategoryLabel(product));
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    /**
     * Adds a product to the cart.
     */
    public void addToCart(Product product) {
        if (product == null) {
            return;
        }

        int availableStock = product.getStock();
        int currentQuantity = cartItems.getOrDefault(product, 0);
        if (availableStock > 0 && currentQuantity >= availableStock) {
            showAlert("Stock insuffisant", "Vous avez déjà ajouté tout le stock disponible pour ce produit.");
            return;
        }

        int quantity = cartItems.getOrDefault(product, 0) + 1;
        cartItems.put(product, quantity);
        StudentShopCart.put(product, quantity);
        updateCartUI();
    }

    /**
     * Removes a product from the cart.
     */
    public void removeFromCart(Product product) {
        cartItems.remove(product);
        StudentShopCart.remove(product);
        updateCartUI();
    }

    /**
     * Clears the entire cart.
     */
    @FXML
    private void handleClearCart(ActionEvent event) {
        cartItems.clear();
        StudentShopCart.clear();
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
        StudentShopCart.replaceWith(cartItems);
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_ORDER_CHECKOUT);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        refreshProductsView();
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        refreshProductsView();
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

    @FXML private void handleDashboard(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_DASHBOARD);
    }

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

    @FXML private void handleStudentAI(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_AI);
    }

    @FXML private void handleProfile(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE);
    }

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
