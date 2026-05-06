package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.dao.jdbc.JdbcModuleDao;
import com.edusmart.model.Course;
import com.edusmart.model.Module;
import com.edusmart.service.CourseService;
import com.edusmart.service.ModuleService;
import com.edusmart.service.impl.CourseServiceImpl;
import com.edusmart.service.impl.ModuleServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.application.Platform;

import com.edusmart.util.ThemeManager;
import com.edusmart.util.ActivityLogger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URL;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import com.edusmart.service.AIService;
import com.edusmart.service.impl.AIServiceImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * ManageCoursesController - Teacher interface for managing courses.
 *
 * Team member: Implement CRUD operations via service layer.
 */
public class ManageCoursesController implements Initializable {

    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, Integer> idColumn;
    @FXML private TableColumn<Course, String> titleColumn;
    @FXML private TableColumn<Course, Double> priceColumn;
    @FXML private TableColumn<Course, Double> coefficientColumn;
    @FXML private TableColumn<Course, String> createdAtColumn;
    @FXML private TableColumn<Course, String> statusColumn;
    @FXML private TableColumn<Course, String> moduleColumn;
    @FXML private TableColumn<Course, Course> ratingColumn;
    @FXML private TableColumn<Course, Course> favoriteColumn;

    @FXML private Label totalCoursesStat;
    @FXML private Label avgPriceStat;
    @FXML private Label topModuleStat;
    @FXML private Label avgRatingStat;
    @FXML private Label activeCountLabel;
    @FXML private Label draftCountLabel;
    @FXML private Label inactiveCountLabel;
    @FXML private javafx.scene.layout.HBox statusDistBar;
    
    @FXML private ComboBox<String> filterStatusBox;
    @FXML private ComboBox<Module> filterModuleBox;
    @FXML private Label aiSuggestionLabel;
    @FXML private ListView<String> activityList;
    @FXML private TextField subscriberEmailField;

    @FXML private TextField titleField;
    @FXML private TextField priceField;
    @FXML private TextField coefficientField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<Module> moduleComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label messageLabel;
    @FXML private Label countLabel;
    @FXML private VBox formPanel;
    @FXML private VBox activityPanel;
    @FXML private Button toggleActivityBtn;
    @FXML private Label formPanelTitle;

    private boolean activityPanelVisible = true;

    // AI Chat UI Elements
    @FXML private VBox chatPanel;
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField chatInputField;
    @FXML private Label chatContextLabel;
    @FXML private HBox loadingIndicatorBox;

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private AIService aiService;
    private Course selectedCourse;
    private final CourseService courseService = new CourseServiceImpl(new JdbcCourseDao());
    private final ModuleService moduleService = new ModuleServiceImpl(new JdbcModuleDao());
    /** module id -> title for table and search */
    private Map<Integer, String> moduleTitleById = new HashMap<>();
    private final com.edusmart.service.SubscriptionService subscriptionService = new com.edusmart.service.impl.SubscriptionServiceImpl(new com.edusmart.dao.jdbc.JdbcSubscriberDao());
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        aiService = new AIServiceImpl();
        setupTable();
        setupForm();
        loadCourses();
        // Panel starts hidden — it will slide in on demand
        if (formPanel != null) {
            formPanel.setVisible(false);
            formPanel.setManaged(false);
            formPanel.setTranslateX(350);
            formPanel.setOpacity(0);
        }
        if (searchField != null) {
            // Live Search
            searchField.textProperty().addListener((obs, oldV, newV) -> applyFiltersAndSort());
        }
        if (priceField != null && aiSuggestionLabel != null) {
            priceField.textProperty().addListener((obs, oldV, newV) -> updateAiSuggestion(newV));
        }
        if (activityList != null) {
            activityList.setItems(ActivityLogger.getActivities());
        }
        Platform.runLater(() -> {
            if (coursesTable != null && coursesTable.getScene() != null) {
                ThemeManager.applyTheme(coursesTable.getScene());
                updateThemeButton();
            }
            animateActivityPanel();
        });
    }

    /** Slide the Activity Panel in from the right with a smooth entrance. */
    private void animateActivityPanel() {
        if (activityPanel == null) return;
        activityPanel.setTranslateX(300);
        activityPanel.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(480), activityPanel);
        slide.setFromX(300);
        slide.setToX(0);
        slide.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(400), activityPanel);
        fade.setFromValue(0);
        fade.setToValue(1);

        javafx.animation.ParallelTransition entrance = new javafx.animation.ParallelTransition(slide, fade);
        entrance.setDelay(Duration.millis(180)); // slight delay so the table loads first
        entrance.play();
    }

    @FXML
    private void handleToggleActivity(ActionEvent event) {
        if (activityPanel == null) return;

        if (activityPanelVisible) {
            // Slide OUT to the right
            TranslateTransition slide = new TranslateTransition(Duration.millis(320), activityPanel);
            slide.setToX(320);
            slide.setInterpolator(javafx.animation.Interpolator.EASE_IN);

            FadeTransition fade = new FadeTransition(Duration.millis(280), activityPanel);
            fade.setToValue(0);

            javafx.animation.ParallelTransition exit = new javafx.animation.ParallelTransition(slide, fade);
            exit.setOnFinished(e -> {
                activityPanel.setVisible(false);
                activityPanel.setManaged(false);
            });
            exit.play();

            if (toggleActivityBtn != null)
                toggleActivityBtn.setText("⏱ Afficher Activité");
            activityPanelVisible = false;

        } else {
            // Slide IN from the right
            activityPanel.setManaged(true);
            activityPanel.setVisible(true);
            activityPanel.setTranslateX(320);
            activityPanel.setOpacity(0);

            TranslateTransition slide = new TranslateTransition(Duration.millis(400), activityPanel);
            slide.setToX(0);
            slide.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(Duration.millis(360), activityPanel);
            fade.setToValue(1);

            new javafx.animation.ParallelTransition(slide, fade).play();

            if (toggleActivityBtn != null)
                toggleActivityBtn.setText("⏱ Masquer Activité");
            activityPanelVisible = true;
        }
    }
    
    private void updateThemeButton() {
        if (coursesTable != null && coursesTable.getScene() != null) {
            Button btn = (Button) coursesTable.getScene().lookup(".btn-secondary"); // naive lookup or we could do better
            // Instead of looking up by class, we can just let it be. But to change text:
            // Actually let's not change text dynamically unless we bind it.
        }
    }

    @FXML
    private void handleToggleTheme(ActionEvent event) {
        ThemeManager.setDarkMode(!ThemeManager.isDarkMode());
        if (coursesTable != null && coursesTable.getScene() != null) {
            ThemeManager.applyTheme(coursesTable.getScene());
        }
        Button btn = (Button) event.getSource();
        btn.setText(ThemeManager.isDarkMode() ? "☀️ Mode Clair" : "🌙 Mode Sombre");
    }

    @FXML
    private void handleSubscribe(ActionEvent event) {
        if (subscriberEmailField == null) return;
        String email = subscriberEmailField.getText();
        if (subscriptionService.addSubscriber(email)) {
            ActivityLogger.log("Abonné", "ajouté", email);
            showMessage("Inscription réussie ! Vous serez notifié des baisses de prix.", false);
            subscriberEmailField.clear();
        } else {
            showMessage("Erreur : Email invalide ou déjà inscrit.", true);
        }
    }

    // ── AI Chat handlers ────────────────────────────────────────

    @FXML
    private void handleOpenChat(ActionEvent event) {
        chatPanel.setVisible(true);
        chatPanel.setManaged(true);
        if (chatMessagesContainer.getChildren().isEmpty()) {
            addChatMessage("Système", "Bonjour Professeur ! Je suis l'assistant IA. Je peux vous aider à créer du contenu, optimiser vos titres ou générer des descriptions.", false);
        }
        updateChatContext();
    }

    @FXML
    private void handleCloseChat(ActionEvent event) {
        chatPanel.setVisible(false);
        chatPanel.setManaged(false);
    }

    private void updateChatContext() {
        if (titleField == null) return;
        String title = titleField.getText().trim();
        if (title.isEmpty() && selectedCourse != null) title = selectedCourse.getTitle();
        if (title == null || title.isEmpty()) {
            chatContextLabel.setText("Contexte: Aucun cours ciblé");
        } else {
            chatContextLabel.setText("Contexte du cours: " + title);
        }
    }

    @FXML
    private void handleAiGenerateDesc(ActionEvent event) {
        updateChatContext();
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            addChatMessage("Système", "Veuillez d'abord entrer un Titre de cours dans le formulaire pour générer une description.", false);
            return;
        }
        sendToAI("Génère une description de cours professionnelle et accrocheuse pour mon cours intitulé: " + title);
    }

    @FXML
    private void handleAiOptimizeTitle(ActionEvent event) {
        updateChatContext();
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            addChatMessage("Système", "Veuillez d'abord entrer un Titre dans le formulaire.", false);
            return;
        }
        sendToAI("Propose-moi 3 titres alternatifs plus percutants et commerciaux pour mon cours intitulé: " + title);
    }

    @FXML
    private void handleAiModuleIdeas(ActionEvent event) {
        updateChatContext();
        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();
        if (title.isEmpty()) {
            addChatMessage("Système", "Veuillez d'abord entrer un Titre dans le formulaire.", false);
            return;
        }
        sendToAI("Propose-moi une structure en 4 modules pertinents pour mon cours '" + title + "'. " + (desc.isEmpty() ? "" : "Description actuelle: " + desc));
    }

    @FXML
    private void handleAiAutoCreateCourse(ActionEvent event) {
        String topic = chatInputField.getText().trim();
        chatInputField.clear();
        
        // Si le texte est très générique (ex: "ajoute ce cours"), on utilise plutôt le Titre du formulaire
        String tLower = topic.toLowerCase();
        if (tLower.equals("ajoute ce cours") || tLower.equals("ajouter cette cours au table") || tLower.equals("créer un cours") || topic.isEmpty()) {
            if (titleField != null && !titleField.getText().trim().isEmpty()) {
                topic = titleField.getText().trim();
            }
        }
        
        if (topic.isEmpty()) {
            addChatMessage("Système", "Veuillez taper un sujet de cours dans la zone de texte, ou entrer un titre dans le formulaire.", false);
            return;
        }

        addChatMessage("Vous", "Auto-générer un cours complet sur : " + topic, true);
        chatInputField.clear();
        
        loadingIndicatorBox.setVisible(true);
        loadingIndicatorBox.setManaged(true);

        final String finalTopic = topic;

        String prompt = "Crée un cours structuré complet sur le sujet suivant: '" + finalTopic + "'. " +
                "IMPORTANT: Renvoie UNIQUEMENT un objet JSON valide, sans aucun texte additionnel ni bloc markdown (ne commence pas par ```json). " +
                "Utilise exactement ces 3 clés: " +
                "\"title\" (String: un titre accrocheur), " +
                "\"description\" (String: une description de 2 paragraphes), " +
                "\"content\" (String: le contenu du cours divisé en chapitres).";

        aiService.askAI(prompt, "Format JSON strict exigé sans formatage markdown.").thenAccept(response -> {
            Platform.runLater(() -> {
                loadingIndicatorBox.setVisible(false);
                loadingIndicatorBox.setManaged(false);
                
                try {
                    // Nettoyer d'éventuels tags markdown restants
                    String json = response.trim();
                    if (json.startsWith("```json")) {
                        json = json.substring(7);
                    }
                    if (json.startsWith("```")) {
                        json = json.substring(3);
                    }
                    if (json.endsWith("```")) {
                        json = json.substring(0, json.length() - 3);
                    }
                    json = json.trim();

                    com.google.gson.JsonObject obj = new com.google.gson.Gson().fromJson(json, com.google.gson.JsonObject.class);
                    
                    String newTitle = obj.has("title") ? obj.get("title").getAsString() : finalTopic;
                    String newDesc = obj.has("description") ? obj.get("description").getAsString() : "";
                    String newContent = obj.has("content") ? obj.get("content").getAsString() : "";

                    Course newCourse = new Course();
                    newCourse.setTitle(newTitle);
                    newCourse.setDescription(newDesc);
                    newCourse.setGeneratedContent(newContent);
                    newCourse.setPrice(49.99); // Valeur par défaut recommandée
                    newCourse.setCreatedAt(java.time.LocalDateTime.now());
                    newCourse.setStatusValue("DRAFT");
                    
                    // Remplir visuellement le formulaire
                    if (titleField != null) titleField.setText(newTitle);
                    if (descriptionArea != null) descriptionArea.setText(newDesc);
                    if (priceField != null) priceField.setText("49.99");
                    if (statusComboBox != null) statusComboBox.setValue("DRAFT");
                    
                    // Sauvegarder dans la table CRUD
                    boolean success = courseService.createCourse(newCourse);
                    
                    if (success) {
                        loadCourses(); // Rafraîchir la table UI
                        // Sélectionner le nouveau cours généré
                        courseList.stream().filter(c -> c.getTitle().equals(newTitle)).findFirst().ifPresent(c -> {
                            populateForm(c);
                        });
                    }
                    addChatMessage("IA", "✅ Merveilleux ! Le cours **" + newTitle + "** a été entièrement généré (titre, description, contenu) et sauvegardé automatiquement dans votre base de données !", false);
                    showToast("Cours auto-généré avec succès!", false);
                    
                } catch (Exception e) {
                    addChatMessage("IA", "❌ Erreur de génération automatique. Assurez-vous que l'IA a bien renvoyé du JSON pur.\n\nErreur: " + e.getMessage() + "\n\nRéponse brute: " + response, false);
                }
            });
        });
    }

    @FXML
    private void handleSendChatMessage(ActionEvent event) {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            String msgLower = message.toLowerCase();
            
            // Intercepter les demandes d'ajout/création de cours en langage naturel
            if (msgLower.contains("ajoute") || msgLower.contains("ajouter") || msgLower.contains("crée") || msgLower.contains("créer") || msgLower.contains("génère") || msgLower.contains("générer")) {
                if (msgLower.contains("cours") || msgLower.contains("table")) {
                    handleAiAutoCreateCourse(new ActionEvent());
                    return;
                }
            }
            
            chatInputField.clear();
            sendToAI(message);
        }
    }

    private void sendToAI(String message) {
        addChatMessage("Vous", message, true);
        
        loadingIndicatorBox.setVisible(true);
        loadingIndicatorBox.setManaged(true);
        
        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();
        String context = "Titre du cours en cours de création: " + title + "\nDescription: " + desc;

        aiService.askAI(message, context).thenAccept(response -> {
            Platform.runLater(() -> {
                loadingIndicatorBox.setVisible(false);
                loadingIndicatorBox.setManaged(false);
                addChatMessage("IA", response, false);
                
                // Magic feature: If the AI returns a text that clearly looks like a description, we could auto-fill it, but for safety we just let the teacher copy it.
            });
        });
    }

    private void addChatMessage(String sender, String text, boolean isUser) {
        VBox bubble = new VBox();
        bubble.setPadding(new Insets(10));
        bubble.setSpacing(4);
        
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");
        
        Label textLabel = new Label(text);
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (isUser ? "#FFFFFF" : "#1E293B") + ";");
        
        bubble.getChildren().addAll(senderLabel, textLabel);
        
        if (isUser) {
            bubble.setStyle("-fx-background-color: #4F46E5; -fx-background-radius: 12 12 0 12;");
            bubble.setAlignment(Pos.CENTER_RIGHT);
        } else {
            bubble.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 12 12 12 0;");
            bubble.setAlignment(Pos.CENTER_LEFT);
        }
        
        HBox row = new HBox(bubble);
        row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));
        
        chatMessagesContainer.getChildren().add(row);
    }

    // ── Drawer helpers ──────────────────────────────────────────

    /** Show the form panel with a slide-in animation from the right. */
    private void showFormPanel(String title) {
        if (formPanel == null) return;
        if (formPanelTitle != null) formPanelTitle.setText(title);
        formPanel.setManaged(true);
        formPanel.setVisible(true);
        // Slide in
        TranslateTransition tt = new TranslateTransition(Duration.millis(320), formPanel);
        tt.setFromX(350);
        tt.setToX(0);
        FadeTransition ft = new FadeTransition(Duration.millis(320), formPanel);
        ft.setFromValue(0);
        ft.setToValue(1);
        tt.play();
        ft.play();
    }

    /** Slide the form panel back to the right and then hide it. */
    private void hideFormPanel() {
        if (formPanel == null) return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), formPanel);
        tt.setFromX(0);
        tt.setToX(350);
        FadeTransition ft = new FadeTransition(Duration.millis(280), formPanel);
        ft.setFromValue(1);
        ft.setToValue(0);
        tt.setOnFinished(e -> {
            formPanel.setVisible(false);
            formPanel.setManaged(false);
        });
        tt.play();
        ft.play();
    }

    /** Open drawer in "Add" mode (empty form). */
    @FXML
    private void handleOpenAdd(ActionEvent event) {
        clearForm();
        showFormPanel("Nouveau Cours");
    }

    /** Open drawer in "Edit" mode for the selected row. */
    @FXML
    private void handleOpenEdit(ActionEvent event) {
        if (selectedCourse == null) {
            showMessage("Sélectionnez un cours à modifier.", true);
            return;
        }
        showFormPanel("Modifier le Cours");
    }

    /** Close / slide-out the drawer. */
    @FXML
    private void handleClosePanel(ActionEvent event) {
        hideFormPanel();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (priceColumn != null) priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        if (ratingColumn != null) {
            ratingColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
            ratingColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); setText(null); return; }
                    // Generate a fake deterministic rating for demo if 0
                    if (item.getRating() == 0.0) item.setRating(3.5 + (item.getId() % 15) / 10.0);
                    Label l = new Label(String.format("%.1f ★", item.getRating()));
                    l.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    setGraphic(l);
                    setText(null);
                }
            });
        }
        
        if (favoriteColumn != null) {
            favoriteColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
            favoriteColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); setText(null); return; }
                    Button btn = new Button(item.isFavorite() ? "★" : "☆");
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (item.isFavorite() ? "#F59E0B" : "#94A3B8") + "; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");
                    btn.setOnAction(e -> {
                        item.setFavorite(!item.isFavorite());
                        getTableView().refresh();
                    });
                    setGraphic(btn);
                    setText(null);
                }
            });
        }
        if (createdAtColumn != null) {
            createdAtColumn.setCellValueFactory(cellData -> {
                LocalDateTime createdAt = cellData.getValue().getCreatedAt();
                return new javafx.beans.property.SimpleStringProperty(
                        createdAt != null ? createdAt.format(DATE_TIME_FORMATTER) : "-"
                );
            });
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusValue"));
            statusColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) { setGraphic(null); setText(null); return; }
                    Label badge = new Label(value);
                    badge.getStyleClass().add("badge");
                    switch (value.toUpperCase()) {
                        case "ACTIVE":   badge.getStyleClass().add("badge-success"); break;
                        case "INACTIVE": badge.getStyleClass().add("badge-danger");  break;
                        case "DRAFT":    badge.getStyleClass().add("badge-warning"); break;
                        case "ARCHIVED": badge.getStyleClass().add("badge-gray");    break;
                        default:         badge.getStyleClass().add("badge-blue");    break;
                    }
                    setGraphic(badge);
                    setText(null);
                }
            });
        }
        if (moduleColumn != null) {
            moduleColumn.setCellValueFactory(cellData -> {
                Integer mid = cellData.getValue().getModuleId();
                if (mid == null) return new SimpleStringProperty("-");
                String title = moduleTitleById.get(mid);
                return new SimpleStringProperty(title != null ? title : "#" + mid);
            });
        }
        if (coursesTable != null) {
            coursesTable.setItems(courseList);
            // Single-click row: populate form and open drawer in edit mode
            coursesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        populateForm(newVal);
                        showFormPanel("Modifier le Cours");
                    }
                });
        }
    }

    private void setupForm() {
        if (sortComboBox != null) {
            sortComboBox.getItems().addAll(
                "Titre (A-Z)", "Titre (Z-A)",
                "Prix (Croissant)", "Prix (Décroissant)",
                "Plus récent", "Plus ancien"
            );
            sortComboBox.setValue("Titre (A-Z)");
            sortComboBox.setOnAction(e -> applyFiltersAndSort());
        }
        if (statusComboBox != null) {
            statusComboBox.getItems().addAll("ACTIVE", "DRAFT", "INACTIVE", "ARCHIVED");
            statusComboBox.setValue("ACTIVE");
        }
        if (filterStatusBox != null) {
            filterStatusBox.getItems().addAll("Tous", "ACTIVE", "DRAFT", "INACTIVE", "ARCHIVED");
            filterStatusBox.setValue("Tous");
            filterStatusBox.setOnAction(e -> applyFiltersAndSort());
        }
        attachModuleComboPresentation();
        reloadModuleComboItems(null);
    }

    private void attachModuleComboPresentation() {
        if (moduleComboBox == null) {
            return;
        }
        moduleComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        moduleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
    }

    private void reloadModuleComboItems(Integer preferredModuleId) {
        if (moduleComboBox == null) {
            return;
        }
        Module placeholder = new Module();
        placeholder.setId(0);
        placeholder.setTitle("(Aucun module)");
        List<Module> items = new ArrayList<>();
        items.add(placeholder);
        try {
            items.addAll(moduleService.getAllModules());
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement modules: " + rootCauseMessage(ex), true);
        }
        moduleComboBox.setItems(FXCollections.observableArrayList(items));
        Module select = items.get(0);
        if (preferredModuleId != null) {
            for (Module m : items) {
                if (m.getId() == preferredModuleId) {
                    select = m;
                    break;
                }
            }
        }
        moduleComboBox.setValue(select);
        
        // Setup filterModuleBox
        if (filterModuleBox != null) {
            Module filterAll = new Module(); filterAll.setId(-1); filterAll.setTitle("Tous les modules");
            List<Module> filterItems = new ArrayList<>();
            filterItems.add(filterAll);
            filterItems.addAll(items.stream().filter(m -> m.getId() > 0).collect(Collectors.toList()));
            filterModuleBox.setItems(FXCollections.observableArrayList(filterItems));
            filterModuleBox.setValue(filterAll);
            
            filterModuleBox.setCellFactory(moduleComboBox.getCellFactory());
            filterModuleBox.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Module item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitle());
                }
            });
            filterModuleBox.setOnAction(e -> applyFiltersAndSort());
        }
    }

    private void refreshModuleTitles() {
        try {
            moduleTitleById = moduleService.getAllModules().stream()
                    .collect(Collectors.toMap(Module::getId, Module::getTitle, (a, b) -> a));
        } catch (RuntimeException ex) {
            moduleTitleById = new HashMap<>();
        }
    }

    private void loadCourses() {
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        try {
            refreshModuleTitles();
            List<Course> all = courseService.getAllCourses();
            
            // Search filter
            String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
            String statusFilter = filterStatusBox != null ? filterStatusBox.getValue() : "Tous";
            Module modFilter = filterModuleBox != null ? filterModuleBox.getValue() : null;
            
            all = all.stream().filter(c -> {
                // Initialize fake rating if needed
                if (c.getRating() == 0.0) c.setRating(3.5 + (c.getId() % 15) / 10.0);
                
                // Status match
                if (!"Tous".equals(statusFilter) && !statusFilter.equalsIgnoreCase(c.getStatusValue())) return false;
                
                // Module match
                if (modFilter != null && modFilter.getId() != -1) {
                    if (c.getModuleId() == null || c.getModuleId() != modFilter.getId()) return false;
                }
                
                // Search query match
                if (!query.isEmpty()) {
                    boolean titleMatch = c.getTitle() != null && c.getTitle().toLowerCase().contains(query);
                    if (titleMatch) return true;
                    Integer mid = c.getModuleId();
                    if (mid != null) {
                        String mt = moduleTitleById.getOrDefault(mid, "").toLowerCase();
                        if (mt.contains(query)) return true;
                    }
                    return false;
                }
                return true;
            }).collect(Collectors.toList());

            // Sort
            String sortMode = sortComboBox != null ? sortComboBox.getValue() : "Titre (A-Z)";
            if (sortMode == null) sortMode = "Titre (A-Z)";
            
            java.util.Comparator<Course> cmp;
            switch (sortMode) {
                case "Titre (Z-A)":
                    cmp = java.util.Comparator.comparing((Course c) -> c.getTitle() == null ? "" : c.getTitle().toLowerCase()).reversed();
                    break;
                case "Prix (Croissant)":
                    cmp = java.util.Comparator.comparing(Course::getPrice);
                    break;
                case "Prix (Décroissant)":
                    cmp = java.util.Comparator.comparing(Course::getPrice).reversed();
                    break;
                case "Plus récent":
                    cmp = java.util.Comparator.comparing(Course::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()));
                    break;
                case "Plus ancien":
                    cmp = java.util.Comparator.comparing(Course::getCreatedAt, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder()));
                    break;
                case "Titre (A-Z)":
                default:
                    cmp = java.util.Comparator.comparing((Course c) -> c.getTitle() == null ? "" : c.getTitle().toLowerCase());
                    break;
            }
            all.sort(cmp);
            
            courseList.setAll(all);
            if (coursesTable != null) coursesTable.refresh();
            if (countLabel != null) countLabel.setText(courseList.size() + " cours");
            
            updateAnalytics(all);
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement cours: " + rootCauseMessage(ex), true);
        }
    }
    
    private void updateAnalytics(List<Course> list) {
        if (totalCoursesStat != null) totalCoursesStat.setText(String.valueOf(list.size()));
        
        if (avgPriceStat != null) {
            double avg = list.stream().mapToDouble(Course::getPrice).average().orElse(0.0);
            avgPriceStat.setText(String.format("%.2f €", avg));
        }
        
        if (topModuleStat != null) {
            Map<Integer, Long> counts = list.stream()
                .filter(c -> c.getModuleId() != null)
                .collect(Collectors.groupingBy(Course::getModuleId, Collectors.counting()));
            
            String top = "Aucun";
            if (!counts.isEmpty()) {
                int topId = counts.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
                top = moduleTitleById.getOrDefault(topId, "Inconnu");
            }
            topModuleStat.setText(top);
        }
        
        if (avgRatingStat != null) {
            double avgRat = list.stream().mapToDouble(Course::getRating).average().orElse(0.0);
            avgRatingStat.setText(String.format("%.1f / 5", avgRat));
        }

        // Status distribution bar
        long active   = list.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatusValue())).count();
        long draft    = list.stream().filter(c -> "DRAFT".equalsIgnoreCase(c.getStatusValue())).count();
        long inactive = list.size() - active - draft;

        if (activeCountLabel != null)   activeCountLabel.setText(active + " actifs");
        if (draftCountLabel != null)    draftCountLabel.setText(draft + " brouillons");
        if (inactiveCountLabel != null) inactiveCountLabel.setText(inactive + " inactifs");

        if (statusDistBar != null && !list.isEmpty()) {
            statusDistBar.getChildren().clear();
            double total = list.size();
            if (active > 0)   addDistSegment(active / total, "#10B981");
            if (draft > 0)    addDistSegment(draft / total, "#F59E0B");
            if (inactive > 0) addDistSegment(inactive / total, "#94A3B8");
        }
    }

    private void addDistSegment(double ratio, String color) {
        javafx.scene.layout.Region seg = new javafx.scene.layout.Region();
        seg.setPrefHeight(8);
        seg.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");
        javafx.scene.layout.HBox.setHgrow(seg, javafx.scene.layout.Priority.SOMETIMES);
        seg.setPrefWidth(ratio * 400);
        statusDistBar.getChildren().add(seg);
    }

    private void updateAiSuggestion(String priceText) {
        if (aiSuggestionLabel == null) return;
        try {
            double p = Double.parseDouble(priceText);
            if (p < 20) {
                aiSuggestionLabel.setText("💡 Astuce: Prix bas. Idéal pour attirer beaucoup d'étudiants.");
                aiSuggestionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669; -fx-background-color: rgba(16,185,129,0.1); -fx-padding: 6 10; -fx-background-radius: 6;");
            } else if (p > 100) {
                aiSuggestionLabel.setText("💡 Astuce: Prix Premium. Assurez-vous d'avoir un contenu exclusif.");
                aiSuggestionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #D97706; -fx-background-color: rgba(245,158,11,0.1); -fx-padding: 6 10; -fx-background-radius: 6;");
            } else {
                aiSuggestionLabel.setText("💡 Astuce: Prix standard. Parfaitement aligné avec le marché.");
                aiSuggestionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4F46E5; -fx-background-color: rgba(79,70,229,0.1); -fx-padding: 6 10; -fx-background-radius: 6;");
            }
        } catch (NumberFormatException e) {
            aiSuggestionLabel.setText("💡 Astuce: Saisissez un prix valide pour obtenir une analyse.");
            aiSuggestionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B; -fx-background-color: rgba(148,163,184,0.1); -fx-padding: 6 10; -fx-background-radius: 6;");
        }
    }

    private void populateForm(Course course) {
        selectedCourse = course;
        if (course == null) return;
        if (titleField != null) titleField.setText(course.getTitle());
        if (priceField != null) priceField.setText(String.valueOf(course.getPrice()));
        if (coefficientField != null) {
            coefficientField.setText(course.getCoefficient() != null ? String.valueOf(course.getCoefficient()) : "");
        }
        if (descriptionArea != null) descriptionArea.setText(course.getDescription());
        if (statusComboBox != null && course.getStatusValue() != null) {
            statusComboBox.setValue(course.getStatusValue().toUpperCase());
        }
        if (course.getCreatedAt() != null) {
            if (datePicker != null) datePicker.setValue(course.getCreatedAt().toLocalDate());
            if (timeField != null) timeField.setText(course.getCreatedAt().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        reloadModuleComboItems(course.getModuleId());
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        try {
            Course course = buildCourseFromForm(true);
            if (courseService.createCourse(course)) {
                ActivityLogger.log("Cours", "créé", course.getTitle());
                showMessage("Cours créé avec succès!", false);
                clearForm();
                loadCourses();
            } else {
                showMessage("Création du cours échouée.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur création cours: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedCourse == null) {
            showMessage("Sélectionnez un cours à modifier.", true);
            return;
        }
        if (!validateForm()) return;
        // Snapshot form before the dialog: focus changes can reset the module ComboBox.
        final Course courseToSave = buildCourseFromForm(false);
        courseToSave.setId(selectedCourse.getId());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la modification du cours \"" + selectedCourse.getTitle() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (courseService.updateCourse(courseToSave)) {
                        ActivityLogger.log("Cours", "modifié", courseToSave.getTitle());
                        showMessage("Cours mis à jour avec succès!", false);
                        loadCourses();
                    } else {
                        showMessage("Mise à jour du cours échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur mise à jour cours: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedCourse == null) {
            showMessage("Sélectionnez un cours à supprimer.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer le cours \"" + selectedCourse.getTitle() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (courseService.deleteCourse(selectedCourse.getId())) {
                        ActivityLogger.log("Cours", "supprimé", selectedCourse.getTitle());
                        courseList.remove(selectedCourse);
                        clearForm();
                        showMessage("Cours supprimé.", false);
                    } else {
                        showMessage("Suppression du cours échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur suppression cours: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSort();
    }

    @FXML
    private void handleExport(ActionEvent event) {
        try {
            java.io.File file = new java.io.File("cours_export.pdf");
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
            document.open();
            
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Liste des Cours", titleFont);
            title.setSpacingAfter(20);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);
            
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(7);
            table.setWidthPercentage(100);
            
            // Headers
            String[] headers = {"ID", "Titre", "Prix", "Note", "Module", "Statut", "Date"};
            for (String header : headers) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header));
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            
            // Data
            for (Course c : courseList) {
                String mod = c.getModuleId() != null ? moduleTitleById.getOrDefault(c.getModuleId(), "") : "";
                String date = c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_TIME_FORMATTER) : "";
                table.addCell(String.valueOf(c.getId()));
                table.addCell(c.getTitle() != null ? c.getTitle() : "");
                table.addCell(String.format("%.2f", c.getPrice()));
                table.addCell(String.format("%.1f", c.getRating()));
                table.addCell(mod);
                table.addCell(c.getStatusValue() != null ? c.getStatusValue() : "");
                table.addCell(date);
            }
            
            document.add(table);
            document.close();
            
            showMessage("Export PDF réussi : " + file.getAbsolutePath(), false);
        } catch (Exception e) {
            showMessage("Erreur d'exportation : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleDuplicate(ActionEvent event) {
        if (selectedCourse == null) {
            showMessage("Sélectionnez un cours à dupliquer.", true);
            return;
        }
        try {
            Course copy = new Course();
            copy.setTitle(selectedCourse.getTitle() + " (Copie)");
            copy.setDescription(selectedCourse.getDescription());
            copy.setPrice(selectedCourse.getPrice());
            copy.setCoefficient(selectedCourse.getCoefficient());
            copy.setModuleId(selectedCourse.getModuleId());
            copy.setStatusValue("DRAFT");
            copy.setCreatedAt(java.time.LocalDateTime.now());
            copy.setGeneratedContent(selectedCourse.getGeneratedContent());
            if (courseService.createCourse(copy)) {
                ActivityLogger.log("Cours", "dupliqué", copy.getTitle());
                showMessage("Cours dupliqué avec succès !", false);
                loadCourses();
            } else {
                showMessage("Duplication échouée.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur duplication: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private boolean validateForm() {
        if (titleField != null && titleField.getText().trim().isEmpty()) {
            showMessage("Le titre du cours est obligatoire.", true);
            return false;
        }
        if (priceField != null) {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) {
                    showMessage("Le prix doit être positif ou nul.", true);
                    return false;
                }
            } catch (NumberFormatException ex) {
                showMessage("Le prix doit être un nombre valide.", true);
                return false;
            }
        }
        if (coefficientField != null && !coefficientField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(coefficientField.getText().trim());
            } catch (NumberFormatException ex) {
                showMessage("Le coefficient doit être un nombre valide.", true);
                return false;
            }
        }
        if (statusComboBox != null && (statusComboBox.getValue() == null || statusComboBox.getValue().isBlank())) {
            showMessage("Le statut du cours est obligatoire.", true);
            return false;
        }
        return true;
    }

    /**
     * @param forCreate when true, never copy id / files from a selected table row (Créer with a row focused must still be a new course).
     */
    private Course buildCourseFromForm(boolean forCreate) {
        Course course = new Course();
        
        java.time.LocalDate date = datePicker != null && datePicker.getValue() != null ? datePicker.getValue() : java.time.LocalDate.now();
        java.time.LocalTime time = java.time.LocalTime.of(10, 0);
        if (timeField != null && !timeField.getText().trim().isEmpty()) {
            try {
                time = java.time.LocalTime.parse(timeField.getText().trim());
            } catch (Exception e) {
                // Ignore parse errors, use default 10:00
            }
        }
        LocalDateTime scheduledDateTime = LocalDateTime.of(date, time);

        if (forCreate || selectedCourse == null) {
            course.setCreatedAt(scheduledDateTime);
            course.setThumbnailPath(null);
            course.setPdfPath(null);
            course.setGeneratedContent(null);
        } else {
            course.setId(selectedCourse.getId());
            course.setCreatedAt(scheduledDateTime);
            course.setThumbnailPath(selectedCourse.getThumbnailPath());
            course.setPdfPath(selectedCourse.getPdfPath());
            course.setGeneratedContent(selectedCourse.getGeneratedContent());
        }
        course.setTitle(titleField != null ? titleField.getText().trim() : "");
        course.setDescription(descriptionArea != null ? descriptionArea.getText().trim() : null);
        course.setPrice(priceField != null ? Double.parseDouble(priceField.getText().trim()) : 0.0);
        
        String coefficientText = coefficientField != null ? coefficientField.getText().trim() : "";
        course.setCoefficient(coefficientText.isEmpty() ? null : Double.parseDouble(coefficientText));

        Module modulePick = moduleComboBox != null ? moduleComboBox.getSelectionModel().getSelectedItem() : null;
        if (modulePick == null && moduleComboBox != null) {
            modulePick = moduleComboBox.getValue();
        }
        course.setModuleId(modulePick != null && modulePick.getId() != 0 ? modulePick.getId() : null);

        String st = statusComboBox != null ? statusComboBox.getValue() : "DRAFT";
        // Auto-promote DRAFT -> ACTIVE
        if ("DRAFT".equals(st) && course.getPrice() > 0 && course.getDescription() != null && course.getDescription().length() > 10) {
            st = "ACTIVE";
            Platform.runLater(() -> showToast("✨ Statut automatisé : DRAFT → ACTIVE", false));
        }
        course.setStatusValue(st);

        return course;
    }

    private void clearForm() {
        selectedCourse = null;
        if (coursesTable != null) {
            coursesTable.getSelectionModel().clearSelection();
        }
        if (titleField != null) titleField.clear();
        if (priceField != null) priceField.clear();
        if (coefficientField != null) coefficientField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        if (statusComboBox != null) statusComboBox.setValue("ACTIVE");
        if (datePicker != null) datePicker.setValue(java.time.LocalDate.now());
        if (timeField != null) timeField.setText("10:00");
        reloadModuleComboItems(null);
    }

    private void showMessage(String message, boolean isError) {
        showToast(message, isError);
    }
    
    private void showToast(String message, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            if (isError) {
                messageLabel.setStyle("-fx-text-fill: #DC2626; -fx-background-color: rgba(239,68,68,0.1); -fx-background-radius: 8; -fx-padding: 8 14;");
            } else {
                messageLabel.setStyle("-fx-text-fill: #059669; -fx-background-color: rgba(16,185,129,0.1); -fx-background-radius: 8; -fx-padding: 8 14;");
            }
            messageLabel.setVisible(true);
            messageLabel.setOpacity(0);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messageLabel);
            fadeIn.setToValue(1);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), messageLabel);
            fadeOut.setDelay(Duration.seconds(3));
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> messageLabel.setVisible(false));
            
            fadeIn.setOnFinished(e -> fadeOut.play());
            fadeIn.play();
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : throwable.getMessage();
    }

    public ObservableList<Course> getCourseList() {
        return courseList;
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
