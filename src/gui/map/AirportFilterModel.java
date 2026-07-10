package gui.map;

import model.Airport;
import model.Model;
import model.ModelListener;
import util.Listeners;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AirportFilterModel implements ModelListener {

    private final Model model;
    private Map<String, Boolean> visible = new LinkedHashMap<>();

    private final Listeners<Runnable> listeners = new Listeners<Runnable>();


    public AirportFilterModel(Model model) {
        this.model = model;
        model.addListener(this);
        refreshVisibility();
    }

    private void refreshVisibility() {
        Map<String, Boolean> fresh = new LinkedHashMap<String, Boolean>();
        for (Airport a : model.getAirports()) {
            Boolean old = visible.get(a.getCode());
            fresh.put(a.getCode(), old == null ? Boolean.TRUE : old);
        }
        visible = fresh;
    }

    public boolean isVisible(String code) {
        Boolean v = visible.get(code);
        if(v == null) return true;
        return v;
    }

    public void setVisible(String code, boolean value) {
        Boolean old = visible.get(code);
        if (old != null && old != value) {
            visible.put(code, value);
            listeners.fire(new Consumer<Runnable>() {
                @Override
                public void accept(Runnable runnable) {
                    runnable.run();
                }
            });
        }
    }

    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    @Override
    public void modelChanged() {
        refreshVisibility();
        listeners.fire(new Consumer<Runnable>() {
            @Override
            public void accept(Runnable runnable) {
                runnable.run();
            }
        });
    }
}
