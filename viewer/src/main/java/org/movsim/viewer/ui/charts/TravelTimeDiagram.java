package org.movsim.viewer.ui.charts;

///*
// * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
// *                             Ralph Germ, Martin Budden
// *                             <movsim@akesting.de>
// * ----------------------------------------------------------------------
// *
// *  This file is part of
// *
// *  MovSim - the multi-model open-source vehicular-traffic simulator
// *
// *  MovSim is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  MovSim is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
// *  <http://www.movsim.org>.
// *
// * ----------------------------------------------------------------------
//*/

import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadNetwork;

import java.awt.*;

public class TravelTimeDiagram extends JFXPanel {

    private final Simulator simulator;
    final LineChart<Number, Number> lineChart;
    XYChart.Series series;
    Thread thread;
    boolean threadRunning = true;
    int currentPoint = 1;
    boolean drawnInLastSecond = false;
    double previousTravelTime = 0;
    double travelTime = 0;
    double currentTime = 0;

    public TravelTimeDiagram(Simulator simulator) {
        this.simulator = simulator;
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("TravelTime");
        //creating the chart
        lineChart = new LineChart<Number, Number>(xAxis, yAxis);

        lineChart.setTitle("Travel Time Diagram");
        //defining a series
        series = new XYChart.Series();
        series.setName("Travel Time");
        thread = new Thread(updateTask());

        Scene scene = new Scene(lineChart, 800,800);
        lineChart.getData().add(series);

        this.setScene(scene);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(!drawnInLastSecond) {
            drawnInLastSecond = true;
            series.getData().add(new XYChart.Data<>(currentPoint, currentTime));
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
                    RoadNetwork roadNetwork = simulator.getRoadNetwork();
                    travelTime =  roadNetwork.totalVehicleTravelTime() / roadNetwork.vehicleCount();
                    currentTime = travelTime - previousTravelTime;
                    previousTravelTime = travelTime;
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
