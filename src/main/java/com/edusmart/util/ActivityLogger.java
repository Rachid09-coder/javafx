package com.edusmart.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLogger {
    
    private static final ObservableList<String> activities = FXCollections.observableArrayList();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public static void log(String entityType, String action, String entityName) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        String msg = String.format("[%s] %s '%s' a été %s", time, entityType, entityName, action);
        activities.add(0, msg); // Add to top
        if (activities.size() > 50) {
            activities.remove(50, activities.size());
        }
    }

    public static ObservableList<String> getActivities() {
        return activities;
    }
}
