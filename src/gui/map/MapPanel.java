package gui.map;

import model.Airport;
import model.Model;
import model.ModelListener;
import model.SelectionListener;
import util.InactivityMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MapPanel extends JPanel implements ModelListener {

    private static final int SQUARE_HALF = 7;
    private static final int PLANE_DIAMETER = 11;
    private static final int HIT_TOLERANCE = 9;
    private static final int MARGIN = 34;

    private static final Color GRID_COLOR = new Color(235, 235, 235);
    private static final Color AXIS_COLOR = new Color(205, 205, 205);
    private static final Color AIRPORT_FILL = new Color(150, 150, 150);
    private static final Color AIRPORT_BORDER = new Color(80, 80, 80);
    private static final Color SELECTED_FILL = new Color(210, 30, 30);
    private static final Color PLANE_FILL = new Color(35, 90, 205);
    private static final Color PLANE_BORDER = new Color(15, 45, 120);
    private static final Color LABEL_COLOR = new Color(40, 40, 40);

    private final Model model;
    private final InactivityMonitor inactivityMonitor;
    private final AirportFilterModel visibility;

    private String selectedCode;
    private int tick;
    private final Timer refreshTimer;
    private SelectionListener selectionListener;

    public MapPanel(Model model, AirportFilterModel visibility, InactivityMonitor inactivityMonitor) {
        this.model = model;
        this.visibility = visibility;
        this.inactivityMonitor = inactivityMonitor;

        setBackground(Color.WHITE);
        model.addListener(this);
        visibility.addChangeListener(new Runnable() {
            @Override
            public void run() {
                if (selectedCode != null && !visibility.isVisible(selectedCode)) {
                    clearSelection();
                }
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Airport hit = findAirportAt(e.getX(), e.getY());
                if (hit == null) {
                    return;
                }
                if (hit.getCode().equals(selectedCode)) {
                    clearSelection();
                }
                else {
                    selectedCode = hit.getCode();
                    if (selectionListener != null) {
                        selectionListener.selectionChanged(hit);
                    }
                }
                repaint();
            }
        });

        refreshTimer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick++;
                if (selectedCode != null) {
                    inactivityMonitor.registerActivity();
                }
                repaint();
            }
        });
    }

    private Airport findAirportAt(int x, int y) {
        int w = getWidth();
        int h = getHeight();
        for (Airport a : model.getAirports()) {
            if (!visibility.isVisible(a.getCode())) {
                continue;
            }
            int ax = toPaintX(a.getX(), w);
            int ay = toPaintY(a.getY(), h);
            if (Math.abs(x - ax) <= HIT_TOLERANCE && Math.abs(y - ay) <= HIT_TOLERANCE) {
                return a;
            }
        }
        return null;
    }

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        refreshTimer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        refreshTimer.stop();
    }

    private int toPaintX(int x, int width) {
        double scale = (double)(width - 2 * MARGIN) / (double)(Airport.X_MAX - Airport.X_MIN);
        return (int)Math.round(MARGIN + (double)(x - Airport.X_MIN) * scale);
    }

    private int toPaintY(int y, int height) {
        double scale = (double)(height - 2 * MARGIN) / (double)(Airport.Y_MAX - Airport.Y_MIN);
        return (int)Math.round(height - MARGIN - (double)(y - Airport.Y_MIN) * scale);
    }


    @Override
    public void modelChanged() {
        if (selectedCode != null && model.findAirport(selectedCode) == null) {
            clearSelection();
        }
        repaint();
    }

    private void clearSelection() {
        selectedCode = null;
        if (selectionListener != null) {
            selectionListener.selectionChanged(null);
        }
    }

    // ------------------------------------------------------------------
    // --- Painting ---
    // ------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        paintGrid(g2, w, h);
        paintAirports(g2, w, h);
        // TODO: implementirati ovo
        paintPlanes(g2, w, h);
        paintLegend(g2, h);

        g2.dispose();
    }

    private void paintGrid(Graphics2D g2, int width, int height) {
        for (int x = Airport.X_MIN; x <= Airport.X_MAX; x += 30) {
            g2.setColor(x == 0 ? AXIS_COLOR : GRID_COLOR);
            int px = toPaintX(x, width);
            g2.drawLine(px, MARGIN, px, height - MARGIN);
        }
        for (int y = Airport.Y_MIN; y <= Airport.Y_MAX; y += 30) {
            g2.setColor(y == 0 ? AXIS_COLOR : GRID_COLOR);
            int py = toPaintY(y, height);
            g2.drawLine(MARGIN, py, width - MARGIN, py);
        }
        g2.setColor(AXIS_COLOR);
        g2.drawRect(MARGIN, MARGIN, width - 2 * MARGIN, height - 2 * MARGIN);
    }

    private void paintAirports(Graphics2D g2, int width, int height) {

        boolean blinkOn = (tick / 2) % 2 == 0;
        g2.setFont(getFont().deriveFont(Font.BOLD, 11f));

        for(Airport a : model.getAirports()) {
            if(!visibility.isVisible(a.getCode())) continue;
            int x = toPaintX(a.getX(), width);
            int y = toPaintY(a.getY(), height);
            boolean isSelected = a.getCode().equals(selectedCode);

            g2.setColor(isSelected && blinkOn ? SELECTED_FILL : AIRPORT_FILL);
            g2.fillRect(x - SQUARE_HALF, y - SQUARE_HALF,
                    2 * SQUARE_HALF, 2 * SQUARE_HALF);
            g2.setColor(isSelected ? SELECTED_FILL.darker() : AIRPORT_BORDER);
            g2.drawRect(x - SQUARE_HALF, y - SQUARE_HALF,
                    2 * SQUARE_HALF, 2 * SQUARE_HALF);

            g2.setColor(isSelected ? SELECTED_FILL.darker() : LABEL_COLOR);
            g2.drawString(a.getCode(), x + SQUARE_HALF + 3, y - SQUARE_HALF - 1);
        }
    }

    private void paintPlanes(Graphics2D g2, int width, int height) {
        int r = PLANE_DIAMETER / 2;

    }

    private void paintLegend(Graphics2D g2, int h) {
        int y = h - 14;
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));

        g2.setColor(AIRPORT_FILL);
        g2.fillRect(MARGIN, y - 9, 10, 10);
        g2.setColor(AIRPORT_BORDER);
        g2.drawRect(MARGIN, y - 9, 10, 10);
        g2.setColor(LABEL_COLOR);
        g2.drawString("Airport", MARGIN + 15, y);

        int x2 = MARGIN + 95;
        g2.setColor(PLANE_FILL);
        g2.fillOval(x2, y - 9, 10, 10);
        g2.setColor(PLANE_BORDER);
        g2.drawOval(x2, y - 9, 10, 10);
        g2.setColor(LABEL_COLOR);
        g2.drawString("Plane", x2 + 15, y);

        g2.setColor(new Color(120, 120, 120));
        g2.setStroke(new BasicStroke(1f));
    }
}
