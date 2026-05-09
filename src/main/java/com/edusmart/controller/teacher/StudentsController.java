package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.model.User;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.UserServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for managing and viewing the list of students.
 */
public class StudentsController implements Initializable {

    @FXML private TableView<User> studentTable;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colPrenom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;

    private final UserService userService = new UserServiceImpl(new JdbcUserDao());
    private final ObservableList<User> studentList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadStudents();
    }

    private void setupTable() {
        // In the User model: name -> lastName, prenom -> firstName
        colName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleValue"));
        
        studentTable.setItems(studentList);
    }

    private void loadStudents() {
        try {
            studentList.setAll(userService.getAllStudents());
        } catch (Exception e) {
            System.err.println("Error loading students: " + e.getMessage());
        }
    }
}
