package com.edusmart.controller.student;

import com.edusmart.model.Certification;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * CertificationController - Student view for displaying certifications and achievements.
 *
 * Team member: Implement loadCertifications() to fetch data from your service layer.
 */
public class CertificationController implements Initializable {

    @FXML private FlowPane certificationsContainer;
    @FXML private Label totalCertificationsLabel;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;

    private ObservableList<Certification> certificationList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        loadCertifications();
    }

    private void setupFilters() {
        if (statusFilter != null) {
            statusFilter.getItems().addAll("Tous", "Émis", "En attente", "Expiré");
            statusFilter.setValue("Tous");
        }
    }

    /**
     * Loads certifications for the current student.
     * TODO: Replace with actual service call.
     */
    private void loadCertifications() {
        // TODO: Load from service
        // certificationList.setAll(CertificationService.getCertificationsForStudent(
        //     SessionManager.getCurrentUser().getId()));

        // Demo data
        certificationList.setAll(
            new Certification(1, 1, 1, "Certificat Java Fondamentaux", LocalDate.now().minusDays(30), Certification.Status.ISSUED),
            new Certification(2, 1, 2, "Certificat Mathématiques Avancées", LocalDate.now().minusDays(60), Certification.Status.ISSUED),
            new Certification(3, 1, 3, "Certificat Physique - Niveau 1", null, Certification.Status.PENDING)
        );

        updateCount();
    }

    private void updateCount() {
        if (totalCertificationsLabel != null) {
            long issued = certificationList.stream()
                .filter(c -> c.getStatus() == Certification.Status.ISSUED)
                .count();
            totalCertificationsLabel.setText(issued + " certification(s) obtenue(s)");
        }
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        // TODO: Apply status filter and re-render
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        // TODO: Filter certifications by search query
    }

    /**
     * Downloads the selected certification as PDF.
     * TODO: Implement PDF download.
     */
    public void handleDownloadCertification(Certification certification) {
        // TODO: Generate and download certificate PDF
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Téléchargement");
        alert.setHeaderText("Certificat: " + certification.getTitle());
        alert.setContentText("Le téléchargement du certificat sera bientôt disponible.");
        alert.showAndWait();
    }

    public ObservableList<Certification> getCertificationList() {
        return certificationList;
    }
}
