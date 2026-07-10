package gui.dataset;

import model.Flight;
import model.Model;
import model.ModelListener;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class FlightTableModel extends AbstractTableModel implements ModelListener {

    private static final String[] COLUMNS = { "From", "To", "Departure", "Duration" };
    private final Model model;
    private List<Flight> data;

    public FlightTableModel(Model model) {
        this.model = model;
        this.data = model.getFlights();
        model.addListener(this);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Flight a = data.get(rowIndex);
        switch (columnIndex) {
            case 0:  return a.getDepartureAirport();
            case 1:  return a.getArrivalAirport();
            case 2:  return a.getDepartureTime();
            case 3:  return a.getFlightDuration();
            default: return "";
        }
    }

    @Override
    public void modelChanged() {
        this.data = this.model.getFlights();
        fireTableDataChanged();
    }
}
