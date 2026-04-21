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
import javafx.stage.Stage;

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
    @FXML private TableColumn<Bulletin, Integer> idColumn;
    @FXML private TableColumn<Bulletin, String> studentColumn;
    @FXML private TableColumn<Bulletin, String> yearColumn;
    @FXML private TableColumn<Bulletin, String> semesterColumn;
    @FXML private TableColumn<Bulletin, Double> averageColumn;
    @FXML private TableColumn<Bulletin, String> statusColumn;
    @FXML private TableColumn<Bulletin, String> mentionColumn;
    @FXML private TableColumn<Bulletin, Integer> rankColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> semesterFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label messageLabel;

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
        if (semesterFilter != null) {
            semesterFilter.setItems(FXCollections.observableArrayList("Tous", "1", "2", "3", "4"));
            semesterFilter.setValue("Tous");
            semesterFilter.setOnAction(e -> applyFilters());
        }
        if (statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList("Tous", "DRAFT", "PUBLISHED", "ARCHIVED"));
            statusFilter.setValue("Tous");
            statusFilter.setOnAction(e -> applyFilters());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (studentColumn != null) {
            studentColumn.setCellValueFactory(cd -> {
                int sId = cd.getValue().getStudentId();
                return new SimpleStringProperty(studentNames.getOrDefault(sId, "Étudiant #" + sId));
            });
        }
        if (yearColumn != null) yearColumn.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        if (semesterColumn != null) semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        if (averageColumn != null) averageColumn.setCellValueFactory(new PropertyValueFactory<>("average"));
        if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (mentionColumn != null) mentionColumn.setCellValueFactory(new PropertyValueFactory<>("mention"));
        if (rankColumn != null) rankColumn.setCellValueFactory(new PropertyValueFactory<>("classRank"));

        filteredBulletins = new FilteredList<>(bulletinList, p -> true);
        SortedList<Bulletin> sortedBulletins = new SortedList<>(filteredBulletins);
        sortedBulletins.comparatorProperty().bind(bulletinsTable.comparatorProperty());
        bulletinsTable.setItems(sortedBulletins);
    }

    private void loadData() {
        try {
            studentNames = userService.getAllUsers().stream()
                .collect(Collectors.toMap(User::getId, User::getFullName, (a, b) -> a));
            bulletinList.setAll(bulletinService.getAllBulletins());
            // Table updates automatically through FilteredList/SortedList
        } catch (Exception ex) {
            showMessage("Erreur chargement: " + rootCause(ex), true);
        }
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        Stage owner = (Stage) bulletinsTable.getScene().getWindow();
        if (BulletinFormController.openDialog(owner, null)) {
            loadData();
            showMessage("Bulletin ajouté.", false);
        }
    }

    @FXML
    private void handleEdit(ActionEvent e) {
        Bulletin sel = bulletinsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez un bulletin.", true); return; }
        Stage owner = (Stage) bulletinsTable.getScene().getWindow();
        if (BulletinFormController.openDialog(owner, sel)) {
            loadData();
            showMessage("Bulletin modifié.", false);
        }
    }

    @FXML
    private void handleDelete(ActionEvent e) {
        Bulletin sel = bulletinsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez un bulletin.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le bulletin #" + sel.getId() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    if (bulletinService.deleteBulletin(sel.getId())) {
                        bulletinList.remove(sel);
                        showMessage("Bulletin supprimé.", false);
                    } else showMessage("Échec de la suppression.", true);
                } catch (Exception ex) { showMessage("Erreur : " + rootCause(ex), true); }
            }
        });
    }

    @FXML
    private void handleDownloadPdf(ActionEvent e) {
        Bulletin sel = bulletinsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Sélectionnez un bulletin.", true); return; }
        try {
            Optional<User> studentOpt = userService.getAllUsers().stream()
                .filter(u -> u.getId() == sel.getStudentId())
                .findFirst();
            if (studentOpt.isPresent()) {
                File pdf = PdfGenerator.generateBulletinPdf(sel, studentOpt.get());
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdf);
                }
                showMessage("PDF ouvert : " + pdf.getName(), false);
            }
        } catch (Exception ex) { showMessage("Erreur PDF: " + rootCause(ex), true); }
    }

    private void applyFilters() {
        if (filteredBulletins == null) return;
        String query = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String sem = semesterFilter != null ? semesterFilter.getValue() : "Tous";
        String status = statusFilter != null ? statusFilter.getValue() : "Tous";

        filteredBulletins.setPredicate(b -> {
            boolean matchesSearch = query.isEmpty() ||
                studentNames.getOrDefault(b.getStudentId(), "").toLowerCase().contains(query) ||
                (b.getAcademicYear() != null && b.getAcademicYear().toLowerCase().contains(query));

            boolean matchesSem = sem == null || sem.equals("Tous") ||
                (b.getSemester() != null && b.getSemester().equals(sem));

            boolean matchesStatus = status == null || status.equals("Tous") ||
                (b.getStatus() != null && b.getStatus().equalsIgnoreCase(status));

            return matchesSearch && matchesSem && matchesStatus;
        });
    }

    private void showMessage(String msg, boolean isErr) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isErr ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }
    private String rootCause(Throwable t) { while(t.getCause()!=null) t=t.getCause(); return t.getMessage(); }

    @FXML private void handleSearch(ActionEvent e) { applyFilters(); }
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
    @FXML private void handleProfile(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
