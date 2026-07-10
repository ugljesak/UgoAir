package model;

/**
 * Interface of listener (Observer pattern).
 */
public interface ModelListener {

    /** Called upon every successful change in airport traffic model. */
    void modelChanged();

}
