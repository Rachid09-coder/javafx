package com.edusmart.controller.shared;

import com.edusmart.model.User;
import com.edusmart.service.UserService;
import com.edusmart.service.impl.UserServiceImpl;
import com.edusmart.dao.jdbc.JdbcUserDao;
import com.edusmart.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import java.io.File;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.image.Image;

public class ProfileController implements Initializable {

    @FXML private Label nameTitleLabel;
    @FXML private Label roleLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField emailAssocField;
    @FXML private PasswordField passwordField;
    @FXML private Canvas signatureCanvas;

    private GraphicsContext gc;

    private final UserService userService = new UserServiceImpl(new JdbcUserDao());
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUser = SceneManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            initSignaturePad();
            loadUserData();
        }
    }

    private void initSignaturePad() {
        gc = signatureCanvas.getGraphicsContext2D();
        gc.setLineWidth(2.5);
        gc.setStroke(Color.web("#1E293B"));
    }

    private void loadUserData() {
        nameTitleLabel.setText(currentUser.getFullName());
        roleLabel.setText(currentUser.getRoleValue());
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
        emailAssocField.setText(currentUser.getEmailAssoc() != null ? currentUser.getEmailAssoc() : "");
        
        // Load existing signature if any
        if (currentUser.getSignaturePath() != null && !currentUser.getSignaturePath().isBlank()) {
            File sigFile = new File(currentUser.getSignaturePath());
            if (sigFile.exists()) {
                Image img = new Image(sigFile.toURI().toString());
                gc.drawImage(img, 0, 0);
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (currentUser == null) return;

        currentUser.setFirstName(firstNameField.getText().trim());
        currentUser.setLastName(lastNameField.getText().trim());
        currentUser.setEmailAssoc(emailAssocField.getText().trim());

        String newPassword = passwordField.getText();
        if (newPassword != null && !newPassword.isEmpty()) {
            currentUser.setPassword(newPassword);
        }
        
        // Save Signature to file
        try {
            File sigDir = new File("signatures");
            if (!sigDir.exists()) sigDir.mkdirs();
            File sigFile = new File(sigDir, "sig_" + currentUser.getId() + ".png");
            
            WritableImage writableImage = new WritableImage((int) signatureCanvas.getWidth(), (int) signatureCanvas.getHeight());
            signatureCanvas.snapshot(null, writableImage);
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", sigFile);
            
            currentUser.setSignaturePath(sigFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Could not save signature image: " + e.getMessage());
        }

        try {
            if (userService.updateUser(currentUser)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Profil mis à jour");
                alert.setHeaderText(null);
                alert.setContentText("Vos informations ont été enregistrées avec succès.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'enregistrer les modifications.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Return to previous scene based on role or just close
        if (currentUser.getRole() == User.Role.TEACHER) {
            SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD);
        } else {
            SceneManager.getInstance().navigateTo(SceneManager.Scene.STUDENT_COURSES);
        }
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        gc.beginPath();
        gc.moveTo(event.getX(), event.getY());
        gc.stroke();
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        gc.lineTo(event.getX(), event.getY());
        gc.stroke();
    }

    @FXML
    private void handleClearSignature(ActionEvent event) {
        gc.clearRect(0, 0, signatureCanvas.getWidth(), signatureCanvas.getHeight());
    }
}
