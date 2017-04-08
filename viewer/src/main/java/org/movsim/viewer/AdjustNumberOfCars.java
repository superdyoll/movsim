package org.movsim.viewer;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.movsim.autogen.Movsim;
import org.movsim.autogen.VehiclePrototypeConfiguration;
import org.movsim.autogen.VehicleType;
import org.movsim.simulator.Simulator;
import org.movsim.viewer.ui.AppFrame;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Created by Lloyd on 14/03/2017.
 */
public class AdjustNumberOfCars extends TimingRunnable {

    int MAX_ITERATIONS = 50;
    int MULTIPLIER;

    private FileWriter writer = null;
    private String valuesString;

    public AdjustNumberOfCars(AppFrame appFrame) {
        super(appFrame);
        MULTIPLIER = 100 / MAX_ITERATIONS;
    }

    @Override
    public void printValues(Simulator simulator) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(",");
        stringBuilder.append(simulator.getRoadNetwork().getStoppedVehicleCount());
        stringBuilder.append(",");
        stringBuilder.append(simulator.getRoadNetwork().totalVehicleFuelUsedLiters());
        stringBuilder.append(",");
        stringBuilder.append(simulator.getRoadNetwork().totalVehicleTravelTime());
        stringBuilder.append(",");
        stringBuilder.append(simulator.getRoadNetwork().totalVehicleTravelDistance());
        stringBuilder.append(",");
        stringBuilder.append(simulator.getRoadNetwork().vehicleCount() + simulator.getRoadNetwork().totalVehiclesRemoved());
        valuesString = stringBuilder.toString();
    }

    @Override
    public boolean whileLoopBoolean(int cumulativeTime, boolean carStopped, boolean trafficSlowed, boolean crashed) {
        return cumulativeTime < MAX_TIME_TO_RUN;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        System.out.println("CALLED THREAD");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            writer = new FileWriter("adjust7.csv");
            writer.append("% ACC, Stopped Vehicle count,totalVehicleFuelUsedLiters,totalVehicleTravelTime, totalVehicleTravelDistance, Total vehicles that were on road,T,s0,s1,delta,a,b,coolness (max 1),safe_deceleration,minimum_gap,threshold_acceleration,right_bias_acceleration,politeness,Time till first stopped car,Time till traffic slows before on ramp\n");

            boolean isSet = false;
            Movsim movsim = null;
            List<FindBestCar.CarTimings> timings = new ArrayList<>();
            for (int x = 0; x < 10; x++) {
                for (int i = 0; i <= MAX_ITERATIONS; i++) {

                    if (!isSet) {
                        System.out.println("CHANGE MOVSIM");
                        movsim = appFrame.getMovsimInput();
                    }

                    List<VehicleType> vehicleTypes = movsim.getScenario().getSimulation().getTrafficComposition().getVehicleType();
                    List<VehicleType> vehicleTypesRoad = movsim.getScenario().getSimulation().getRoad().get(1).getTrafficComposition().getVehicleType();
                    System.out.println(MAX_ITERATIONS);
                    System.out.println(MULTIPLIER);

                    double selfDriving = ((double) i * MULTIPLIER) / 100f;
                    System.out.println("SELF-Driving " + selfDriving);

                    for (int j = 2; j >= 0; j--) {
                        VehicleType vehicleType = vehicleTypes.get(j);
                        VehicleType vehicleTypeRoad = vehicleTypesRoad.get(j);

                        if (j == 0) {
                            double percentage = (1f - selfDriving) * 0.8f;
                            System.out.println("Percentage = " + percentage);
                            double result = Math.round(Math.max(0, Math.min(1000, (percentage * 1000d)))) / 1000d;
                            System.out.println(result);
                            vehicleType.setFraction(result);
                            vehicleTypeRoad.setFraction(result);

                        }else if (j == 1){
                            double percentage = (1f - selfDriving) * 0.2f;
                            double result = Math.round(Math.max(0, Math.min(1000, (percentage * 1000d)))) / 1000d;
                            System.out.println("Percentage = " + percentage);
                            System.out.println(result);
                            vehicleType.setFraction(result);
                            vehicleTypeRoad.setFraction(result);
                        } else if (j == 2) {
                            double result = selfDriving;
                            System.out.println(result);
                            vehicleType.setFraction(result);
                            vehicleTypeRoad.setFraction(result);
                        }
                        System.out.println(vehicleType.getLabel() + " fraction " + vehicleType.getFraction());
                    }

                    runSimulation(timings, movsim, i);
                    Thread.sleep(10);
                    System.out.println(x + "," + i * MULTIPLIER + valuesString + "," + timings.get(i).toString());
                    writer.append(x + "," +  i * MULTIPLIER + valuesString + "," + timings.get(i).toString());
                    writer.flush();
                    System.out.println("Changing sim " + i);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
