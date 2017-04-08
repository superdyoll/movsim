package org.movsim.viewer;

import org.movsim.autogen.ModelParameterACC;
import org.movsim.autogen.ModelParameterMOBIL;
import org.movsim.autogen.Movsim;
import org.movsim.simulator.Simulator;
import org.movsim.viewer.ui.AppFrame;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Created by Lloyd on 16/03/2017.
 */
public abstract class TimingRunnable implements Runnable {

    Thread runner;
    AppFrame appFrame;

    public TimingRunnable (AppFrame appFrame) {
        this.appFrame = appFrame;
        this.runner = new Thread(this);
        this.runner.start();
    }

    protected final static int MAX_TIME_TO_RUN = 18000;

    protected List<CarTimings> runSimulation(List<CarTimings> timings, Movsim movsim, int index) throws JAXBException, SAXException, InterruptedException {
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
        timings.add(new FindBestCar.CarTimings(movsim));
        do {
            Thread.sleep(10);
            cumulativeTime += 10;
            try {
                if (simulator.getRoadNetwork().findById(2).getStoppedVehicleCount() > 0 && carStopped == false) {
                    System.out.println("CAR STOPPED");
                    carStopped = true;
                    timings.get(index).setCarStopped(cumulativeTime);
                }
                if (simulator.getRoadNetwork().findById(1).getSlowestSpeed() < 15 && trafficSlowed == false) {
                    System.out.println("ROAD SLOWED");
                    trafficSlowed = true;
                    timings.get(index).setCarsSlow(cumulativeTime);
                }
                if (simulator.getRoadNetwork().findById(1).getObstacleCount() > 0){
                    crashed = true;
                    System.out.println("CRASHED CRASHED CRASHED");
                    timings.get(index).setCarsSlow(0);
                    timings.get(index).setCarStopped(0);
                }
            } catch (NullPointerException|ConcurrentModificationException e){
                System.out.println("Another error caught");
            }

        } while (whileLoopBoolean(cumulativeTime,carStopped,trafficSlowed,crashed));
        if (carStopped == false) {
            timings.get(index).setCarStopped(cumulativeTime);
        }
        if (trafficSlowed == false) {
            timings.get(index).setCarsSlow(cumulativeTime);
        }
        simulator.getSimulationRunnable().stop();
        printValues(simulator);
        return timings;
    }

    public abstract void printValues(Simulator simulator);

    public abstract boolean whileLoopBoolean(int cumulativeTime, boolean carStopped, boolean trafficSlowed, boolean crashed);

    protected class CarTimings {
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
            modelMOBIL.setSafeDeceleration(this.modelParameterMOBIL.getSafeDeceleration());
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

        public ModelParameterACC getACC() {
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

        public ModelParameterMOBIL getMobil() {
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
