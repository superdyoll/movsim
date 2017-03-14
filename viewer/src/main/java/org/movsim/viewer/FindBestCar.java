package org.movsim.viewer;

import org.movsim.autogen.*;
import org.movsim.simulator.Simulator;
import org.movsim.viewer.ui.AppFrame;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Created by Lloyd on 09/03/2017.
 */
public class FindBestCar implements Runnable {

    private final static int MAX_TIME_TO_RUN = 18000;

    Thread runner;
    AppFrame appFrame;

    public FindBestCar(AppFrame appFrame) {
        this.appFrame = appFrame;
        this.runner = new Thread(this);
        this.runner.start();
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
        FileWriter writer = null;
        try {
            writer = new FileWriter("test7.csv");
            writer.append("T,s0,s1,delta,a,b,coolness (max 1),safe_deceleration,minimum_gap,threshold_acceleration,right_bias_acceleration,politeness,Time till first stopped car,Time till traffic slows before on ramp\n");

            int max_times = 50;
            int x = 0;

            boolean isSet = false;
            Movsim movsim = null;

            do {
                VehiclePrototypeConfiguration vehiclePrototypeConfiguration;
                x++;
                List<CarTimings> timings = new ArrayList<>();
                for (int i = 0; i < 24; i++) {
                    try {
                        if (!isSet) {
                            System.out.println("CHANGE MOVSIM");
                            movsim = appFrame.getMovsimInput();
                        }
                        isSet = false;
                        vehiclePrototypeConfiguration = movsim.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0);
                        try{
                            setModelParameters(i, vehiclePrototypeConfiguration);
                        }catch (NullPointerException e){}
                        Simulator simulator = appFrame.getSimulator();
                        simulator.getSimulationRunnable().stop();
                        simulator.getRoadNetwork().clear();
                        simulator.setMovsimInput(movsim);
                        simulator.initialize();
                        simulator.reset();
                        appFrame.getTrafficCanvas().setSleepTime(1);
                        appFrame.getTrafficCanvas().start();
                        int cumulativeTime = 0;
                        boolean carStopped = false;
                        boolean trafficSlowed = false;
                        boolean crashed = false;
                        timings.add(new CarTimings(movsim));
                        do {
                            Thread.sleep(10);
                            cumulativeTime += 10;
                            try {
                                if (simulator.getRoadNetwork().findById(2).getStoppedVehicleCount() > 0 && carStopped == false) {
                                    System.out.println("CAR STOPPED");
                                    carStopped = true;
                                    timings.get(i).setCarStopped(cumulativeTime);
                                }
                                if (simulator.getRoadNetwork().findById(1).getSlowestSpeed() < 15 && trafficSlowed == false) {
                                    System.out.println("ROAD SLOWED");
                                    trafficSlowed = true;
                                    timings.get(i).setCarsSlow(cumulativeTime);
                                }
                                if (simulator.getRoadNetwork().findById(1).getObstacleCount() > 0){
                                    crashed = true;
                                    System.out.println("CRASHED CRASHED CRASHED");
                                    timings.get(i).setCarsSlow(0);
                                    timings.get(i).setCarStopped(0);
                                }
                            } catch (NullPointerException|ConcurrentModificationException e){
                                System.out.println("Another error caught");
                            }

                        } while (cumulativeTime < MAX_TIME_TO_RUN && (carStopped == false || trafficSlowed == false) && crashed == false);
                        if (carStopped == false) {
                            timings.get(i).setCarStopped(cumulativeTime);
                        }
                        if (trafficSlowed == false) {
                            timings.get(i).setCarsSlow(cumulativeTime);
                        }
                        simulator.getSimulationRunnable().stop();
                        Thread.sleep(10);
                        System.out.println(timings.get(i).toString());
                        writer.append(timings.get(i).toString());
                        writer.flush();
                        System.out.println("Changing sim " + i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                }
                int currentBestCarStop = 0;
                int currentBestCarSlow = 0;
                int improvementBestStop = 0;
                int improvementBestSlow = 0;

                Collections.shuffle(timings);
                for (CarTimings carTimings : timings) {
                    if (carTimings != null) {
                        int improvementSlow = carTimings.getCarsSlow() - currentBestCarSlow;
                        int improvementStop = carTimings.getCarStopped() - currentBestCarStop;


                        if (improvementStop > improvementBestStop && improvementStop > improvementBestSlow && currentBestCarStop < MAX_TIME_TO_RUN - 100 ) {
                            System.out.println("LONGER TO STOP " + carTimings.getCarStopped() + " > " + currentBestCarStop);
                            improvementBestStop = improvementStop;
                            improvementBestSlow = improvementSlow;
                            currentBestCarStop = carTimings.getCarStopped();
                            currentBestCarSlow = carTimings.getCarsSlow();
                            movsim = carTimings.getMovsim();
                            isSet = true;
                        } else if (improvementSlow > improvementBestStop && improvementSlow > improvementBestSlow && currentBestCarStop < MAX_TIME_TO_RUN - 100) {
                            System.out.println("LONGER TO SLOW "  + carTimings.getCarsSlow() + " > " + currentBestCarSlow);
                            improvementBestSlow = improvementSlow;
                            improvementBestStop = improvementStop;
                            currentBestCarSlow = carTimings.getCarsSlow();
                            currentBestCarStop = carTimings.getCarStopped();
                            movsim = carTimings.getMovsim();
                            isSet = true;
                        }
                    }
                }
                System.out.println("COMPLETED HILL CLIMB");
            } while (x < max_times);
        } catch (IOException e) {
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

    private void setModelParameters (int index, VehiclePrototypeConfiguration vehiclePrototypeConfiguration) {
        ModelParameterACC modelParameterACC = vehiclePrototypeConfiguration.getAccelerationModelType().getModelParameterACC();
        ModelParameterMOBIL modelParameterMOBIL = vehiclePrototypeConfiguration.getLaneChangeModelType().getModelParameterMOBIL();
        switch (index) {
            case 0:
                modelParameterACC.setT(modelParameterACC.getT() + 0.2);
                break;
            case 1:
                modelParameterACC.setT(modelParameterACC.getT() - 0.4);
                break;
            case 2:
                modelParameterACC.setT(modelParameterACC.getT() + 0.2);
                modelParameterACC.setS0(modelParameterACC.getS0() + 0.5);
                break;
            case 3:
                modelParameterACC.setS0(modelParameterACC.getS0() - 1);
                break;
            case 4:
                modelParameterACC.setS0(modelParameterACC.getS0() + 0.5);
                modelParameterACC.setS1(modelParameterACC.getS1() + 0.5);
                break;
            case 5:
                modelParameterACC.setS1(modelParameterACC.getS1() - 1);
                break;
            case 6:
                modelParameterACC.setS1(modelParameterACC.getS1() + 0.5);
                modelParameterACC.setDelta(modelParameterACC.getDelta() + 0.5);
                break;
            case 7:
                modelParameterACC.setDelta(modelParameterACC.getDelta() - 1);
                break;
            case 8:
                modelParameterACC.setDelta(modelParameterACC.getDelta() + 0.5);
                modelParameterACC.setA(modelParameterACC.getA() + 0.2);
                break;
            case 9:
                modelParameterACC.setA(modelParameterACC.getA() - 0.4);
                break;
            case 10:
                modelParameterACC.setA(modelParameterACC.getA() + 0.2);
                modelParameterACC.setB(modelParameterACC.getB() + 0.2);
                break;
            case 11:
                modelParameterACC.setB(modelParameterACC.getB() - 0.4);
                break;
            case 12:
                modelParameterACC.setB(modelParameterACC.getB() + 0.2);
                modelParameterACC.setCoolness(modelParameterACC.getCoolness() + 0.1);
                break;
            case 13:
                modelParameterACC.setCoolness(modelParameterACC.getCoolness() - 0.2);
                break;
            case 14:
                modelParameterACC.setCoolness(modelParameterACC.getCoolness() + 0.1);
                modelParameterMOBIL.setSafeDeceleration(modelParameterMOBIL.getSafeDeceleration() + 0.5);
                break;
            case 15:
                modelParameterMOBIL.setSafeDeceleration(modelParameterMOBIL.getSafeDeceleration() - 1);
                break;
            case 16:
                modelParameterMOBIL.setSafeDeceleration(modelParameterMOBIL.getSafeDeceleration() + 0.5);
                modelParameterMOBIL.setMinimumGap(modelParameterMOBIL.getMinimumGap() + 0.5);
                break;
            case 17:
                modelParameterMOBIL.setMinimumGap(modelParameterMOBIL.getMinimumGap() - 1);
                break;
            case 18:
                modelParameterMOBIL.setMinimumGap(modelParameterMOBIL.getMinimumGap() + 0.5);
                modelParameterMOBIL.setThresholdAcceleration(modelParameterMOBIL.getThresholdAcceleration() + 0.05);
                break;
            case 19:
                modelParameterMOBIL.setThresholdAcceleration(modelParameterMOBIL.getThresholdAcceleration() - 0.1);
                break;
            case 20:
                modelParameterMOBIL.setThresholdAcceleration(modelParameterMOBIL.getThresholdAcceleration() + 0.05);
                modelParameterMOBIL.setRightBiasAcceleration(modelParameterMOBIL.getRightBiasAcceleration() + 0.05);
                break;
            case 21:
                modelParameterMOBIL.setRightBiasAcceleration(modelParameterMOBIL.getRightBiasAcceleration() - 0.1);
                break;
            case 22:
                modelParameterMOBIL.setRightBiasAcceleration(modelParameterMOBIL.getRightBiasAcceleration() + 0.05);
                modelParameterMOBIL.setPoliteness(modelParameterMOBIL.getPoliteness() + 0.1);
                break;
            case 23:
                modelParameterMOBIL.setPoliteness(modelParameterMOBIL.getPoliteness() - 0.2);
                break;
        }
    }

    private class CarTimings {
        private int carStopped = 0;
        private int carsSlow = 0;
        private Movsim movsim = null;
        private ModelParameterACC modelParameterACC = null;
        private ModelParameterMOBIL modelParameterMOBIL = null;

        public CarTimings(Movsim movsim){
            this.movsim = movsim;
            ModelParameterACC modelACC = new ModelParameterACC();
            ModelParameterMOBIL modelMOBIL = new ModelParameterMOBIL();
            getACC();
            getMobil();
            modelACC.setT(this.modelParameterACC.getT());
            modelACC.setS0(this.modelParameterACC.getS0());
            modelACC.setS1(this.modelParameterACC.getS1());
            modelACC.setDelta(this.modelParameterACC.getDelta());
            modelACC.setA(this.modelParameterACC.getA());
            modelACC.setB(this.modelParameterACC.getB());
            modelACC.setCoolness(this.modelParameterACC.getCoolness());
            modelMOBIL.setMinimumGap(this.modelParameterMOBIL.getMinimumGap());
            modelMOBIL.setThresholdAcceleration(this.modelParameterMOBIL.getThresholdAcceleration());
            modelMOBIL.setRightBiasAcceleration(this.modelParameterMOBIL.getRightBiasAcceleration());
            modelMOBIL.setPoliteness(this.modelParameterMOBIL.getPoliteness());

            modelParameterACC = modelACC;
            modelParameterMOBIL = modelMOBIL;
        }

        public int getCarStopped() {
            return carStopped;
        }

        public void setCarStopped(int carStopped) {
            this.carStopped = carStopped;
        }

        public int getCarsSlow() {
            return carsSlow;
        }

        public void setCarsSlow(int carsSlow) {
            this.carsSlow = carsSlow;
        }

        @Override
        public String toString() {
            return getACC().getT() + "," +
                    getACC().getS0() + "," +
                    getACC().getS1() + "," +
                    getACC().getDelta() + "," +
                    getACC().getA() + "," +
                    getACC().getB() + "," +
                    getACC().getCoolness() + "," +
                    getMobil().getSafeDeceleration() + "," +
                    getMobil().getMinimumGap() + "," +
                    getMobil().getThresholdAcceleration() + "," +
                    getMobil().getRightBiasAcceleration() + "," +
                    getMobil().getPoliteness() + "," +
                    carStopped + "," +
                    carsSlow + "\n";
        }

        private ModelParameterACC getACC() {
            if (modelParameterACC == null) {
                try {
                    modelParameterACC = movsim.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0).getAccelerationModelType().getModelParameterACC();
                } catch (NullPointerException e) {
                    try {
                        Thread.sleep(10);
                        modelParameterACC = movsim.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0).getAccelerationModelType().getModelParameterACC();
                    } catch (NullPointerException ex) {
                        try {
                            Thread.sleep(10);
                            modelParameterACC = movsim.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0).getAccelerationModelType().getModelParameterACC();
                        } catch (NullPointerException ec) {
                            System.out.println("We're too deep");
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return modelParameterACC;
        }

        private ModelParameterMOBIL getMobil() {
            if (modelParameterMOBIL == null) {
                try {
                    modelParameterMOBIL = movsim.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0).getLaneChangeModelType().getModelParameterMOBIL();
                } catch (NullPointerException e) {
                    try {
                        Thread.sleep(10);
                        modelParameterMOBIL = movsim.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0).getLaneChangeModelType().getModelParameterMOBIL();
                    } catch (NullPointerException ex) {
                        try {
                            Thread.sleep(10);
                            modelParameterMOBIL = movsim.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0).getLaneChangeModelType().getModelParameterMOBIL();
                        } catch (NullPointerException ec) {
                            System.out.println("We're too deep");
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return modelParameterMOBIL;
        }

        public Movsim getMovsim() {
            ModelParameterACC modelACC = movsim.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0).getAccelerationModelType().getModelParameterACC();
            ModelParameterMOBIL modelMOBIL = movsim.getVehiclePrototypes().getVehiclePrototypeConfiguration().get(0).getLaneChangeModelType().getModelParameterMOBIL();

            modelACC.setT(this.modelParameterACC.getT());
            modelACC.setS0(this.modelParameterACC.getS0());
            modelACC.setS1(this.modelParameterACC.getS1());
            modelACC.setDelta(this.modelParameterACC.getDelta());
            modelACC.setA(this.modelParameterACC.getA());
            modelACC.setB(this.modelParameterACC.getB());
            modelACC.setCoolness(this.modelParameterACC.getCoolness());
            modelMOBIL.setMinimumGap(this.modelParameterMOBIL.getMinimumGap());
            modelMOBIL.setThresholdAcceleration(this.modelParameterMOBIL.getThresholdAcceleration());
            modelMOBIL.setRightBiasAcceleration(this.modelParameterMOBIL.getRightBiasAcceleration());
            modelMOBIL.setPoliteness(this.modelParameterMOBIL.getPoliteness());

            return movsim;
        }

        public void setMovsim(Movsim movsim) {
            this.movsim = movsim;
        }
    }
}
