package com.edusmart.controller.student;

import com.edusmart.dao.jdbc.JdbcBulletinDao;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.Bulletin;
import com.edusmart.model.Grade;
import com.edusmart.model.User;
import com.edusmart.service.BulletinService;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.BulletinServiceImpl;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.util.PdfGenerator;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * BulletinController - Student view for displaying report cards / grades.
 *
 * Team member: Implement loadGrades() to fetch data from your service layer.
 */
public class BulletinController implements Initializable {

    @FXML private TableView<Grade> gradesTable;
    @FXML private TableColumn<Grade, String> subjectColumn;
    @FXML private TableColumn<Grade, Double> scoreColumn;
    @FXML private TableColumn<Grade, Double> maxScoreColumn;
    @FXML private TableColumn<Grade, String> semesterColumn;
    @FXML private TableColumn<Grade, String> commentColumn;

    @FXML private ComboBox<String> semesterFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private Label averageLabel;
    @FXML private Label rankLabel;
    @FXML private Label metierLabel;
    @FXML private Button printButton;
    @FXML private Button downloadButton;

    private final BulletinService bulletinService = new BulletinServiceImpl(new JdbcBulletinDao());
    private final UserService userService = new UserServiceImpl(new JdbcUserDao());
    private Bulletin currentBulletin;

    private ObservableList<Grade> gradeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupFilters();
        loadGrades();
    }

    private void setupTable() {
        if (scoreColumn != null) scoreColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        if (semesterColumn != null) semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        if (gradesTable != null) gradesTable.setItems(gradeList);
    }

    private void setupFilters() {
        if (semesterFilter != null) {
            semesterFilter.getItems().addAll("Semestre 1", "Semestre 2", "Annuel");
            semesterFilter.setValue("Semestre 1");
        }
        if (yearFilter != null) {
            yearFilter.getItems().addAll("2023-2024", "2024-2025");
            yearFilter.setValue("2024-2025");
        }
    }

    private int resolveStudentId() {
        User u = SceneManager.getInstance().getCurrentUser();
        return u != null ? u.getId() : 1;
    }

    private void loadGrades() {
        int sId = resolveStudentId();
        // Load latest published bulletin for student
        List<Bulletin> bulletins = bulletinService.getAllBulletins().stream()
                .filter(b -> b.getStudentId() == sId && "PUBLISHED".equalsIgnoreCase(b.getStatus()))
                .sorted((b1, b2) -> b2.getAcademicYear().compareTo(b1.getAcademicYear()))
                .toList();

        if (!bulletins.isEmpty()) {
            currentBulletin = bulletins.get(0);
            updateStatistics();
        }
        // Always load raw grades even if no publication
        gradeList.clear();
        gradeList.addAll(new com.edusmart.dao.jdbc.JdbcGradeDao().findByStudentId(sId));
    }

    private void updateStatistics() {
        if (currentBulletin != null) {
            averageLabel.setText(String.format("Moyenne: %.2f/20", currentBulletin.getAverage()));
            rankLabel.setText("Rang: " + (currentBulletin.getClassRank() != null ? currentBulletin.getClassRank() : "N/A"));
            if (metierLabel != null) metierLabel.setText("Métier: " + (currentBulletin.getMetier() != null ? currentBulletin.getMetier() : "--"));
        } else if (averageLabel != null) {
            double avg = gradeList.stream().mapToDouble(Grade::getNote).average().orElse(0);
            averageLabel.setText(String.format("Moyenne (Provisoire): %.2f/20", avg));
            rankLabel.setText("Rang: Non publié");
        }
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        System.out.println("Filtres appliqués au bulletin.");
    }

    /**
     * Prints the bulletin.
     */
    @FXML
    private void handlePrint(ActionEvent event) {
        System.out.println("Impression du bulletin en cours...");
    }

    /**
     * Downloads the bulletin as PDF.
     */
    @FXML
    private void handleDownload(ActionEvent event) {
        if (currentBulletin == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Aucun bulletin disponible pour téléchargement.");
            alert.showAndWait();
            return;
        }
        try {
            File pdfFile;
            if (currentBulletin.getPdfPath() != null && new File(currentBulletin.getPdfPath()).exists()) {
                pdfFile = new File(currentBulletin.getPdfPath());
            } else {
                // Generate on the fly if missing
                Optional<User> student = userService.getAllUsers().stream()
                        .filter(u -> u.getId() == resolveStudentId())
                        .findFirst();
                if (student.isPresent()) {
                    pdfFile = PdfGenerator.generateBulletinPdf(currentBulletin, student.get());
                } else {
                    throw new Exception("Utilisateur introuvable");
                }
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur PDF: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    public ObservableList<Grade> getGradeList() {
        return gradeList;
    }

    // ── Navigation handlers ──────────────────────────────────────────────

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

    @FXML private void handleProfile(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE);
    }

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
