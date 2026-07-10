package simulation;

public class Plane {

    private final String fromCode;
    private final String toCode;
    private final double x;
    private final double y;
    private final double progress; // [0, 1] from departure, to arrival

    public Plane(String fromCode, String toCode, double x, double y, double progress) {
        this.fromCode = fromCode;
        this.toCode = toCode;
        this.x = x;
        this.y = y;
        this.progress = progress;
    }

    public String getFromCode() { return fromCode; }
    public String getToCode() { return toCode; }
    public double getX() { return x; }
    public double getY() { return y; }

    public double getProgress() { return progress; }

    public String getLabel() {
        return fromCode + "-" + toCode;
    }
}
