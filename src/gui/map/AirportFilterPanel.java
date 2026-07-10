package gui.map;

import model.Airport;
import model.Model;
import model.ModelListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class AirportFilterPanel extends JPanel implements ModelListener {
    private final Model model;
    private final AirportFilterModel visibility;
    private final JPanel listPanel = new JPanel();

    public AirportFilterPanel(Model model, AirportFilterModel visibility) {
        super(new BorderLayout());
        this.model = model;
        this.visibility = visibility;

        setBorder(BorderFactory.createTitledBorder("Airports"));
        setPreferredSize(new Dimension(250, 10));

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(listPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        model.addListener(this);
        rebuild();
    }

    private void rebuild() {
        listPanel.removeAll();
        for (Airport a : model.getAirports()) {
            final String code = a.getCode();
            String label = code + " - " + a.getName()
                    + " (" + a.getX() + ", " + a.getY() + ")";
            final JCheckBox cb = new JCheckBox(label, visibility.isVisible(code));
            cb.setToolTipText("Show/Hide airport " + code + " on map.");
            cb.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    visibility.setVisible(code, cb.isSelected());
                }
            });
            listPanel.add(cb);
        }
        listPanel.add(Box.createVerticalGlue());
        listPanel.revalidate();
        listPanel.repaint();
    }

    @Override
    public void modelChanged() {
        rebuild();
    }
}
