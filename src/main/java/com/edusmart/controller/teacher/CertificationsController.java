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
            private final Button btnPublish = new Button("✉");
            private final Button btnSms = new Button("📱");
            private final Button btnRevoke = new Button("❌");
            private final HBox container = new HBox(5, btnPdf, btnPublish, btnSms, btnRevoke);

            {
                container.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                btnPdf.getStyleClass().add("btn-action-edit");
                btnPublish.getStyleClass().add("btn-action-edit");
                btnSms.getStyleClass().add("btn-action-edit");
                btnRevoke.getStyleClass().add("btn-action-delete");
                btnPdf.setTooltip(new Tooltip("Télécharger PDF"));
                btnPublish.setTooltip(new Tooltip("Envoyer par Email"));
                btnSms.setTooltip(new Tooltip("Notifier par SMS"));
                btnRevoke.setTooltip(new Tooltip("Révoquer"));
                
                btnPdf.setOnAction(e -> handleDownloadPdf(getTableView().getItems().get(getIndex())));
                btnPublish.setOnAction(e -> handlePublish(getTableView().getItems().get(getIndex())));
                btnSms.setOnAction(e -> handleSms(getTableView().getItems().get(getIndex())));
                btnRevoke.setOnAction(e -> handleRevoke(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Certification c = getTableView().getItems().get(getIndex());
                    btnRevoke.setDisable("REVOKED".equals(c.getStatus()));
                    setGraphic(container);
                }
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
        java.util.List<User> students = userService.getAllUsers().stream().filter(u -> u.getRole() == User.Role.STUDENT).toList();
        java.util.List<Certification> existingCerts = certService.getAllCertifications();
        int created = 0;

        for (User student : students) {
            boolean hasCert = existingCerts.stream().anyMatch(c -> c.getStudentId() == student.getId());
            if (!hasCert) {
                Certification c = new Certification();
                c.setUniqueNumber("CERT-" + java.time.Year.now().getValue() + "-" + String.format("%03d", student.getId()));
                c.setCertificationType("Participation au programme");
                c.setStudentId(student.getId());
                c.setIssuedAt(java.time.LocalDateTime.now());
                c.setStatus(Certification.STATUS_ISSUED);
                try {
                    certService.issueCertification(c);
                    created++;
                } catch(Exception ex) {}
            }
        }
        if (created > 0) {
            loadData();
            showAlert("Succès", "Certifications générées", created + " nouvelle(s) certification(s) générée(s) pour les élèves restants.");
        } else {
            showAlert("Information", "Génération terminée", "Tous les étudiants ont déjà au moins une certification.");
        }
    }

    private void handlePublish(Certification c) {
        try {
            User u = userService.getAllUsers().stream()
                .filter(user -> user.getId() == c.getStudentId()).findFirst().orElse(null);
            if (u != null && u.getEmail() != null) {
                String studentName = u.getFirstName() + " " + u.getLastName();
                String dateDelivrance = c.getIssuedAt() != null ? c.getIssuedAt().format(FMT_DATE) : "";
                String validite = c.getValidUntil() != null ? c.getValidUntil().format(FMT_DATE) : "Permanente";
                String htmlBody = com.edusmart.util.MailSender.buildCertificationEmailBody(
                    studentName, c.getCertificationType(), dateDelivrance, validite, c.getUniqueNumber());
                
                com.edusmart.util.MailSender.sendEmailWithAttachment(u.getEmail(), "Votre Certification EduSmart", htmlBody, null);
                showAlert("Succès", "Email Envoyé", "La certification a été envoyée à " + u.getEmail());
            } else {
                showAlert("Avertissement", "Email introuvable", "L'adresse email de cet étudiant est introuvable.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Échec d'envoi", "L'email n'a pas pu être envoyé : " + ex.getMessage());
        }
    }

    private void handleSms(Certification c) {
        try {
            User u = userService.getAllUsers().stream()
                .filter(user -> user.getId() == c.getStudentId()).findFirst().orElse(null);
            if (u != null && u.getNumtel() != null && !u.getNumtel().isEmpty()) {
                String studentName = u.getFirstName() + " " + u.getLastName();
                String formattedPhone = u.getNumtel().startsWith("+") ? u.getNumtel() : "+216" + u.getNumtel();
                String msg = "EduSmart - Bonjour " + studentName + ", votre certification " + c.getCertificationType() + " est maintenant disponible.";
                com.edusmart.util.SmsSender.sendSms(formattedPhone, msg);
                showAlert("Succès", "SMS Envoyé", "Un SMS a été envoyé au numéro " + formattedPhone);
            } else {
                showAlert("Avertissement", "Numéro introuvable", "Le numéro de téléphone de cet étudiant est introuvable.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Échec d'envoi", "Le SMS n'a pas pu être envoyé : " + ex.getMessage());
        }
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
    @FXML private void handleGradeManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_GRADE_MANAGEMENT); }
    @FXML private void handleMetierManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_METIER_MANAGEMENT); }
    @FXML private void handleProfile(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE); }
    @FXML private void handleSearch(ActionEvent e) { applyFilters(); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
