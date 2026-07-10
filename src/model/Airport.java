package model;

import exception.ValidationException;

public final class Airport {

    public static final int X_MIN = -180, X_MAX = 180;
    public static final int Y_MIN = -90, Y_MAX = 90;

    private final int x, y;
    private final String name;
    private final String code;


    public Airport(String name, String code, int x, int y) throws ValidationException {
        this.name = checkName(name);
        this.code = checkCode(code);
        this.x = checkCoordinate(x, 'X');
        this.y = checkCoordinate(y, 'Y');
    }


    private String checkName(String name) throws ValidationException {
        if(name == null || name.trim().isEmpty()) {
            throw new ValidationException("Airport name is empty.");
        }
        String n = name.trim();
        // "[\\p{L}0-9 ]+" za sve Unicode charove
        if(!n.matches("[a-zA-Z0-9 ]+")) {
            throw new ValidationException("Airport name can contain only alphabetical and numerical characters.");
        }

        return n;
    }

    private String checkCode(String code) throws ValidationException {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Kod aerodroma nije unet. Unesite tacno 3 velika slova (e.g. BEG).");
        }

        String c = code.trim();
        if(c.length() != 3) {
            throw new ValidationException("Invalid airport code: '" + c + "'. Length of code must be 3.");
        }

        boolean ok = true;
        for (int i = 0; i < 3; i++) {
            char ch = c.charAt(i);
            if (ch < 'A' || ch > 'Z') {
                ok = false;
                break;
            }
        }
        if (!ok) {
            throw new ValidationException("Invalid airport code'" + c +
                    "'. Code can contain only 3 characters of English alphabet [A-Z], (e.g. BEG, FRA.");
        }

        return c;
    }
    private int checkCoordinate(int value, char axis) throws ValidationException {
        int axisMin = axis == 'X' ? X_MIN : Y_MIN;
        int axisMax = axis == 'X' ? X_MAX : Y_MAX;

        if(value < axisMin || value > axisMax) {
            throw new ValidationException(axis + " coordinate must be in range [" + axisMin +
                    ", " + axisMax + "], but got: " + value + ".");
        }

        return value;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Airport)) return false;
        return code.equals(((Airport) obj).code);
    }

    @Override
    public String toString() {
        return "[" + code + "] - " + name + " (" + x + ", " + y + ")";
    }
}
