package com.benjft.activemattertool.simulation;

public class ClusterInspector implements Inspector {

    private final double width, height;
    private double[][] particles;
    private int[][][] grid;

    public ClusterInspector(Simulation sim) {
        this.width = sim.getWidth();
        this.height = sim.getHeight();
    }

    @Override
    public void setState(double[][] particles, int[][][] grid) {

    }

    @Override
    public double[] getValues() {
        double[] clusters = new double[particles.length];

        for (int iCol = 0; iCol < grid.length; ++iCol) {
            int iColLeft = iCol - 1;
            double dLeft = 0;
            int iColRight = iCol + 1;
            double dRight = 0;
            if (iCol == 0) {
                iColLeft = grid.length - 1;
                dLeft = -this.width;
            } else if (iCol == grid.length - 1) {
                iColRight = 0;
                dRight = this.width;
            }

            int[][] col = grid[iCol];
            int[][] colLeft = grid[iColLeft];
            int[][] colRight = grid[iColRight];

            for (int iRow = 0; iRow < col.length; ++iRow) {
                int[] cell = col[iRow];
                if (cell.length == 0) continue;

                int iRowUp = iRow - 1;
                double dUp = 0;
                if (iRowUp == 0) {
                    iRowUp = col.length - 1;
                    dUp = -this.height;
                }

                int[] cellLeft = colLeft[iRow];
                int[] cellLeftUp = colLeft[iRowUp];
                int[] cellUp = col[iRowUp];
                int[] cellRightUp = colRight[iRowUp];

                for (int idx1 : cell) {
                    findClustered(idx1, cell, 0, 0, clusters);
                    findClustered(idx1, cellLeft, dLeft, 0, clusters);
                    findClustered(idx1, cellLeftUp, dLeft, dUp, clusters);
                    findClustered(idx1, cellUp, 0, dUp, clusters);
                    findClustered(idx1, cellRightUp, dRight, dUp, clusters);
                }
            }
        }

        return clusters;
    }

    private void findClustered(int idx1, int[] cell2, double shiftX, double shiftY,
                               double[] clusters) {
        double[] particle1 = this.particles[idx1];
        for (int idx2 : cell2) {
            if (idx1 == idx2) continue;

            double[] particle2 = this.particles[idx2];
            double dX = particle2[0] + shiftX - particle1[0];
            double dY = particle2[1] + shiftY - particle1[1];
            double r = dX * dX + dY * dY;
            if (r < 1) {
                clusters[idx1] += 1;
                clusters[idx2] += 1;
            }
        }
    }
}
