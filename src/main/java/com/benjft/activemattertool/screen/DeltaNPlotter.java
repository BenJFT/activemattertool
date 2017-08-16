package com.benjft.activemattertool.screen;

import com.benjft.activemattertool.simulation.ProcessDeltaN;
import com.benjft.activemattertool.simulation.Simulation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.util.Random;

public class DeltaNPlotter extends SimPlotter {

    private final double[] packingFractions;
    private final int[] Nls;
    private final double speed, k, dPos, dAng, dTime;
    private final long masterSeed;
    private final Random random;
    //    private Simulation[][] simulations;
    private ProcessDeltaN[][] processors;

    /**
     * Launches a number of simulations concurrently and plots their Delta N vs N
     *
     * @param stage            the stage to plot on
     * @param packingFractions the packing fractions to use
     * @param Nls              the target average numbers for sampling subsystems
     * @param Nt               the total number in each sim
     * @param speed            the self propulsion speed
     * @param k                the interaction strength
     * @param dPos             the noise in position
     * @param dAng             the noise in heading
     * @param dTime            the integration time-step
     * @param masterSeed       seed to generate simulations from
     */
    public DeltaNPlotter(Stage stage, double[] packingFractions, int[] Nls, int Nt,
                         double speed, double k, double dPos, double dAng, double dTime, long masterSeed) {
        super(stage);

        this.packingFractions = packingFractions;
        this.Nls = Nls;

//        this.width = width;
//        this.height = height;
        this.speed = speed;
        this.k = k;
        this.dPos = dPos;
        this.dAng = dAng;
        this.dTime = dTime;
        this.masterSeed = masterSeed;

        this.random = new Random(this.masterSeed);
//        this.simulations = new Simulation[packingFractions.length][this.Nls.length];
        this.processors = new ProcessDeltaN[this.packingFractions.length][this.Nls.length];

        SimView[] simViews = new SimView[packingFractions.length];

        getYAxis().setAutoRanging(false);
        getXAxis().setAutoRanging(false);

        // Axis limit bindings
        DoubleProperty lowerX = new SimpleDoubleProperty(Double.MAX_VALUE);
        DoubleProperty upperX = new SimpleDoubleProperty(Double.MIN_VALUE);
        DoubleProperty lowerY = new SimpleDoubleProperty(Double.MAX_VALUE);
        DoubleProperty upperY = new SimpleDoubleProperty(Double.MIN_VALUE);

        // create simulations
        for (int i = 0; i < packingFractions.length; ++i) {
            double packingFraction = packingFractions[i];
            Series<Number, Number> series = new Series<>();
            series.setName(String.format("\u03d5\u2248%.3f", packingFraction));
            Simulation sim = Simulation.newInstance(packingFraction, Nt, this.speed, this.k, this.dPos, this.dAng,
                    this.dTime, random.nextLong());
            // bind listeners
            for (int j = 0; j < this.Nls.length; ++j) {
                int Nl = this.Nls[j];

                ProcessDeltaN process = new ProcessDeltaN(sim, Nl, random.nextLong());
                double logY = Math.log10(process.getValue());
                double logX = Math.log10(process.getMean());
                Data<Number, Number> data = new Data<>(logX, logY);

                if (logY < lowerY.get()) lowerY.set(logY);
                if (logY > upperY.get()) upperY.set(logY);
                if (logX < lowerX.get()) lowerX.set(logX);
                if (logX > upperX.get()) upperX.set(logX);

                process.getValueProperty()
                       .addListener((observable, oldValue, newValue) -> {
                           double v = Math.log10(newValue.doubleValue());
                           data.setYValue(v);
                           if (v > upperY.get())
                               upperY.set(v);
                           if (v < lowerY.get())
                               lowerY.set(v);
                       });
                process.getMeanProperty()
                       .addListener((observable, oldValue, newValue) -> {
                           double v = Math.log10(newValue.doubleValue());
                           data.setXValue(v);
                           if (v > upperX.get())
                               upperX.set(v);
                           if (v < lowerX.get())
                               lowerX.set(v);
                       });

//                this.simulations[i][j] = sim;
                this.processors[i][j] = process;
                series.getData()
                      .add(data);
            }
            simViews[i] = new SimView(new Stage(), sim);
            this.getLineChart()
                .getData()
                .add(series);
        }

        stage.setScene(new Scene(this.getLineChart(), 800, 600));
        stage.show();

        getYAxis().setAutoRanging(false);
        getYAxis().upperBoundProperty()
                  .bind(upperY);
        getYAxis().lowerBoundProperty()
                  .bind(lowerY);
        getXAxis().setAutoRanging(false);
        getXAxis().upperBoundProperty()
                  .bind(upperX);
        getXAxis().lowerBoundProperty()
                  .bind(lowerX);

        getLineChart().setAnimated(false);


//        AnimationTimer animationTimer = new AnimationTimer() {
//            @Override
//            public void handle(long now) {
//                for (Simulation[] sims: simulations)
//                    for (Simulation sim: sims)
//                        sim.advanceAndGetParticles();
//                for (ProcessDeltaN[] procs: processors) {
//                    for (ProcessDeltaN proc: procs)
//                        System.out.printf("%.3f ", proc.getValue());
//                    System.out.println();
//                }
//                System.out.println();
//            }
//        };
//        animationTimer.start();
    }
}
