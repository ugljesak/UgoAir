package util;

public interface InactivityListener {

    /**
     * Called last 5 seconds, every for itself for every remaining
     * second (there is 5, 4, 3, 2, 1 seconds left).
     */
    void inactivityWarningTick(int secondsLeft);

    /**
     * Called when inactivity warning is being canceled
     * by some activity.
     */
    void inactivityWarningCancelled();

    /**
     * Called upon 60 seconds of user inactivity, should
     * exit program successfully.
     */
    void inactivityTimeout();
}
