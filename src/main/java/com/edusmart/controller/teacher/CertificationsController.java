package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCertificationDao;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.Certification;
import com.edusmart.model.User;
import com.edusmart.service.CertificationService;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.CertificationServiceImpl;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.util.PdfGenerator;
import com.edusmart.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * CertificationsController - Table-only controller + dialogs.
 */
public class CertificationsController implements Initializable {

    @FXML private TableView<Certification> certificationsTable;
    @FXML private TableColumn<Certification, String> uniqueNumberColumn;
    @FXML private TableColumn<Certification, String> typeColumn;
    @FXML private TableColumn<Certification, String> studentColumn;
    @FXML private TableColumn<Certification, String> metierColumn;
    @FXML private TableColumn<Certification, String> statusColumn;
    @FXML private TableColumn<Certification, String> issuedAtColumn;
    @FXML private TableColumn<Certification, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    private final CertificationService certService = new CertificationServiceImpl(new JdbcCertificationDao());
    private final UserService userService = new UserServiceImpl(new JdbcUserDao());

    private final ObservableList<Certification> certList = FXCollections.observableArrayList();
    private FilteredList<Certification> filteredCerts;
    private Map<Integer, String> studentNames = new HashMap<>();

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilters();
        if (metierColumn != null) metierColumn.setCellValueFactory(new PropertyValueFactory<>("metier"));
        loadData();
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("Tous", "PENDING", "ISSUED", "REVOKED"));
        statusFilter.setValue("Tous");
        searchField.textProperty().addListener((obs, old, nw) -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
    }

    private void setupTable() {
        uniqueNumberColumn.setCellValueFactory(new PropertyValueFactory<>("uniqueNumber"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("certificationType"));
        studentColumn.setCellValueFactory(cd -> {
            int sId = cd.getValue().getStudentId();
            return new SimpleStringProperty(studentNames.getOrDefault(sId, "Élève #" + sId));
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        issuedAtColumn.setCellValueFactory(cd -> {
            LocalDateTime d = cd.getValue().getIssuedAt();
            return new SimpleStringProperty(d != null ? d.format(FMT_DATE) : "-");
        });

        setupActionsColumn();

        filteredCerts = new FilteredList<>(certList, p -> true);
        SortedList<Certification> sortedCerts = new SortedList<>(filteredCerts);
        sortedCerts.comparatorProperty().bind(certificationsTable.comparatorProperty());
        certificationsTable.setItems(sortedCerts);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnPdf = new Button("📄");
            private final Button btnRevoke = new Button("❌");
            private final HBox container = new HBox(5, btnPdf, btnRevoke);

            {
                container.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                btnPdf.getStyleClass().add("btn-action-edit");
                btnRevoke.getStyleClass().add("btn-action-delete");
                btnPdf.setOnAction(e -> handleDownloadPdf(getTableView().getItems().get(getIndex())));
                btnRevoke.setOnAction(e -> handleRevoke(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(container);
            }
        });
    }

    private void loadData() {
        try {
            studentNames = userService.getAllUsers().stream()
                .collect(Collectors.toMap(User::getId, User::getFullName, (a, b) -> a));
            certList.setAll(certService.getAllCertifications());
        } catch (Exception ex) {
            showAlert("Erreur", "Chargement impossible", ex.getMessage());
        }
    }

    private void applyFilters() {
        if (filteredCerts == null) return;
        String query = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();

        filteredCerts.setPredicate(c -> {
            boolean matchesSearch = query.isEmpty() ||
                (c.getUniqueNumber() != null && c.getUniqueNumber().toLowerCase().contains(query)) ||
                studentNames.getOrDefault(c.getStudentId(), "").toLowerCase().contains(query);
            boolean matchesStatus = status.equals("Tous") || c.getStatus().equalsIgnoreCase(status);
            return matchesSearch && matchesStatus;
        });
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        // Formulaire d'ajout
    }

    private void handleRevoke(Certification c) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Révoquer cette certification ?", ButtonType.YES, ButtonType.NO);
        if (conf.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            certService.revokeCertification(c.getId(), "Révoqué par admin");
            loadData();
        }
    }

    private void handleDownloadPdf(Certification c) {
        try {
            User student = userService.getAllUsers().stream()
                .filter(u -> u.getId() == c.getStudentId()).findFirst().orElse(null);
            if (student != null) {
                File pdf = PdfGenerator.generateCertificationPdf(c, student);
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(pdf);
            }
        } catch (Exception ex) {
            showAlert("Erreur PDF", "Génération échouée", ex.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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
    @FXML private void handleSearch(ActionEvent e) { applyFilters(); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
