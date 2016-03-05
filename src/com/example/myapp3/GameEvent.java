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
}
