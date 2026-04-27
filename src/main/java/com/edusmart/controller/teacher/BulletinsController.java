package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcBulletinDao;
import com.edusmart.model.Bulletin;
import com.edusmart.service.BulletinService;
import com.edusmart.service.impl.BulletinServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Teacher CRUD for {@code bulletin} table.
 */
public class BulletinsController implements Initializable {

    @FXML private TableView<Bulletin> bulletinsTable;
    @FXML private TableColumn<Bulletin, Integer> idColumn;
    @FXML private TableColumn<Bulletin, Integer> studentIdColumn;
    @FXML private TableColumn<Bulletin, String> academicYearColumn;
    @FXML private TableColumn<Bulletin, String> semesterColumn;
    @FXML private TableColumn<Bulletin, Double> averageColumn;
    @FXML private TableColumn<Bulletin, String> statusColumn;
    @FXML private TableColumn<Bulletin, String> mentionColumn;

    @FXML private TextField searchField;
    @FXML private TextField studentIdField;
    @FXML private TextField academicYearField;
    @FXML private TextField semesterField;
    @FXML private TextField averageField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField mentionField;
    @FXML private TextField classRankField;
    @FXML private TextField hmacHashField;
    @FXML private TextField pdfPathField;
    @FXML private TextField verificationCodeField;
    @FXML private TextField validatedAtField;
    @FXML private TextField publishedAtField;
    @FXML private TextField revokedAtField;
    @FXML private TextArea revocationReasonArea;
    @FXML private TextField validatedByIdField;
    @FXML private TextField publishedByIdField;
    @FXML private Label messageLabel;

    private ObservableList<Bulletin> bulletinList = FXCollections.observableArrayList();
    private Bulletin selectedBulletin;
    private final BulletinService bulletinService = new BulletinServiceImpl(new JdbcBulletinDao());

    private static final DateTimeFormatter DISPLAY_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupForm();
        loadBulletins();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (studentIdColumn != null) studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        if (academicYearColumn != null) academicYearColumn.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        if (semesterColumn != null) semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        if (averageColumn != null) averageColumn.setCellValueFactory(new PropertyValueFactory<>("average"));
        if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (mentionColumn != null) mentionColumn.setCellValueFactory(new PropertyValueFactory<>("mention"));
        if (bulletinsTable != null) {
            bulletinsTable.setItems(bulletinList);
            bulletinsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void setupForm() {
        if (statusComboBox != null) {
            statusComboBox.getItems().addAll("DRAFT", "PENDING", "VALIDATED", "PUBLISHED", "REVOKED");
            statusComboBox.setValue("DRAFT");
        }
    }

    private void loadBulletins() {
        try {
            bulletinList.setAll(bulletinService.getAllBulletins());
        } catch (RuntimeException ex) {
            showMessage("Erreur chargement bulletins: " + rootCauseMessage(ex), true);
        }
    }

    private void populateForm(Bulletin b) {
        selectedBulletin = b;
        if (b == null) return;
        if (studentIdField != null) studentIdField.setText(String.valueOf(b.getStudentId()));
        if (academicYearField != null) academicYearField.setText(b.getAcademicYear());
        if (semesterField != null) semesterField.setText(b.getSemester());
        if (averageField != null) {
            averageField.setText(b.getAverage() != null ? String.valueOf(b.getAverage()) : "");
        }
        if (statusComboBox != null && b.getStatus() != null) statusComboBox.setValue(b.getStatus());
        if (mentionField != null) mentionField.setText(b.getMention());
        if (classRankField != null) {
            classRankField.setText(b.getClassRank() != null ? String.valueOf(b.getClassRank()) : "");
        }
        if (hmacHashField != null) hmacHashField.setText(b.getHmacHash() != null ? b.getHmacHash() : "");
        if (pdfPathField != null) pdfPathField.setText(b.getPdfPath() != null ? b.getPdfPath() : "");
        if (verificationCodeField != null) {
            verificationCodeField.setText(b.getVerificationCode() != null ? b.getVerificationCode() : "");
        }
        if (validatedAtField != null) {
            validatedAtField.setText(b.getValidatedAt() != null ? b.getValidatedAt().format(DISPLAY_DT) : "");
        }
        if (publishedAtField != null) {
            publishedAtField.setText(b.getPublishedAt() != null ? b.getPublishedAt().format(DISPLAY_DT) : "");
        }
        if (revokedAtField != null) {
            revokedAtField.setText(b.getRevokedAt() != null ? b.getRevokedAt().format(DISPLAY_DT) : "");
        }
        if (revocationReasonArea != null) {
            revocationReasonArea.setText(b.getRevocationReason() != null ? b.getRevocationReason() : "");
        }
        if (validatedByIdField != null) {
            validatedByIdField.setText(b.getValidatedById() != null ? String.valueOf(b.getValidatedById()) : "");
        }
        if (publishedByIdField != null) {
            publishedByIdField.setText(b.getPublishedById() != null ? String.valueOf(b.getPublishedById()) : "");
        }
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        try {
            Bulletin bulletin = buildBulletinFromForm(false);
            if (bulletinService.createBulletin(bulletin)) {
                showMessage("Bulletin créé avec succès!", false);
                clearForm();
                selectedBulletin = null;
                loadBulletins();
            } else {
                showMessage("Création du bulletin échouée.", true);
            }
        } catch (RuntimeException ex) {
            showMessage("Erreur création bulletin: " + rootCauseMessage(ex), true);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedBulletin == null) {
            showMessage("Sélectionnez un bulletin à modifier.", true);
            return;
        }
        if (!validateForm()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer la modification du bulletin ID " + selectedBulletin.getId() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    Bulletin bulletin = buildBulletinFromForm(true);
                    bulletin.setId(selectedBulletin.getId());
                    if (bulletinService.updateBulletin(bulletin)) {
                        showMessage("Bulletin mis à jour!", false);
                        loadBulletins();
                    } else {
                        showMessage("Mise à jour du bulletin échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur mise à jour bulletin: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedBulletin == null) {
            showMessage("Sélectionnez un bulletin à supprimer.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le bulletin ID " + selectedBulletin.getId() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (bulletinService.deleteBulletin(selectedBulletin.getId())) {
                        bulletinList.remove(selectedBulletin);
                        clearForm();
                        selectedBulletin = null;
                        showMessage("Bulletin supprimé.", false);
                    } else {
                        showMessage("Suppression du bulletin échouée.", true);
                    }
                } catch (RuntimeException ex) {
                    showMessage("Erreur suppression bulletin: " + rootCauseMessage(ex), true);
                }
            }
        });
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        selectedBulletin = null;
        if (bulletinsTable != null) bulletinsTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) {
            loadBulletins();
            return;
        }
        List<Bulletin> filtered = bulletinService.getAllBulletins().stream()
                .filter(b ->
                        (b.getAcademicYear() != null && b.getAcademicYear().toLowerCase().contains(query))
                                || (b.getStatus() != null && b.getStatus().toLowerCase().contains(query))
                                || String.valueOf(b.getStudentId()).contains(query))
                .collect(Collectors.toList());
        bulletinList.setAll(filtered);
    }

    private boolean validateForm() {
        if (studentIdField == null || studentIdField.getText().trim().isEmpty()) {
            showMessage("L'ID élève (student_id) est obligatoire.", true);
            return false;
        }
        try {
            Integer.parseInt(studentIdField.getText().trim());
        } catch (NumberFormatException ex) {
            showMessage("L'ID élève doit être un entier valide.", true);
            return false;
        }
        if (academicYearField == null || academicYearField.getText().trim().isEmpty()) {
            showMessage("L'année académique est obligatoire.", true);
            return false;
        }
        if (semesterField == null || semesterField.getText().trim().isEmpty()) {
            showMessage("Le semestre est obligatoire.", true);
            return false;
        }
        if (statusComboBox == null || statusComboBox.getValue() == null || statusComboBox.getValue().isBlank()) {
            showMessage("Le statut est obligatoire.", true);
            return false;
        }
        if (mentionField == null || mentionField.getText().trim().isEmpty()) {
            showMessage("La mention est obligatoire.", true);
            return false;
        }
        if (averageField != null && !averageField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(averageField.getText().trim());
            } catch (NumberFormatException ex) {
                showMessage("La moyenne doit être un nombre valide.", true);
                return false;
            }
        }
        if (classRankField != null && !classRankField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(classRankField.getText().trim());
            } catch (NumberFormatException ex) {
                showMessage("Le rang de classe doit être un entier valide.", true);
                return false;
            }
        }
        for (TextField tf : new TextField[]{validatedByIdField, publishedByIdField}) {
            if (tf != null && !tf.getText().trim().isEmpty()) {
                try {
                    Integer.parseInt(tf.getText().trim());
                } catch (NumberFormatException ex) {
                    showMessage("Les IDs validateur / publieur doivent être des entiers valides.", true);
                    return false;
                }
            }
        }
        for (TextField tf : new TextField[]{validatedAtField, publishedAtField, revokedAtField}) {
            if (tf != null && !tf.getText().trim().isEmpty()) {
                if (parseDateTime(tf.getText().trim()) == null) {
                    showMessage("Format date/heure invalide (utilisez yyyy-MM-dd HH:mm:ss).", true);
                    return false;
                }
            }
        }
        return true;
    }

    private Bulletin buildBulletinFromForm(boolean forUpdate) {
        Bulletin b = new Bulletin();
        b.setStudentId(Integer.parseInt(studentIdField.getText().trim()));
        b.setAcademicYear(academicYearField.getText().trim());
        b.setSemester(semesterField.getText().trim());
        String avgText = averageField != null ? averageField.getText().trim() : "";
        b.setAverage(avgText.isEmpty() ? null : Double.parseDouble(avgText));
        b.setStatus(statusComboBox.getValue());
        b.setMention(mentionField.getText().trim());
        String rankText = classRankField != null ? classRankField.getText().trim() : "";
        b.setClassRank(rankText.isEmpty() ? null : Integer.parseInt(rankText));
        b.setHmacHash(emptyToNull(hmacHashField));
        b.setPdfPath(emptyToNull(pdfPathField));
        b.setVerificationCode(emptyToNull(verificationCodeField));
        b.setValidatedAt(parseDateTimeField(validatedAtField));
        b.setPublishedAt(parseDateTimeField(publishedAtField));
        b.setRevokedAt(parseDateTimeField(revokedAtField));
        b.setRevocationReason(emptyToNull(revocationReasonArea));
        b.setValidatedById(parseIntegerField(validatedByIdField));
        b.setPublishedById(parseIntegerField(publishedByIdField));

        LocalDateTime now = LocalDateTime.now();
        if (forUpdate) {
            b.setCreatedAt(selectedBulletin.getCreatedAt() != null ? selectedBulletin.getCreatedAt() : now);
            b.setUpdatedAt(now);
        } else {
            b.setCreatedAt(now);
            b.setUpdatedAt(null);
        }
        return b;
    }

    private static String emptyToNull(TextField tf) {
        if (tf == null || tf.getText() == null) return null;
        String t = tf.getText().trim();
        return t.isEmpty() ? null : t;
    }

    private static String emptyToNull(TextArea ta) {
        if (ta == null || ta.getText() == null) return null;
        String t = ta.getText().trim();
        return t.isEmpty() ? null : t;
    }

    private static Integer parseIntegerField(TextField tf) {
        if (tf == null || tf.getText() == null || tf.getText().trim().isEmpty()) return null;
        return Integer.parseInt(tf.getText().trim());
    }

    private LocalDateTime parseDateTimeField(TextField tf) {
        if (tf == null || tf.getText() == null) return null;
        return parseDateTime(tf.getText().trim());
    }

    /**
     * Accepts {@code yyyy-MM-dd HH:mm:ss} or ISO-8601; empty → null.
     */
    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        try {
            return LocalDateTime.parse(t, DISPLAY_DT);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(t);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    private void clearForm() {
        if (studentIdField != null) studentIdField.clear();
        if (academicYearField != null) academicYearField.clear();
        if (semesterField != null) semesterField.clear();
        if (averageField != null) averageField.clear();
        if (statusComboBox != null) statusComboBox.setValue("DRAFT");
        if (mentionField != null) mentionField.clear();
        if (classRankField != null) classRankField.clear();
        if (hmacHashField != null) hmacHashField.clear();
        if (pdfPathField != null) pdfPathField.clear();
        if (verificationCodeField != null) verificationCodeField.clear();
        if (validatedAtField != null) validatedAtField.clear();
        if (publishedAtField != null) publishedAtField.clear();
        if (revokedAtField != null) revokedAtField.clear();
        if (revocationReasonArea != null) revocationReasonArea.clear();
        if (validatedByIdField != null) validatedByIdField.clear();
        if (publishedByIdField != null) publishedByIdField.clear();
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : throwable.getMessage();
    }

    public ObservableList<Bulletin> getBulletinList() {
        return bulletinList;
    }

    // ── Navigation ──────────────────────────────────────────────────────

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
