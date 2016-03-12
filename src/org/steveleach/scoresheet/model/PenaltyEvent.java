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

    public PenaltyEvent(int period, String clockTime, String team, String subType, String player, int minutes, GameRules rules) {
        this();
        super.period = period;
        super.setClockTime(clockTime, rules);
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
        return String.format("%s - %s %s %dm (%s) for %s (%s)", gameTime, team, eventType, minutes, subType, player, finishTime());
    }

    public String finishTime() {
        try {
            String startTime = getGameTime();
            int mins = Integer.parseInt(startTime.substring(0, 2));
            int secs = Integer.parseInt(startTime.substring(3, 5));
            mins += getMinutes();
            return String.format("%02d:%02d", mins, secs);
        } catch (NullPointerException | NumberFormatException e) {
            return "00:00";
        }
    }
}
