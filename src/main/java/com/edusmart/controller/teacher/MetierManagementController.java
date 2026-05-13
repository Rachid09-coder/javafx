package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcMetierDao;
import com.edusmart.dao.jdbc.JdbcMetierAvanceDao;
import com.edusmart.model.Metier;
import com.edusmart.model.MetierAvance;
import com.edusmart.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MetierManagementController implements Initializable {

    @FXML private TableView<Metier> metierTable;
    @FXML private TableColumn<Metier, String> nomColumn;
    @FXML private TableColumn<Metier, String> descColumn;

    @FXML private TableView<MetierAvance> metierAvanceTable;
    @FXML private TableColumn<MetierAvance, String> nomAvColumn;
    @FXML private TableColumn<MetierAvance, Integer> metierIdColumn;

    private final JdbcMetierDao metierDao = new JdbcMetierDao();
    private final JdbcMetierAvanceDao metierAvanceDao = new JdbcMetierAvanceDao();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTables();
        loadData();
    }

    private void setupTables() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        nomAvColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        metierIdColumn.setCellValueFactory(new PropertyValueFactory<>("metierId"));
    }

    private void loadData() {
        metierTable.setItems(FXCollections.observableArrayList(metierDao.findAll()));
        metierAvanceTable.setItems(FXCollections.observableArrayList(metierAvanceDao.findAll()));
    }

    @FXML
    private void handleAddMetier(ActionEvent e) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouveau Métier");
        dialog.setHeaderText("Création d'un métier");
        dialog.setContentText("Nom:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            Metier m = new Metier(0, name, "Description par défaut");
            metierDao.create(m);
            loadData();
        });
    }

    @FXML private void handleDashboard(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD); }
    @FXML private void handleLogout(ActionEvent e) { SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN); }
}
