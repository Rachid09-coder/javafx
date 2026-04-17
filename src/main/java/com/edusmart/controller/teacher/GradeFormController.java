package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.dao.jdbc.JdbcGradeDao;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.Course;
import com.edusmart.model.Grade;
import com.edusmart.model.User;
import com.edusmart.service.CourseService;
import com.edusmart.service.GradeService;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.CourseServiceImpl;
import com.edusmart.service.impl.GradeServiceImpl;
import com.edusmart.service.impl.UserServiceImpl;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GradeFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private ComboBox<User> studentComboBox;
    @FXML private Label studentError;
    @FXML private ComboBox<Course> courseComboBox;
    @FXML private TextField subjectField;
    @FXML private Label subjectError;
    @FXML private TextField scoreField;
    @FXML private Label scoreError;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private Label semesterError;
    @FXML private TextField academicYearField;
    @FXML private TextArea commentArea;
    @FXML private Label globalError;

    private final GradeService gradeService = new GradeServiceImpl(new JdbcGradeDao());
    private final UserService userService = new UserServiceImpl(new JdbcUserDao());
    private final CourseService courseService = new CourseServiceImpl(new JdbcCourseDao());

    private Grade gradeToEdit;
    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        semesterComboBox.setItems(FXCollections.observableArrayList("S1", "S2", "S3", "S4"));
        semesterComboBox.setValue("S1");
        setupStudentCombo();
        setupCourseCombo();
        subjectField.textProperty().addListener((o, ov, nv) -> clearError(subjectField, subjectError));
        scoreField.textProperty().addListener((o, ov, nv) -> clearError(scoreField, scoreError));
        
        // Automation: Subject follows Course
        courseComboBox.valueProperty().addListener((o, ov, nv) -> {
            if (nv != null && nv.getId() != 0) {
                subjectField.setText(nv.getTitle());
            }
        });

        // Current Year Default
        academicYearField.setText("2024-2025");
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

    private void setupCourseCombo() {
        courseComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Course c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getTitle());
            }
        });
        courseComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Course c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getTitle());
            }
        });
        List<Course> courses = new ArrayList<>();
        Course none = new Course(); none.setId(0); none.setTitle("(Aucun cours)");
        courses.add(none);
        try { courses.addAll(courseService.getAllCourses()); } catch (Exception ignored) {}
        courseComboBox.setItems(FXCollections.observableArrayList(courses));
        courseComboBox.setValue(courses.get(0));
    }

    public void setAddMode() { titleLabel.setText("Nouvelle Note"); gradeToEdit = null; }

    public void setEditMode(Grade grade) {
        titleLabel.setText("Modifier la Note");
        gradeToEdit = grade;
        // The subject is derived from the course or a map in real use
        // For now, if we don't have it in Grade model, we can try to find it in courseComboBox
        courseComboBox.getItems().stream()
            .filter(c -> c.getId() == grade.getCourseId())
            .findFirst().ifPresent(c -> {
                courseComboBox.setValue(c);
                subjectField.setText(c.getTitle());
            });
        scoreField.setText(String.valueOf(grade.getNote()));
        semesterComboBox.setValue(grade.getSemester() != null ? grade.getSemester() : "S1");
        academicYearField.setText(grade.getAcademicYear() != null ? grade.getAcademicYear() : "");
        studentComboBox.getItems().stream()
            .filter(u -> u.getId() == grade.getStudentId())
            .findFirst().ifPresent(studentComboBox::setValue);
        courseComboBox.getItems().stream()
            .filter(c -> c.getId() == grade.getCourseId())
            .findFirst().ifPresent(courseComboBox::setValue);
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave(ActionEvent e) {
        if (!validateForm()) return;
        try {
            Grade grade = buildGrade();
            boolean ok;
            if (gradeToEdit == null) ok = gradeService.createGrade(grade);
            else { grade.setId(gradeToEdit.getId()); ok = gradeService.updateGrade(grade); }
            if (ok) { saved = true; closeStage(); }
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
        if (subjectField.getText().trim().isEmpty()) {
            showFieldError(subjectField, subjectError, "Matière obligatoire."); valid = false;
        }
        double scoreVal = 0;
        String scoreText = scoreField.getText().trim();
        if (scoreText.isEmpty()) {
            showFieldError(scoreField, scoreError, "Note obligatoire."); valid = false;
        } else { try { scoreVal = Double.parseDouble(scoreText); if (scoreVal < 0) { showFieldError(scoreField, scoreError, "Note doit être ≥ 0."); valid = false; } } catch (NumberFormatException ex) { showFieldError(scoreField, scoreError, "Nombre invalide."); valid = false; } }
        if (semesterComboBox.getValue() == null) {
            semesterError.setText("Semestre obligatoire."); semesterError.setVisible(true); semesterError.setManaged(true); valid = false;
        }
        return valid;
    }

    private Grade buildGrade() {
        Grade g = new Grade();
        User student = studentComboBox.getValue();
        if (student != null) g.setStudentId(student.getId());
        Course course = courseComboBox.getValue();
        g.setCourseId(course != null && course.getId() != 0 ? course.getId() : 0);
        g.setModuleId(course != null ? course.getModuleId() : null);
        g.setNote(Double.parseDouble(scoreField.getText().trim()));
        g.setSemester(semesterComboBox.getValue());
        g.setAcademicYear(academicYearField.getText().trim());
        g.setCoefficient(1.0); // Default coefficient
        g.setSession("Principale"); // Default session
        return g;
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

    public static boolean openDialog(Stage owner, Grade grade) {
        try {
            FXMLLoader loader = new FXMLLoader(GradeFormController.class.getResource("/fxml/teacher/grade-form.fxml"));
            javafx.scene.Parent root = loader.load();
            GradeFormController ctrl = loader.getController();
            if (grade == null) ctrl.setAddMode(); else ctrl.setEditMode(grade);
            Stage stage = new Stage();
            stage.initOwner(owner); stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(grade == null ? "Ajouter une note" : "Modifier la note");
            stage.setResizable(false);
            Scene scene = new Scene(root);
            URL css = GradeFormController.class.getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene); stage.showAndWait();
            return ctrl.isSaved();
        } catch (IOException ex) { ex.printStackTrace(); return false; }
    }
}
