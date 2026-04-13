package com.edusmart.controller.teacher;

import com.edusmart.model.User;
import javafx.beans.property.SimpleStringProperty;
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
 * StudentManagementController - Teacher interface for managing enrolled students.
 *
 * Team member: Implement CRUD operations and course enrollment via service layer.
 */
public class StudentManagementController implements Initializable {

    @FXML private TableView<User> studentsTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> fullNameColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> courseFilter;
    @FXML private Label studentsCountLabel;
    @FXML private Label messageLabel;

    // Form fields for adding a student
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> enrollCourseComboBox;

    private ObservableList<User> studentList = FXCollections.observableArrayList();
    private User selectedStudent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupFilters();
        loadStudents();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (firstNameColumn != null) firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        if (lastNameColumn != null) lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        if (emailColumn != null) emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (fullNameColumn != null)
            fullNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));

        if (studentsTable != null) {
            studentsTable.setItems(studentList);
            studentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> { selectedStudent = newVal; populateForm(newVal); });
        }
    }

    private void setupFilters() {
        if (courseFilter != null)
            courseFilter.getItems().addAll("Tous les cours", "Introduction à Java", "Mathématiques Avancées");
        if (enrollCourseComboBox != null)
            enrollCourseComboBox.getItems().addAll("Introduction à Java", "Mathématiques Avancées", "Physique Quantique");
    }

    /**
     * Loads all students enrolled in the teacher's courses.
     * TODO: Replace with service call.
     */
    private void loadStudents() {
        // TODO: studentList.setAll(StudentService.getStudentsByTeacher(teacherId));

        studentList.setAll(
            new User(1, "Ahmed", "Benali", "ahmed.benali@example.com", User.Role.STUDENT),
            new User(2, "Fatima", "Ouali", "fatima.ouali@example.com", User.Role.STUDENT),
            new User(3, "Youssef", "Zahra", "youssef.zahra@example.com", User.Role.STUDENT),
            new User(4, "Nadia", "Hamid", "nadia.hamid@example.com", User.Role.STUDENT),
            new User(5, "Khalid", "Mansour", "khalid.mansour@example.com", User.Role.STUDENT)
        );
        updateCount();
    }

    private void populateForm(User student) {
        if (student == null) return;
        if (firstNameField != null) firstNameField.setText(student.getFirstName());
        if (lastNameField != null) lastNameField.setText(student.getLastName());
        if (emailField != null) emailField.setText(student.getEmail());
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) {
            loadStudents();
            return;
        }
        // TODO: Filter via service or local list
        ObservableList<User> filtered = studentList.filtered(s ->
            s.getFullName().toLowerCase().contains(query) ||
            s.getEmail().toLowerCase().contains(query));
        studentsTable.setItems(filtered);
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        // TODO: Filter students by selected course
    }

    /**
     * Enrolls a student in a course.
     * TODO: Implement via EnrollmentService.
     */
    @FXML
    private void handleEnroll(ActionEvent event) {
        if (selectedStudent == null) { showMessage("Sélectionnez un étudiant.", true); return; }
        String course = enrollCourseComboBox != null ? enrollCourseComboBox.getValue() : null;
        if (course == null) { showMessage("Sélectionnez un cours.", true); return; }
        // TODO: EnrollmentService.enroll(selectedStudent.getId(), courseId);
        showMessage(selectedStudent.getFullName() + " inscrit(e) au cours: " + course, false);
    }

    /**
     * Removes the selected student from the platform.
     * TODO: Implement via StudentService.
     */
    @FXML
    private void handleRemoveStudent(ActionEvent event) {
        if (selectedStudent == null) { showMessage("Sélectionnez un étudiant.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer l'étudiant \"" + selectedStudent.getFullName() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // TODO: StudentService.deleteStudent(selectedStudent.getId());
                studentList.remove(selectedStudent);
                updateCount();
                showMessage("Étudiant supprimé.", false);
            }
        });
    }

    @FXML
    private void handleClear(ActionEvent event) {
        if (firstNameField != null) firstNameField.clear();
        if (lastNameField != null) lastNameField.clear();
        if (emailField != null) emailField.clear();
        selectedStudent = null;
    }

    private void updateCount() {
        if (studentsCountLabel != null)
            studentsCountLabel.setText(studentList.size() + " étudiant(s)");
    }

    private void showMessage(String msg, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #10B981;");
            messageLabel.setVisible(true);
        }
    }

    public ObservableList<User> getStudentList() {
        return studentList;
    }
}
