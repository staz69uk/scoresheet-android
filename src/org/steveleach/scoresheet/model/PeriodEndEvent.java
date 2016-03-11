package org.steveleach.scoresheet.model;

/**
 * Created by steve on 05/03/16.
 */
public class PeriodEndEvent extends GameEvent {

    public PeriodEndEvent() {
        setTeam("");
        setEventType("Period end");
        setPlayer("");
    }

    public PeriodEndEvent(int period) {
        this();
        setPeriod(period);
        super.setClockTime("0000");
    }

    @Override
    public String toString() {
        return String.format("%s - Period %d ended", gameTime, period);
    }
}
