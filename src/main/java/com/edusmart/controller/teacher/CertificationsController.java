package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCertificationDao;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.Certification;
import com.edusmart.model.User;
import com.edusmart.service.CertificationService;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.CertificationServiceImpl;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Teacher UI — certifications mapped to the {@code certification} table.
 */
public class CertificationsController implements Initializable {

    private static final DateTimeFormatter ISSUED_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private TableView<Certification> certificationsTable;
    @FXML private TableColumn<Certification, Integer> idColumn;
    @FXML private TableColumn<Certification, Integer> studentIdColumn;
    @FXML private TableColumn<Certification, String> typeColumn;
    @FXML private TableColumn<Certification, String> issuedAtColumn;
    @FXML private TableColumn<Certification, String> verificationColumn;
    @FXML private TableColumn<Certification, String> statusColumn;
    @FXML private TableColumn<Certification, String> bulletinIdColumn;

    @FXML private ComboBox<User> studentUserComboBox;
    @FXML private TextField bulletinIdField;
    @FXML private TextField typeField;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker validUntilDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label messageLabel;
    @FXML private Label totalLabel;

    private final CertificationService certificationService = new CertificationServiceImpl(new JdbcCertificationDao());
    private final UserService userService = new UserServiceImpl(new JdbcUserDao());

    private ObservableList<Certification> certificationList = FXCollections.observableArrayList();
    private Certification selectedCertification;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupStudentCombo();
        setupTable();
        setupForm();
        loadCertifications();
    }

    private void setupStudentCombo() {
        if (studentUserComboBox == null) {
            return;
        }
        studentUserComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(User u) {
                if (u == null) {
                    return "";
                }
                String fn = u.getFirstName() != null ? u.getFirstName() : "";
                String ln = u.getLastName() != null ? u.getLastName() : "";
                return (fn + " " + ln).trim() + " (#" + u.getId() + ")";
            }

            @Override
            public User fromString(String s) {
                return null;
            }
        });
        reloadStudents();
    }

    private void reloadStudents() {
        if (studentUserComboBox == null) {
            return;
        }
        try {
            List<User> students = userService.getAllUsers().stream()
                    .filter(u -> u.getRoleValue() != null && "STUDENT".equalsIgnoreCase(u.getRoleValue().trim()))
                    .collect(Collectors.toList());
            studentUserComboBox.setItems(FXCollections.observableArrayList(students));
        } catch (RuntimeException ex) {
            showMessage("Impossible de charger les étudiants: " + rootCauseMessage(ex), true);
        }
    }

    private void setupTable() {
        if (idColumn != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (studentIdColumn != null) {
            studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        }
        if (typeColumn != null) {
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("certificationType"));
        }
        if (issuedAtColumn != null) {
            issuedAtColumn.setCellValueFactory(c -> {
                LocalDateTime at = c.getValue().getIssuedAt();
                return new SimpleStringProperty(at == null ? "" : at.format(ISSUED_FMT));
            });
        }
        if (verificationColumn != null) {
            verificationColumn.setCellValueFactory(c -> {
                String code = c.getValue().getVerificationCode();
                if (code == null) {
                    return new SimpleStringProperty("");
                }
                String shortCode = code.length() > 12 ? code.substring(0, 12) + "…" : code;
                return new SimpleStringProperty(shortCode);
            });
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }
        if (bulletinIdColumn != null) {
            bulletinIdColumn.setCellValueFactory(c -> {
                Integer bid = c.getValue().getBulletinId();
                return new SimpleStringProperty(bid == null ? "—" : String.valueOf(bid));
            });
        }
        if (certificationsTable != null) {
            certificationsTable.setItems(certificationList);
            certificationsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void setupForm() {
        if (statusComboBox != null) {
            statusComboBox.getItems().setAll(
                    Certification.STATUS_PENDING,
                    Certification.STATUS_ISSUED,
                    Certification.STATUS_EXPIRED,
                    Certification.STATUS_REVOKED
            );
            statusComboBox.setValue(Certification.STATUS_ISSUED);
        }
        if (issueDatePicker != null) {
            issueDatePicker.setValue(LocalDate.now());
        }
    }

    private void loadCertifications() {
        try {
            certificationList.setAll(certificationService.getAllCertifications());
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement certifications: " + rootCauseMessage(ex), true);
        }
        updateCount();
    }

    private void populateForm(Certification cert) {
        selectedCertification = cert;
        if (cert == null) {
            return;
        }
        if (typeField != null) {
            typeField.setText(cert.getCertificationType() != null ? cert.getCertificationType() : "");
        }
        if (issueDatePicker != null && cert.getIssuedAt() != null) {
            issueDatePicker.setValue(cert.getIssuedAt().toLocalDate());
        }
        if (validUntilDatePicker != null) {
            validUntilDatePicker.setValue(cert.getValidUntil() != null ? cert.getValidUntil().toLocalDate() : null);
        }
        if (statusComboBox != null && cert.getStatus() != null) {
            statusComboBox.setValue(cert.getStatus().toUpperCase());
        }
        if (bulletinIdField != null) {
            bulletinIdField.setText(cert.getBulletinId() != null ? String.valueOf(cert.getBulletinId()) : "");
        }
        if (studentUserComboBox != null) {
            Optional<User> match = studentUserComboBox.getItems().stream()
                    .filter(u -> u.getId() == cert.getStudentId())
                    .findFirst();
            match.ifPresent(u -> studentUserComboBox.setValue(u));
        }
    }

    @FXML
    private void handleIssue(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        Certification c = buildCertificationFromForm();
        try {
            if (certificationService.issueCertification(c)) {
                showMessage("Certification enregistrée.", false);
                clearForm();
                selectedCertification = null;
                reloadStudents();
                loadCertifications();
            } else {
                showMessage("Échec de l'enregistrement.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleRevoke(ActionEvent event) {
        if (selectedCertification == null) {
            showMessage("Sélectionnez une certification dans le tableau.", true);
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Révocation");
        dialog.setHeaderText("Motif de révocation");
        dialog.setContentText("Raison:");
        Optional<String> reason = dialog.showAndWait();
        if (reason.isEmpty()) {
            return;
        }
        try {
            if (certificationService.revokeCertification(selectedCertification.getId(), reason.get())) {
                showMessage("Certification révoquée.", false);
                loadCertifications();
            } else {
                showMessage("Révocation impossible (ligne introuvable ?).", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        selectedCertification = null;
        if (certificationsTable != null) {
            certificationsTable.getSelectionModel().clearSelection();
        }
    }

    private Certification buildCertificationFromForm() {
        Certification c = new Certification();
        User u = studentUserComboBox != null ? studentUserComboBox.getValue() : null;
        c.setStudentId(u != null ? u.getId() : 0);
        c.setCertificationType(typeField != null ? typeField.getText().trim() : "");
        LocalDate issueD = issueDatePicker != null ? issueDatePicker.getValue() : LocalDate.now();
        c.setIssuedAt(issueD != null ? issueD.atStartOfDay() : LocalDateTime.now());
        if (validUntilDatePicker != null && validUntilDatePicker.getValue() != null) {
            c.setValidUntil(validUntilDatePicker.getValue().atTime(23, 59, 59));
        }
        c.setStatus(statusComboBox != null && statusComboBox.getValue() != null
                ? statusComboBox.getValue().trim().toUpperCase()
                : Certification.STATUS_ISSUED);
        c.setBulletinId(parseOptionalBulletinId());
        c.setPdfPath(null);
        c.setHmacHash(null);
        c.setRevokedAt(null);
        c.setRevocationReason(null);
        return c;
    }

    private Integer parseOptionalBulletinId() {
        if (bulletinIdField == null) {
            return null;
        }
        String t = bulletinIdField.getText().trim();
        if (t.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("ID bulletin invalide.");
        }
    }

    private boolean validateForm() {
        if (studentUserComboBox == null || studentUserComboBox.getValue() == null) {
            showMessage("Choisissez un étudiant.", true);
            return false;
        }
        if (typeField == null || typeField.getText().trim().isEmpty()) {
            showMessage("Le type de certification est obligatoire.", true);
            return false;
        }
        if (typeField.getText().trim().length() > 50) {
            showMessage("Le type ne doit pas dépasser 50 caractères.", true);
            return false;
        }
        if (issueDatePicker == null || issueDatePicker.getValue() == null) {
            showMessage("La date d'émission est obligatoire.", true);
            return false;
        }
        try {
            parseOptionalBulletinId();
        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage(), true);
            return false;
        }
        return true;
    }

    private void clearForm() {
        if (typeField != null) {
            typeField.clear();
        }
        if (issueDatePicker != null) {
            issueDatePicker.setValue(LocalDate.now());
        }
        if (validUntilDatePicker != null) {
            validUntilDatePicker.setValue(null);
        }
        if (bulletinIdField != null) {
            bulletinIdField.clear();
        }
        if (statusComboBox != null) {
            statusComboBox.setValue(Certification.STATUS_ISSUED);
        }
        if (studentUserComboBox != null) {
            studentUserComboBox.setValue(null);
        }
    }

    private void updateCount() {
        if (totalLabel != null) {
            totalLabel.setText(certificationList.size() + " certification(s)");
        }
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    private static String rootCauseMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage() != null ? t.getMessage() : t.toString();
    }

    public ObservableList<Certification> getCertificationList() {
        return certificationList;
    }

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
