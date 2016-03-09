package com.example.myapp3;

import java.util.TreeMap;

/**
 * Created by steve on 05/03/16.
 */
public class GameReport {

    private final ScoresheetModel model;

    public GameReport(ScoresheetModel model) {
        this.model = model;
    }

    public String report() {
        String sep = "\n";
        StringBuilder sb = new StringBuilder();

        sb.append("HOME GOALS\n");
        reportEvents(GoalEvent.class, model.getHomeTeam().getName(), sb);
        sb.append(sep);

        sb.append("HOME PENALTIES\n");
        reportEvents(PenaltyEvent.class, model.getHomeTeam().getName(), sb);
        sb.append(sep);

        sb.append("AWAY GOALS\n");
        reportEvents(GoalEvent.class, model.getAwayTeam().getName(), sb);
        sb.append(sep);

        sb.append("AWAY PENALTIES\n");
        reportEvents(PenaltyEvent.class, model.getAwayTeam().getName(), sb);
        sb.append(sep);

        sb.append("HOME PLAYERS\n");
        reportPlayers(model.getHomeTeam().getName(), sb);
        sb.append(sep);

        sb.append("AWAY PLAYERS\n");
        reportPlayers(model.getAwayTeam().getName(), sb);
        sb.append(sep);

        sb.append("PERIOD SCORES\n");
        reportPeriodScores(sb);
        sb.append(sep);

        sb.append("PENALTY TOTALS\n");
        reportPenaltyTotals(sb);
        sb.append(sep);

        return sb.toString();
    }

    private void reportPenaltyTotals(StringBuilder sb) {
        int[] homeMins = {0,0,0};
        int[] awayMins = {0,0,0};
        for (GameEvent event : model.getEvents()) {
            if (event instanceof PenaltyEvent) {
                PenaltyEvent penalty = (PenaltyEvent)event;
                int periodIndex = event.getPeriod()-1;
                if (event.getTeam().equals(model.getHomeTeam().getName())) {
                    homeMins[periodIndex] += penalty.getMinutes();

                } else {
                    awayMins[periodIndex] += penalty.getMinutes();
                }
            }
        }
        int homeTotal = homeMins[0] + homeMins[1] + homeMins[2];
        int awayTotal = awayMins[0] + awayMins[1] + awayMins[2];
        sb.append(String.format("%s : %d %d %d = %d\n",model.getHomeTeam().getName(), homeMins[0], homeMins[1], homeMins[2], homeTotal));
        sb.append(String.format("%s : %d %d %d = %d\n",model.getAwayTeam().getName(), awayMins[0], awayMins[1], awayMins[2], awayTotal));
    }

    private void reportPeriodScores(StringBuilder sb) {
        int[] homeScores = {0,0,0};
        int[] awayScores = {0,0,0};
        for (GameEvent event : model.getEvents()) {
            if (event instanceof GoalEvent) {
                int periodIndex = event.getPeriod()-1;
                if (event.getTeam().equals(model.getHomeTeam().getName())) {
                    homeScores[periodIndex]++;

                } else {
                    awayScores[periodIndex]++;
                }
            }
        }
        sb.append(String.format("%s : %d %d %d = %d\n",model.getHomeTeam().getName(), homeScores[0], homeScores[1], homeScores[2], model.getHomeGoals()));
        sb.append(String.format("%s : %d %d %d = %d\n",model.getAwayTeam().getName(), awayScores[0], awayScores[1], awayScores[2], model.getAwayGoals()));
    }

    class PlayerStats {
        int goals = 0;
        int assists = 0;
        int penaltyMins = 0;
    }

    class PlayerStatsMap extends TreeMap<Integer, PlayerStats> {
        PlayerStats getStats(Integer player) {
            if (containsKey(player)) {
                return get(player);
            } else {
                PlayerStats stats = new PlayerStats();
                put(player,stats);
                return stats;
            }
        }
    }

    private void reportPlayers(String team, StringBuilder sb) {
        PlayerStatsMap stats = new PlayerStatsMap();
        for (GameEvent event : model.getEvents()) {
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

        for (int playerId : stats.keySet()) {
            PlayerStats playerStats = stats.get(playerId);
            sb.append(String.format("%d : goals=%d, assists=%d, pen.mins=%d\n", playerId, playerStats.goals, playerStats.assists, playerStats.penaltyMins));
        }
    }

    private void reportEvents(Class<?> eventClass, String team, StringBuilder sb) {
        for (GameEvent event : model.getEvents()) {
            if (event.getClass().equals(eventClass) && (event.getTeam().equals(team))) {
                sb.append(event.toString());
                sb.append("\n");
            }
        }
    }

}
