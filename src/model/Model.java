package model;

import exception.ValidationException;
import util.Listeners;

import java.util.*;

public class Model {

    private final List<Airport> airports = new ArrayList<>();
    private final Map<String, Airport> map = new LinkedHashMap<>();

    private final List<Flight> flights = new ArrayList<>();

    private final Listeners<ModelListener> listeners = new Listeners<ModelListener>();


    public void addListener(ModelListener l) { listeners.add(l); }
    public void removeListener(ModelListener l) { listeners.remove(l); }
    private void fireChanged() {
        //listeners.fire(l -> l.modelChanged());
        listeners.fire(ModelListener::modelChanged);
    }

    // ========================================================================================
    // --- Airports ---
    // ========================================================================================

    public synchronized void addAirport(Airport airport) throws ValidationException {
        Airport existing = map.get(airport.getCode());
        if (existing != null) {
            throw new ValidationException("Airport [" + airport.getCode() + "] already exists ('"
                            + existing.getName() + "'). Code must be unique.");
        }
        airports.add(airport);
        map.put(airport.getCode(), airport);

        fireChanged();
    }

    public synchronized void removeAirport(String code) throws ValidationException {
        Airport a = map.get(code);
        if (a == null) {
            throw new ValidationException("Airport [" + code + "] doesn't exists in table.");
        }

        flights.removeIf(f -> f.getDepartureAirport().equals(code) || f.getArrivalAirport().equals(code));

        airports.remove(a);
        map.remove(code);
        fireChanged();
    }

    public synchronized Airport findAirport(String code) {
        return map.get(code);
    }

    public synchronized List<Airport> getAirports() {
        return List.copyOf(airports);
    }

    public synchronized Map<String, Airport> getMap() {
        return Map.copyOf(map);
    }

    // ========================================================================================
    // --- Flights ---
    // ========================================================================================

    public synchronized void addFlight(Flight flight) throws ValidationException {

        if (!map.containsKey(flight.getDepartureAirport())) {
            throw new ValidationException("Departure airport [" + flight.getDepartureAirport() + "] doesn't exist in table.");
        }
        if (!map.containsKey(flight.getArrivalAirport())) {
            throw new ValidationException("Arrival airport [" + flight.getArrivalAirport() + "] doesn't exist in table.");
        }
        flights.add(flight);

        fireChanged();
    }

    public synchronized void removeFlight(int index) throws ValidationException {
        if (index < 0 || index >= flights.size()) {
            throw new ValidationException("Flight doesn't exist in table anymore.");
        }
        flights.remove(index);

        fireChanged();
    }

    public synchronized List<Flight> getFlights() {
        return List.copyOf(flights);
    }

    public synchronized void replaceAll(List<Airport> newAirports, List<Flight> newFlights) throws ValidationException {
        Map<String, Airport> newMap = new LinkedHashMap<String, Airport>();
        for (Airport a : newAirports) {
            if (newMap.put(a.getCode(), a) != null) {
                throw new ValidationException("Loaded file contains multiple usage of airport code [" + a.getCode() + "]. Codes must be unique.");
            }
        }
        for (Flight f : newFlights) {
            if (!newMap.containsKey(f.getDepartureAirport()) || !newMap.containsKey(f.getArrivalAirport())) {
                throw new ValidationException("Loaded file contains airport code which is not in table [" + f.getDepartureAirport() + "].");
            }
        }

        airports.clear();
        airports.addAll(newAirports);
        map.clear();
        map.putAll(newMap);
        flights.clear();
        flights.addAll(newFlights);

        fireChanged();
    }

    public synchronized boolean isEmpty() {
        return airports.isEmpty() && flights.isEmpty();
    }
}
