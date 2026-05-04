module com.edusmart {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.prefs;
    requires itextpdf;
    requires jakarta.mail;
    requires org.eclipse.angus.mail;
    requires com.calendarfx.view;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.services.calendar;
    requires java.net.http;
    requires com.google.gson;
    requires com.google.api.client.json.gson;

    opens com.edusmart to javafx.fxml;
    opens com.edusmart.controller.auth to javafx.fxml;
    opens com.edusmart.controller.student to javafx.fxml;
    opens com.edusmart.controller.teacher to javafx.fxml;
    opens com.edusmart.model to javafx.base;
    opens com.edusmart.util to javafx.fxml;

    exports com.edusmart;
    exports com.edusmart.controller.auth;
    exports com.edusmart.controller.student;
    exports com.edusmart.controller.teacher;
    exports com.edusmart.model;
    exports com.edusmart.util;
}
