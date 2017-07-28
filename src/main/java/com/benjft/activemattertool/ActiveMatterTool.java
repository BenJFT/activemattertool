package com.benjft.activemattertool;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ActiveMatterTool extends Application {

    private final Map<String, Consumer<String>> setterMap = new HashMap<>();
    private final Map<String, String> helpMap = new LinkedHashMap<>();
    private double width = 10,
            height = 10,
            speed = 0.1,
            k = 4,
            dPos = 0.01,
            dAng = 0.3,
            dTime = 0.05,
            packingFraction = 0.7;
    private long seed = 0;

    {
        final String keyWidth = "width",
                keyHeight = "height",
                keySpeed = "speed",
                keyK = "strength",
                keyDPos = "pos_noise",
                keyDAng = "ang_noise",
                keyDTime = "time_step",
                keyPackingFraction = "packing",
                keySeed = "seed",
                keyHelp = "help",
                helpWidth = "=<double>(%.3f) Sets the width of the simulation space in units of particle diameters",
                helpHeight = "=<double>(%.3f) Sets the height of the simulation space in units of particle diameters",
                helpSpeed = "=<double>(%.3f) Sets the speed of the particles in units of diameters per second",
                helpK = "=<double>(%.3f) Sets the constant for the interaction strength between overlapping particles",
                helpDPos = "=<double>(%.3f) Sets the noise in the particles position in units of diameters per second",
                helpDAng = "=<double>(%.3f) Sets the noise in the particles heading in units of radians per second",
                helpDTime = "=<double>(%.3f) Sets the step in time for each integration",
                helpPackingFraction = "=<double>(%.3f) Sets the packing fraction to fill space to",
                helpSeed = "=<long>(%d) Sets the random number seed to use for the simulation",
                helpHelp = "=<optional Key> prints this help screen, or the help for a given argument";

        setterMap.put(keyWidth, v -> width = Double.valueOf(v));
        setterMap.put(keyHeight, v -> height = Double.valueOf(v));
        setterMap.put(keySpeed, v -> speed = Double.valueOf(v));
        setterMap.put(keyK, v -> k = Double.valueOf(v));
        setterMap.put(keyDPos, v -> dPos = Double.valueOf(v));
        setterMap.put(keyDAng, v -> dAng = Double.valueOf(v));
        setterMap.put(keyDTime, v -> dTime = Double.valueOf(v));
        setterMap.put(keyPackingFraction, v -> packingFraction = Double.valueOf(v));
        setterMap.put(keySeed, v -> seed = Long.valueOf(v));
        setterMap.put(keyHelp, v -> {
            String h = helpMap.get(v);
            this.printHelp(v, h);
            System.exit(0);
        });

        helpMap.put(keyWidth, String.format(helpWidth, width));
        helpMap.put(keyHeight, String.format(helpHeight, height));
        helpMap.put(keySpeed, String.format(helpSpeed, speed));
        helpMap.put(keyK, String.format(helpK, k));
        helpMap.put(keyDPos, String.format(helpDPos, dPos));
        helpMap.put(keyDAng, String.format(helpDAng, dAng));
        helpMap.put(keyDTime, String.format(helpDTime, dTime));
        helpMap.put(keyPackingFraction, String.format(helpPackingFraction, packingFraction));
        helpMap.put(keySeed, String.format(helpSeed, seed));
        helpMap.put(keyHelp, helpHelp);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (this.getParameters()
                .getUnnamed()
                .contains("--help")) printAllHelp();
        else if (!this.getParameters()
                      .getUnnamed()
                      .isEmpty()) {
            System.out.println("No such key/flag:" + this.getParameters()
                                                         .getUnnamed()
                                                         .get(0));
            this.printAllHelp();
        }
        this.getParameters()
            .getNamed()
            .forEach((k, v) -> {
                try {
                    setterMap.get(k.toLowerCase())
                             .accept(v);
                } catch (NullPointerException e) {
                    System.out.println("No such key: " + k);
                    this.printAllHelp();
                }
            });

        System.out.println(width);
        System.out.println(height);
        System.out.println(speed);
        System.out.println(k);
        System.out.println(dPos);
        System.out.println(dAng);
        System.out.println(dTime);
        System.out.println(packingFraction);
        System.out.println(seed);

    }

    private void printAllHelp() {
        helpMap.forEach(this::printHelp);
        System.exit(0);
    }

    private void printHelp(String k, String v) {
        System.out.println("--" + k + v);
    }
}
