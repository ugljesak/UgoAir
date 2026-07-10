package data;

import exception.DataFormatException;
import exception.ValidationException;
import model.Airport;
import model.Flight;
import util.SimulationTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataLoaderCSV extends DataLoader {
    private static final String SECTION_AIRPORTS = "# AIRPORTS";
    private static final String SECTION_FLIGHTS = "# FLIGHTS";
    private static final String HEADER_AIRPORTS = "CODE,NAME,X,Y";
    private static final String HEADER_FLIGHTS = "FROM,TO,DEPARTURE,DURATION";

    private static final class Line {
        final int number;
        final String text;
        Line(int number, String text) {
            this.number = number;
            this.text = text;
        }
    }

    @Override
    protected Dataset parse(String content, String filename) throws DataFormatException {
        List<Line> lines = new ArrayList<>();
        String[] raw = content.split("\r\n|\r|\n", -1);

        for (int i = 0; i < raw.length; i++) {
            String t = raw[i].trim();
            if (!t.isEmpty()) {
                lines.add(new Line(i + 1, t));
            }
        }

        if (lines.isEmpty()) {
            throw new DataFormatException("File '" + filename + "' is empty.");
        }

        int idx = 0;
        Line first = lines.get(idx);
        if (!first.text.equalsIgnoreCase(SECTION_AIRPORTS)) {
            throw new DataFormatException("Line " + first.number + ": expected section '" +
                    SECTION_AIRPORTS + "', but found '" + first.text + "'.");
        }
        idx++;

        if (idx >= lines.size() || !lines.get(idx).text.equalsIgnoreCase(HEADER_AIRPORTS)) {
            String found = idx < lines.size() ? lines.get(idx).text : "EOF";
            throw new DataFormatException("Expected airport header context '" +
                    HEADER_AIRPORTS + "', but found '" + found + "'.");
        }
        idx++;

        // --- Parsing airports ---
        List<Airport> airports = new ArrayList<Airport>();
        Map<String, Integer> map = new LinkedHashMap<>();
        boolean flightsFound = false;
        while (idx < lines.size()) {
            Line ln = lines.get(idx);
            if (ln.text.equalsIgnoreCase(SECTION_FLIGHTS)) {
                flightsFound = true;
                idx++;
                break;
            }
            if (ln.text.startsWith("#")) {
                throw new DataFormatException("Line " + ln.number + ": unexpected section '" + ln.text + "'.");
            }
            airports.add(parseAirport(ln, map));
            idx++;
        }
        if (!flightsFound) {
            throw new DataFormatException("File doesn't contain section '" + SECTION_FLIGHTS + "'.");
        }

        if (idx >= lines.size() || !lines.get(idx).text.equalsIgnoreCase(HEADER_FLIGHTS)) {
            String found = idx < lines.size() ? lines.get(idx).text : "EOF";
            throw new DataFormatException("Expected flight header context '" +
                    HEADER_FLIGHTS + "', but found '" + found + "'.");
        }
        idx++;

        // --- Parsing flights ---
        List<Flight> flights = new ArrayList<>();
        while (idx < lines.size()) {
            Line ln = lines.get(idx);
            if (ln.text.startsWith("#")) {
                throw new DataFormatException("Line " + ln.number + ": unexpected section '" + ln.text + "'.");
            }
            flights.add(parseFlight(ln, map));
            idx++;
        }

        return new Dataset(airports, flights);
    }

    private Airport parseAirport(Line ln, Map<String, Integer> map) throws DataFormatException {
        String[] p = ln.text.split(",", -1);
        if (p.length != 4) {
            throw new DataFormatException("Line " + ln.number + ": expected 4 values (" +
                    HEADER_AIRPORTS + "), found " + p.length + ".");
        }
        Airport airport = getAirport(ln.number, p);
        Integer prev = map.put(airport.getCode(), ln.number);
        if (prev != null) {
            throw new DataFormatException("Line " + ln.number + ": airport code '" + airport.getCode() +
                    "' already exists and defined at '" + prev + "'.");
        }
        return airport;
    }

    private static Airport getAirport(int lineNo, String[] p) throws DataFormatException {
        String code = p[0].trim();
        String name = p[1].trim();
        int x, y;
        try {
             x = Integer.parseInt(p[2].trim());
        } catch (NumberFormatException e) {
            throw new DataFormatException("Line " + lineNo + ": X coordinate must be integer, found '" + p[2].trim() + "').", e);
        }
        try {
            y = Integer.parseInt(p[3].trim());
        } catch (NumberFormatException e) {
            throw new DataFormatException("Line " + lineNo + ": Y coordinate must be integer, found '" + p[3].trim() + "'.", e);
        }

        Airport airport;
        try {
            airport = new Airport(code, name, x, y);
        } catch (ValidationException e) {
            throw new DataFormatException("Line " + lineNo + ": " + e.getMessage(), e);
        }
        return airport;
    }

    private Flight parseFlight(Line ln, Map<String, Integer> map) throws DataFormatException {
        String[] p = ln.text.split(",", -1);
        if (p.length != 4) {
            throw new DataFormatException("Line " + ln.number + ": expected 4 values (" +
                    HEADER_FLIGHTS + "), found " + p.length + ".");
        }
        String from = p[0].trim();
        String to = p[1].trim();
        SimulationTime departure;
        try {
            departure = SimulationTime.parseFromText(p[2].trim());
        } catch (ValidationException e) {
            throw new DataFormatException("Line " + ln.number + ": " + e.getMessage(), e);
        }
        int duration;
        try {
            duration = Integer.parseInt(p[3].trim());
        } catch (NumberFormatException e) {
            throw new DataFormatException("Line " + ln.number + ": flight duration must be integer representing minutes " +
                    ", found '" + p[3].trim() + "').", e);
        }

        Flight flight;
        try {
            flight = new Flight(from, to, departure, duration);
        } catch (ValidationException e) {
            throw new DataFormatException("Line " + ln.number + ": " + e.getMessage(), e);
        }

        if (!map.containsKey(flight.getDepartureAirport())) {
            throw new DataFormatException("Line " + ln.number + ": departure airport for flight doesn't exist in file '" +
                    flight.getDepartureAirport() + "'.");
        }
        if (!map.containsKey(flight.getArrivalAirport())) {
            throw new DataFormatException("Line " + ln.number + ": arrival airport for flight doesn't exist in file '" +
                    flight.getArrivalAirport() + "'.");
        }

        return flight;
    }

    @Override
    protected String serialize(Dataset data) {
        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();

        sb.append(SECTION_AIRPORTS).append(nl);
        sb.append(HEADER_AIRPORTS).append(nl);
        for (Airport a : data.getAirports()) {
            sb.append(a.getCode()).append(',')
                    .append(a.getName()).append(',')
                    .append(a.getX()).append(',')
                    .append(a.getY()).append(nl);
        }

        sb.append(SECTION_FLIGHTS).append(nl);
        sb.append(HEADER_FLIGHTS).append(nl);
        for (Flight f : data.getFlights()) {
            sb.append(f.getDepartureAirport()).append(',')
                    .append(f.getArrivalAirport()).append(',')
                    .append(f.getDepartureTime()).append(',')
                    .append(f.getFlightDuration()).append(nl);
        }

        return sb.toString();
    }

    @Override
    public String getFormatName() {
        return "CSV";
    }
}
