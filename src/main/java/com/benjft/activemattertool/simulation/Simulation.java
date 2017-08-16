package com.benjft.activemattertool.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Simulation {
    private final double width, height, speed, k, dPos, dAng, dTime;
    private final int nParticles;

    private final int nCols, nRows;
    private final double cellWidth, cellHeight;

    private double[][] particles;
    private final ExecutorService integrationExecutor = Executors.newSingleThreadExecutor();
    private final Random random;
    private final List<StateProcessor<?>> stateProcessors = new ArrayList<>();
    private int[][][] grid;
    private Future<int[][][]> futureGrid;

    private Simulation(double width, double height, double speed, double k, double dPos, double dAng,
                       double dTime, int nParticles, long seed) {
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.k = k;
        this.dPos = dPos;
        this.dAng = dAng;
        this.dTime = dTime;
        this.nParticles = nParticles;

        this.nCols = (int) this.width;
        this.nRows = (int) this.height;
        this.cellWidth = this.width / nCols;
        this.cellHeight = this.height / nRows;

        this.random = new Random(seed);

        // initialise with a randomised scattering of particles
        this.particles = new double[nParticles][3];
        for (double[] particle : this.particles) {
            particle[0] = random.nextDouble() * this.width;
            particle[1] = random.nextDouble() * this.height;
            particle[2] = random.nextDouble() * Math.PI * 2;
        }
        // load the initial grid
        this.grid = this.formToGrid(particles);
    }

    /**
     * created a grid containing the indices of particles in each grid cell
     *
     * @param particles the particle array to be formed into the grid
     * @return the grd of indices
     */
    private int[][][] formToGrid(double[][] particles) {

        int[][][] grid = new int[nCols][nRows][0];
        for (int i = 0; i < particles.length; ++i) {
            double[] particle = particles[i];
            int col = (int) (particle[0] / cellWidth);
            int row = (int) (particle[1] / cellHeight);

            // copy, update, and replace the current contents of the cell
            int[] cell = grid[col][row];
            int[] updatedCell = new int[cell.length + 1];
            System.arraycopy(cell, 0, updatedCell, 0, cell.length);
            updatedCell[cell.length] = i;
            grid[col][row] = updatedCell;
        }

        return grid;
    }

    /**
     * Creates a new simulation instance with the specified properties
     *
     * @param width           the width of the simulation in particle diameters
     * @param height          the height of the simulation in particle diameters
     * @param speed           the self propulsion speed of the particles in diameters per unit time
     * @param k               the strength of the inter-particle interactions
     * @param dPos            the noise in the particles positions in diameters per unit time
     * @param dAng            the noise in particle heading in radians per unit time
     * @param dTime           the timestep for integrating
     * @param packingFraction the target packing fraction (will be as close to this as possible)
     * @param seed            seed number for the random number generator
     * @return the new simulation instance
     */
    public static Simulation newInstance(double width, double height, double speed, double k, double dPos, double dAng,
                                         double dTime, double packingFraction, long seed) {
        int nParticles = (int) Math.round(width * height * packingFraction / (Math.PI * 0.25));

        return new Simulation(width, height, speed, k, dPos, dAng, dTime, nParticles, seed);
    }

    public static Simulation newInstance(double packingFraction, int Nt, double speed, double k, double dPos,
                                         double dAng, double dTime, long seed) {
        double width = Math.sqrt(Nt * Math.PI * 0.25 / packingFraction);

        return new Simulation(width, width, speed, k, dPos, dAng, dTime, Nt, seed);
    }

    public double getWidth() {
        return width;
    }

    /**
     * Performs a single Euler integration step with length dTime
     *
     * @return the state after the integration step
     */
    private double[][] integrate() {
        double[][] deltas = this.getDeltas();
        double[][] updatedParticles = new double[this.particles.length][3];

        for (int i = 0; i < this.particles.length; ++i) {
            updatedParticles[i][0] = this.particles[i][0] + deltas[i][0] * this.dTime;
            updatedParticles[i][1] = this.particles[i][1] + deltas[i][1] * this.dTime;
            updatedParticles[i][2] = this.particles[i][2] + deltas[i][2] * this.dTime;

            // ensure wrapped to simulation width and height
            if (updatedParticles[i][0] < 0) updatedParticles[i][0] += this.width;
            else if (updatedParticles[i][0] >= this.width) updatedParticles[i][0] -= this.width;

            if (updatedParticles[i][1] < 0) updatedParticles[i][1] += this.height;
            else if (updatedParticles[i][1] >= this.height) updatedParticles[i][1] -= this.height;
        }

        return updatedParticles;
    }

    /**
     * Calculates the shift in values between frames (before multiplication with timestep)
     *
     * @return the amount each value will shift by on a per particle level.
     */
    private double[][] getDeltas() {
        // makes a grid where each cell contains the indices of the particles within it
        final int[][][] cells = this.grid;
        // stores the delta information for each particle
        final double[][] deltas = new double[this.particles.length][3];

        // iterates through the columns of the grid
        for (int col = 0; col < cells.length; ++col) {
            // indices of adjacent columns (and corrections for if they're off the side)
            int colLeft = col - 1;
            double dLeft = 0;
            int colRight = col + 1;
            double dRight = 0;
            if (colLeft < 0) {
                colLeft = cells.length - 1;
                dLeft = -this.width;
            } else if (colRight >= cells.length) {
                colRight = 0;
                dRight = this.width;
            }

            // local references for column and adjacent columns
            int[][] column = cells[col];
            int[][] columnLeft = cells[colLeft];
            int[][] columnRight = cells[colRight];

            // iterates through the cell of the column
            for (int row = 0; row < column.length; ++row) {
                int[] cell = column[row];
                // if the cell is empty nothing needs to be done so continue from the next cell
                if (cell.length == 0) continue;

                // find the index of the row directly above. (accounts for wrapping)
                int rowUp = row - 1;
                double dUp = 0;
                if (rowUp < 0) {
                    rowUp = column.length - 1;
                    dUp = -this.height;
                }

                // checks collisions for each particle in the cell with particles in some adjacent cells
                for (int idx : cell) {
                    double[] particle = this.particles[idx];

                    // adds deltas due to collisions
                    this.calculateDeltas(idx, particle, cell, 0, 0, deltas);
                    this.calculateDeltas(idx, particle, columnLeft[row], dLeft, 0, deltas);
                    this.calculateDeltas(idx, particle, columnLeft[rowUp], dLeft, dUp, deltas);
                    this.calculateDeltas(idx, particle, column[rowUp], 0, dUp, deltas);
                    this.calculateDeltas(idx, particle, columnRight[rowUp], dRight, dUp, deltas);

                    // adds deltas due to movement and noise
                    deltas[idx][0] += this.speed * Math.sin(particle[2]) + dPos * random.nextGaussian();
                    deltas[idx][1] += this.speed * Math.cos(particle[2]) + dPos * random.nextGaussian();
                    deltas[idx][2] += this.dAng * random.nextGaussian();
                }
            }
        }
        return deltas;
    }

    /**
     * applies the delta due to collisions between the passed particle and the particles in the passed cell
     *
     * @param idx1      the index of the particle
     * @param particle1 the values of the particle
     * @param cell2     the cell to check collisions with
     * @param shiftX    correction for periodic bounds
     * @param shiftY    correction for periodic bounds
     * @param deltas    the deltas array
     */
    private void calculateDeltas(int idx1, double[] particle1, int[] cell2, double shiftX, double shiftY,
                                 double[][] deltas) {
        // itterate over each particle in cell2
        for (int idx2 : cell2) {
            double[] particle2 = this.particles[idx2];

            // distance from particle 1
            double dX = shiftX + particle2[0] - particle1[0];
            double dY = shiftY + particle2[1] - particle1[1];
            double r = dX * dX + dY * dY;

            // if r squared < (2*particle_radius) squared calculate interaction.
            if (r < 1 && r != 0) {
                r = Math.sqrt(r);
                // interaction magnitude
                double f = -k * (1 - r) / r;
                // components resolved onto each particle
                deltas[idx1][0] += f * dX;
                deltas[idx1][1] += f * dY;
                deltas[idx2][0] -= f * dX;
                deltas[idx2][1] -= f * dY;
            }
        }
    }

    public int getNCols() {
        return nCols;
    }

    public double getHeight() {
        return height;
    }

    public double getSpeed() {
        return speed;
    }

    public double getK() {
        return k;
    }

    public double getDPos() {
        return dPos;
    }

    public double getDAng() {
        return dAng;
    }

    public double getDTime() {
        return dTime;
    }

    public int getNParticles() {
        return nParticles;
    }

    public double[][] getParticles() {
        return particles;
    }

    public int getNRows() {
        return nRows;
    }

    private Future<double[][]> futureParticles;

    public double getCellWidth() {
        return cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    public int[][][] getGrid() {
        return this.grid;
    }

    public void registerStateProcessor(StateProcessor<?> stateProcessor) {
        this.stateProcessors.add(stateProcessor);
    }

    /**
     * finds the state after a single integration step and sets it internally. Starts the calculation for the next step
     * in advance (to be recovered when this is next called.
     *
     * @return the new state of the system
     */
    public double[][] advanceAndGetParticles() {
        // try to acquire futures that should have been running in the background
        // blocks till they have finished
        if (futureParticles != null) try {
            this.particles = futureParticles.get();
            this.grid = futureGrid.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
            e.getCause()
             .printStackTrace();
            System.exit(1);
        }
        // start new futures to be ready when this method is next called
        futureParticles = integrationExecutor.submit(this::integrate);
        futureGrid = integrationExecutor.submit(() -> {
            try {
                // futureGrid relies on futureParticles
                return this.formToGrid(futureParticles.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                e.getCause()
                 .printStackTrace();
                System.exit(1);
            }
            return null;
        });
        // advance stateProcessors with new futures (blocks till they are done with the old ones)
        for (StateProcessor<?> stateProcessor : stateProcessors) {
            stateProcessor.advance(futureParticles, futureGrid);
        }
        return this.particles;
    }

    /**
     * calculates the exact packing fraction
     * @return the packing fraction
     */
    public double getPackingFraction() {
        return this.nParticles * Math.PI * 0.25d / (this.width * this.height);
    }
}