package model;

import expection.ValidationException;
import util.SimulationTime;

public final class Flight {

    private final String departureAirport, arrivalAirport;
    private final SimulationTime departureTime;
    private final int duration;

    public Flight(String departureAirport, String arrivalAirport, SimulationTime departureTime, int duration) throws ValidationException {
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;

        if(departureAirport == null || departureAirport.trim().isEmpty()) {
            throw new ValidationException("Departure airport must be entered.");
        }
        if(arrivalAirport == null || arrivalAirport.trim().isEmpty()) {
            throw new ValidationException("Arrival airport must be entered.");
        }

        if(this.departureAirport.equals(this.arrivalAirport)) {
            throw new ValidationException("Departure and arrival airports must be different.");
        }
        if(departureTime == null) {
            throw new ValidationException("Departure time not registered, expected in HH:MM format.");
        }
        this.departureTime = departureTime;
        if (duration <= 0) {
            throw new ValidationException("Duration of the flight needs to be a positive integer. (got: " + duration + ").");
        }
        this.duration = duration;
    }

    public String getDepartureAirport() { return departureAirport; }
    public String getArrivalAirport() { return arrivalAirport; }
    public SimulationTime getDepartureTime() { return departureTime; }
    public int getFlightDuration() { return duration; }
    public SimulationTime getArrivalTime()  { return departureTime.addTime(duration); }

    @Override
    public String toString() {
        return "[" + departureAirport + "] -> [" + arrivalAirport + "] at " + departureTime + " (" + duration + " min)";
    }
}