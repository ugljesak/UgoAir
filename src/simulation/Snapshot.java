package simulation;

import java.util.ArrayList;
import java.util.List;

public final class Snapshot {

    private final SimulationState state;
    private final double simMinutes;
    private final List<Plane> planes;
    private final int landedCount;
    private final int totalCount;

    public Snapshot(SimulationState state, double simMinutes,
                    List<Plane> planes, int landedCount, int totalCount) {
        this.state = state;
        this.simMinutes = simMinutes;
        this.planes = List.copyOf(planes);
        this.landedCount = landedCount;
        this.totalCount = totalCount;
    }

    public static Snapshot empty() {
        return new Snapshot(SimulationState.READY, 0.0, new ArrayList<Plane>(), 0, 0);
    }

    public SimulationState getState() { return state; }
    public double getSimMinutes() { return simMinutes; }
    public List<Plane> getPlanes() { return planes; }
    public int getInAirCount() { return planes.size(); }
    public int getLandedCount() { return landedCount; }
    public int getTotalCount() { return totalCount; }
}
