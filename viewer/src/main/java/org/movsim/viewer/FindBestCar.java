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
public class FindBestCar extends TimingRunnable {

    public FindBestCar(AppFrame appFrame) {
        super(appFrame);
    }




    private  CarTimings carTimings;

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
            writer = new FileWriter("test11.csv");
            writer.append("T,s0,s1,delta,a,b,coolness (max 1),safe_deceleration,minimum_gap,threshold_acceleration,right_bias_acceleration,politeness,Time till first stopped car,Time till traffic slows before on ramp\n");

            int max_times = 50;
            int x = 0;

            boolean isSet = false;
            Movsim movsim = null;

            do {
                VehiclePrototypeConfiguration vehiclePrototypeConfiguration;
                x++;
                List<CarTimings> timings = new ArrayList<>();
                carTimings = new CarTimings(appFrame.getMovsimInput());
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
                        timings = runSimulation(timings,movsim,i);
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

                Collections.shuffle(timings);
                for (CarTimings carTimings : timings) {
                    if (carTimings != null) {
                        if ((carTimings.getCarsSlow() + carTimings.getCarStopped()) > (currentBestCarSlow + currentBestCarStop)){
                            System.out.println("LONGER TO STOP " + carTimings.getCarStopped() + " > " + currentBestCarStop);
                            System.out.println("LONGER TO SLOW "  + carTimings.getCarsSlow() + " > " + currentBestCarSlow);
                            currentBestCarStop = carTimings.getCarStopped();
                            currentBestCarSlow = carTimings.getCarsSlow();
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
                modelParameterACC.setT(carTimings.getACC().getT() + 0.2);
                break;
            case 1:
                modelParameterACC.setT(carTimings.getACC().getT() - 0.2);
                break;
            case 2:
                modelParameterACC.setT(carTimings.getACC().getT());
                modelParameterACC.setS0(carTimings.getACC().getS0() + 0.5);
                break;
            case 3:
                modelParameterACC.setS0(carTimings.getACC().getS0() - 0.5);
                break;
            case 4:
                modelParameterACC.setS0(carTimings.getACC().getS0());
                modelParameterACC.setS1(carTimings.getACC().getS1() + 0.5);
                break;
            case 5:
                modelParameterACC.setS1(carTimings.getACC().getS1() - 0.5);
                break;
            case 6:
                modelParameterACC.setS1(carTimings.getACC().getS1());
                modelParameterACC.setDelta(carTimings.getACC().getDelta() + 0.5);
                break;
            case 7:
                modelParameterACC.setDelta(carTimings.getACC().getDelta() - 0.5);
                break;
            case 8:
                modelParameterACC.setDelta(carTimings.getACC().getDelta());
                modelParameterACC.setA(carTimings.getACC().getA() + 0.4);
                break;
            case 9:
                modelParameterACC.setA(carTimings.getACC().getA() - 0.4);
                break;
            case 10:
                modelParameterACC.setA(carTimings.getACC().getA());
                modelParameterACC.setB(carTimings.getACC().getB() + 0.4);
                break;
            case 11:
                modelParameterACC.setB(carTimings.getACC().getB() - 0.4);
                break;
            case 12:
                modelParameterACC.setB(carTimings.getACC().getB());
                modelParameterACC.setCoolness(carTimings.getACC().getCoolness() + 0.1);
                break;
            case 13:
                modelParameterACC.setCoolness(carTimings.getACC().getCoolness() - 0.1);
                break;
            case 14:
                modelParameterACC.setCoolness(carTimings.getACC().getCoolness());
                modelParameterMOBIL.setSafeDeceleration(carTimings.getMobil().getSafeDeceleration() + 1);
                break;
            case 15:
                modelParameterMOBIL.setSafeDeceleration(carTimings.getMobil().getSafeDeceleration() - 1);
                break;
            case 16:
                modelParameterMOBIL.setSafeDeceleration(carTimings.getMobil().getSafeDeceleration());
                modelParameterMOBIL.setMinimumGap(carTimings.getMobil().getMinimumGap() + 0.5);
                break;
            case 17:
                modelParameterMOBIL.setMinimumGap(carTimings.getMobil().getMinimumGap() - 0.5);
                break;
            case 18:
                modelParameterMOBIL.setMinimumGap(carTimings.getMobil().getMinimumGap());
                modelParameterMOBIL.setThresholdAcceleration(carTimings.getMobil().getThresholdAcceleration() + 0.05);
                break;
            case 19:
                modelParameterMOBIL.setThresholdAcceleration(carTimings.getMobil().getThresholdAcceleration() - 0.05);
                break;
            case 20:
                modelParameterMOBIL.setThresholdAcceleration(carTimings.getMobil().getThresholdAcceleration());
                modelParameterMOBIL.setRightBiasAcceleration(carTimings.getMobil().getRightBiasAcceleration() + 0.05);
                break;
            case 21:
                modelParameterMOBIL.setRightBiasAcceleration(carTimings.getMobil().getRightBiasAcceleration() - 0.05);
                break;
            case 22:
                modelParameterMOBIL.setRightBiasAcceleration(carTimings.getMobil().getRightBiasAcceleration());
                modelParameterMOBIL.setPoliteness(carTimings.getMobil().getPoliteness() + 0.1);
                break;
            case 23:
                modelParameterMOBIL.setPoliteness(carTimings.getMobil().getPoliteness() - 0.1);
                break;
        }
    }

    @Override
    public void printValues(Simulator simulator) {

    }
}
