/*  Copyright 2016 Steve Leach

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package org.steveleach.scoresheet.model;

import org.steveleach.scoresheet.io.WeakList;

import java.util.*;

/**
 * Information about an ice hockey game, including the events that took place during the game.
 *
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
    private WeakList<ModelAware> listeners = new WeakList<>();

    /**
     * Adds an event to the game.
     *
     * All registered listeners are updated.
     *
     * @param event
     */
    public void addEvent(GameEvent event) {
        events.add(event);
        sortEvents();
        notifyListeners(new ModelUpdate("Event added"));
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

    public void addOfficial(GameOfficial.Role role, String name) {
        officials.add(new GameOfficial(role,name));
    }

    /**
     * Adds a new listener to this model.
     *
     * The listener will be notified of model changes.
     * Listeners are held via weak references, and so the listener list
     * will not prevent the listener being garbage collected.
     *
     * @param listener
     */
    public void addListener(ModelAware listener) {
        listeners.add(listener);
    }

    /**
     * Notify all listeners that the model has been updated.
     *
     * @param update
     */
    public void notifyListeners(ModelUpdate update) {
        listeners.removeDeadItems();

        for (ModelAware listener : listeners) {
            if (listener != null) {
                listener.onModelUpdated(update);
            }
        }
    }



}
