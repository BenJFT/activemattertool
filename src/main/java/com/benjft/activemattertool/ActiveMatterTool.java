package com.benjft.activemattertool;

import com.benjft.activemattertool.screen.DeltaNPlotter;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main class, app launches from here.
 */
public class ActiveMatterTool extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: setup input
        DeltaNPlotter plotter = new DeltaNPlotter(primaryStage, new double[]{0.2, 0.3, 0.4, 0.5}, new
                int[]{10, 20, 40, 80, 160, 320, 640, 1280, 2560, 5120}, 10000, 0.1, 2, 0.025,
                0.005, 0.05, 0);
    }
}
