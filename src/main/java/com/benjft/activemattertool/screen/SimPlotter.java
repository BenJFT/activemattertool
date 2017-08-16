package com.benjft.activemattertool.screen;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.stage.Stage;

public class SimPlotter {

    private final NumberAxis xAxis, yAxis;
    private final LineChart<Number, Number> lineChart;

    //    private final Scene scene;
    protected SimPlotter(Stage stage) {
        this.xAxis = new NumberAxis();
        this.yAxis = new NumberAxis();
        this.lineChart = new LineChart<>(xAxis, yAxis);
//        this.scene = new Scene(this.lineChart);
    }

    public LineChart<Number, Number> getLineChart() {
        return lineChart;
    }

//    public Scene getScene() {
//        return scene;
//    }

    public NumberAxis getXAxis() {
        return xAxis;
    }

    public NumberAxis getYAxis() {
        return yAxis;
    }
}
