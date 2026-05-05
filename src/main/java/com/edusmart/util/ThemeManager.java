package com.edusmart.util;

import javafx.scene.Scene;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String THEME_KEY = "isDarkMode";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    
    public static boolean isDarkMode() {
        return prefs.getBoolean(THEME_KEY, false);
    }
    
    public static void setDarkMode(boolean isDark) {
        prefs.putBoolean(THEME_KEY, isDark);
    }
    
    public static void applyTheme(Scene scene) {
        if (scene == null || scene.getRoot() == null) return;
        if (isDarkMode()) {
            if (!scene.getRoot().getStyleClass().contains("dark-theme")) {
                scene.getRoot().getStyleClass().add("dark-theme");
            }
        } else {
            scene.getRoot().getStyleClass().remove("dark-theme");
        }
    }
}
