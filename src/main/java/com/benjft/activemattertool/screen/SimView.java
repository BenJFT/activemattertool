package com.benjft.activemattertool.screen;

import com.benjft.activemattertool.simulation.Simulation;
import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Creates a view of the simulation in real-time.
 */
public class SimView {
    private final Simulation simulation;

    private final DoubleProperty scale, width, height;
    // makes for easier binding between the particle positions and the positions of the circles representing them
    private DoubleProperty[][] positions;

    public SimView(Stage stage, Simulation simulation) {
        this.simulation = simulation;
        width = new ReadOnlyDoubleWrapper(this.simulation.getWidth());
        height = new ReadOnlyDoubleWrapper(this.simulation.getHeight());
        scale = new SimpleDoubleProperty(3); // TODO: bind to window size

        this.start(stage);
    }

    /**
     * populates the stage with a representation of the simulation
     *
     * @param stage the stage to be populated
     */
    private void start(Stage stage) {
        Pane root = new Pane();

        double[][] particles = simulation.getParticles();
        Circle[] circles = createPositions(particles);

        updatePositions(simulation.getParticles());
        root.getChildren()
            .addAll(circles);

        root.getChildren()
            .addAll(this.getOverlays());

        AnimationTimer animation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updatePositions(simulation.advanceAndGetParticles());
            }
        };
        animation.start();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * binds a set of circles to the positions of the particles.
     * Each particle is linked to four circles to help better show interactions across the boundary (shown in red on
     * the display)
     *
     * @param particles the set of particles
     * @return the cet of circles representing them
     */
    private Circle[] createPositions(double[][] particles) {
        this.positions = new DoubleProperty[particles.length][2];
        Circle[] circles = new Circle[particles.length * 4];

        // loop through each particle and populate arrays with required objects
        for (int i = 0; i < particles.length; ++i) {
            // create bindable property for this particle
            positions[i][0] = new SimpleDoubleProperty(particles[i][0]);
            positions[i][1] = new SimpleDoubleProperty(particles[i][1]);

            // create and bind circles for this particle

            // circle will be shifted up and left by half to help show boundary interactions
            Circle circle0 = new Circle();
            circle0.setFill(Color.gray(0, 0.5));
            circle0.radiusProperty()
                   .bind(scale.multiply(0.5d));
            circle0.centerXProperty()
                   .bind(width.multiply(-0.5)
                              .add(positions[i][0])
                              .multiply(scale));
            circle0.centerYProperty()
                   .bind(height.multiply(-0.5)
                               .add(positions[i][1])
                               .multiply(scale));

            // circle will be shifted down and left by half to help show boundary interactions
            Circle circle1 = new Circle();
            circle1.setFill(Color.gray(0, 0.5));
            circle1.radiusProperty()
                   .bind(scale.multiply(0.5d));
            circle1.centerXProperty()
                   .bind(width.multiply(-0.5)
                              .add(positions[i][0])
                              .multiply(scale));
            circle1.centerYProperty()
                   .bind(height.multiply(+0.5)
                               .add(positions[i][1])
                               .multiply(scale));

            // circle will be shifted up and right by half to help show boundary interactions
            Circle circle2 = new Circle();
            circle2.setFill(Color.gray(0, 0.5));
            circle2.radiusProperty()
                   .bind(scale.multiply(0.5d));
            circle2.centerXProperty()
                   .bind(width.multiply(+0.5)
                              .add(positions[i][0])
                              .multiply(scale));
            circle2.centerYProperty()
                   .bind(height.multiply(-0.5)
                               .add(positions[i][1])
                               .multiply(scale));

            // circle will be shifted down and right by half to help show boundary interactions
            Circle circle3 = new Circle();
            circle3.setFill(Color.gray(0, 0.5));
            circle3.radiusProperty()
                   .bind(scale.multiply(0.5d));
            circle3.centerXProperty()
                   .bind(width.multiply(+0.5)
                              .add(positions[i][0])
                              .multiply(scale));
            circle3.centerYProperty()
                   .bind(height.multiply(+0.5)
                               .add(positions[i][1])
                               .multiply(scale));

            circles[i] = circle0;
            circles[positions.length + i] = circle1;
            circles[2 * positions.length + i] = circle2;
            circles[3 * positions.length + i] = circle3;
        }

        return circles;
    }

    /**
     * updates the positions stored as a DoubleProperty[] from the positions of the particles and therefore the
     * positions of the circles on the screen.
     *
     * @param particles the updated particle positions
     */
    private void updatePositions(double[][] particles) {
//        double[][] particles = simulation.getParticles();
        for (int i = 0; i < particles.length; ++i) {
            double[] particle = particles[i];
            DoubleProperty[] pos = positions[i];
            // setting positions here updates positions for circles via the binding set up previously
            pos[0].set(particle[0]);
            pos[1].set(particle[1]);
        }
    }

    /**
     * gets the overlays to show the simulation boundaries and hide duplicates off the edge.
     *
     * @return the overlays
     */
    private Node[] getOverlays() {

        // create a pair of white rectangles to cover the quadrants already shown
        Rectangle r1 = new Rectangle(), r2 = new Rectangle();
        r1.yProperty()
          .bind(height.multiply(scale));
        r1.heightProperty()
          .bind(height.add(1)
                      .multiply(scale)
                      .divide(2));
        r1.widthProperty()
          .bind(width.add(1)
                     .multiply(scale)
                     .multiply(1.5));
        r1.setFill(Color.gray(1));

        r2.xProperty()
          .bind(width.multiply(scale));
        r2.heightProperty()
          .bind(height.add(1)
                      .multiply(scale));
        r2.widthProperty()
          .bind(width.add(1)
                     .multiply(scale)
                     .divide(2));
        r2.setFill(Color.gray(1));

        // create a pair of red l;ines to mark the boundaries of the simulation space
        Line l1 = new Line(), l2 = new Line();
        l1.startXProperty()
          .bind(width.multiply(scale)
                     .divide(2));
        l1.endXProperty()
          .bind(width.multiply(scale)
                     .divide(2));
        l1.endYProperty()
          .bind(height.multiply(scale));
        l1.setStroke(Color.RED);

        l2.startYProperty()
          .bind(height.multiply(scale)
                      .divide(2));
        l2.endYProperty()
          .bind(height.multiply(scale)
                      .divide(2));
        l2.endXProperty()
          .bind(width.multiply(scale));
        l2.setStroke(Color.RED);

        return new Node[]{r1, r2, l1, l2};
    }
}
