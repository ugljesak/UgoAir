package gui.dataset;


import exception.ValidationException;
import model.Airport;
import model.Flight;
import model.Model;
import model.ModelListener;
import util.SimulationTime;

import javax.swing.*;
import java.awt.*;

public class FlightFormPanel extends JPanel implements ModelListener {

    private final Model model;
    private final JComboBox<String> fromCombo = new JComboBox<String>();
    private final JComboBox<String> toCombo = new JComboBox<String>();
    private final JTextField timeField = new JTextField(5);
    private final JTextField durationField = new JTextField(5);
    private final JButton addButton = new JButton("Add Flight");

    public FlightFormPanel(Model model) {
        super(new GridBagLayout());
        this.model = model;
        setBorder(BorderFactory.createTitledBorder("New Flight"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("From:"), c);
        c.gridx = 1;
        add(fromCombo, c);
        c.gridx = 2;
        add(new JLabel("To:"), c);
        c.gridx = 3;
        add(toCombo, c);

        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Departure (HH:MM):"), c);
        c.gridx = 1;
        add(timeField, c);
        c.gridx = 2;
        add(new JLabel("Duration (min):"), c);
        c.gridx = 3;
        add(durationField, c);

        c.gridx = 3;
        c.gridy = 2;
        c.anchor = GridBagConstraints.EAST;
        add(addButton, c);

        model.addListener(this);
        refreshCombos();
    }

    @Override
    public void modelChanged() {
        refreshCombos();
    }

    private void refreshCombos() {
        String fromSel = (String)fromCombo.getSelectedItem();
        String toSel = (String)toCombo.getSelectedItem();
        fromCombo.removeAllItems();
        toCombo.removeAllItems();
        for (Airport a : model.getAirports()) {
            fromCombo.addItem(a.getCode());
            toCombo.addItem(a.getCode());
        }
        if (fromSel != null) fromCombo.setSelectedItem(fromSel);
        if (toSel != null) toCombo.setSelectedItem(toSel);
    }

    public JButton getAddButton() {
        return addButton;
    }

    public Flight readFlight() throws ValidationException {
        String from = (String)fromCombo.getSelectedItem();
        String to = (String)toCombo.getSelectedItem();
        if (from == null || to == null) {
            throw new ValidationException("No airports have been selected.");
        }
        SimulationTime time = SimulationTime.parseFromText(timeField.getText());
        String d = durationField.getText().trim();
        if(d.isEmpty()) {
            throw new ValidationException("Duration of the flight must be entered.");
        }
        int duration;
        try {
            duration = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            throw new ValidationException("Duration of the flight must be positive integer (number of minutes), found '" + d + "'.");
        }
        return new Flight(from, to, time, duration);
    }

    public void clearFields() {
        timeField.setText("");
        durationField.setText("");
        timeField.requestFocusInWindow();
    }
}


