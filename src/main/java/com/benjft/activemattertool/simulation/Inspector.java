package com.benjft.activemattertool.simulation;

public interface Inspector {
    void setState(double[][] particles, int[][][] grid);

    double[] getValues();
}
