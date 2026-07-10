package gui.map;

import exception.SimulationException;
import model.Model;
import simulation.Engine;
import simulation.SimulationState;
import simulation.Snapshot;
import gui.Dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimulationPanel extends JPanel {
    private final Engine engine;
    private final Model model;

    private final JButton startButton = new JButton("Start");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton resetButton = new JButton("Reset");

    private final JLabel clockLabel = new JLabel();
    private final JLabel countLabel = new JLabel();

    private final Timer timer;

    public SimulationPanel(Engine engine, Model model) {
        super(new FlowLayout(FlowLayout.LEFT, 10, 6));
        this.engine = engine;
        this.model = model;

        setBorder(BorderFactory.createTitledBorder("Simulation"));

        add(startButton);
        add(pauseButton);
        add(resetButton);
        add(Box.createHorizontalStrut(16));
        add(clockLabel);
        add(Box.createHorizontalStrut(16));
        add(countLabel);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (engine.getSimulationState() == SimulationState.PAUSED) {
                        engine.resumeSimulation();
                    } else {
                        engine.startSimulation(model);
                    }
                } catch (SimulationException ex) {
                    Dialog.error(SimulationPanel.this, ex.getMessage());
                }
                refresh();
            }
        });
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(engine.getSimulationState() == SimulationState.RUNNING) {
                    engine.pauseSimulation();
                }
                else if(engine.getSimulationState() == SimulationState.PAUSED) {
                    engine.resumeSimulation();
                }
                refresh();
            }
        });
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                engine.resetSimulation();
                refresh();
            }
        });

        timer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        refresh();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    private void refresh() {
        Snapshot snap = engine.getSnapshot();
        SimulationState state = engine.getSimulationState();

        int totalMinutes = (int) snap.getSimMinutes();
        clockLabel.setText(String.format("Simulation time: %02d:%02d",
                (totalMinutes / 60) % 24, totalMinutes % 60));
        countLabel.setText("In air: " + snap.getInAirCount()
                + "   Arrived: " + snap.getLandedCount() + "/" + snap.getTotalCount());

        startButton.setText(state == SimulationState.PAUSED ? "Resume" : "Start");
        startButton.setEnabled(state == SimulationState.READY || state == SimulationState.PAUSED);
        pauseButton.setEnabled(state == SimulationState.RUNNING);
        resetButton.setEnabled(state != SimulationState.READY);
    }
}