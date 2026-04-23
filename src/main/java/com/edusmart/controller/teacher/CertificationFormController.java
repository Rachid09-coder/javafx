package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcBulletinDao;
import com.edusmart.dao.jdbc.JdbcCertificationDao;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.Bulletin;
import com.edusmart.model.Certification;
import com.edusmart.model.User;
import com.edusmart.service.CertificationService;
import com.edusmart.service.BulletinService;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.CertificationServiceImpl;
import com.edusmart.service.impl.BulletinServiceImpl;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.util.MailSender;
import com.edusmart.util.PdfGenerator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur du formulaire Certification (dialog séparé).
 */
public class CertificationFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField typeField;
    @FXML private Label typeError;
    @FXML private ComboBox<User> studentComboBox;
    @FXML private Label studentError;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<Bulletin> bulletinComboBox;
    @FXML private DatePicker validUntilPicker;
    @FXML private ComboBox<String> metierComboBox;
    @FXML private Label globalError;

    private final CertificationService certService = new CertificationServiceImpl(new JdbcCertificationDao());
    private final UserService userService = new UserServiceImpl(new JdbcUserDao());
    private final BulletinService bulletinService = new BulletinServiceImpl(new JdbcBulletinDao());

    private Certification certToEdit;
    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusComboBox.setItems(FXCollections.observableArrayList(
                Certification.STATUS_ISSUED, Certification.STATUS_PENDING,
                Certification.STATUS_EXPIRED, Certification.STATUS_REVOKED));
        statusComboBox.setValue(Certification.STATUS_ISSUED);
        setupStudentCombo();
        setupBulletinCombo();
        setupMetierCombo();
        typeField.textProperty().addListener((o, ov, nv) -> clearError(typeField, typeError));
    }

    private void setupMetierCombo() {
        metierComboBox.setItems(FXCollections.observableArrayList(
            new com.edusmart.dao.jdbc.JdbcMetierDao().findAll().stream()
                .map(com.edusmart.model.Metier::getNom)
                .toList()
        ));
    }

    private void setupStudentCombo() {
        studentComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getFullName() + " (" + u.getEmail() + ")");
            }
        });
        studentComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getFullName());
            }
        });
        List<User> users = new ArrayList<>();
        try { users.addAll(userService.getAllUsers()); } catch (Exception ignored) {}
        studentComboBox.setItems(FXCollections.observableArrayList(users));
    }

    private void setupBulletinCombo() {
        bulletinComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Bulletin b, boolean empty) {
                super.updateItem(b, empty);
                setText(empty || b == null ? null :
                        "Bulletin #" + b.getId() + " – " + b.getAcademicYear() + " " + b.getSemester());
            }
        });
        bulletinComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Bulletin b, boolean empty) {
                super.updateItem(b, empty);
                setText(empty || b == null ? null : "Bulletin #" + b.getId());
            }
        });
        List<Bulletin> bulletins = new ArrayList<>();
        try { bulletins.addAll(bulletinService.getAllBulletins()); } catch (Exception ignored) {}
        bulletinComboBox.setItems(FXCollections.observableArrayList(bulletins));
    }

    public void setAddMode() { titleLabel.setText("Nouvelle Certification"); certToEdit = null; }

    public void setEditMode(Certification cert) {
        titleLabel.setText("Modifier la Certification");
        certToEdit = cert;
        typeField.setText(cert.getCertificationType() != null ? cert.getCertificationType() : "");
        if (cert.getStatus() != null) statusComboBox.setValue(cert.getStatus());
        if (cert.getMetier() != null) metierComboBox.setValue(cert.getMetier());
        if (cert.getValidUntil() != null) validUntilPicker.setValue(cert.getValidUntil().toLocalDate());
        // Pre-select student
        if (cert.getStudentId() > 0) {
            studentComboBox.getItems().stream()
                .filter(u -> u.getId() == cert.getStudentId())
                .findFirst().ifPresent(studentComboBox::setValue);
        }
        // Pre-select bulletin
        if (cert.getBulletinId() != null) {
            bulletinComboBox.getItems().stream()
                .filter(b -> b.getId() == cert.getBulletinId())
                .findFirst().ifPresent(bulletinComboBox::setValue);
        }
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave(ActionEvent e) {
        if (!validateForm()) return;
        try {
            Certification cert = buildCertification();
            boolean ok;
            if (certToEdit == null) ok = certService.issueCertification(cert);
            else { cert.setId(certToEdit.getId()); ok = certService.updateCertification(cert); }
            if (ok) {
                saved = true;
                // NEW: Generate PDF and send Email
                try {
                    User student = studentComboBox.getValue();
                    if (student != null && student.getEmail() != null) {
                        File pdf = PdfGenerator.generateCertificationPdf(cert, student);
                        cert.setPdfPath(pdf.getAbsolutePath());
                        certService.updateCertification(cert); // Update with PDF path

                        String issuedDate  = cert.getIssuedAt()  != null ? cert.getIssuedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))  : "N/A";
                        String validUntil  = cert.getValidUntil() != null ? cert.getValidUntil().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
                        String htmlBody = MailSender.buildCertificationEmailBody(
                            student.getFullName(),
                            cert.getCertificationType(),
                            issuedDate,
                            validUntil,
                            cert.getUniqueNumber() != null ? cert.getUniqueNumber() : String.valueOf(cert.getId()));
                        MailSender.sendEmailWithAttachment(
                            student.getEmail(),
                            "Votre Certification EduSmart — " + cert.getCertificationType(),
                            htmlBody,
                            pdf
                        );
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); // Log but don't block
                }
                closeStage();
            }
            else showGlobalError("Opération échouée.");
        } catch (IllegalArgumentException ex) { showGlobalError(ex.getMessage()); }
        catch (Exception ex) { showGlobalError("Erreur : " + rootCause(ex)); }
    }

    @FXML
    private void handleCancel(ActionEvent e) { closeStage(); }

    private boolean validateForm() {
        boolean valid = true;
        if (typeField.getText().trim().isEmpty()) {
            showFieldError(typeField, typeError, "Type de certification obligatoire."); valid = false;
        }
        if (studentComboBox.getValue() == null) {
            studentError.setText("Étudiant obligatoire."); studentError.setVisible(true); studentError.setManaged(true);
            studentComboBox.setStyle("-fx-border-color:#EF4444;"); valid = false;
        }
        return valid;
    }

    private Certification buildCertification() {
        Certification c = new Certification();
        
        // Preserve existing fields if editing
        if (certToEdit != null) {
            c.setId(certToEdit.getId());
            c.setVerificationCode(certToEdit.getVerificationCode());
            c.setPdfPath(certToEdit.getPdfPath());
            c.setUniqueNumber(certToEdit.getUniqueNumber());
            c.setHmacHash(certToEdit.getHmacHash());
            c.setRevokedAt(certToEdit.getRevokedAt());
            c.setRevocationReason(certToEdit.getRevocationReason());
            c.setIssuedAt(certToEdit.getIssuedAt());
        } else {
            c.setIssuedAt(LocalDateTime.now());
        }
        
        // Update fields from form
        c.setCertificationType(typeField.getText().trim());
        c.setStatus(statusComboBox.getValue());
        
        User student = studentComboBox.getValue();
        if (student != null) c.setStudentId(student.getId());
        
        LocalDate validUntil = validUntilPicker.getValue();
        c.setValidUntil(validUntil != null ? validUntil.atStartOfDay() : null);
        
        Bulletin b = bulletinComboBox.getValue();
        c.setBulletinId(b != null ? b.getId() : null);
        
        c.setMetier(metierComboBox.getValue());
        
        return c;
    }

    private void showFieldError(TextField f, Label l, String msg) {
        f.setStyle("-fx-border-color:#EF4444;-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;");
        l.setText(msg); l.setVisible(true); l.setManaged(true);
    }
    private void clearError(TextField f, Label l) { f.setStyle(""); l.setVisible(false); l.setManaged(false); }
    private void showGlobalError(String msg) { globalError.setText(msg); globalError.setVisible(true); globalError.setManaged(true); }
    private void closeStage() { ((Stage) titleLabel.getScene().getWindow()).close(); }
    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : "Erreur";
    }

    public static boolean openDialog(Stage owner, Certification cert) {
        try {
            FXMLLoader loader = new FXMLLoader(CertificationFormController.class.getResource("/fxml/teacher/certification-form.fxml"));
            javafx.scene.Parent root = loader.load();
            CertificationFormController ctrl = loader.getController();
            if (cert == null) ctrl.setAddMode(); else ctrl.setEditMode(cert);
            Stage stage = new Stage();
            stage.initOwner(owner); stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(cert == null ? "Ajouter une certification" : "Modifier la certification");
            stage.setResizable(false);
            Scene scene = new Scene(root);
            URL css = CertificationFormController.class.getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene); stage.showAndWait();
            return ctrl.isSaved();
        } catch (IOException ex) { ex.printStackTrace(); return false; }
    }
}
