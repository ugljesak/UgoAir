package simulation;

import exception.SimulationException;
import model.Airport;
import model.Flight;
import model.Model;
import util.Listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Engine extends Thread {

    public static final int TICK = 100;

    private final Object lock = new Object();

    private SimulationState state = SimulationState.READY;
    private List<ScheduledFlight> schedule = new ArrayList<>();

    private long timeCounter;
    private long lastResume;

    private volatile Snapshot snapshot = Snapshot.empty();
    private volatile boolean alive = true;

    private final Listeners<SimulationListener> listeners = new Listeners<>();

    public Engine() {
        super("SimulationEngine");
        setDaemon(true);
    }

    public void addListener(SimulationListener l) { listeners.add(l); }
    public void removeListener(SimulationListener l) {listeners.remove(l); }

    public Snapshot getSnapshot() {return snapshot;}
    public SimulationState getSimulationState() {
        synchronized (lock) {
            return state;
        }
    }

    public void startSimulation(Model model) throws SimulationException {
        List<Airport> airports = model.getAirports();
        List<Flight> flights = model.getFlights();
        Map<String, Airport> map = model.getMap();
        if(flights.isEmpty()) {
            throw new SimulationException("There are no entered flights for simulation to run.");
        }

        synchronized (lock) {
            if(state == SimulationState.RUNNING || state == SimulationState.PAUSED) {
                throw new SimulationException("Simulation is already being ran.");
            }
            schedule = FlightScheduler.schedule(flights, map);
            timeCounter = 0L;
            lastResume = System.nanoTime();
            setState(SimulationState.RUNNING);
            lock.notifyAll();
        }
    }

    public void pauseSimulation() {
        synchronized (lock) {
            if(state == SimulationState.RUNNING) {
                timeCounter += (System.nanoTime() - lastResume) / 1000000L;
                setState(SimulationState.PAUSED);
            }
        }
    }

    public void resumeSimulation() {
        synchronized (lock) {
            if(state == SimulationState.PAUSED) {
                lastResume = System.nanoTime();
                setState(SimulationState.RUNNING);
                lock.notifyAll();
            }
        }
    }

    public void resetSimulation() {
        synchronized (lock) {
            schedule = new ArrayList<ScheduledFlight>();
            timeCounter = 0L;
            snapshot = Snapshot.empty();
            setState(SimulationState.READY);
        }
    }

    public void shutdown() {
        alive = false;
        synchronized (lock) {
            lock.notifyAll();
        }
        interrupt();
    }

    @Override
    public void run() {
        while(alive) {
            synchronized (lock) {
                while(alive && state != SimulationState.RUNNING){
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        if(!alive) return;
                    }
                }
            }

            step();

            try {
                sleep(TICK);
            } catch(InterruptedException e){

            }
        }
    }

    private void step() {
        double simTime;
        List<ScheduledFlight> sch;

        synchronized (lock) {
            if (state != SimulationState.RUNNING) return;
            long time = timeCounter + (System.nanoTime() - lastResume) / 1000000L;
            simTime = (double) time / 100.0;
            sch = schedule;
        }

        List<Plane> planes = new ArrayList<>();
        int landedCount = 0;
        double lastArrival = 0.0;
        for (ScheduledFlight sf : sch) {
            int depTime = sf.getActualDepartureTime();
            int arrTime = depTime + sf.getFlight().getFlightDuration();

            if (arrTime > lastArrival) lastArrival = arrTime;

            if (simTime >= arrTime) {
                landedCount++;
            } else if (simTime >= depTime) {
                double progress = (simTime - depTime) / (double) sf.getFlight().getFlightDuration();
                Airport from = sf.getFrom();
                Airport to = sf.getTo();
                double x = from.getX() + progress * (to.getX() - from.getX());
                double y = from.getY() + progress * (to.getY() - from.getY());
                planes.add(new Plane(from.getCode(), to.getCode(), x, y, progress));
            }
        }

        SimulationState nextState;
        synchronized (lock) {
            if (state != SimulationState.RUNNING) {
                return;
            }
            if (!sch.isEmpty() && landedCount == sch.size()) {
                simTime = lastArrival;
                setState(SimulationState.FINISHED);
            }
            nextState = state;
        }

        snapshot = new Snapshot(nextState, simTime, planes, landedCount, sch.size());
    }

    private void setState(final SimulationState newState) {
        if (state != newState) {
            state = newState;
            listeners.fire(new Consumer<SimulationListener>() {
                @Override
                public void accept(SimulationListener l) {
                    l.simulationStateChanged(newState);
                }
            });
        }
    }
}
