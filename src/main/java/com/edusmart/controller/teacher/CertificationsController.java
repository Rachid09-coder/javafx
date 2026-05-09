package com.edusmart.controller.teacher;

import com.edusmart.model.Certification;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * CertificationsController - Teacher interface for managing student certifications.
 *
 * Team member: Implement CRUD operations via service layer.
 */
public class CertificationsController implements Initializable {

    @FXML private TableView<Certification> certificationsTable;
    @FXML private TableColumn<Certification, Integer> idColumn;
    @FXML private TableColumn<Certification, Integer> studentIdColumn;
    @FXML private TableColumn<Certification, String> titleColumn;
    @FXML private TableColumn<Certification, LocalDate> issueDateColumn;
    @FXML private TableColumn<Certification, Certification.Status> statusColumn;

    @FXML private ComboBox<String> studentComboBox;
    @FXML private ComboBox<String> courseComboBox;
    @FXML private TextField titleField;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker expiryDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label messageLabel;
    @FXML private Label totalLabel;

    private ObservableList<Certification> certificationList = FXCollections.observableArrayList();
    private Certification selectedCertification;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupForm();
        loadCertifications();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (studentIdColumn != null) studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (issueDateColumn != null) issueDateColumn.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (certificationsTable != null) {
            certificationsTable.setItems(certificationList);
            certificationsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void setupForm() {
        if (studentComboBox != null)
            studentComboBox.getItems().addAll("Étudiant 1", "Étudiant 2", "Étudiant 3");
        if (courseComboBox != null)
            courseComboBox.getItems().addAll("Introduction à Java", "Mathématiques Avancées");
        if (statusComboBox != null)
            statusComboBox.getItems().addAll("PENDING", "ISSUED", "EXPIRED", "REVOKED");
    }

    /**
     * Loads all certifications.
     * TODO: Replace with service call.
     */
    private void loadCertifications() {
        // TODO: certificationList.setAll(CertificationService.getAllCertifications());

        certificationList.setAll(
            new Certification(1, 1, 1, "Certificat Java Fondamentaux", LocalDate.now().minusDays(30), Certification.Status.ISSUED),
            new Certification(2, 2, 2, "Certificat Mathématiques", LocalDate.now().minusDays(15), Certification.Status.ISSUED),
            new Certification(3, 3, 1, "Certificat Java Avancé", null, Certification.Status.PENDING)
        );
        updateCount();
    }

    private void populateForm(Certification cert) {
        selectedCertification = cert;
        if (cert == null) return;
        if (titleField != null) titleField.setText(cert.getTitle());
        if (issueDatePicker != null) issueDatePicker.setValue(cert.getIssueDate());
        if (expiryDatePicker != null) expiryDatePicker.setValue(cert.getExpiryDate());
        if (statusComboBox != null && cert.getStatus() != null) statusComboBox.setValue(cert.getStatus().name());
    }

    @FXML
    private void handleIssue(ActionEvent event) {
        if (!validateForm()) return;
        // TODO: CertificationService.issueCertification(buildCertificationFromForm());
        showMessage("Certificat émis avec succès!", false);
        clearForm();
        loadCertifications();
    }

    @FXML
    private void handleRevoke(ActionEvent event) {
        if (selectedCertification == null) { showMessage("Sélectionnez un certificat.", true); return; }
        // TODO: CertificationService.revokeCertification(selectedCertification.getId());
        showMessage("Certificat révoqué.", false);
        loadCertifications();
    }

    @FXML
    private void handleClear(ActionEvent event) { clearForm(); selectedCertification = null; }

    private boolean validateForm() {
        if (titleField != null && titleField.getText().trim().isEmpty()) {
            showMessage("Le titre du certificat est obligatoire.", true);
            return false;
        }
        return true;
    }

    private void clearForm() {
        if (titleField != null) titleField.clear();
        if (issueDatePicker != null) issueDatePicker.setValue(null);
        if (expiryDatePicker != null) expiryDatePicker.setValue(null);
    }

    private void updateCount() {
        if (totalLabel != null)
            totalLabel.setText(certificationList.size() + " certification(s)");
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    public ObservableList<Certification> getCertificationList() {
        return certificationList;
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
