package data;

import model.Airport;
import model.Flight;

import java.util.List;

public final class Dataset {

    private final List<Airport> airports;
    private final List<Flight> flights;

    public Dataset(List<Airport> airports, List<Flight> flights) {
        this.airports = List.copyOf(airports);
        this.flights = List.copyOf(flights);
    }

    public List<Airport> getAirports() { return airports; }
    public List<Flight> getFlights() { return flights; }
}
