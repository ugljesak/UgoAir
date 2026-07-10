package util;

import java.util.HashSet;
import java.util.Set;

public class InactivityMonitor extends Thread {

    public static final long TIMEOUT_MS = 60000L;
    public static final long WARNING_MS = 5000L;

    private final InactivityListener listener;
    private final Object lock = new Object();

    private long lastActivity = System.currentTimeMillis();

    private volatile boolean alive = true;
    private volatile boolean isWarning = false;
    private volatile int lastSecond = -1;

    public InactivityMonitor(InactivityListener listener) {
        super("InactivityMonitor");
        setDaemon(true);
        this.listener = listener;
    }

    public void registerActivity() {
        boolean cancelled = false;
        synchronized (lock) {
            lastActivity = System.currentTimeMillis();
            if (isWarning) {
                isWarning = false;
                lastSecond = -1;
                cancelled = true;
            }
        }
        if (cancelled) {
            listener.inactivityWarningCancelled();
        }
    }

    public void shutdown() {
        alive = false;
        interrupt();
    }

    @Override
    public void run() {
        while (alive) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                if (!alive) {
                    return;
                }
            }

            boolean flagTick = false;
            boolean flagTimeout = false;
            int secondsLeft = -1;

            synchronized (lock) {
                long remaining = TIMEOUT_MS - (System.currentTimeMillis() - lastActivity);

                if (remaining <= 0) { // Shutdown
                    flagTimeout = true;
                    alive = false;
                } else if (remaining <= WARNING_MS) {
                    isWarning = true;
                    secondsLeft = (int) Math.ceil(remaining / 1000.0);
                    if (secondsLeft != lastSecond) {
                        lastSecond = secondsLeft;
                        flagTick = true;
                    }
                }
            }

            if (flagTick) {
                listener.inactivityWarningTick(secondsLeft);
            }
            if (flagTimeout) {
                listener.inactivityTimeout();
            }
        }
    }
}
