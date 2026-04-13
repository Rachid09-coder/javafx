package com.edusmart.controller.teacher;

import com.edusmart.model.Course;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ManageCoursesController - Teacher interface for managing courses.
 *
 * Team member: Implement CRUD operations via service layer.
 */
public class ManageCoursesController implements Initializable {

    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, Integer> idColumn;
    @FXML private TableColumn<Course, String> titleColumn;
    @FXML private TableColumn<Course, String> instructorColumn;
    @FXML private TableColumn<Course, Integer> modulesColumn;
    @FXML private TableColumn<Course, Integer> hoursColumn;
    @FXML private TableColumn<Course, Course.Status> statusColumn;

    @FXML private TextField titleField;
    @FXML private TextField instructorField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    private ObservableList<Course> courseList = FXCollections.observableArrayList();
    private Course selectedCourse;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupForm();
        loadCourses();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (instructorColumn != null) instructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        if (modulesColumn != null) modulesColumn.setCellValueFactory(new PropertyValueFactory<>("moduleCount"));
        if (hoursColumn != null) hoursColumn.setCellValueFactory(new PropertyValueFactory<>("totalHours"));
        if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (coursesTable != null) {
            coursesTable.setItems(courseList);
            coursesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> populateForm(newVal));
        }
    }

    private void setupForm() {
        if (statusComboBox != null) {
            statusComboBox.getItems().addAll("ACTIVE", "DRAFT", "INACTIVE", "ARCHIVED");
            statusComboBox.setValue("ACTIVE");
        }
    }

    /**
     * Loads all courses managed by the teacher.
     * TODO: Replace with service call.
     */
    private void loadCourses() {
        // TODO: courseList.setAll(CourseService.getCoursesByTeacher(teacherId));

        courseList.setAll(
            new Course(1, "Introduction à Java", "Prof. Martin", 8, 40, Course.Status.ACTIVE),
            new Course(2, "Mathématiques Avancées", "Prof. Dupont", 12, 60, Course.Status.ACTIVE),
            new Course(3, "Physique Quantique", "Prof. Bernard", 10, 50, Course.Status.DRAFT)
        );
    }

    private void populateForm(Course course) {
        selectedCourse = course;
        if (course == null) return;
        if (titleField != null) titleField.setText(course.getTitle());
        if (instructorField != null) instructorField.setText(course.getInstructor());
        if (descriptionArea != null) descriptionArea.setText(course.getDescription());
        if (statusComboBox != null && course.getStatus() != null)
            statusComboBox.setValue(course.getStatus().name());
    }

    /**
     * Creates a new course.
     * TODO: Implement via CourseService.
     */
    @FXML
    private void handleCreate(ActionEvent event) {
        if (!validateForm()) return;
        // TODO: CourseService.createCourse(buildCourseFromForm());
        showMessage("Cours créé avec succès!", false);
        clearForm();
        loadCourses();
    }

    /**
     * Updates the selected course.
     * TODO: Implement via CourseService.
     */
    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedCourse == null) {
            showMessage("Sélectionnez un cours à modifier.", true);
            return;
        }
        if (!validateForm()) return;
        // TODO: CourseService.updateCourse(selectedCourse.getId(), buildCourseFromForm());
        showMessage("Cours mis à jour avec succès!", false);
        loadCourses();
    }

    /**
     * Deletes the selected course.
     * TODO: Implement via CourseService.
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedCourse == null) {
            showMessage("Sélectionnez un cours à supprimer.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer le cours \"" + selectedCourse.getTitle() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // TODO: CourseService.deleteCourse(selectedCourse.getId());
                courseList.remove(selectedCourse);
                clearForm();
                showMessage("Cours supprimé.", false);
            }
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        // TODO: Filter courseList by search query
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        selectedCourse = null;
    }

    private boolean validateForm() {
        if (titleField != null && titleField.getText().trim().isEmpty()) {
            showMessage("Le titre du cours est obligatoire.", true);
            return false;
        }
        return true;
    }

    private void clearForm() {
        if (titleField != null) titleField.clear();
        if (instructorField != null) instructorField.clear();
        if (descriptionArea != null) descriptionArea.clear();
    }

    private void showMessage(String message, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    public ObservableList<Course> getCourseList() {
        return courseList;
    }
}
