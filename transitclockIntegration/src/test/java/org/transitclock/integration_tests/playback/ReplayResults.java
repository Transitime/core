package org.transitclock.integration_tests.playback;

/**
 * Results of the old prediction comparison to new predictions.
 */
public class ReplayResults {
    int oldTotalPreds = 0, newTotalPreds = 0, bothTotalPreds = 0;

    double oldTotalError = 0, newTotalError = 0;

    int oldBetter = 0, newBetter = 0;

    public int getOldTotalPreds() {
        return oldTotalPreds;
    }

    public void setOldTotalPreds(int oldTotalPreds) {
        this.oldTotalPreds = oldTotalPreds;
    }

    public int getNewTotalPreds() {
        return newTotalPreds;
    }

    public void setNewTotalPreds(int newTotalPreds) {
        this.newTotalPreds = newTotalPreds;
    }

    public int getBothTotalPreds() {
        return bothTotalPreds;
    }

    public void setBothTotalPreds(int bothTotalPreds) {
        this.bothTotalPreds = bothTotalPreds;
    }

    public double getOldTotalError() {
        return oldTotalError;
    }

    public void setOldTotalError(double oldTotalError) {
        this.oldTotalError = oldTotalError;
    }

    public double getNewTotalError() {
        return newTotalError;
    }

    public void setNewTotalError(double newTotalError) {
        this.newTotalError = newTotalError;
    }

    public int getOldBetter() {
        return oldBetter;
    }

    public void setOldBetter(int oldBetter) {
        this.oldBetter = oldBetter;
    }

    public int getNewBetter() {
        return newBetter;
    }

    public void setNewBetter(int newBetter) {
        this.newBetter = newBetter;
    }
}
