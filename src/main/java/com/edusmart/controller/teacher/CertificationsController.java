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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * CertificationsController - Table-only controller + dialogs.
 */
public class CertificationsController implements Initializable {

    @FXML private TableView<Certification> certificationsTable;
    @FXML private TableColumn<Certification, Integer> idColumn;
    @FXML private TableColumn<Certification, String> typeColumn;
    @FXML private TableColumn<Certification, String> studentColumn;
    @FXML private TableColumn<Certification, String> statusColumn;
    @FXML private TableColumn<Certification, String> issuedAtColumn;
    @FXML private TableColumn<Certification, String> validUntilColumn;
    @FXML private TableColumn<Certification, String> uniqueNumberColumn;

    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    private final CertificationService certService = new CertificationServiceImpl(new JdbcCertificationDao());
    private final UserService userService = new UserServiceImpl(new JdbcUserDao());

    private final ObservableList<Certification> certList = FXCollections.observableArrayList();
    private Map<Integer, String> studentNames = new HashMap<>();

    private static final DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (typeColumn != null) typeColumn.setCellValueFactory(new PropertyValueFactory<>("certificationType"));
        if (studentColumn != null) {
            studentColumn.setCellValueFactory(cd -> {
                int sId = cd.getValue().getStudentId();
                return new SimpleStringProperty(studentNames.getOrDefault(sId, "Élève #" + sId));
            });
        }
        if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (issuedAtColumn != null) {
            issuedAtColumn.setCellValueFactory(cd -> {
                LocalDateTime d = cd.getValue().getIssuedAt();
                return new SimpleStringProperty(d != null ? d.format(FMT_DATETIME) : "");
            });
        }
        if (validUntilColumn != null) {
            validUntilColumn.setCellValueFactory(cd -> {
                LocalDateTime d = cd.getValue().getValidUntil();
                return new SimpleStringProperty(d != null ? d.format(FMT_DATE) : "-");
            });
        }
        if (uniqueNumberColumn != null) uniqueNumberColumn.setCellValueFactory(new PropertyValueFactory<>("uniqueNumber"));

        if (certificationsTable != null) certificationsTable.setItems(certList);
    }

    private void loadData() {
        try {
            studentNames = userService.getAllUsers().stream()
                .collect(Collectors.toMap(User::getId, User::getFullName, (a,b)->a));
            certList.setAll(certService.getAllCertifications());
            if (certificationsTable != null) certificationsTable.refresh();
        } catch (Exception ex) { showMessage("Erreur chargement: " + rootCause(ex), true); }
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        Stage owner = (Stage) certificationsTable.getScene().getWindow();
        if (CertificationFormController.openDialog(owner, null)) {
            loadData();
            showMessage("Certification ajoutée.", false);
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        Certification sel = certificationsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez une certification.", true); return; }
        Stage owner = (Stage) certificationsTable.getScene().getWindow();
        if (CertificationFormController.openDialog(owner, sel)) {
            loadData();
            showMessage("Certification modifiée.", false);
        }
    }

    @FXML
    private void handleDelete(ActionEvent e) {
        Certification sel = certificationsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez une certification.", true); return; }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la certification #" + sel.getId() + " ?", ButtonType.YES, ButtonType.NO);
        conf.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    if (certService.deleteCertification(sel.getId())) {
                        certList.remove(sel); showMessage("Supprimée.", false);
                    } else showMessage("Échec de la suppression.", true);
                } catch (Exception ex) { showMessage("Erreur : " + rootCause(ex), true); }
            }
        });
    }

    @FXML
    private void handleRevoke(ActionEvent e) {
        Certification sel = certificationsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez une certification à révoquer.", true); return; }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Révoquer cette certification. Êtes-vous sûr ?", ButtonType.YES, ButtonType.NO);
        conf.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    // Fix: Method requires two arguments: id and reason
                    if (certService.revokeCertification(sel.getId(), "Révoquée par enseignant")) {
                        loadData(); showMessage("Certification révoquée !", false);
                    } else showMessage("Échec révocation.", true);
                } catch (Exception ex) { showMessage("Erreur: " + rootCause(ex), true); }
            }
        });
    }

    @FXML
    private void handleDownloadPdf(ActionEvent e) {
        Certification sel = certificationsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez une certification.", true); return; }
        try {
            Optional<User> studentOpt = userService.getAllUsers().stream()
                    .filter(u -> u.getId() == sel.getStudentId())
                    .findFirst();
            if (studentOpt.isPresent()) {
                File pdf = PdfGenerator.generateCertificationPdf(sel, studentOpt.get());
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdf);
                }
                showMessage("PDF ouvert : " + pdf.getName(), false);
            }
        } catch (Exception ex) { showMessage("Erreur PDF: " + rootCause(ex), true); }
    }

    @FXML
    private void handleSearch(ActionEvent e) {
        String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (q.isEmpty()) { loadData(); return; }
        List<Certification> filtered = certService.getAllCertifications().stream()
                .filter(c -> (c.getUniqueNumber() != null && c.getUniqueNumber().toLowerCase().contains(q))
                          || (c.getCertificationType() != null && c.getCertificationType().toLowerCase().contains(q))
                          || studentNames.getOrDefault(c.getStudentId(), "").toLowerCase().contains(q))
                .collect(Collectors.toList());
        certList.setAll(filtered);
    }

    private void showMessage(String msg, boolean isErr) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isErr ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }
    private String rootCause(Throwable t) { while(t.getCause()!=null) t=t.getCause(); return t.getMessage(); }

    @FXML private void handleDashboard(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD); }
    @FXML private void handleManageCourses(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES); }
    @FXML private void handleManageModules(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES); }
    @FXML private void handleManageExams(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS); }
    @FXML private void handleGradeManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_GRADE_MANAGEMENT); }
    @FXML private void handleShopManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT); }
    @FXML private void handleCategoryManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CATEGORY_MANAGEMENT); }
    @FXML private void handleBulletins(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS); }
    @FXML private void handleCertifications(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS); }
    @FXML private void handleAnalysisAI(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI); }
    @FXML private void handleStudentManagement(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
