module com.edusmart {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires java.desktop;
    requires jakarta.mail;
    requires com.github.librepdf.openpdf;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires com.google.gson;
    requires okhttp3;
    requires twilio;


    opens com.edusmart to javafx.fxml;
    opens com.edusmart.controller.auth to javafx.fxml;
    opens com.edusmart.controller.student to javafx.fxml;
    opens com.edusmart.controller.teacher to javafx.fxml;
    opens com.edusmart.controller.shared to javafx.fxml;
    opens com.edusmart.model to javafx.base;
    opens com.edusmart.util to javafx.fxml;
    opens com.edusmart.service to com.google.gson;

    exports com.edusmart;
    exports com.edusmart.controller.auth;
    exports com.edusmart.controller.student;
    exports com.edusmart.controller.teacher;
    exports com.edusmart.controller.shared;
    exports com.edusmart.model;
    exports com.edusmart.util;
    exports com.edusmart.service;
}
