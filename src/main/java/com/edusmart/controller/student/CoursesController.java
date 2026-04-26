package com.edusmart.controller.student;

import com.edusmart.model.Course;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * CoursesController - Student view for browsing available courses.
 *
 * Team member: Implement loadCourses() to fetch data from your service layer.
 */
public class CoursesController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private FlowPane coursesContainer;
    @FXML private Label coursesCountLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private ObservableList<Course> courseList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        loadCourses();
    }

    private void setupFilters() {
        if (categoryFilter != null) {
            categoryFilter.getItems().addAll("Tous", "Mathématiques", "Informatique", "Sciences", "Langues", "Arts");
            categoryFilter.setValue("Tous");
        }
        if (statusFilter != null) {
            statusFilter.getItems().addAll("Tous", "Actif", "Brouillon", "Archivé");
            statusFilter.setValue("Tous");
        }
    }

    /**
     * Loads all courses available to the student.
     */
    private void loadCourses() {

        // Demo data
        courseList.setAll(
            new Course(1, "Introduction à Java", "Prof. Martin", 8, 40, Course.Status.ACTIVE),
            new Course(2, "Mathématiques Avancées", "Prof. Dupont", 12, 60, Course.Status.ACTIVE),
            new Course(3, "Physique Quantique", "Prof. Bernard", 10, 50, Course.Status.ACTIVE),
            new Course(4, "Anglais Professionnel", "Prof. Smith", 6, 30, Course.Status.DRAFT)
        );

        updateCoursesCount();
    }

    private void updateCoursesCount() {
        if (coursesCountLabel != null) {
            coursesCountLabel.setText(courseList.size() + " cours disponibles");
        }
    }

    /**
     * Handles course search by title.
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        System.out.println("Recherche cours: " + query);
    }

    /**
     * Handles filter changes.
     */
    @FXML
    private void handleFilterChange(ActionEvent event) {
        System.out.println("Filtres modifiés.");
    }

    /**
     * Opens the detail view for a selected course.
     */
    public void handleViewCourse(Course course) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cours sélectionné");
        alert.setHeaderText(course.getTitle());
        alert.setContentText("Instructeur: " + course.getInstructor() +
                "\nModules: " + course.getModuleCount() +
                "\nHeures: " + course.getTotalHours());
        alert.showAndWait();
    }

    public List<Course> getCourseList() {
        return courseList;
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

    @FXML private void handleStudentAI(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_AI);
    }

    @FXML private void handleProfile(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE);
    }

    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
