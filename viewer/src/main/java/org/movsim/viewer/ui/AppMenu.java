/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */

package org.movsim.viewer.ui;

import org.movsim.simulator.Simulator;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.CCS;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.CCS.Waves;
import org.movsim.utilities.FileUtils;
import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.util.SwingHelper;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.EventObject;
import java.util.Properties;
import java.util.ResourceBundle;

@SuppressWarnings({"synthetic-access", "serial"})
public class AppMenu extends MovSimMenuBase {
    private static final long serialVersionUID = -1741830983719200790L;
    private final Simulator simulator;
    private final AppFrame frame;
    private final Properties properties;

    /**
     * Folder where the building blocks simulations are stored.
     */
    private static final String BUILDING_BLOCKS_FOLDER = "sim/buildingBlocks/";

    /**
     * Folder where the games simulations are stored.
     */
    private static final String GAMES_FOLDER = "sim/games/";

    /**
     * Folder where the example simulations are stored.
     */
    private static final String EXAMPLE_FOLDER = "sim/examples/";

    public AppMenu(AppFrame mainFrame, Simulator simulator, CanvasPanel canvasPanel,
                   TrafficCanvas trafficCanvas, ResourceBundle resourceBundle, Properties properties) {
        super(canvasPanel, trafficCanvas, resourceBundle, simulator);
        this.frame = mainFrame;
        this.simulator = simulator;
        this.properties = properties;
    }

    public void initMenus() {
        final JMenuBar menuBar = new JMenuBar();

        menuBar.add(fileMenu());
        menuBar.add(scenarioMenu());
        menuBar.add(modelMenu());
        menuBar.add(outputMenu());
        menuBar.add(viewMenu());
        menuBar.add(helpMenu());

        frame.setJMenuBar(menuBar);
    }

    private JMenu fileMenu() {
        final JMenu menuFile = new JMenu(resourceString("FileMenu")); //$NON-NLS-1$

        menuFile.add(new JMenuItem(new OpenAction(resourceString("FileMenuOpen"))));

        menuFile.add(new JMenuItem(new AbstractAction(resourceString("FileMenuPreferences")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handlePreferences(actionEvent);
            }
        })).setEnabled(false);

        menuFile.addSeparator();

        menuFile.add(new AbstractAction(resourceString("FileMenuExit")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleQuit(actionEvent);
            }
        });

        return menuFile;
    }

    private JMenu outputMenu() {
        final JMenu outputMenu = new JMenu(resourceString("OutputMenu"));

        outputMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("TravelTime")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleTravelTimeDiagram(actionEvent);
            }
        })).setEnabled(true);
        outputMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("Detectors")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleDetectorsDiagram(actionEvent);
            }
        })).setEnabled(false);
        outputMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("FloatingCars")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleFloatingCarsDiagram(actionEvent);
            }
        })).setEnabled(false);
        outputMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("SpatioTemporal")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleSpatioTemporalDiagram(actionEvent);
            }
        })).setEnabled(false);
        outputMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("Consumption")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleFuelConsumptionDiagram(actionEvent);
            }
        })).setEnabled(false);
        outputMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("MeanSpeed")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleMeanSpeedDiagram(actionEvent);
            }
        })).setEnabled(true);

        return outputMenu;
    }

    private JMenu scenarioMenu() {
        final JMenu scenarioMenu = new JMenu(resourceString("ScenarioMenu"));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("OnRamp")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("onramp", BUILDING_BLOCKS_FOLDER);
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("OffRamp")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("offramp", BUILDING_BLOCKS_FOLDER);
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("FlowConservingBottleNeck")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("flow_conserving_bottleneck", BUILDING_BLOCKS_FOLDER);
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("SpeedLimitOUTDATED")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("speedlimit", BUILDING_BLOCKS_FOLDER);
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("TrafficLight")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("trafficlight", BUILDING_BLOCKS_FOLDER);
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("LaneClosing")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("laneclosure", BUILDING_BLOCKS_FOLDER);
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("CloverLeaf")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("cloverleaf", BUILDING_BLOCKS_FOLDER);
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("RoundAbout")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        })).setEnabled(false);

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("CityInterSection")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.notImplemented(canvasPanel);
            }
        })).setEnabled(false);

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("RingRoad")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("ringroad_1lane", BUILDING_BLOCKS_FOLDER);
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("RingRoad2Lanes")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("ringroad_2lanes", BUILDING_BLOCKS_FOLDER);
                uiDefaultReset();
            }
        }));

        scenarioMenu.addSeparator();

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("GameRampMetering")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("ramp_metering", GAMES_FOLDER);
                trafficCanvas.setVehicleColorMode(TrafficCanvas.VehicleColorMode.EXIT_COLOR);
                uiDefaultReset();
            }
        }));

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("GameRouting")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("routing", GAMES_FOLDER);
                trafficCanvas.setVehicleColorMode(TrafficCanvas.VehicleColorMode.EXIT_COLOR);
                uiDefaultReset();
            }
        }));

        scenarioMenu.addSeparator();

        scenarioMenu.add(new JMenuItem(new AbstractAction(resourceString("Vasaloppet")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                trafficCanvas.setupTrafficScenario("vasa_CCS", EXAMPLE_FOLDER);
                uiDefaultReset();
                CCS.setWave(Waves.FOURWAVES);
            }
        }));

        return scenarioMenu;
    }

    private JMenu viewMenu() {
        final JMenu viewMenu = new JMenu(resourceString("ViewMenu"));

        final JMenu colorVehiclesMenu = new JMenu(resourceString("VehicleColors"));

        final JMenuItem menuItemVehicleColorSpeedDependant = new JMenuItem(resourceString("VehicleColorSpeedDependant"));
        final JMenuItem menuItemVehicleColorRandom = new JMenuItem(resourceString("VehicleColorRandom"));
        colorVehiclesMenu.add(menuItemVehicleColorSpeedDependant);
        colorVehiclesMenu.add(menuItemVehicleColorRandom);
        menuItemVehicleColorSpeedDependant.setEnabled(false);
        menuItemVehicleColorRandom.setEnabled(false);
        viewMenu.add(colorVehiclesMenu);

        viewMenu.addSeparator();

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("LogOutput")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleLogOutput(actionEvent);
            }
        }));

        viewMenu.addSeparator();

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawRoadIds")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleDrawRoadIds(actionEvent);
            }
        })).setSelected(trafficCanvas.isDrawRoadId());

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawSources")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleDrawSources(actionEvent);
            }
        })).setSelected(trafficCanvas.isDrawSources());

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawSinks")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleDrawSinks(actionEvent);
            }
        })).setSelected(trafficCanvas.isDrawSinks());

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawSpeedLimits")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleDrawSpeedLimits(actionEvent);
            }
        })).setSelected(trafficCanvas.isDrawSpeedLimits());

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawFlowConservingBottleNecks")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            }
        })).setEnabled(false);

        viewMenu.addSeparator();

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawRoutesTravelTime")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            }
        })).setEnabled(false);

        viewMenu.add(new JCheckBoxMenuItem(new AbstractAction(resourceString("DrawRoutesSpatioTemporal")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            }
        })).setEnabled(false);

        return viewMenu;
    }

    private JMenu modelMenu() {
        final JMenu modelMenu = new JMenu(resourceString("ModelMenu"));
        modelMenu.add(new JMenuItem(new AbstractAction(resourceString("ModelMenuViewParams")) {//$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SwingHelper.notImplemented(canvasPanel);
            }
        })).setEnabled(false);
        return modelMenu;
    }

    private void handlePreferences(EventObject event) {
        ViewerPreferences viewerPreferences = new ViewerPreferences(resourceBundle);
    }

    private void handleQuit(EventObject event) {
        canvasPanel.quit();
        frame.dispose();
        System.exit(0); // also kills all existing threads
    }

    private void uiDefaultReset() {
        trafficCanvas.setVmaxForColorSpectrum(Double.parseDouble(properties.getProperty("vmaxForColorSpectrum")));
        frame.statusPanel.setWithProgressBar(true);
        frame.statusPanel.reset();
        trafficCanvas.start();
    }

    // --------------------------------------------------------------------------------------
    class OpenAction extends AbstractAction {

        public OpenAction(String title) {
            super(title);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String cwd = ""; //$NON-NLS-1$
            try {
                cwd = new java.io.File(".").getCanonicalPath(); //$NON-NLS-1$
            } catch (final java.io.IOException ignored) {
            }
            // System.out.println("cwd = " + cwd); //$NON-NLS-1$
            final File path = new File(cwd);
            final JFileChooser fileChooser = new JFileChooser(path);
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().endsWith(".xprj"); //$NON-NLS-1$
                }

                @Override
                public String getDescription() {
                    return "XPRJ files";
                }
            });
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            final int ret = fileChooser.showOpenDialog(canvasPanel);
            if (ret == JFileChooser.APPROVE_OPTION) {
                final File file = fileChooser.getSelectedFile();
                if (file != null && file.isFile()) {
                    // if the user has selected a file, then load it
                    try {
                        simulator.loadScenarioFromXml(FileUtils.getProjectName(file),
                                FileUtils.getCanonicalPathWithoutFilename(file));
                    } catch (JAXBException | SAXException e) {
                        throw new IllegalArgumentException(e.toString());
                    }
                    uiDefaultReset();
                    trafficCanvas.forceRepaintBackground();
                }
            }
        }
    }
}
