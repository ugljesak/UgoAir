package test;

import exception.ValidationException;
import model.Airport;
import model.Flight;
import model.Model;
import util.SimulationTime;

import java.util.List;

public class testModel {

    static int passed = 0, failed = 0;

    public static void main(String[] args) throws Exception {

        // ------------------------------------------------------------
        // 1) Osnovno dodavanje aerodroma + duplikat kod
        // ------------------------------------------------------------
        Model m = new Model();
        Airport beg = new Airport("Belgrade Nikola Tesla", "BEG", 10, 45);
        Airport lhr = new Airport("London Heathrow", "LHR", 0, 51);
        Airport jfk = new Airport("John F Kennedy International", "JFK", -37, 41);

        m.addAirport(beg);
        m.addAirport(lhr);
        m.addAirport(jfk);
        check(m.getAirports().size() == 3, "dodata 3 aerodroma");

        boolean duplicateRejected = false;
        try {
            m.addAirport(new Airport("Beograd duplikat", "BEG", 1, 1));
        } catch (ValidationException e) {
            duplicateRejected = true;
        }
        check(duplicateRejected, "duplikat koda BEG odbijen");
        check(m.getAirports().size() == 3, "broj aerodroma nepromenjen posle odbijenog duplikata");

        // ------------------------------------------------------------
        // 2) getAirports() vraca KOPIJU, ne internu listu
        // ------------------------------------------------------------
        List<Airport> snapshot = m.getAirports();
        boolean listImmutableOrCopy;
        try {
            snapshot.add(new Airport("Hakovan aerodrom", "XXX", 0, 0));
            // ako dodje dovde bez izuzetka, mora da NIJE uticalo na model
            listImmutableOrCopy = m.getAirports().size() == 3;
        } catch (UnsupportedOperationException e) {
            // List.copyOf() vraca nemenjivu listu - i ovo je ispravno ponasanje
            listImmutableOrCopy = true;
        }
        check(listImmutableOrCopy, "getAirports() ne dozvoljava izmenu internog stanja");

        // ------------------------------------------------------------
        // 3) findAirport
        // ------------------------------------------------------------
        check(m.findAirport("BEG") != null, "findAirport pronalazi BEG");
        check(m.findAirport("ZZZ") == null, "findAirport vraca null za nepostojeci kod");

        // ------------------------------------------------------------
        // 4) Dodavanje letova - validne i nevalidne reference
        // ------------------------------------------------------------
        Flight begJfk = new Flight("BEG", "JFK", SimulationTime.parseFromText("00:30"), 420);
        Flight begLhr = new Flight("BEG", "LHR", SimulationTime.parseFromText("17:10"), 170);
        Flight jfkLhr = new Flight("JFK", "LHR", SimulationTime.parseFromText("09:00"), 90);

        m.addFlight(begJfk);
        m.addFlight(begLhr);
        m.addFlight(jfkLhr);
        check(m.getFlights().size() == 3, "dodata 3 leta");

        boolean badFromRejected = false;
        try {
            m.addFlight(new Flight("ZZZ", "LHR", SimulationTime.parseFromText("10:00"), 60));
        } catch (ValidationException e) {
            badFromRejected = true;
        }
        check(badFromRejected, "let sa nepostojecim polaznim aerodromom odbijen");

        boolean badToRejected = false;
        try {
            m.addFlight(new Flight("BEG", "ZZZ", SimulationTime.parseFromText("10:00"), 60));
        } catch (ValidationException e) {
            badToRejected = true;
        }
        check(badToRejected, "let sa nepostojecim dolaznim aerodromom odbijen");
        check(m.getFlights().size() == 3, "broj letova nepromenjen posle odbijenih pokusaja");

        // ------------------------------------------------------------
        // 5) removeFlight - validan i nevalidan indeks
        // ------------------------------------------------------------
        boolean badIndexRejected = false;
        try {
            m.removeFlight(99);
        } catch (ValidationException e) {
            badIndexRejected = true;
        }
        check(badIndexRejected, "removeFlight sa nepostojecim indeksom odbijen");

        m.removeFlight(2); // brise jfkLhr (poslednji dodat, index 2)
        check(m.getFlights().size() == 2, "removeFlight ispravno smanjio listu na 2");

        // vrati jfkLhr radi sledeceg testa (cascade brisanje)
        m.addFlight(jfkLhr);
        check(m.getFlights().size() == 3, "jfkLhr vracen, opet 3 leta");

        // ------------------------------------------------------------
        // 6) removeAirport - CASCADE brisanje povezanih letova
        //    BEG je from/to u 2 leta (begJfk, begLhr); JFK-LHR ga ne koristi
        // ------------------------------------------------------------
        m.removeAirport("BEG");
        check(m.findAirport("BEG") == null, "BEG obrisan iz aerodroma");
        check(m.getAirports().size() == 2, "ostala 2 aerodroma (LHR, JFK)");
        check(m.getFlights().size() == 1, "cascade obrisao 2 leta vezana za BEG, ostao samo JFK->LHR");
        check(m.getFlights().get(0).getDepartureAirport().equals("JFK"),
                "preostali let je bas JFK->LHR (nepovezan sa BEG)");

        boolean removeMissingRejected = false;
        try {
            m.removeAirport("BEG"); // vec obrisan
        } catch (ValidationException e) {
            removeMissingRejected = true;
        }
        check(removeMissingRejected, "removeAirport nepostojeceg koda odbijen");

        // ------------------------------------------------------------
        // 7) replaceAll - transakciono ponasanje (ispravna verzija)
        // ------------------------------------------------------------
        Model m3 = new Model();
        m3.addAirport(new Airport("Postojeci", "PST", 1, 1));
        check(m3.getAirports().size() == 1, "m3 ima 1 aerodrom pre pokusaja replaceAll");

        // (a) Duplikat koda u NOVIM podacima -> mora biti odbijeno, STARO stanje netaknuto
        List<Airport> dupAirports = List.of(
                new Airport("A", "AAA", 0, 0),
                new Airport("B duplikat", "AAA", 1, 1)
        );
        boolean dupRejected = false;
        try {
            m3.replaceAll(dupAirports, List.of());
        } catch (ValidationException e) {
            dupRejected = true;
        }
        check(dupRejected, "replaceAll odbija duplikat koda u novim podacima");
        check(m3.getAirports().size() == 1 && m3.findAirport("PST") != null,
                "staro stanje netaknuto posle odbijenog replaceAll (duplikat)");

        // (b) Let referise aerodrom koji NIJE u novoj listi -> odbijeno, staro stanje netaknuto
        List<Airport> okAirports = List.of(new Airport("A", "AAA", 0, 0));
        List<Flight> badFlights = List.of(
                new Flight("AAA", "ZZZ", SimulationTime.parseFromText("10:00"), 60)
        );
        boolean refRejected = false;
        try {
            m3.replaceAll(okAirports, badFlights);
        } catch (ValidationException e) {
            refRejected = true;
        }
        check(refRejected, "replaceAll odbija let koji referise nepostojeci aerodrom");
        check(m3.getAirports().size() == 1 && m3.findAirport("PST") != null,
                "staro stanje netaknuto posle odbijenog replaceAll (los ref)");

        // (c) Ispravan replaceAll -> mora da prodje i potpuno zameni stanje
        List<Airport> newAirports = List.of(
                new Airport("A", "AAA", 0, 0),
                new Airport("B", "BBB", 10, 10)
        );
        List<Flight> newFlights = List.of(
                new Flight("AAA", "BBB", SimulationTime.parseFromText("08:00"), 45)
        );
        m3.replaceAll(newAirports, newFlights);
        check(m3.getAirports().size() == 2, "replaceAll uspesno zamenio aerodrome (2 nova)");
        check(m3.getFlights().size() == 1, "replaceAll uspesno zamenio letove (1 novi)");
        check(m3.findAirport("PST") == null, "stari aerodrom PST vise ne postoji posle uspesnog replaceAll");

        // ------------------------------------------------------------
        // 8) isEmpty
        // ------------------------------------------------------------
        Model m4 = new Model();
        check(m4.isEmpty(), "prazan model prijavljuje isEmpty() == true");
        m4.addAirport(new Airport("X", "XXX", 0, 0));
        check(!m4.isEmpty(), "model sa jednim aerodromom prijavljuje isEmpty() == false");
    }

    static void check(boolean cond, String name) {
        if (cond) { passed++; System.out.println("PASS  " + name); }
        else      { failed++; System.out.println("FAIL  " + name); }
    }
}
