module com.edusmart {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;

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
