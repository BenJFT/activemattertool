package com.benjft.activemattertool.simulation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public interface StateProcessor<T> {
    ExecutorService executorPool = Executors.newWorkStealingPool();

    void advance(Future<double[][]> futureState, Future<int[][][]> futureGrid);

    T getValue();

    boolean isActive();

    void setActive(boolean active);
}
