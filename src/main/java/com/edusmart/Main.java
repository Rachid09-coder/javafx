package com.edusmart;

import com.edusmart.util.SceneManager;
import com.edusmart.util.LocalServer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * EduSmart - Main Application Entry Point
 *
 * This is the main JavaFX application class.
 * It initializes the stage and navigates to the login screen.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.init(primaryStage);
        
        LocalServer.start();

        primaryStage.setTitle("EduSmart - Educational Platform");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(750);

        // Navigate to the login screen on startup
        sceneManager.navigateTo(SceneManager.Scene.LOGIN);

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        LocalServer.stop();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
