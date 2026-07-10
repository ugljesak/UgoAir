package gui.dataset;

import model.Airport;
import model.Model;
import model.ModelListener;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class AirportTableModel extends AbstractTableModel implements ModelListener {

    private static final String[] COLUMNS = { "Code", "Name", "X", "Y" };
    private final Model model;
    private List<Airport> data;

    public AirportTableModel(Model model) {
        this.model = model;
        this.data = model.getAirports();
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
        Airport a = data.get(rowIndex);
        switch (columnIndex) {
            case 0:  return a.getCode();
            case 1:  return a.getName();
            case 2:  return a.getX();
            case 3:  return a.getY();
            default: return "";
        }
    }

    @Override
    public void modelChanged() {
        this.data = this.model.getAirports();
        fireTableDataChanged();
    }
}
