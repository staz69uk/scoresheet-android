package com.example.myapp3;

/**
 * Created by steve on 29/02/16.
 */
public class GameEvent {
    protected String player = "0";
    protected String team = "Home";
    protected String gameTime = "00:00";
    protected String eventType = "Goal";

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
}
