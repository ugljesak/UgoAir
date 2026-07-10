package simulation;

import model.Airport;
import model.Flight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightScheduler {
    public static final int SLOT_MINUTES = 10;

    private FlightScheduler() {}

    public static List<ScheduledFlight> schedule(List<Flight> flights, Map<String, Airport> map) {

        List<Flight> sorted = new ArrayList<Flight>(flights);
        sorted.sort((a, b) -> Integer.compare(a.getDepartureTime().getTotalMinutes(), b.getDepartureTime().getTotalMinutes()));

        Map<String, Integer> lastDeparture = new HashMap<String, Integer>();

        List<ScheduledFlight> result = new ArrayList<ScheduledFlight>();
        for (Flight f : sorted) {
            String code = f.getDepartureAirport();
            int desired = f.getDepartureTime().getTotalMinutes();

            Integer last = lastDeparture.get(code);
            int actual = (last == null) ? desired : Math.max(desired, last + SLOT_MINUTES);

            lastDeparture.put(code, actual);

            Airport from = map.get(f.getDepartureAirport());
            Airport to = map.get(f.getArrivalAirport());
            result.add(new ScheduledFlight(f, from, to, actual));
        }

        result.sort((a, b) -> Integer.compare(a.getActualDepartureTime(), b.getActualDepartureTime()));
        return result;
    }
}
