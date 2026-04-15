package com.edusmart.controller.student;

import com.edusmart.dao.jdbc.JdbcCertificationDao;
import com.edusmart.model.Certification;
import com.edusmart.service.CertificationService;
import com.edusmart.service.impl.CertificationServiceImpl;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Student view — lists {@link Certification} rows for the current student (see {@link #resolveStudentId()}).
 */
public class CertificationController implements Initializable {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    @FXML private FlowPane certificationsContainer;
    @FXML private Label totalCertificationsLabel;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;

    private final CertificationService certificationService = new CertificationServiceImpl(new JdbcCertificationDao());
    private ObservableList<Certification> certificationList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        loadCertifications();
    }

    private void setupFilters() {
        if (statusFilter != null) {
            statusFilter.getItems().setAll("Tous", "Émis", "En attente", "Expiré", "Révoqué");
            statusFilter.setValue("Tous");
        }
    }

    private int resolveStudentId() {
        String p = System.getProperty("edusmart.studentId");
        if (p != null) {
            try {
                return Integer.parseInt(p.trim());
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }
        return 1;
    }

    private void loadCertifications() {
        try {
            certificationList.setAll(certificationService.getCertificationsForStudent(resolveStudentId()));
        } catch (RuntimeException ex) {
            if (certificationsContainer != null) {
                certificationsContainer.getChildren().setAll(new Label("Erreur de chargement: " + rootCauseMessage(ex)));
            }
            return;
        }
        refreshCards();
        updateCount();
    }

    private List<Certification> filteredCertifications() {
        String q = searchField != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";
        String filter = statusFilter != null && statusFilter.getValue() != null ? statusFilter.getValue() : "Tous";

        return certificationList.stream()
                .filter(c -> matchesStatusFilter(c, filter))
                .filter(c -> q.isEmpty() || matchesSearch(c, q))
                .collect(Collectors.toList());
    }

    private static boolean matchesStatusFilter(Certification c, String filterLabel) {
        String s = c.getStatus() != null ? c.getStatus().toUpperCase(Locale.ROOT) : "";
        return switch (filterLabel) {
            case "Émis" -> Certification.STATUS_ISSUED.equals(s);
            case "En attente" -> Certification.STATUS_PENDING.equals(s);
            case "Expiré" -> Certification.STATUS_EXPIRED.equals(s);
            case "Révoqué" -> Certification.STATUS_REVOKED.equals(s);
            default -> true;
        };
    }

    private static boolean matchesSearch(Certification c, String q) {
        String type = c.getCertificationType() != null ? c.getCertificationType().toLowerCase(Locale.ROOT) : "";
        String code = c.getVerificationCode() != null ? c.getVerificationCode().toLowerCase(Locale.ROOT) : "";
        return type.contains(q) || code.contains(q);
    }

    private void refreshCards() {
        if (certificationsContainer == null) {
            return;
        }
        certificationsContainer.getChildren().clear();
        for (Certification c : filteredCertifications()) {
            certificationsContainer.getChildren().add(buildCard(c));
        }
    }

    private VBox buildCard(Certification c) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.getStyleClass().add("card");

        Label icon = new Label(statusIcon(c.getStatus()));
        icon.setStyle("-fx-font-size: 36px;");

        Label title = new Label(c.getCertificationType() != null ? c.getCertificationType() : "—");
        title.getStyleClass().add("course-title");

        String when = c.getIssuedAt() != null
                ? "Émise le: " + c.getIssuedAt().format(DATE_FMT)
                : "Date non renseignée";
        Label meta = new Label(when);
        meta.getStyleClass().add("course-meta");

        Label code = new Label("Code: " + (c.getVerificationCode() != null ? c.getVerificationCode() : "—"));
        code.setWrapText(true);
        code.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

        HBox badges = new HBox(8);
        Label st = new Label(statusLabelFr(c.getStatus()));
        st.getStyleClass().addAll("badge", badgeClassForStatus(c.getStatus()));

        badges.getChildren().add(st);

        Button download = new Button("⬇ Télécharger / PDF");
        download.setMaxWidth(Double.MAX_VALUE);
        download.getStyleClass().add("btn-primary");
        boolean hasPdf = c.getPdfPath() != null && !c.getPdfPath().isBlank();
        download.setDisable(!hasPdf);
        download.setOnAction(e -> handleDownloadCertification(c));

        if (!hasPdf) {
            download.setText("PDF non disponible");
            download.getStyleClass().setAll("btn-secondary");
        }

        card.getChildren().addAll(icon, title, meta, code, badges, download);
        return card;
    }

    private static String statusIcon(String status) {
        if (status == null) {
            return "📄";
        }
        return switch (status.toUpperCase(Locale.ROOT)) {
            case Certification.STATUS_ISSUED -> "🏆";
            case Certification.STATUS_PENDING -> "⏳";
            case Certification.STATUS_EXPIRED -> "⌛";
            case Certification.STATUS_REVOKED -> "⛔";
            default -> "📄";
        };
    }

    private static String statusLabelFr(String status) {
        if (status == null) {
            return "—";
        }
        return switch (status.toUpperCase(Locale.ROOT)) {
            case Certification.STATUS_ISSUED -> "Émis";
            case Certification.STATUS_PENDING -> "En attente";
            case Certification.STATUS_EXPIRED -> "Expiré";
            case Certification.STATUS_REVOKED -> "Révoqué";
            default -> status;
        };
    }

    private static String badgeClassForStatus(String status) {
        if (status == null) {
            return "badge-warning";
        }
        return switch (status.toUpperCase(Locale.ROOT)) {
            case Certification.STATUS_ISSUED -> "badge-success";
            case Certification.STATUS_PENDING -> "badge-warning";
            case Certification.STATUS_EXPIRED -> "badge-warning";
            case Certification.STATUS_REVOKED -> "badge-danger";
            default -> "badge-warning";
        };
    }

    private void updateCount() {
        if (totalCertificationsLabel != null) {
            long issued = certificationList.stream().filter(Certification::isIssued).count();
            totalCertificationsLabel.setText(issued + " certification(s) obtenue(s) sur " + certificationList.size());
        }
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        refreshCards();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        refreshCards();
    }

    public void handleDownloadCertification(Certification certification) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Certification");
        alert.setHeaderText(certification.getCertificationType());
        if (certification.getPdfPath() != null && !certification.getPdfPath().isBlank()) {
            alert.setContentText("Fichier PDF:\n" + certification.getPdfPath());
        } else {
            alert.setContentText("Aucun fichier PDF n'est associé à cette certification.");
        }
        alert.showAndWait();
    }

    public ObservableList<Certification> getCertificationList() {
        return certificationList;
    }

    private static String rootCauseMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage() != null ? t.getMessage() : t.toString();
    }

    @FXML private void handleCourses(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES);
    }

    @FXML private void handleExams(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS);
    }

    @FXML private void handleBulletin(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_BULLETIN);
    }

    @FXML private void handleCertification(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_CERTIFICATION);
    }

    @FXML private void handleShop(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP);
    }

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
