package org.steveleach.scoresheet.model;

import java.util.*;

/**
 * Created by steve on 02/03/16.
 */
public class ScoresheetModel {
    private List<GameEvent> events = new LinkedList<>();
    private Team homeTeam = new Team("Home");
    private Team awayTeam = new Team("Away");
    private Date gameDateTime = new Date();
    private String gameLocation = "";
    private String competition = "";
    private List<GameOfficial> officials = new LinkedList<>();
    private GameRules rules = new GameRules();
    private int homeTimeouts = 0;
    private int awayTimeouts = 0;

    public void addEvent(GameEvent event) {
        events.add(event);
        sortEvents();
    }

    public Date getGameDateTime() {
        return gameDateTime;
    }

    public void setGameDateTime(Date gameDateTime) {
        this.gameDateTime = gameDateTime;
    }

    public String getGameLocation() {
        return gameLocation;
    }

    public void setGameLocation(String gameLocation) {
        this.gameLocation = gameLocation;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public List<GameEvent> getEvents() {
        return events;
    }

    public int getHomeGoals() {
        return getGoals(homeTeam.getName());
    }

    public int getGoals(String team) {
        int goals = 0;
        for (GameEvent event : events) {
            if (event instanceof GoalEvent) {
                if (event.getTeam().equals(team)) {
                    goals++;
                }
            }
        }
        return goals;
    }

    public void sortEvents() {
        Collections.sort(events, new Comparator<GameEvent>() {
            @Override
            public int compare(GameEvent event1, GameEvent event2) {
                return event1.getGameTime().compareTo(event2.getGameTime());
            }
        });
    }

    public int getAwayGoals() {
        return getGoals(awayTeam.getName());
    }

    public int getPeriod() {
        int period = 1;
        for (GameEvent event : events) {
            if (event instanceof PeriodEndEvent) {
                period++;
            }
        }
        return period;
    }

    public String getCompetition() {
        return competition;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
    }

    public List<GameOfficial> getOfficials() {
        return officials;
    }

    public void setOfficials(List<GameOfficial> officials) {
        this.officials = officials;
    }

    public int getHomeTimeouts() {
        return homeTimeouts;
    }

    public void setHomeTimeouts(int homeTimeouts) {
        this.homeTimeouts = homeTimeouts;
    }

    public int getAwayTimeouts() {
        return awayTimeouts;
    }

    public void setAwayTimeouts(int awayTimeouts) {
        this.awayTimeouts = awayTimeouts;
    }

    public String fullReport() {
        return new GameReport(this).report();
    }

    public GameRules getRules() {
        return rules;
    }

    public void setRules(GameRules rules) {
        this.rules = rules;
    }
}
