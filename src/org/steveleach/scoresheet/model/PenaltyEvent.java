package org.steveleach.scoresheet.model;

/**
 * Created by steve on 05/03/16.
 */
public class PenaltyEvent extends GameEvent {
    private int minutes = 2;

    public PenaltyEvent() {
        super();
        eventType = "Penalty";
    }

    public PenaltyEvent(int period, String clockTime, String team, String subType, String player, int minutes) {
        this();
        super.period = period;
        super.setClockTime(clockTime);
        super.subType = subType;
        super.team = team;
        super.player = player;
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    @Override
    public String toString() {
        return String.format("%s - %s %s %dm (%s) for %s", gameTime, team, eventType, minutes, subType, player);
    }
}
