package com.edusmart.controller.student;

import com.edusmart.dao.jdbc.JdbcCategoryDao;
import com.edusmart.dao.jdbc.JdbcOrderDao;
import com.edusmart.dao.jdbc.JdbcProductDao;
import com.edusmart.dao.jdbc.JdbcPromoCodeDao;
import com.edusmart.model.Category;
import com.edusmart.model.Order;
import com.edusmart.model.OrderItem;
import com.edusmart.model.Product;
import com.edusmart.model.PromoCode;
import com.edusmart.service.CategoryService;
import com.edusmart.service.OrderService;
import com.edusmart.service.ProductService;
import com.edusmart.service.PromoCodeService;
import com.edusmart.service.impl.CategoryServiceImpl;
import com.edusmart.service.impl.OrderServiceImpl;
import com.edusmart.service.impl.ProductServiceImpl;
import com.edusmart.service.impl.PromoCodeServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * ShopController - Student view for browsing and purchasing products.
 *
 * Team member: Implement loadProducts(), addToCart(), checkout() methods.
 */
public class ShopController implements Initializable {

    @FXML private FlowPane productsContainer;
    @FXML private VBox cartPanel;
    @FXML private ListView<CartItem> cartListView;
    @FXML private Label cartTotalLabel;
    @FXML private Label cartSubtotalLabel;
    @FXML private Label cartDiscountLabel;
    @FXML private Label cartItemCountLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private Button checkoutButton;
    @FXML private TextField promoCodeField;
    @FXML private Button applyPromoButton;
    @FXML private Label promoStatusLabel;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ProductService productService = new ProductServiceImpl(new JdbcProductDao());
    private final CategoryService categoryService = new CategoryServiceImpl(new JdbcCategoryDao());
    private final PromoCodeService promoCodeService = new PromoCodeServiceImpl(new JdbcPromoCodeDao());
    private final OrderService orderService = new OrderServiceImpl(new JdbcOrderDao());

    private final Category allCategory = new Category(0, "Tous", null, null, null);
    private final ObservableList<CartItem> cartModel = FXCollections.observableArrayList();
    private final Map<Integer, CartItem> cartByProductId = new HashMap<>();
    private final Map<Integer, Category> categoryById = new HashMap<>();

    private PromoCode appliedPromo;
    private double appliedDiscountAmount;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        loadProducts();
        renderProducts(productList);
        setupCartListView();
        updateCartSummary();
    }

    private void setupFilters() {
        if (categoryFilter == null) return;

        categoryFilter.setConverter(new StringConverter<>() {
            @Override public String toString(Category c) {
                return c == null ? "" : Objects.toString(c.getName(), "");
            }
            @Override public Category fromString(String string) {
                return categoryFilter.getItems().stream()
                    .filter(c -> c != null && Objects.equals(c.getName(), string))
                    .findFirst()
                    .orElse(allCategory);
            }
        });

        try {
            categoryById.clear();
            categoryById.put(allCategory.getId(), allCategory);
            for (Category c : categoryService.getAllCategories()) {
                if (c != null) categoryById.put(c.getId(), c);
            }

            categoryFilter.getItems().setAll(
                categoryById.values().stream()
                    .sorted(Comparator.comparingInt(Category::getId))
                    .collect(Collectors.toList())
            );
            categoryFilter.setValue(allCategory);
        } catch (RuntimeException ex) {
            categoryFilter.getItems().setAll(allCategory);
            categoryFilter.setValue(allCategory);
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
        if (product == null) return;

        CartItem item = cartByProductId.get(product.getId());
        if (item == null) {
            if (product.getStock() <= 0) {
                showAlert("Stock insuffisant", "Ce produit est en rupture de stock.");
                return;
            }
            item = new CartItem(product, 1);
            cartByProductId.put(product.getId(), item);
            cartModel.add(item);
        } else {
            if (item.getQuantity() + 1 > product.getStock()) {
                showAlert("Stock insuffisant", "Quantité max atteinte (stock: " + product.getStock() + ").");
                return;
            }
            item.setQuantity(item.getQuantity() + 1);
        }
        updateCartSummary();
    }

    /**
     * Removes a product from the cart.
     */
    public void removeFromCart(Product product) {
        if (product == null) return;
        CartItem item = cartByProductId.remove(product.getId());
        if (item != null) cartModel.remove(item);
        updateCartSummary();
    }

    /**
     * Clears the entire cart.
     */
    @FXML
    private void handleClearCart(ActionEvent event) {
        cartByProductId.clear();
        cartModel.clear();
        updateCartSummary();
    }

    /**
     * Initiates the checkout process for items in the cart.
     */
    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartModel.isEmpty()) {
            showAlert("Panier vide", "Ajoutez des articles à votre panier avant de procéder au paiement.");
            return;
        }

        com.edusmart.model.User user = SceneManager.getInstance().getCurrentUser();
        if (user == null || user.getId() <= 0) {
            showAlert("Connexion requise", "Veuillez vous connecter avant de commander.");
            return;
        }

        double subtotal = getCartSubtotal();
        double discount = appliedDiscountAmount;
        double total = Math.max(0, subtotal - discount);

        Order order = new Order();
        order.setStudentId(user.getId());
        order.setPromoCodeId(appliedPromo != null ? appliedPromo.getId() : null);
        order.setTotalBeforeDiscount(subtotal);
        order.setDiscountAmount(discount);
        order.setTotalAfterDiscount(total);
        order.setStatus("PENDING");

        java.util.List<OrderItem> items = cartModel.stream().map(ci -> {
            OrderItem it = new OrderItem();
            it.setProductId(ci.getProduct().getId());
            it.setProductName(Objects.toString(ci.getProduct().getName(), "Produit"));
            it.setQuantity(ci.getQuantity());
            it.setUnitPrice(ci.getProduct().getPrice());
            it.setTotalPrice(ci.getProduct().getPrice() * ci.getQuantity());
            return it;
        }).collect(Collectors.toList());

        try {
            int orderId = orderService.createOrderWithItems(order, items);
            if (appliedPromo != null) {
                promoCodeService.markUsed(appliedPromo.getId(), user.getId());
            }

            showAlert("Commande", "Commande enregistrée (ID: " + orderId + ").\nTotal: " + String.format("%.2f €", total));
            clearCartAndPromo();
        } catch (Exception ex) {
            showAlert("Erreur", "Impossible d'enregistrer la commande.\n" + rootCause(ex));
        }
    }

    @FXML
    private void handleApplyPromo(ActionEvent event) {
        com.edusmart.model.User user = SceneManager.getInstance().getCurrentUser();
        if (user == null || user.getId() <= 0) {
            showPromoStatus("Veuillez vous connecter pour utiliser un code promo.", true);
            return;
        }
        String code = promoCodeField != null ? promoCodeField.getText() : null;
        if (code == null || code.trim().isEmpty()) {
            appliedPromo = null;
            appliedDiscountAmount = 0;
            showPromoStatus("Code promo retiré.", false);
            updateCartSummary();
            return;
        }

        try {
            PromoCode promo = promoCodeService.getActivePromoByCode(code.trim()).orElse(null);
            if (promo == null) {
                appliedPromo = null;
                appliedDiscountAmount = 0;
                showPromoStatus("Code invalide ou inactif.", true);
                updateCartSummary();
                return;
            }

            if (promoCodeService.hasStudentUsedPromo(promo.getId(), user.getId())) {
                appliedPromo = null;
                appliedDiscountAmount = 0;
                showPromoStatus("Ce code promo a déjà été utilisé.", true);
                updateCartSummary();
                return;
            }

            appliedPromo = promo;
            appliedDiscountAmount = computeDiscount(getCartSubtotal(), promo.getDiscountPercent());
            showPromoStatus("Code appliqué: -" + formatMoney(appliedDiscountAmount), false);
            updateCartSummary();
        } catch (Exception ex) {
            appliedPromo = null;
            appliedDiscountAmount = 0;
            showPromoStatus("Erreur lors de la validation du code.", true);
            updateCartSummary();
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndRender();
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        applyFiltersAndRender();
    }

    private void applyFiltersAndRender() {
        String q = searchField != null && searchField.getText() != null
            ? searchField.getText().trim().toLowerCase()
            : "";
        Category selectedCategory = categoryFilter != null ? categoryFilter.getValue() : allCategory;
        int selectedCategoryId = selectedCategory != null ? selectedCategory.getId() : 0;

        ObservableList<Product> filtered = productList.stream()
            .filter(p -> {
                if (p == null) return false;
                boolean matchesText = q.isEmpty()
                    || (p.getName() != null && p.getName().toLowerCase().contains(q));
                boolean matchesCat = selectedCategoryId <= 0 || p.getCategoryId() == selectedCategoryId;
                return matchesText && matchesCat;
            })
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        renderProducts(filtered);
    }

    private void renderProducts(ObservableList<Product> products) {
        if (productsContainer == null) return;

        productsContainer.getChildren().clear();
        for (Product p : products) {
            productsContainer.getChildren().add(buildProductCard(p));
        }
    }

    private VBox buildProductCard(Product p) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(220);

        String iconText = "📦";
        Category c = categoryById.get(p.getCategoryId());
        if (c != null && c.getIcon() != null && !c.getIcon().isBlank()) iconText = c.getIcon().trim();

        Label icon = new Label(iconText);
        icon.setStyle("-fx-font-size: 36px;");

        Label name = new Label(Objects.toString(p.getName(), "Produit"));
        name.getStyleClass().add("course-title");

        String categoryName = c != null && c.getName() != null ? c.getName() : ("Catégorie #" + p.getCategoryId());
        Label meta = new Label(categoryName + " • Stock: " + p.getStock());
        meta.getStyleClass().add("course-meta");

        Label price = new Label(String.format("%.2f €", p.getPrice()));
        price.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E3A8A;");

        Button add = new Button("Ajouter au panier 🛒");
        add.getStyleClass().add("btn-primary");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setDisable(p.getStock() <= 0);
        add.setOnAction(e -> addToCart(p));

        card.getChildren().addAll(icon, name, meta, price, add);
        return card;
    }

    private void setupCartListView() {
        if (cartListView == null) return;
        cartListView.setItems(cartModel);

        cartListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label name = new Label(Objects.toString(item.getProduct().getName(), "Produit"));
                name.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);

                Button minus = new Button("−");
                minus.getStyleClass().add("btn-secondary");
                minus.setMinSize(30, 30);
                minus.setPrefSize(30, 30);

                Label qty = new Label();
                qty.textProperty().bind(item.quantityProperty().asString());
                qty.setMinWidth(28);
                qty.setAlignment(Pos.CENTER);

                Button plus = new Button("+");
                plus.getStyleClass().add("btn-secondary");
                plus.setMinSize(30, 30);
                plus.setPrefSize(30, 30);

                Label lineTotal = new Label();
                lineTotal.textProperty().bind(item.quantityProperty().multiply(item.getProduct().getPrice()).asString("%.2f €"));
                lineTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E3A8A;");
                lineTotal.setMinWidth(84);
                lineTotal.setAlignment(Pos.CENTER_RIGHT);

                Button remove = new Button("✕");
                remove.getStyleClass().add("btn-danger");
                remove.setMinSize(30, 30);
                remove.setPrefSize(30, 30);

                minus.setOnAction(e -> {
                    int next = item.getQuantity() - 1;
                    if (next <= 0) {
                        cartByProductId.remove(item.getProduct().getId());
                        cartModel.remove(item);
                    } else {
                        item.setQuantity(next);
                    }
                    updateCartSummary();
                });

                plus.setOnAction(e -> {
                    int next = item.getQuantity() + 1;
                    if (next > item.getProduct().getStock()) {
                        showAlert("Stock insuffisant", "Quantité max atteinte (stock: " + item.getProduct().getStock() + ").");
                        return;
                    }
                    item.setQuantity(next);
                    updateCartSummary();
                });

                remove.setOnAction(e -> {
                    cartByProductId.remove(item.getProduct().getId());
                    cartModel.remove(item);
                    updateCartSummary();
                });

                HBox row = new HBox(8, name, minus, qty, plus, lineTotal, remove);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 6 6 6 6;");

                setText(null);
                setGraphic(row);
            }
        });
    }

    private void updateCartSummary() {
        int itemCount = cartModel.stream().mapToInt(CartItem::getQuantity).sum();
        double subtotal = getCartSubtotal();
        if (appliedPromo != null) {
            appliedDiscountAmount = computeDiscount(subtotal, appliedPromo.getDiscountPercent());
        } else {
            appliedDiscountAmount = 0;
        }
        double total = Math.max(0, subtotal - appliedDiscountAmount);

        if (cartItemCountLabel != null) cartItemCountLabel.setText(itemCount + " article(s)");
        if (cartSubtotalLabel != null) cartSubtotalLabel.setText("Sous-total: " + formatMoney(subtotal));
        if (cartDiscountLabel != null) cartDiscountLabel.setText("Remise: -" + formatMoney(appliedDiscountAmount));
        if (cartTotalLabel != null) cartTotalLabel.setText("Total: " + formatMoney(total));
        if (checkoutButton != null) checkoutButton.setDisable(cartModel.isEmpty());
    }

    private double getCartSubtotal() {
        return cartModel.stream()
            .mapToDouble(ci -> ci.getProduct().getPrice() * ci.getQuantity())
            .sum();
    }

    private double computeDiscount(double subtotal, double discountPercent) {
        double pct = Math.max(0, Math.min(100, discountPercent));
        return subtotal * (pct / 100.0);
    }

    private String formatMoney(double value) {
        return String.format("%.2f €", value);
    }

    private void showPromoStatus(String msg, boolean isErr) {
        if (promoStatusLabel == null) return;
        promoStatusLabel.setText(msg);
        promoStatusLabel.setStyle(isErr ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
        promoStatusLabel.setManaged(true);
        promoStatusLabel.setVisible(true);
    }

    private void clearCartAndPromo() {
        cartByProductId.clear();
        cartModel.clear();
        appliedPromo = null;
        appliedDiscountAmount = 0;
        if (promoCodeField != null) promoCodeField.clear();
        if (promoStatusLabel != null) {
            promoStatusLabel.setText("");
            promoStatusLabel.setVisible(false);
            promoStatusLabel.setManaged(false);
        }
        updateCartSummary();
    }

    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : t.toString();
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

    public Map<Integer, CartItem> getCartItems() { return cartByProductId; }

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

    public static final class CartItem {
        private final Product product;
        private final IntegerProperty quantity = new SimpleIntegerProperty(0);

        public CartItem(Product product, int quantity) {
            this.product = Objects.requireNonNull(product, "product");
            setQuantity(quantity);
        }

        public Product getProduct() { return product; }

        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int value) { quantity.set(Math.max(0, value)); }
        public IntegerProperty quantityProperty() { return quantity; }
    }
}
