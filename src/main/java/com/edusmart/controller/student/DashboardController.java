package com.edusmart.controller.student;

import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.dao.jdbc.JdbcGradeDao;
import com.edusmart.model.Grade;
import com.edusmart.model.User;
import com.edusmart.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label studentNameLabel;
    @FXML private Label avgGradeLabel;
    @FXML private Label coursesCountLabel;
    @FXML private Label upcomingExamsLabel;
    @FXML private Label creditsLabel;

    @FXML private TableView<Grade> recentGradesTable;
    @FXML private TableColumn<Grade, String> colSubject;
    @FXML private TableColumn<Grade, Double> colGrade;
    @FXML private TableColumn<Grade, String> colStatus;

    @FXML private Label nextExamTitle;
    @FXML private Label nextExamDate;

    private final JdbcGradeDao gradeDao = new JdbcGradeDao();
    private final JdbcCourseDao courseDao = new JdbcCourseDao();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = SceneManager.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Bonjour, " + user.getFirstName() + " !");
            studentNameLabel.setText(user.getFullName());
        }

        setupTable();
        loadStats();
        loadRecentGrades();
        loadNextExam();
    }

    private void setupTable() {
        colSubject.setCellValueFactory(cd -> new SimpleStringProperty("Cours #" + cd.getValue().getCourseId()));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("note"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("session"));
    }

    private void loadStats() {
        int studentId = getStudentId();
        List<Grade> grades = gradeDao.findByStudentId(studentId);
        double avg = grades.stream().mapToDouble(Grade::getNote).average().orElse(0.0);
        avgGradeLabel.setText(String.format("%.2f", avg));

        try {
            coursesCountLabel.setText(String.valueOf(courseDao.findAll().size()));
        } catch (Exception e) { coursesCountLabel.setText("0"); }
        
        upcomingExamsLabel.setText("3"); // Mock
        creditsLabel.setText("250 €"); // Mock
    }

    private void loadRecentGrades() {
        List<Grade> grades = gradeDao.findByStudentId(getStudentId());
        recentGradesTable.setItems(FXCollections.observableArrayList(
            grades.stream().limit(5).toList()
        ));
    }

    private void loadNextExam() {
        nextExamTitle.setText("Algorithmique");
        nextExamDate.setText("Mardi, 15 Mai à 09:00");
    }

    private int getStudentId() {
        User u = SceneManager.getInstance().getCurrentUser();
        return u != null ? u.getId() : 2;
    }

    // Navigation
    @FXML private void handleDashboard(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_DASHBOARD); }
    @FXML private void handleCourses(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES); }
    @FXML private void handleExams(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_EXAMS); }
    @FXML private void handleBulletin(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_BULLETIN); }
    @FXML private void handleCertification(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_CERTIFICATION); }
    @FXML private void handleShop(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_SHOP); }
    @FXML private void handleStudentAI(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_AI); }
    @FXML private void handleProfile(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.PROFILE); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
