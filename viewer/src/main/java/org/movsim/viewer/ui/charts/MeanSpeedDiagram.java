package org.movsim.viewer.ui.charts;

import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadNetwork;

import java.awt.*;

/**
 * Created by Lloyd on 09/02/2017.
 */
public class MeanSpeedDiagram extends JFXPanel{

    private final Simulator simulator;
    final LineChart<Number, Number> lineChart;
    XYChart.Series series;
    Thread thread;
    boolean threadRunning = true;
    int currentPoint = 1;
    boolean drawnInLastSecond = false;

    public MeanSpeedDiagram(Simulator simulator) {
        this.simulator = simulator;
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Mean Speed");
        //creating the chart
        lineChart = new LineChart<Number, Number>(xAxis, yAxis);

        lineChart.setTitle("Mean Speed Diagram");
        //defining a series
        series = new XYChart.Series();
        series.setName("Mean Speed");
        thread = new Thread(updateTask());

        Scene scene = new Scene(lineChart, 800,800);
        lineChart.getData().add(series);
        lineChart.setCreateSymbols(false);

        this.setScene(scene);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(!drawnInLastSecond) {
            drawnInLastSecond = true;
            RoadNetwork roadNetwork = simulator.getRoadNetwork();
            double meanSpeed =  roadNetwork.vehiclesMeanSpeed();
            series.getData().add(new XYChart.Data<>(currentPoint, meanSpeed));
            //System.out.println(travelTime);
            currentPoint++;
        }
    }

    private Task<Void> updateTask() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int currentPoint = 1;
                while (threadRunning) {
                    drawnInLastSecond = false;
                    repaint();
                    Thread.sleep(1000);
                }
                return null;
            }
        };

        return task;
    }

    public void start() {
        System.out.println("Starting data gather");
        threadRunning = true;
        thread.start();
    }

    public void stop(){
        threadRunning = false;
    }
}
