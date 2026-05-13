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
    @FXML private ComboBox<String> typeFilter;
    @FXML private TextField rankFilter;

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
        if (typeFilter != null) {
            typeFilter.setItems(FXCollections.observableArrayList("Tous", "Régulier", "Final"));
            typeFilter.setValue("Tous");
            typeFilter.setOnAction(e -> applyFilters());
        }

        searchField.textProperty().addListener((obs, old, nw) -> applyFilters());
        if (rankFilter != null) rankFilter.textProperty().addListener((obs, old, nw) -> applyFilters());
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
            private final Button btnPdf      = new Button("📄");
            private final Button btnValidate = new Button("⬆️");
            private final Button btnEmail    = new Button("✉");
            private final Button btnSms      = new Button("📱");
            private final Button btnRevoke   = new Button("❌");
            private final HBox container = new HBox(5, btnPdf, btnValidate, btnEmail, btnSms, btnRevoke);

            {
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                btnPdf.getStyleClass().add("btn-action-edit");
                btnValidate.getStyleClass().add("btn-action-edit");
                btnEmail.getStyleClass().add("btn-action-edit");
                btnSms.getStyleClass().add("btn-action-edit");
                btnRevoke.getStyleClass().add("btn-action-delete");

                btnPdf.setTooltip(new Tooltip("Télécharger PDF"));
                btnValidate.setTooltip(new Tooltip("Publier le bulletin"));
                btnEmail.setTooltip(new Tooltip("Envoyer par Email"));
                btnSms.setTooltip(new Tooltip("Notifier par SMS"));
                btnRevoke.setTooltip(new Tooltip("Révoquer"));

                btnPdf.setOnAction(e      -> handleDownloadPdf(getTableView().getItems().get(getIndex())));
                btnValidate.setOnAction(e -> handlePublish(getTableView().getItems().get(getIndex())));
                btnEmail.setOnAction(e    -> handleSendEmail(getTableView().getItems().get(getIndex())));
                btnSms.setOnAction(e      -> handleSms(getTableView().getItems().get(getIndex())));
                btnRevoke.setOnAction(e   -> handleRevoke(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Bulletin b = getTableView().getItems().get(getIndex());
                // Publish button disabled only when already published/revoked
                btnValidate.setDisable("PUBLISHED".equals(b.getStatus()) || "REVOKED".equals(b.getStatus()));
                // Email button always enabled
                btnEmail.setDisable(false);
                setGraphic(container);
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

    private void handlePublish(Bulletin b) {
        if ("PUBLISHED".equals(b.getStatus())) return;
        b.setStatus("PUBLISHED");
        b.setPublishedAt(LocalDateTime.now());
        bulletinService.updateBulletin(b);
        loadData();
        showAlert("Succès", "Publié", "Le bulletin a été publié.", Alert.AlertType.INFORMATION);
    }

    private void handleSendEmail(Bulletin b) {
        try {
            com.edusmart.model.User u = userService.getAllUsers().stream()
                .filter(user -> user.getId() == b.getStudentId()).findFirst().orElse(null);
            if (u != null && u.getEmail() != null && !u.getEmail().isEmpty()) {
                String studentName = u.getFirstName() + " " + u.getLastName();
                String htmlBody = com.edusmart.util.MailSender.buildBulletinEmailBody(
                    studentName, b.getAcademicYear(), b.getSemester(),
                    String.valueOf(b.getAverage()), b.getMention(), String.valueOf(b.getClassRank()));
                com.edusmart.util.MailSender.sendEmailWithAttachment(u.getEmail(), "Votre Bulletin EduSmart", htmlBody, null);
                showAlert("Succès", "Email Envoyé", "Le bulletin a été envoyé à " + u.getEmail(), Alert.AlertType.INFORMATION);
            } else {
                showAlert("Avertissement", "Email non envoyé", "Adresse email introuvable pour cet étudiant.", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec d'envoi", "L'email n'a pas pu être envoyé : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleSms(Bulletin b) {
        try {
            com.edusmart.model.User u = userService.getAllUsers().stream()
                .filter(user -> user.getId() == b.getStudentId()).findFirst().orElse(null);
                
            if (u != null && u.getNumtel() != null && !u.getNumtel().isEmpty()) {
                String studentName = u.getFirstName() + " " + u.getLastName();
                String formattedPhone = u.getNumtel().startsWith("+") ? u.getNumtel() : "+216" + u.getNumtel();
                
                com.edusmart.util.SmsSender.notifyBulletin(formattedPhone, studentName, b.getSemester(), b.getAcademicYear());
                showAlert("Succès", "SMS Envoyé", "Un SMS a été envoyé au numéro " + formattedPhone, Alert.AlertType.INFORMATION);
            } else {
                showAlert("Avertissement", "SMS non envoyé", "Numéro de téléphone introuvable ou invalide.", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec d'envoi", "Le SMS n'a pas pu être envoyé : " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
        java.util.List<com.edusmart.model.User> students = userService.getAllUsers().stream().filter(u -> u.getRole() == com.edusmart.model.User.Role.STUDENT).toList();
        java.util.List<Bulletin> existingBulletins = bulletinService.getAllBulletins();
        int created = 0;
        String sem = semesterFilter != null ? semesterFilter.getValue() : "1";
        if (sem == null || sem.equals("Tous")) sem = "1";
        final String targetSem = sem;

        for (com.edusmart.model.User student : students) {
            boolean hasBulletin = existingBulletins.stream().anyMatch(b -> b.getStudentId() == student.getId() && b.getSemester().equals(targetSem));
            if (!hasBulletin) {
                Double avg = bulletinService.calculateStudentAverage(student.getId(), targetSem);
                if (avg != null && avg > 0) {
                    Bulletin b = new Bulletin();
                    b.setStudentId(student.getId());
                    b.setAcademicYear("2023-2024");
                    b.setSemester(targetSem);
                    b.setAverage(avg);
                    b.setType("Régulier");
                    b.setStatus("DRAFT");
                    bulletinService.createBulletin(b);
                    created++;
                }
            }
        }
        if (created > 0) {
            bulletinService.recalculateRanks("2023-2024", targetSem);
            loadData();
            showAlert("Succès", "Bulletins générés", created + " nouveau(x) bulletin(s) brouillon généré(s) pour le semestre " + targetSem + ".", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Information", "Génération terminée", "Tous les étudiants ayant des notes ont déjà un bulletin pour ce semestre.", Alert.AlertType.INFORMATION);
        }
    }



    @FXML
    public void applyFilters() {
        if (filteredBulletins == null) return;
        String query = searchField.getText().toLowerCase().trim();
        String sem = semesterFilter.getValue();
        String status = statusFilter.getValue();
        String type = typeFilter != null ? typeFilter.getValue() : "Tous";
        String rankStr = rankFilter != null ? rankFilter.getText().trim() : "";
        Integer maxRankParsed = null;
        try { if (!rankStr.isEmpty()) maxRankParsed = Integer.parseInt(rankStr); } catch (Exception ignore) {}
        final Integer maxRank = maxRankParsed;

        filteredBulletins.setPredicate(b -> {
            boolean matchesSearch = query.isEmpty() ||
                studentNames.getOrDefault(b.getStudentId(), "").toLowerCase().contains(query);
            boolean matchesSem = sem == null || sem.equals("Tous") || b.getSemester().equals(sem);
            boolean matchesStatus = status == null || status.equals("Tous") || b.getStatus().equalsIgnoreCase(status);
            boolean matchesType = type == null || type.equals("Tous") || (b.getType() != null && b.getType().equalsIgnoreCase(type));
            boolean matchesRank = maxRank == null || (b.getClassRank() != null && b.getClassRank() <= maxRank);
            return matchesSearch && matchesSem && matchesStatus && matchesType && matchesRank;
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
    @FXML private void handleGradeManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_GRADE_MANAGEMENT); }
    @FXML private void handleMetierManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_METIER_MANAGEMENT); }
    @FXML private void handleProfile(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
