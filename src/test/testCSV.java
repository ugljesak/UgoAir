package test;

import data.DataLoaderCSV;
import data.Dataset;
import exception.AppException;
import exception.DataFormatException;
import model.Airport;
import model.Flight;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class testCSV {

    static int passed = 0, failed = 0;

    public static void main(String[] args) throws Exception {

        DataLoaderCSV loader = new DataLoaderCSV();
        File testFile = new File("data/test.csv");

        // 1) Osnovno parsiranje
        Dataset ds = loader.load(testFile);
        check(ds.getAirports().size() == 16, "ucitano 16 aerodroma");
        check(ds.getFlights().size() == 27, "ucitano 27 letova");

        // 2) Konkretan aerodrom - BEG - tacne vrednosti svih polja
        Airport beg = findAirport(ds, "BEG");
        check(beg != null, "BEG pronadjen u ucitanim aerodromima");
        check(beg != null && beg.getName().equals("Belgrade Nikola Tesla"),
                "BEG ima tacan naziv 'Belgrade Nikola Tesla'");
        check(beg != null && beg.getX() == 10, "BEG ima tacnu X koordinatu (10)");
        check(beg != null && beg.getY() == 45, "BEG ima tacnu Y koordinatu (45)");

        // Prvi red u fajlu - najbolji test za eventualni swap code/name
        Airport lhr = findAirport(ds, "LHR");
        check(lhr != null, "LHR pronadjen");
        check(lhr != null && lhr.getCode().equals("LHR"), "LHR.getCode() vraca 'LHR' (ne naziv)");
        check(lhr != null && lhr.getName().equals("London Heathrow"),
                "LHR.getName() vraca 'London Heathrow' (ne kod)");

        // 3) Konkretan let - BEG->JFK u 00:30, 420 min
        Flight begJfk = findFlight(ds, "BEG", "JFK");
        check(begJfk != null, "let BEG->JFK pronadjen");
        check(begJfk != null && begJfk.getDepartureTime().getTotalMinutes() == 30,
                "let BEG->JFK polazi u 00:30 (30 minuta)");
        check(begJfk != null && begJfk.getFlightDuration() == 420,
                "let BEG->JFK traje 420 minuta");

        // 4) Round-trip: parse -> serialize -> mora biti IDENTICAN tekst
        String serialized = callSerialize(loader, ds);
        String original = normalize(readFile(testFile));
        String roundTrip = normalize(serialized);
        check(original.equals(roundTrip), "round-trip: serijalizovan tekst identican originalu");
        if (!original.equals(roundTrip)) {
            printFirstDiff(original, roundTrip);
        }

        File tmp = File.createTempFile("roundtrip", ".csv");
        writeFile(tmp, serialized);
        Dataset ds2 = loader.load(tmp);
        check(ds2.getAirports().size() == 16, "round-trip ucitavanje: opet 16 aerodroma");
        check(ds2.getFlights().size() == 27, "round-trip ucitavanje: opet 27 letova");
        tmp.delete();

        // 5) Neispravni fajlovi - svaki mora baciti DataFormatException
        checkRejects("prazan fajl", "");
        checkRejects("pogresna prva sekcija", "# WHATEVER\nCODE,NAME,X,Y\n");
        checkRejects("pogresno zaglavlje kolona (airports)",
                "# AIRPORTS\nSIFRA,IME,X,Y\nBEG,Beograd,0,0\n# FLIGHTS\nFROM,TO,DEPARTURE,DURATION\n");
        checkRejects("nedostaje sekcija FLIGHTS",
                "# AIRPORTS\nCODE,NAME,X,Y\nBEG,Beograd,0,0\n");
        checkRejects("premalo kolona u redu aerodroma",
                "# AIRPORTS\nCODE,NAME,X,Y\nBEG,Beograd,0\n# FLIGHTS\nFROM,TO,DEPARTURE,DURATION\n");
        checkRejects("X koordinata nije broj",
                "# AIRPORTS\nCODE,NAME,X,Y\nBEG,Beograd,abc,0\n# FLIGHTS\nFROM,TO,DEPARTURE,DURATION\n");
        checkRejects("X koordinata van opsega",
                "# AIRPORTS\nCODE,NAME,X,Y\nBEG,Beograd,999,0\n# FLIGHTS\nFROM,TO,DEPARTURE,DURATION\n");
        checkRejects("kod nije 3 slova",
                "# AIRPORTS\nCODE,NAME,X,Y\nBELGRADE,Beograd,0,0\n# FLIGHTS\nFROM,TO,DEPARTURE,DURATION\n");
        checkRejects("duplikat koda aerodroma",
                "# AIRPORTS\nCODE,NAME,X,Y\nBEG,Beograd,0,0\nBEG,Beograd Drugi,1,1\n# FLIGHTS\nFROM,TO,DEPARTURE,DURATION\n");
        checkRejects("let referise nepostojeci aerodrom",
                "# AIRPORTS\nCODE,NAME,X,Y\nBEG,Beograd,0,0\n# FLIGHTS\nFROM,TO,DEPARTURE,DURATION\nBEG,ZZZ,08:00,60\n");
        checkRejects("neispravno vreme polaska",
                "# AIRPORTS\nCODE,NAME,X,Y\nBEG,Beograd,0,0\nFRA,Frankfurt,1,1\n# FLIGHTS\nFROM,TO,DEPARTURE,DURATION\nBEG,FRA,25:99,60\n");
        checkRejects("trajanje leta nije broj",
                "# AIRPORTS\nCODE,NAME,X,Y\nBEG,Beograd,0,0\nFRA,Frankfurt,1,1\n# FLIGHTS\nFROM,TO,DEPARTURE,DURATION\nBEG,FRA,08:00,abc\n");

        File missing = new File("data/ovaj_fajl_ne_postoji_123.csv");
        boolean missingRejected = false;
        try {
            loader.load(missing);
        } catch (AppException e) {
            missingRejected = true;
        }
        check(missingRejected, "ucitavanje nepostojeceg fajla baca AppException");

        System.out.println();
        System.out.println("UKUPNO: " + passed + " proslo, " + failed + " palo.");
        System.exit(failed == 0 ? 0 : 1);
    }

    private static Airport findAirport(Dataset ds, String code) {
        for (Airport a : ds.getAirports()) if (a.getCode().equals(code)) return a;
        return null;
    }

    private static Flight findFlight(Dataset ds, String from, String to) {
        for (Flight f : ds.getFlights())
            if (f.getDepartureAirport().equals(from) && f.getArrivalAirport().equals(to)) return f;
        return null;
    }

    private static String callSerialize(DataLoaderCSV loader, Dataset ds) throws Exception {
        File tmp = File.createTempFile("serialize_check", ".csv");
        loader.save(tmp, ds);
        String text = readFile(tmp);
        tmp.delete();
        return text;
    }

    private static void checkRejects(String description, String content) {
        File tmp = null;
        try {
            tmp = File.createTempFile("badcsv", ".csv");
            writeFile(tmp, content);
            new DataLoaderCSV().load(tmp);
            check(false, "odbijeno: " + description + " (NIJE bacen izuzetak!)");
        } catch (DataFormatException e) {
            check(true, "odbijeno: " + description + " -> '" + e.getMessage() + "'");
        } catch (AppException e) {
            check(true, "odbijeno: " + description + " (AppException) -> '" + e.getMessage() + "'");
        } catch (Exception e) {
            check(false, "odbijeno: " + description + " ali bacen NEOCEKIVAN izuzetak: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            if (tmp != null) tmp.delete();
        }
    }

    private static String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), "UTF-8");
    }

    private static void writeFile(File file, String content) throws IOException {
        FileWriter w = new FileWriter(file);
        try { w.write(content); } finally { w.close(); }
    }

    private static String normalize(String s) { return s.replace("\r\n", "\n").trim(); }

    private static void printFirstDiff(String a, String b) {
        int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                int start = Math.max(0, i - 20);
                System.out.println("      Prva razlika na poziciji " + i + ":");
                System.out.println("      original:    ..." + a.substring(start, Math.min(a.length(), i + 20)) + "...");
                System.out.println("      round-trip:  ..." + b.substring(start, Math.min(b.length(), i + 20)) + "...");
                return;
            }
        }
    }

    static void check(boolean cond, String name) {
        if (cond) { passed++; System.out.println("PASS  " + name); }
        else      { failed++; System.out.println("FAIL  " + name); }
    }
}