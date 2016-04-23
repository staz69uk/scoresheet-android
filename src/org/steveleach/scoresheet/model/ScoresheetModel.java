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

import org.steveleach.scoresheet.support.WeakSet;

import java.util.*;

/**
 * Information about an ice hockey game, including the events that took place during the game.
 * <p>
 * The primary component of a ScoresheetModel is a list of {@link GameEvent} objects.
 *
 * @author Steve Leach
 */
public class ScoresheetModel {
    private static final int SECS_PER_MIN = 60;
    private List<GameEvent> events = new LinkedList<>();
    private Team homeTeam = new Team("Home");
    private Team awayTeam = new Team("Away");
    private Date gameDateTime = new Date();
    private String gameLocation = "";
    private String competition = "";
    private List<GameOfficial> officials = new LinkedList<>();
    private GameRules rules = GameRules.UK_REC_RULES;
    private int homeTimeouts = 0;
    private int awayTimeouts = 0;
    private WeakSet<ModelAware> listeners = new WeakSet<>();
    private boolean isChanged = false;

    /**
     * Adds an event to the game.
     *
     * Sets the gameTime for the event from the clockTime, and notifies all registered listeners of the change.
     *
     * @param event
     *          the event to add to the game
     */
    public void addEvent(GameEvent event) {
        fixupClock(event);
        events.add(event);
        sortEvents();
        notifyListeners(ModelUpdate.EVENT_ADDED);
    }

    /**
     * Removes an event from the game history.
     *
     * Notifies all registered listeners of the change.
     *
     * @param index
     *          the index of the event to remove
     */
    public void removeEvent(int index) {
        events.remove(index);
        notifyListeners(ModelUpdate.EVENT_REMOVED);
    }

    /**
     * Removes all events from the game history.
     *
     * Notifies all registered listeners of the change.
     */
    public void clearEvents() {
        events.clear();
        notifyListeners(ModelUpdate.EVENTS_CLEARED);
    }

    private void fixupClock(GameEvent event) {
        if (event.getGameTime().equals(GameEvent.GAME_TIME_ERROR)) {
            String gameTime = gameTimeFromClock(event.getPeriod(), event.getClockTime());
            event.setGameTime(gameTime);
        }
    }

    /**
     * Converts clock time to game time based on the rules for this game.
     *
     * @param period
     *          the current game period
     * @param clockTime
     *          time remaining in the period in "mmss" format
     * @return
     *          time elapsed since the start of the game in "mm:ss" format
     */
    public String gameTimeFromClock(int period, String clockTime) {
        if ((clockTime == null) || (clockTime.length() < 3)) {
            return GameEvent.GAME_TIME_ERROR;
        }
        if (clockTime.length() == 3) {
            clockTime = "0" + clockTime;
        }
        try {
            int periodMins = rules.getPeriodMinutes();
            int mins = getIntValue(clockTime, 0, 2);
            int secs = getIntValue(clockTime, 2, 4);
            int remainingSecs = mins * SECS_PER_MIN + secs;
            int totalSecsPlayed = periodMins * SECS_PER_MIN - remainingSecs + ((period - 1) * periodMins * SECS_PER_MIN);
            int minsPlayed = totalSecsPlayed / SECS_PER_MIN;
            int secsPlayed = totalSecsPlayed % SECS_PER_MIN;
            return String.format("%02d:%02d", minsPlayed, secsPlayed);
        } catch (NullPointerException | NumberFormatException e) {
            return GameEvent.GAME_TIME_ERROR;
        }
    }

    private int getIntValue(String text, int start, int end) {
        return Integer.parseInt(text.substring(start,end));
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

    /**
     * Sorts all events in the game history by time (game clock).
     */
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
     */
    public void addListener(ModelAware listener) {
        listeners.add(listener);
    }

    /**
     * Notify all listeners that the model has been updated.
     */
    public void notifyListeners(ModelUpdate update) {
        for (ModelAware listener : listeners) {
            if (listener != null) {
                notifyListener(listener,update);
            }
        }
    }

    private void notifyListener(ModelAware listener, ModelUpdate update) {
        // Don't let a bad listener spoil everything
        try {
            listener.onModelUpdated(update);
        } catch (RuntimeException e) {
            // silently ignore
        }
    }

    public List<GoalEvent> goals(String team) {
        LinkedList<GoalEvent> goals = new LinkedList<>();
        for (GameEvent event : events) {
            if (event instanceof GoalEvent) {
                if (event.getTeam().equals(team)) {
                    goals.add((GoalEvent) event);
                }
            }
        }
        return goals;
    }

    public List<PenaltyEvent> penalties(String team) {
        LinkedList<PenaltyEvent> penalties = new LinkedList<>();
        for (GameEvent event : events) {
            if (event instanceof PenaltyEvent) {
                if (event.getTeam().equals(team)) {
                    penalties.add((PenaltyEvent) event);
                }
            }
        }
        return penalties;
    }

    /**
     * Returns the game time of the latest event in the game.
     */
    public String maxGameTime() {
        if (events.size() == 0) {
            return GameEvent.GAME_TIME_ZERO;
        } else {
            return events.get(events.size()-1).getGameTime();
        }
    }

    public class PlayerStats {
        public int playerNum = 0;
        public int goals = 0;
        public int assists = 0;
        public int penaltyMins = 0;
    }

    private class PlayerStatsMap extends TreeMap<Integer, PlayerStats> {
        PlayerStats getStats(Integer player) {
            if (containsKey(player)) {
                return get(player);
            } else {
                PlayerStats stats = new PlayerStats();
                stats.playerNum = player;
                put(player,stats);
                return stats;
            }
        }
    }

    public Map<Integer,PlayerStats> getPlayerStats(String team) {
        PlayerStatsMap stats = new PlayerStatsMap();
        for (GameEvent event : getEvents()) {
            if (event.getTeam().equals(team)) {
                int playerId = Integer.parseInt(event.getPlayer());
                if (event instanceof GoalEvent) {
                    GoalEvent goal = (GoalEvent)event;
                    stats.getStats(playerId).goals += 1;
                    if (goal.getAssist1() > 0) {
                        stats.getStats(goal.getAssist1()).assists++;
                    }
                    if (goal.getAssist2() > 0) {
                        stats.getStats(goal.getAssist2()).assists++;
                    }
                } else if (event instanceof PenaltyEvent) {
                    PenaltyEvent penaltyEvent = (PenaltyEvent)event;
                    stats.getStats(playerId).penaltyMins += penaltyEvent.getMinutes();
                }
            }
        }
        return stats;
    }

    public int[] penaltyTotals(String team) {
        int[] totals = {0,0,0,0,0};
        for (PenaltyEvent penalty: penalties(team)) {
            int periodIndex = penalty.getPeriod()-1;
            totals[periodIndex] += penalty.getMinutes();
        }
        totals[4] = sum(totals);
        return totals;
    }

    public int[] goalTotals(String team) {
        int[] totals = {0,0,0,0,0};
        for (GoalEvent event : goals(team)) {
            int periodIndex = event.getPeriod()-1;
            totals[periodIndex]++;
        }
        totals[4] = sum(totals);
        return totals;
    }

    private int sum(int[] values) {
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    /**
     * Returns the score at a specified point in the game.
     */
    public HomeAway<Integer> scoreAt(final String gameTime) {
        int home = 0;
        int away = 0;
        for (GameEvent event : events) {
            if (event.getGameTime().compareTo(gameTime) > 0) {
                break;
            }
            if (event instanceof GoalEvent) {
                if (event.getTeam().equals(getHomeTeam().getName())) {
                    home++;
                } else if (event.getTeam().equals(getAwayTeam().getName())) {
                    away++;
                }
            }
        }
        return new HomeAway<>(home,away);
    }

    public int[] assistTotals(String team) {
        int[] totals = {0,0,0,0,0};
        for (GoalEvent event : goals(team)) {
            int periodIndex = event.getPeriod()-1;
            GoalEvent goal = (GoalEvent) event;
            int assists = 0;
            assists += goal.getAssist1() > 0 ? 1 : 0;
            assists += goal.getAssist2() > 0 ? 1 : 0;
            totals[periodIndex] += assists;
        }
        totals[4] = sum(totals);
        return totals;
    }

    public void setHomeTeamName(final String newName) {
        setTeamName(homeTeam, newName);
    }

    private void setTeamName(Team team, String newName) {
        String oldName = team.getName();
        changeTeamNamesInEvents(oldName,newName);
        team.setName(newName);
    }

    public void setAwayTeamName(final String newName) {
        setTeamName(awayTeam, newName);
    }

    private void changeTeamNamesInEvents(String oldName, String newName) {
        for (GameEvent event : events) {
            if (event.getTeam().equals(oldName)) {
                event.setTeam(newName);
            }
        }
    }
}
