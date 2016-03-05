package com.example.myapp3;

import android.text.Editable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by steve on 29/02/16.
 */
public class GameEvent {
    protected int period = 0;
    protected String player = "0";
    protected String team = "Home";
    protected String gameTime = "00:00";
    protected String eventType = "Goal";
    protected String subType = "";
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getGameTime() {
        return gameTime;
    }

    public void setGameTime(String gameTime) {
        this.gameTime = gameTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return String.format("%s %-8s %-8s", gameTime, eventType, team);
    }

    public void setTimeFrom(Date dateTime) {
        setGameTime(timeFormat.format(dateTime));
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getSubType() {
        return subType;
    }

    /**
     * IMPORTANT - make sure the period is set correctly before use.
     * @param clockTime
     */
    public void setClockTime(String clockTime) {
        setGameTime(gameTimeFromClock(clockTime));
    }

    String gameTimeFromClock(final String clockTime) {
        if (clockTime.length() != 4) {
            throw new IllegalArgumentException("Clock time must be 4 digits");
        }
        int mins = Integer.parseInt(clockTime.substring(0,2));
        int secs = Integer.parseInt(clockTime.substring(2,4));
        int remainingSecs = mins * 60 + secs;
        int totalSecsPlayed = 20 * 60 - remainingSecs + ((period-1)*20*60);
        int minsPlayed = totalSecsPlayed / 60;
        int secsPlayed = totalSecsPlayed % 60;
        return String.format("%02d:%02d",minsPlayed,secsPlayed);
    }

}
