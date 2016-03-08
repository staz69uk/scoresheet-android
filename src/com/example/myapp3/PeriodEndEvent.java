package com.example.myapp3;

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
        super.period = period;
        super.setClockTime("0000");
    }

    @Override
    public String toString() {
        return String.format("%s Period %d ended", gameTime, period);
    }
}
