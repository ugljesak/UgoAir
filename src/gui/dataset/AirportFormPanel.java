package gui.dataset;

import exception.ValidationException;
import model.Airport;

import javax.swing.*;
import java.awt.*;

public class AirportFormPanel extends JPanel {

    private final JTextField nameField = new JTextField(16);
    private final JTextField codeField = new JTextField(5);
    private final JTextField xField = new JTextField(6);
    private final JTextField yField = new JTextField(6);
    private final JButton addButton = new JButton("Add Airport");

    public AirportFormPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("New Airport"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        add(new JLabel("Name:"), c);
        c.gridx = 1; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL;
        add(nameField, c);

        c.gridwidth = 1; c.fill = GridBagConstraints.NONE;
        c.gridx = 0; c.gridy = 1;
        add(new JLabel("Code:"), c);
        c.gridx = 1;
        add(codeField, c);

        c.gridx = 0; c.gridy = 2;
        add(new JLabel("X [" + Airport.X_MIN + ", " + Airport.X_MAX + "]:"), c);
        c.gridx = 1;
        add(xField, c);
        c.gridx = 2;
        add(new JLabel("Y [" + Airport.Y_MIN + ", " + Airport.Y_MAX + "]:"), c);
        c.gridx = 3;
        add(yField, c);

        c.gridx = 3; c.gridy = 3; c.anchor = GridBagConstraints.EAST;
        add(addButton, c);
    }

    public JButton getAddButton() {
        return addButton;
    }

    public Airport readAirport() throws ValidationException {
        String name = nameField.getText();
        String code = codeField.getText();
        int x = parseCoordinate(xField.getText(), 'X');
        int y = parseCoordinate(yField.getText(), 'Y');
        return new Airport(name, code, x, y);
    }

    private int parseCoordinate(String text, char axis) throws ValidationException {
        String t = text.trim();
        if (t.isEmpty()) {
            throw new ValidationException(axis + " coordinate cannot be empty.");
        }
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            throw new ValidationException(axis + " coordinate must be integer, found '" + t + "'.");
        }
    }

    public void clearFields() {
        nameField.setText("");
        codeField.setText("");
        xField.setText("");
        yField.setText("");
        nameField.requestFocusInWindow();
    }
}