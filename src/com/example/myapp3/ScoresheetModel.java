package com.example.myapp3;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by steve on 02/03/16.
 */
public class ScoresheetModel {
    private List<GameEvent> events = new LinkedList<>();
    private int homeGoals = 0;
    private int awayGoals = 0;
    private int period = 1;

    public List<GameEvent> getEvents() {
        return events;
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public void setHomeGoals(int homeGoals) {
        this.homeGoals = homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    public void setAwayGoals(int awayGoals) {
        this.awayGoals = awayGoals;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void incHomeGoals() {
        homeGoals += 1;
    }

    public void incAwayGoals() {
        awayGoals += 1;
    }
}
