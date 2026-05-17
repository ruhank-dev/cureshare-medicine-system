package com.cureshare.app;

import com.cureshare.utils.FontManager;
import com.cureshare.views.auth.LoginScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        FontManager.loadFonts();
        stage.setTitle("CureShare — Smart Medicine Redistribution BMS");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setWidth(1366);
        stage.setHeight(768);
        stage.centerOnScreen();
        new LoginScreen().show(stage);
    }

    public static void main(String[] args) {
        // Enable hardware acceleration and smooth rendering
        System.setProperty("prism.order", "d3d,sw");       // prefer Direct3D on Windows
        System.setProperty("prism.vsync", "true");          // sync to monitor refresh rate
        System.setProperty("prism.text", "t2k");            // smoother text rendering
        System.setProperty("javafx.animation.fullspeed", "false"); // respect vsync
        launch(args);
    }
}
