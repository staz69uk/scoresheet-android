package com.example.myapp3;

/**
 * Created by steve on 05/03/16.
 */
public class PenaltyEvent extends GameEvent {
    private int minutes = 2;

    public PenaltyEvent() {
        super();
        eventType = "Penalty";
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %dm (%s) for %s", gameTime, team, eventType, minutes, subType, player);
    }
}
