package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcBulletinDao;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.Bulletin;
import com.edusmart.model.User;
import com.edusmart.service.BulletinService;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.BulletinServiceImpl;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.util.PdfGenerator;
import com.edusmart.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * BulletinsController - Liste des bulletins (table seule + dialogs).
 */
public class BulletinsController implements Initializable {

    @FXML private TableView<Bulletin> bulletinsTable;
    @FXML private TableColumn<Bulletin, String> studentColumn;
    @FXML private TableColumn<Bulletin, String> periodColumn;
    @FXML private TableColumn<Bulletin, Double> averageColumn;
    @FXML private TableColumn<Bulletin, Integer> rankColumn;
    @FXML private TableColumn<Bulletin, String> metierColumn;
    @FXML private TableColumn<Bulletin, String> statusColumn;
    @FXML private TableColumn<Bulletin, String> mentionColumn;
    @FXML private TableColumn<Bulletin, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> semesterFilter;
    @FXML private ComboBox<String> statusFilter;

    private final BulletinService bulletinService = new BulletinServiceImpl(new JdbcBulletinDao());
    private final UserService userService = new UserServiceImpl(new JdbcUserDao());

    private final ObservableList<Bulletin> bulletinList = FXCollections.observableArrayList();
    private FilteredList<Bulletin> filteredBulletins;
    private Map<Integer, String> studentNames = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupFilters() {
        semesterFilter.setItems(FXCollections.observableArrayList("Tous", "1", "2"));
        semesterFilter.setValue("Tous");
        statusFilter.setItems(FXCollections.observableArrayList("Tous", "DRAFT", "VALIDATED", "PUBLISHED", "REVOKED"));
        statusFilter.setValue("Tous");

        searchField.textProperty().addListener((obs, old, nw) -> applyFilters());
        semesterFilter.setOnAction(e -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
    }

    private void setupTable() {
        studentColumn.setCellValueFactory(cd -> {
            int sId = cd.getValue().getStudentId();
            return new SimpleStringProperty(studentNames.getOrDefault(sId, "Étudiant #" + sId));
        });

        periodColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getAcademicYear() + " - S" + cd.getValue().getSemester()
        ));

        averageColumn.setCellValueFactory(new PropertyValueFactory<>("average"));
        if (rankColumn != null) rankColumn.setCellValueFactory(new PropertyValueFactory<>("classRank"));
        if (metierColumn != null) metierColumn.setCellValueFactory(new PropertyValueFactory<>("metier"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        mentionColumn.setCellValueFactory(new PropertyValueFactory<>("mention"));

        setupActionsColumn();

        filteredBulletins = new FilteredList<>(bulletinList, p -> true);
        SortedList<Bulletin> sortedBulletins = new SortedList<>(filteredBulletins);
        sortedBulletins.comparatorProperty().bind(bulletinsTable.comparatorProperty());
        bulletinsTable.setItems(sortedBulletins);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnPdf = new Button("📄");
            private final Button btnValidate = new Button("✅");
            private final Button btnPublish = new Button("✉");
            private final Button btnRevoke = new Button("❌");
            private final HBox container = new HBox(5, btnPdf, btnValidate, btnPublish, btnRevoke);

            {
                container.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                btnPdf.getStyleClass().add("btn-action-edit");
                btnValidate.getStyleClass().add("btn-action-edit");
                btnPublish.getStyleClass().add("btn-action-edit");
                btnRevoke.getStyleClass().add("btn-action-delete");

                btnPdf.setTooltip(new Tooltip("Télécharger PDF"));
                btnValidate.setTooltip(new Tooltip("Valider le bulletin"));
                btnPublish.setTooltip(new Tooltip("Publier & Envoyer Email"));
                btnRevoke.setTooltip(new Tooltip("Révoquer"));

                btnPdf.setOnAction(e -> handleDownloadPdf(getTableView().getItems().get(getIndex())));
                btnValidate.setOnAction(e -> handleValidate(getTableView().getItems().get(getIndex())));
                btnPublish.setOnAction(e -> handlePublish(getTableView().getItems().get(getIndex())));
                btnRevoke.setOnAction(e -> handleRevoke(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Bulletin b = getTableView().getItems().get(getIndex());
                    btnPublish.setDisable("PUBLISHED".equals(b.getStatus()));
                    btnValidate.setDisable("VALIDATED".equals(b.getStatus()) || "PUBLISHED".equals(b.getStatus()));
                    setGraphic(container);
                }
            }
        });
    }

    private void loadData() {
        try {
            studentNames = userService.getAllUsers().stream()
                .collect(Collectors.toMap(User::getId, User::getFullName, (a, b) -> a));
            bulletinList.setAll(bulletinService.getAllBulletins());
        } catch (Exception ex) {
            showAlert("Erreur", "Chargement impossible", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRecalculateRanks(ActionEvent e) {
        String year = "2023-2024"; // Should be picked from a selector ideally
        String sem = semesterFilter.getValue().equals("Tous") ? "1" : semesterFilter.getValue();
        bulletinService.recalculateRanks(year, sem);
        loadData();
        showAlert("Succès", "Calcul terminé", "Les rangs ont été recalculés pour le semestre " + sem, Alert.AlertType.INFORMATION);
    }

    private void handleValidate(Bulletin b) {
        b.setStatus("VALIDATED");
        b.setValidatedAt(LocalDateTime.now());
        bulletinService.updateBulletin(b);
        loadData();
    }

    private void handlePublish(Bulletin b) {
        b.setStatus("PUBLISHED");
        b.setPublishedAt(LocalDateTime.now());
        bulletinService.updateBulletin(b);
        // Simulation d'envoi d'email
        showAlert("Notification", "Email Envoyé", "Le bulletin a été publié et envoyé à l'étudiant.", Alert.AlertType.INFORMATION);
        loadData();
    }

    private void handleRevoke(Bulletin b) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Révocation");
        dialog.setHeaderText("Raison de la révocation");
        dialog.setContentText("Pourquoi révoquez-vous ce bulletin ?");
        Optional<String> res = dialog.showAndWait();
        res.ifPresent(reason -> {
            b.setStatus("REVOKED");
            b.setRevokedAt(LocalDateTime.now());
            b.setRevocationReason(reason);
            bulletinService.updateBulletin(b);
            loadData();
        });
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        // Logique pour ouvrir le formulaire d'ajout
    }

    @FXML
    public void applyFilters() {
        if (filteredBulletins == null) return;
        String query = searchField.getText().toLowerCase().trim();
        String sem = semesterFilter.getValue();
        String status = statusFilter.getValue();

        filteredBulletins.setPredicate(b -> {
            boolean matchesSearch = query.isEmpty() ||
                studentNames.getOrDefault(b.getStudentId(), "").toLowerCase().contains(query);
            boolean matchesSem = sem.equals("Tous") || b.getSemester().equals(sem);
            boolean matchesStatus = status.equals("Tous") || b.getStatus().equalsIgnoreCase(status);
            return matchesSearch && matchesSem && matchesStatus;
        });
    }

    private void handleDownloadPdf(Bulletin b) {
        try {
            User student = userService.getAllUsers().stream()
                .filter(u -> u.getId() == b.getStudentId()).findFirst().orElse(null);
            if (student != null) {
                File pdf = PdfGenerator.generateBulletinPdf(b, student);
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(pdf);
            }
        } catch (Exception ex) {
            showAlert("Erreur PDF", "Génération échouée", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }

    @FXML private void handleDashboard(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD); }
    @FXML private void handleManageCourses(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES); }
    @FXML private void handleManageModules(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES); }
    @FXML private void handleManageExams(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS); }
    @FXML private void handleShopManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT); }
    @FXML private void handleBulletins(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS); }
    @FXML private void handleCertifications(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS); }
    @FXML private void handleAnalysisAI(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI); }
    @FXML private void handleStudentManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT); }
    @FXML private void handleMetierManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_METIER_MANAGEMENT); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
