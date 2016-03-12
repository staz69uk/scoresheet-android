package org.steveleach.scoresheet.model;

/**
 * Created by steve on 12/03/16.
 */
public class GameRules {

    private int regulationPeriods = 3;
    private boolean allowTie = false;
    private int periodMinutes = 20;
    private int overtimeMinutes = 5;
    private boolean penalties = true;

    public int getRegulationPeriods() {
        return regulationPeriods;
    }

    public void setRegulationPeriods(int regulationPeriods) {
        this.regulationPeriods = regulationPeriods;
    }

    public boolean isAllowTie() {
        return allowTie;
    }

    public void setAllowTie(boolean allowTie) {
        this.allowTie = allowTie;
    }

    public int getPeriodMinutes() {
        return periodMinutes;
    }

    public void setPeriodMinutes(int periodMinutes) {
        this.periodMinutes = periodMinutes;
    }

    public int getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(int overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
    }

    public boolean isPenalties() {
        return penalties;
    }

    public void setPenalties(boolean penalties) {
        this.penalties = penalties;
    }
}
