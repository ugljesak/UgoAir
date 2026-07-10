package util;

import exception.ValidationException;

public final class SimulationTime implements Comparable<SimulationTime> {

    private final int totalMinutes;

    private SimulationTime(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }
    private SimulationTime(int hours, int minutes) {
        this.totalMinutes = hours * 60 + minutes;
    }

    public static SimulationTime of(int hours, int minutes) throws ValidationException {
        if (hours < 0 || hours > 23) {
            throw new ValidationException("Hour must be in range 0-23 (got: " + hours + ").");
        }
        if (minutes < 0 || minutes > 59) {
            throw new ValidationException("Minute must be in range 0-59 (got: " + minutes + ").");
        }
        return new SimulationTime(hours, minutes);
    }

    public static SimulationTime parseFromText(String text) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Time must be in format 'HH:MM' (e.g. 08:30).");
        }
        String s = text.trim();
        int colon = s.indexOf(':');
        if (colon < 0 || s.indexOf(':', colon + 1) >= 0) {
            throw new ValidationException("Invalid format: '" + s + "'. Expected HH:MM (e.g. 08:30).");
        }
        String hPart = s.substring(0, colon).trim();
        String mPart = s.substring(colon + 1).trim();
        int h, m;
        try {
            h = Integer.parseInt(hPart);
            m = Integer.parseInt(mPart);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid format: '" + s + "'. Expected HH:MM (e.g. 08:30).");
        }
        return of(h, m);
    }

    public int getMinutes() {
        return totalMinutes % 60;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public int getHours() {
        return (totalMinutes % 1440) / 60;
    }


    public int getDays() {
        return totalMinutes / 1440;
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d", getHours(), getMinutes());
    }
    public String toSimulationClockString() {
        return getDays() > 0
                ? String.format("%02d:%02d (+%d dan)", getHours(), getMinutes(), getDays())
                : String.format("%02d:%02d", getHours(), getMinutes());
    }

    @Override
    public int compareTo(SimulationTime other) {
        return Integer.compare(this.totalMinutes, other.totalMinutes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SimulationTime)) return false;
        return totalMinutes == ((SimulationTime) obj).totalMinutes;
    }

    @Override
    public int hashCode() {
        return totalMinutes;
    }

    public SimulationTime addTime(int timeMinutes) {
        return new SimulationTime(this.totalMinutes + timeMinutes);
    }
}
