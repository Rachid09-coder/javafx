package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcBulletinDao;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.Bulletin;
import com.edusmart.model.User;
import com.edusmart.service.BulletinService;
import com.edusmart.service.UserService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur du formulaire Bulletin (dialog séparé).
 * Calcule automatiquement la mention selon la moyenne.
 */
public class BulletinFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private ComboBox<User> studentComboBox;
    @FXML private Label studentError;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField academicYearField;
    @FXML private Label yearError;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private Label semesterError;
    @FXML private TextField averageField;
    @FXML private Label averageError;
    @FXML private TextField mentionField;
    @FXML private TextField classRankField;
    @FXML private Label rankError;
    @FXML private Label globalError;

    private final BulletinService bulletinService = new BulletinServiceImpl(new JdbcBulletinDao());
    private final UserService userService = new UserServiceImpl(new JdbcUserDao());

    private Bulletin bulletinToEdit;
    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusComboBox.setItems(FXCollections.observableArrayList("DRAFT", "PUBLISHED", "VALIDATED", "REVOKED"));
        statusComboBox.setValue("DRAFT");
        semesterComboBox.setItems(FXCollections.observableArrayList("S1", "S2", "S3", "S4"));
        semesterComboBox.setValue("S1");
        setupStudentCombo();
        // Auto-compute mention on average change
        averageField.textProperty().addListener((o, ov, nv) -> {
            clearError(averageField, averageError);
            updateMentionPreview(nv);
        });
        academicYearField.textProperty().addListener((o, ov, nv) -> clearError(academicYearField, yearError));
        
        // Listeners for auto-calculation
        studentComboBox.valueProperty().addListener((o, ov, nv) -> autoCalculateAverageAndRank());
        semesterComboBox.valueProperty().addListener((o, ov, nv) -> autoCalculateAverageAndRank());

        // Current Year Default
        academicYearField.setText("2024-2025");
    }

    private void autoCalculateAverageAndRank() {
        User student = studentComboBox.getValue();
        String semester = semesterComboBox.getValue();
        
        if (student != null && semester != null) {
            Double avg = bulletinService.calculateStudentAverage(student.getId(), semester);
            if (avg != null) {
                averageField.setText(String.format("%.2f", avg).replace(",", "."));
                Integer rank = bulletinService.calculateStudentRank(student.getId(), semester, avg);
                if (rank != null) {
                    classRankField.setText(String.valueOf(rank));
                }
            } else {
                averageField.setText("");
                classRankField.setText("");
            }
        }
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

    private void updateMentionPreview(String avgText) {
        try {
            double avg = Double.parseDouble(avgText.trim());
            mentionField.setText(computeMention(avg));
        } catch (NumberFormatException ignored) {
            mentionField.setText("");
        }
    }

    private String computeMention(double avg) {
        if (avg < 10) return "Ajourné";
        if (avg < 12) return "Passable";
        if (avg < 14) return "Assez Bien";
        if (avg < 16) return "Bien";
        if (avg < 18) return "Très Bien";
        return "Excellent";
    }

    public void setAddMode() { titleLabel.setText("Nouveau Bulletin"); bulletinToEdit = null; }

    public void setEditMode(Bulletin bulletin) {
        titleLabel.setText("Modifier le Bulletin");
        bulletinToEdit = bulletin;
        if (bulletin.getStatus() != null) statusComboBox.setValue(bulletin.getStatus());
        academicYearField.setText(bulletin.getAcademicYear() != null ? bulletin.getAcademicYear() : "");
        if (bulletin.getSemester() != null) semesterComboBox.setValue(bulletin.getSemester());
        if (bulletin.getAverage() != null) {
            averageField.setText(String.valueOf(bulletin.getAverage()));
            mentionField.setText(computeMention(bulletin.getAverage()));
        }
        if (bulletin.getMention() != null) mentionField.setText(bulletin.getMention());
        classRankField.setText(bulletin.getClassRank() != null ? String.valueOf(bulletin.getClassRank()) : "");
        // Pre-select student
        if (bulletin.getStudentId() > 0) {
            studentComboBox.getItems().stream()
                .filter(u -> u.getId() == bulletin.getStudentId())
                .findFirst().ifPresent(studentComboBox::setValue);
        }
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave(ActionEvent e) {
        if (!validateForm()) return;
        try {
            Bulletin b = buildBulletin();
            boolean ok;
            if (bulletinToEdit == null) ok = bulletinService.createBulletin(b);
            else { b.setId(bulletinToEdit.getId()); ok = bulletinService.updateBulletin(b); }
            if (ok) {
                saved = true;
                // NEW: Generate PDF and send Email
                try {
                    User student = studentComboBox.getValue();
                    if (student != null && student.getEmail() != null) {
                        File pdf = PdfGenerator.generateBulletinPdf(b, student);
                        b.setPdfPath(pdf.getAbsolutePath());
                        bulletinService.updateBulletin(b); // Update with PDF path

                        String htmlBody = MailSender.buildBulletinEmailBody(
                            student.getFullName(),
                            b.getAcademicYear(),
                            b.getSemester(),
                            b.getAverage() != null ? String.format("%.2f / 20", b.getAverage()) : "N/A",
                            b.getMention() != null ? b.getMention() : "N/A",
                            b.getClassRank() != null ? b.getClassRank() + "ème" : "N/A");
                        MailSender.sendEmailWithAttachment(
                            student.getEmail(),
                            "Votre Bulletin de Notes EduSmart — " + b.getAcademicYear(),
                            htmlBody,
                            pdf
                        );
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
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
        if (studentComboBox.getValue() == null) {
            studentError.setText("Étudiant obligatoire."); studentError.setVisible(true); studentError.setManaged(true);
            studentComboBox.setStyle("-fx-border-color:#EF4444;"); valid = false;
        }
        if (academicYearField.getText().trim().isEmpty()) {
            showFieldError(academicYearField, yearError, "Année académique obligatoire."); valid = false;
        }
        if (semesterComboBox.getValue() == null) {
            semesterError.setText("Semestre obligatoire."); semesterError.setVisible(true); semesterError.setManaged(true);
            valid = false;
        }
        String avgText = averageField.getText().trim();
        if (!avgText.isEmpty()) {
            try {
                double avg = Double.parseDouble(avgText);
                if (avg < 0 || avg > 20) { showFieldError(averageField, averageError, "Moyenne entre 0 et 20."); valid = false; }
            } catch (NumberFormatException ex) {
                showFieldError(averageField, averageError, "Nombre invalide."); valid = false;
            }
        }
        String rankText = classRankField.getText().trim();
        if (!rankText.isEmpty()) {
            try { Integer.parseInt(rankText); }
            catch (NumberFormatException ex) { showFieldError(classRankField, rankError, "Nombre entier invalide."); valid = false; }
        }
        return valid;
    }

    private Bulletin buildBulletin() {
        Bulletin b = new Bulletin();
        
        // Preserve existing fields if editing
        if (bulletinToEdit != null) {
            b.setId(bulletinToEdit.getId());
            b.setHmacHash(bulletinToEdit.getHmacHash());
            b.setPdfPath(bulletinToEdit.getPdfPath());
            b.setVerificationCode(bulletinToEdit.getVerificationCode());
            b.setValidatedAt(bulletinToEdit.getValidatedAt());
            b.setPublishedAt(bulletinToEdit.getPublishedAt());
            b.setRevokedAt(bulletinToEdit.getRevokedAt());
            b.setRevocationReason(bulletinToEdit.getRevocationReason());
            b.setCreatedAt(bulletinToEdit.getCreatedAt());
            b.setValidatedById(bulletinToEdit.getValidatedById());
            b.setPublishedById(bulletinToEdit.getPublishedById());
        } else {
            b.setCreatedAt(LocalDateTime.now());
        }
        b.setUpdatedAt(LocalDateTime.now());
        
        // Update fields from form
        User student = studentComboBox.getValue();
        if (student != null) b.setStudentId(student.getId());
        
        b.setStatus(statusComboBox.getValue());
        b.setAcademicYear(academicYearField.getText().trim());
        b.setSemester(semesterComboBox.getValue());
        
        String avgText = averageField.getText().trim();
        b.setAverage(avgText.isEmpty() ? null : Double.parseDouble(avgText));
        b.setMention(mentionField.getText().trim().isEmpty() ? null : mentionField.getText().trim());
        
        String rankText = classRankField.getText().trim();
        b.setClassRank(rankText.isEmpty() ? null : Integer.parseInt(rankText));
        
        return b;
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

    public static boolean openDialog(Stage owner, Bulletin bulletin) {
        try {
            FXMLLoader loader = new FXMLLoader(BulletinFormController.class.getResource("/fxml/teacher/bulletin-form.fxml"));
            javafx.scene.Parent root = loader.load();
            BulletinFormController ctrl = loader.getController();
            if (bulletin == null) ctrl.setAddMode(); else ctrl.setEditMode(bulletin);
            Stage stage = new Stage();
            stage.initOwner(owner); stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(bulletin == null ? "Ajouter un bulletin" : "Modifier le bulletin");
            stage.setResizable(false);
            Scene scene = new Scene(root);
            URL css = BulletinFormController.class.getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene); stage.showAndWait();
            return ctrl.isSaved();
        } catch (IOException ex) { ex.printStackTrace(); return false; }
    }
}
