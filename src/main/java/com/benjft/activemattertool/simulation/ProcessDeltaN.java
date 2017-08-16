package com.benjft.activemattertool.simulation;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ProcessDeltaN implements StateProcessor<Double> {
    private final Simulation sim;
    private final Random random;
    private final double Nl;
    private boolean active = true;
    private DoubleProperty mean, value;
    private Future<DoublePair> futureValue;

    /**
     * create a new processor that tracks the Delta N vs N of a simulation with a given target average Nl
     *
     * @param sim  the sim to track
     * @param Nl   the target average
     * @param seed the seed for monte-carlo methods
     */
    public ProcessDeltaN(Simulation sim, double Nl, long seed) {
        this.sim = sim;
        this.random = new Random(seed);
        this.Nl = Nl;
        // setup initial values
        DoublePair pair = this.getUpdatedValue(sim.getParticles(), sim.getGrid());
        mean = new SimpleDoubleProperty(pair.a);
        value = new SimpleDoubleProperty(pair.b);
        // register to be auto-updated
        sim.registerStateProcessor(this);
    }

    /**
     * gets the new values after an update is passed
     *
     * @param state the positions of the particles
     * @param grid  the resolved grid storing the particles
     * @return a pair containing the average in the area, and the standard deviation from this average
     */
    private DoublePair getUpdatedValue(double[][] state, int[][][] grid) {

        double newM = 0, oldM = 0, newS = 0, oldS = 0, newVar = 0, oldVar = 0;
        int count = 0;
        final int minCount = 500;

        final double len = Math.sqrt(Math.PI * 0.25 * Nl / sim.getPackingFraction());

        // use monte-carlo method to sample at set number of random points in the simulation space
        // takes samples till result converges
        while (++count <= minCount) {
            // generate random centre to sample near
            double cx = random.nextDouble() * sim.getWidth();
            double cy = random.nextDouble() * sim.getHeight();

            // the number within len of the center point
            double n = this.getNNear(cx, cy, len, state, grid);

            // advance running mean and variance (Uses the Welford method)
            if (count <= 1) {
                newM = n;
            } else {
                oldM = newM;
                oldS = newS;
                oldVar = newVar;
                newM = oldM + (n - oldM) / count;
                newS = oldS + (n - oldM) * (n - newM);
                newVar = newS / (count - 1);
            }
        }

        return new DoublePair(newM, Math.sqrt(newVar));
    }

    private int getNNear(double cx, double cy, double len, double[][] state, int[][][] grid) {
        int count = 0;

        len /= 2;
        // iterate columns that could contain particles close enough
        for (double x = -len; x <= len; x += sim.getCellWidth()) {
            // round to column number
            int col = (int) ((cx + x) / sim.getCellWidth());
            double shiftX = 0;

            // bind to limit and set shift in x coord if wrapped.
            if (col < 0) {
                col += sim.getNCols();
                shiftX = -sim.getWidth();
            } else if (col >= sim.getNCols()) {
                col -= sim.getNCols();
                shiftX = +sim.getWidth();
            }

            // iterate rows that could contain particles close enough
            for (double y = -len; y <= len; y += sim.getCellHeight()) {
                // round to column number
                int row = (int) ((cy + y) / sim.getCellHeight());
                double shiftY = 0;

                // bind to limit and set shift in x coord if wrapped.
                if (row < 0) {
                    row += sim.getNRows();
                    shiftY = -sim.getHeight();
                } else if (row >= sim.getNRows()) {
                    row -= sim.getNRows();
                    shiftY = +sim.getHeight();
                }

                // iterate over particles in loaded cell and add to count if near enough
                int[] cell = grid[col][row];
                for (int idx : cell) {
                    double[] particle = state[idx];

                    double dx = particle[0] + shiftX - cx;
                    double dy = particle[1] + shiftY - cy;

//                    double r = Math.sqrt(dx * dx + dy * dy);
                    if (Math.abs(dx) < len && Math.abs(dy) < len) ++count;
                }
            }
        }
        return count;
    }

    @Override
    /**
     * advances the internal state and starts working on the future states based on the futures passed
     */
    public void advance(Future<double[][]> state, Future<int[][][]> futureGrid) {
        // block until current future state is recovered
        if (this.isActive()) {
            if (futureValue != null) try {
                DoublePair pair = futureValue.get();
                mean.set(pair.a);
                value.set(pair.b);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                e.getCause()
                 .printStackTrace();
                System.exit(1);
            }

            // start new future state
            futureValue = StateProcessor.executorPool.submit(() -> {
                try {
                    return this.getUpdatedValue(state.get(), futureGrid.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    e.getCause()
                     .printStackTrace();
                    System.exit(1);
                }
                return new DoublePair(0, 0);
            });
        }
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Double getValue() {
        return this.value.get();
    }

    public ReadOnlyDoubleProperty getValueProperty() {
        return this.value;
    }

    public double getMean() {
        return mean.get();
    }

    public ReadOnlyDoubleProperty getMeanProperty() {
        return mean;
    }

    /**
     * Internal helper class for returning a pair of doubles
     */
    private class DoublePair {
        final double a, b;

        DoublePair(double a, double b) {
            this.a = a;
            this.b = b;
        }
    }
}
