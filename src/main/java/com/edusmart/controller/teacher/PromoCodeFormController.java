package com.edusmart.controller.teacher;

import com.edusmart.dao.jdbc.JdbcPromoCodeDao;
import com.edusmart.model.PromoCode;
import com.edusmart.service.PromoCodeService;
import com.edusmart.service.impl.PromoCodeServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PromoCodeFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField codeField;
    @FXML private Label codeError;
    @FXML private TextField percentField;
    @FXML private Label percentError;
    @FXML private CheckBox activeCheckBox;
    @FXML private Label globalError;

    private final PromoCodeService promoCodeService = new PromoCodeServiceImpl(new JdbcPromoCodeDao());
    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (codeField != null) codeField.textProperty().addListener((o, ov, nv) -> clearError(codeField, codeError));
        if (percentField != null) percentField.textProperty().addListener((o, ov, nv) -> clearError(percentField, percentError));
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave(ActionEvent e) {
        if (!validateForm()) return;
        try {
            PromoCode promo = new PromoCode();
            promo.setCode(codeField.getText().trim());
            promo.setDiscountPercent(Double.parseDouble(percentField.getText().trim()));
            promo.setActive(activeCheckBox != null && activeCheckBox.isSelected());

            boolean ok = promoCodeService.createPromoCode(promo);
            if (ok) {
                saved = true;
                closeStage();
            } else {
                showGlobalError("Opération échouée.");
            }
        } catch (IllegalArgumentException ex) {
            showGlobalError(ex.getMessage());
        } catch (Exception ex) {
            showGlobalError("Erreur : " + rootCause(ex));
        }
    }

    @FXML
    private void handleCancel(ActionEvent e) {
        closeStage();
    }

    private boolean validateForm() {
        boolean valid = true;

        String code = codeField != null ? codeField.getText().trim() : "";
        if (code.isEmpty()) {
            showFieldError(codeField, codeError, "Code obligatoire.");
            valid = false;
        }

        String pctText = percentField != null ? percentField.getText().trim() : "";
        if (pctText.isEmpty()) {
            showFieldError(percentField, percentError, "Pourcentage obligatoire.");
            valid = false;
        } else {
            try {
                double pct = Double.parseDouble(pctText);
                if (pct < 0 || pct > 100) {
                    showFieldError(percentField, percentError, "Doit être entre 0 et 100.");
                    valid = false;
                }
            } catch (NumberFormatException ex) {
                showFieldError(percentField, percentError, "Nombre invalide.");
                valid = false;
            }
        }

        return valid;
    }

    private void showFieldError(TextField f, Label l, String msg) {
        if (f != null) f.setStyle("-fx-border-color:#EF4444;-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;");
        if (l != null) { l.setText(msg); l.setVisible(true); l.setManaged(true); }
    }

    private void clearError(TextField f, Label l) {
        if (f != null) f.setStyle("");
        if (l != null) { l.setVisible(false); l.setManaged(false); }
    }

    private void showGlobalError(String msg) {
        if (globalError == null) return;
        globalError.setText(msg);
        globalError.setVisible(true);
        globalError.setManaged(true);
    }

    private void closeStage() {
        if (titleLabel != null) ((Stage) titleLabel.getScene().getWindow()).close();
    }

    private String rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : "Erreur";
    }

    public static boolean openDialog(Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(PromoCodeFormController.class.getResource("/fxml/teacher/promo-code-form.fxml"));
            javafx.scene.Parent root = loader.load();
            PromoCodeFormController ctrl = loader.getController();

            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Ajouter un code promo");
            stage.setResizable(false);

            Scene scene = new Scene(root);
            URL css = PromoCodeFormController.class.getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();
            return ctrl.isSaved();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}

