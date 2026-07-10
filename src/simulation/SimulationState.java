package simulation;

public enum SimulationState {
    READY("Simulation ready to start"),
    RUNNING("Simulation running"),
    PAUSED("Simulation paused"),
    FINISHED("Simulation finished");

    private final String description;

    SimulationState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
