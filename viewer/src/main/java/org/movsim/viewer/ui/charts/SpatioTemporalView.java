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
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import org.movsim.autogen.Road;
import org.movsim.output.route.SpatioTemporal;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;

import java.awt.*;
import java.util.Iterator;

public class SpatioTemporalView extends JFXPanel {

    private final Simulator simulator;
    final ScatterChart<Number, Number> scatterChart;
    XYChart.Series fast;
    XYChart.Series slow;
    XYChart.Series reallySlow;
    Thread thread;
    boolean threadRunning = true;
    int currentPoint = 1;
    boolean drawnInLastSecond = false;
    double previousTravelTime = 0;
    double travelTime = 0;
    double currentTime = 0;
    RoadSegment roadSegment;

    public SpatioTemporalView(Simulator simulator) {
        this.simulator = simulator;

        // Get longest road segment
        roadSegment = getLongestRoadSegment();

        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Distance");
        //creating the chart
        scatterChart = new ScatterChart<Number, Number>(xAxis, yAxis);

        scatterChart.setTitle("Spatio Temporal Diagram");
        //defining a series
        fast = new XYChart.Series();
        fast.setName("Fast");

        slow = new XYChart.Series();
        slow.setName("Slow");

        reallySlow = new XYChart.Series();
        reallySlow.setName("Really Slow");
        thread = new Thread(updateTask());

        Scene scene = new Scene(scatterChart, 800,800);
        scene.getStylesheets().add("org/movsim/viewer/ui/charts/css/spatioTemporal.css");
        scatterChart.getData().add(fast);
        scatterChart.getData().add(slow);
        scatterChart.getData().add(reallySlow);

        this.setScene(scene);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(!drawnInLastSecond) {
            drawnInLastSecond = true;
            fast.getData().add(new XYChart.Data<>(currentPoint, currentTime));
            //System.out.println(travelTime);
            currentPoint++;
        }
    }

    private RoadSegment getLongestRoadSegment(){
        RoadSegment longestRoadSegment = null;

        Iterator<RoadSegment> roadNetwork = simulator.getRoadNetwork().iterator();
        while(roadNetwork.hasNext()){
            RoadSegment roadSegment = roadNetwork.next();
            if (longestRoadSegment == null || roadSegment.roadLength() > longestRoadSegment.roadLength()){
                longestRoadSegment = roadSegment;
            }
        }

        return longestRoadSegment;
    }

    private Task<Void> updateTask() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int currentPoint = 1;
                while (threadRunning) {
                    drawnInLastSecond = false;

                    //travelTime =  roadNetwork.totalVehicleTravelTime() / roadNetwork.vehicleCount();
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
