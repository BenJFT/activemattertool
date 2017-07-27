package com.benjft.activemattertool;

import org.apache.commons.cli.*;

public class App {

    private static final String optStrWidth = "w",
            optStrHeight = "h",
            optStrSpeed = "v",
            optStrK = "k",
            optStrDPos = "P",
            optStrDAng = "A",
            optStrDTime = "T",
            optStrPackFrac = "p",
            optStrSeed = "s";

    public static void main(String[] args) {

        CommandLineParser parser = new DefaultParser();
        Options options = App.getOptions();
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                new HelpFormatter().printHelp("activemattertool", options);
                return;
            }

            double width = Double.valueOf(line.getOptionValue(optStrWidth, "10"));
            double height = Double.valueOf(line.getOptionValue(optStrHeight, "10"));
            double speed = Double.valueOf(line.getOptionValue(optStrSpeed, "0.1"));
            double k = Double.valueOf(line.getOptionValue(optStrK, "3"));
            double dPos = Double.valueOf(line.getOptionValue(optStrDPos, "0.01"));
            double dAng = Double.valueOf(line.getOptionValue(optStrDAng, "0.3"));
            double dTime = Double.valueOf(line.getOptionValue(optStrDTime, "0.05"));
            double packingFraction = Double.valueOf(line.getOptionValue(optStrPackFrac, "0.7"));
            long seed = Long.valueOf(line.getOptionValue(optStrSeed, "0"));

//            Simulation sim = Simulation.newInstance(width, height, speed, k, dPos, dAng, dTime, packingFraction, seed);
            System.out.println("width = " + width);
            System.out.println("height = " + height);
            System.out.println("speed = " + speed);
            System.out.println("k = " + k);
            System.out.println("dPos = " + dPos);
            System.out.println("dAng = " + dAng);
            System.out.println("dTime = " + dTime);
            System.out.println("packingFraction = " + packingFraction);
            System.out.println("seed = " + seed);
        } catch (ParseException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        Option optionWidth = Option.builder(optStrWidth)
                                   .longOpt("width")
                                   .hasArg()
                                   .argName("width")
                                   .type(Double.TYPE)
                                   .desc("Sets the width of the simulation in units of particle diameter.")
                                   .build();
        options.addOption(optionWidth);

        Option optionHeight = Option.builder(optStrHeight)
                                    .longOpt("height")
                                    .hasArg()
                                    .argName("height")
                                    .type(Double.TYPE)
                                    .desc("Sets the height of the simulation in units of particle diameter.")
                                    .build();
        options.addOption(optionHeight);

        Option optionSpeed = Option.builder(optStrSpeed)
                                   .longOpt("speed")
                                   .hasArg()
                                   .argName("speed")
                                   .type(Double.TYPE)
                                   .desc("Sets the speed of the simulation particles in units of particle diameter / " +
                                           "second.")
                                   .build();
        options.addOption(optionSpeed);

        Option optionK = Option.builder(optStrK)
                               .hasArg()
                               .argName("k")
                               .type(Double.TYPE)
                               .desc("Sets the strength of the repulsive force between touching particles")
                               .build();
        options.addOption(optionK);

        Option optionDPos = Option.builder(optStrDPos)
                                  .longOpt("dPos")
                                  .hasArg()
                                  .argName("dPos")
                                  .type(Double.TYPE)
                                  .desc("Sets the strength of the noise in the particles position in units of " +
                                          "particle diameters per second.")
                                  .build();
        options.addOption(optionDPos);

        Option optionDAng = Option.builder(optStrDAng)
                                  .longOpt("dAng")
                                  .hasArg()
                                  .argName("dAng")
                                  .type(Double.TYPE)
                                  .desc("Sets the strength of noise in the particle heading in units of radians per " +
                                          "second.")
                                  .build();
        options.addOption(optionDAng);

        Option optionDTime = Option.builder(optStrDTime)
                                   .longOpt("dTime")
                                   .hasArg()
                                   .argName("dTime")
                                   .type(Double.TYPE)
                                   .desc("Sets the time step for each integration step in units of seconds.")
                                   .build();
        options.addOption(optionDTime);

        Option optPackFrac = Option.builder(optStrPackFrac)
                                   .longOpt("packFrac")
                                   .hasArg()
                                   .argName("packingFraction")
                                   .type(Double.TYPE)
                                   .desc("Sets the fraction of space to be covered by particles.")
                                   .build();
        options.addOption(optPackFrac);

        Option optionSeed = Option.builder(optStrSeed)
                                  .longOpt("seed")
                                  .hasArg()
                                  .argName("seed")
                                  .type(Long.TYPE)
                                  .desc("Sets the seed for the random number generator.")
                                  .build();
        options.addOption(optionSeed);

        Option optionHelp = Option.builder("help")
                                  .desc("Prints this help screen.")
                                  .build();
        options.addOption(optionHelp);

        return options;
    }
}
