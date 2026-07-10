package simulation;

import model.Airport;
import model.Flight;

public final class ScheduledFlight {

    private final Flight flight;
    private final Airport from, to;
    private final int actualDepartureMin;

    public ScheduledFlight(Flight flight, Airport from, Airport to, int actualDepartureMin) {
        this.flight = flight;
        this.from = from;
        this.to = to;
        this.actualDepartureMin = actualDepartureMin;
    }

    public Flight getFlight() { return flight; }

    public Airport getFrom() { return from; }
    public Airport getTo() { return to; }

    public int getActualDepartureTime() { return actualDepartureMin; }
    public int getScheduledDepartureTime() { return flight.getDepartureTime().getTotalMinutes(); }

    public boolean isDelayed() { return actualDepartureMin != getScheduledDepartureTime(); }

    @Override
    public String toString() {
        return flight.getDepartureAirport() + " -> " + flight.getArrivalAirport()
                + " (wanted " + flight.getDepartureTime() + ", got "
                + actualDepartureMin/60 + ":" + actualDepartureMin%60 + " min)";
    }
}