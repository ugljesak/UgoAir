package gui;

import data.DataLoader;
import data.DataLoaderCSV;
import data.Dataset;
import exception.AppException;
import gui.dataset.*;
import gui.map.AirportFilterModel;
import gui.map.AirportFilterPanel;
import gui.map.MapPanel;
import model.Airport;
import model.Flight;
import model.Model;
import util.InactivityListener;
import util.InactivityMonitor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;

public class MainFrame extends JFrame {

    private final Model model = new Model();

    private final InactivityMonitor inactivityMonitor = new InactivityMonitor(new InactivityListener() {
        @Override
        public void inactivityWarningTick(int secondsLeft) {
            System.out.println("[InactivityMonitor] Warning: " + secondsLeft + "s left.");
        }
        @Override
        public void inactivityWarningCancelled() {
            System.out.println("[InactivityMonitor] Warning cancelled.");
        }
        @Override
        public void inactivityTimeout() {
            System.out.println("[InactivityMonitor] TIMEOUT: terminating program.");
            System.exit(0);
        }
    });

    private final AirportFormPanel airportForm = new AirportFormPanel();
    private final FlightFormPanel flightForm = new FlightFormPanel(model);

    private final AirportTableModel airportTableModel = new AirportTableModel(model);
    private final FlightTableModel flightTableModel = new FlightTableModel(model);
    private final JTable airportTable = new JTable(airportTableModel);
    private final JTable flightTable = new JTable(flightTableModel);

    private final JLabel statusLabel = new JLabel(" ");

    private final AirportFilterModel visibility = new AirportFilterModel(model);
    private final MapPanel mapPanel = new MapPanel(model, visibility, inactivityMonitor);
    private final AirportFilterPanel filterPanel = new AirportFilterPanel(model, visibility);

    public MainFrame() {
        super("UgoAir - Simulator of flight traffic.");
        inactivityMonitor.start();

        airportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        airportTable.getTableHeader().setReorderingAllowed(false);
        flightTable.getTableHeader().setReorderingAllowed(false);

        JPanel dataPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dataPanel.add(buildHalf(airportForm, airportTable, "Delete Airport", e -> onDeleteAirport()));
        dataPanel.add(buildHalf(flightForm, flightTable, "Delete Flight", e -> onDeleteFlight()));

        airportForm.getAddButton().addActionListener(e -> onAddAirport());
        flightForm.getAddButton().addActionListener(e -> onAddFlight());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(dataPanel, BorderLayout.CENTER);
        getContentPane().add(buildStatusBar(), BorderLayout.SOUTH);
        setJMenuBar(buildMenuBar());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null);

        JPanel mapTab = new JPanel(new BorderLayout(8, 8));
        mapTab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mapTab.add(mapPanel, BorderLayout.CENTER);
        mapTab.add(filterPanel, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Data", dataPanel);
        tabs.addTab("Map", mapTab);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(buildStatusBar(), BorderLayout.SOUTH);
    }


    private JPanel buildHalf(JPanel form, JTable table, String deleteLabel, java.awt.event.ActionListener onDelete) {
        JPanel half = new JPanel(new BorderLayout(0, 8));
        half.add(form, BorderLayout.NORTH);
        half.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton deleteButton = new JButton(deleteLabel);
        deleteButton.addActionListener(onDelete);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(deleteButton);
        half.add(south, BorderLayout.SOUTH);
        return half;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        panel.add(statusLabel, BorderLayout.CENTER);
        return panel;
    }

    private JMenuBar buildMenuBar() {
        JMenu fileMenu = new JMenu("File");

        JMenuItem loadItem = new JMenuItem("Load");
        loadItem.addActionListener(e -> onLoad());

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> onSave());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenuBar bar = new JMenuBar();
        bar.add(fileMenu);
        return bar;
    }

    // ------------------------------------------------------------
    //  Actions on data
    // ------------------------------------------------------------
    private void onAddAirport() {
        try {
            Airport a = airportForm.readAirport();
            model.addAirport(a);
            airportForm.clearFields();
            setStatus("Added airport " + a.getCode() + " - " + a.getName() + ".");
        } catch (AppException ex) {
            Dialog.error(this, ex.getMessage());
        }
    }

    private void onAddFlight() {
        try {
            Flight f = flightForm.readFlight();
            model.addFlight(f);
            flightForm.clearFields();
            setStatus("Added flight " + f.getDepartureAirport() + " -> " + f.getArrivalAirport() + ".");
        } catch (AppException ex) {
            Dialog.error(this, ex.getMessage());
        }
    }

    private void onDeleteAirport() {
        int row = airportTable.getSelectedRow();
        if (row < 0) {
            Dialog.info(this, "Please select airport from table which you want to delete.");
            return;
        }
        Airport a = model.getAirports().get(row);
        if (!Dialog.confirm(this, "Deleted airport " + a.getCode() + "? "
                + "This will remove all flights associated with this airport.")) {
            return;
        }
        try {
            model.removeAirport(a.getCode());
            setStatus("Airport removed " + a.getCode() + ".");
        } catch (AppException ex) {
            Dialog.error(this, ex.getMessage());
        }
    }

    private void onDeleteFlight() {
        int row = flightTable.getSelectedRow();
        if (row < 0) {
            Dialog.info(this, "Please select flight from table which you want to delete.");
            return;
        }
        if (!Dialog.confirm(this, "Delete selected flight?")) {
            return;
        }
        try {
            model.removeFlight(row);
            setStatus("Flight removed.");
        } catch (AppException ex) {
            Dialog.error(this, ex.getMessage());
        }
    }

    // ------------------------------------------------------------
    //  Fajl - ucitaj/sacuvaj
    // ------------------------------------------------------------
    private JFileChooser createFileChooser() {
        File dataDir = new File("data");
        JFileChooser fc = new JFileChooser(dataDir.isDirectory() ? dataDir : new File("."));
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(new FileNameExtensionFilter("CSV file (*.csv, *.CSV)", "csv", "CSV"));
        return fc;
    }

    private void onLoad() {
        JFileChooser fc = createFileChooser();
        fc.setDialogTitle("Loading data");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        try {
            DataLoader loader = new DataLoaderCSV();
            Dataset data = loader.load(file);
            model.replaceAll(data.getAirports(), data.getFlights());
            setStatus("Loaded " + data.getAirports().size() + " airports and "
                    + data.getFlights().size() + " flights from '" + file.getName() + "'.");
        } catch (AppException ex) {
            Dialog.error(this, ex.getMessage());
            setStatus("Loading from file failed.");
        }
    }

    private void onSave() {
        JFileChooser fc = createFileChooser();
        fc.setDialogTitle("Saving data");
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        if (file.exists() && !Dialog.confirm(this, "File '" + file.getName() + "' already exists. Do you want to overwrite it?")) {
            return;
        }
        try {
            DataLoader loader = new DataLoaderCSV();
            loader.save(file, new Dataset(model.getAirports(), model.getFlights()));
            setStatus("Saved in '" + file.getName() + "'.");
        } catch (AppException ex) {
            Dialog.error(this, ex.getMessage());
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}