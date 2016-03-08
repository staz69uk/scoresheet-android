package com.example.myapp3;

/**
 * Created by steve on 29/02/16.
 */
public class GoalEvent extends GameEvent {

    private int assist1 = 0;
    private int assist2 = 0;

    public GoalEvent() {
        super();
        super.setEventType("Goal");
    }

    public GoalEvent(int period, String time, String team, String goalType, int scorer, int assist1, int assist2) {
        this();
        setPeriod(period);
        setClockTime(time);
        setTeam(team);
        setSubType(goalType);
        setPlayer(""+scorer);
        setAssist1(assist1);
        setAssist2(assist2);
    }

    public int getAssist1() {
        return assist1;
    }

    public void setAssist1(int assist1) {
        this.assist1 = assist1;
    }

    public int getAssist2() {
        return assist2;
    }

    public void setAssist2(int assist2) {
        this.assist2 = assist2;
    }

    @Override
    public String toString() {
        String assists = "";
        if (assist2 > 0) {
            assists = String.format(" from %d, %d", assist1, assist2);
        } else if (assist1 > 0) {
            assists = String.format(" from %d", assist1);
        }
        return String.format("%s %s %s (%s) scored by %s %s", gameTime, team, eventType, subType, getPlayer(), assists);
    }
}
