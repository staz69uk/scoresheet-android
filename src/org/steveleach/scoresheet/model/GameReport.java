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

import java.util.Map;
import java.util.TreeMap;

/**
 * A textual report describing an ice hockey game.
 *
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

        sb.append("HOME - TEAM\n");
        reportPlayers(model.getHomeTeam().getName(), sb);
        sb.append(sep);

        sb.append("SCORING - HOME\n");
        reportEvents(GoalEvent.class, model.getHomeTeam().getName(), sb);
        sb.append(sep);

        sb.append("PENALTIES - HOME\n");
        reportEvents(PenaltyEvent.class, model.getHomeTeam().getName(), sb);
        sb.append(sep);

        sb.append("AWAY - TEAM\n");
        reportPlayers(model.getAwayTeam().getName(), sb);
        sb.append(sep);

        sb.append("SCORING - AWAY\n");
        reportEvents(GoalEvent.class, model.getAwayTeam().getName(), sb);
        sb.append(sep);

        sb.append("PENALTIES - AWAY\n");
        reportEvents(PenaltyEvent.class, model.getAwayTeam().getName(), sb);
        sb.append(sep);

        sb.append("PERIOD SCORES\n");
        reportPeriodScores(sb);
        sb.append(sep);

        sb.append("PENALTY MINUTES\n");
        reportPenaltyTotals(sb);
        sb.append(sep);

        return sb.toString();
    }

    private void reportPenaltyTotals(StringBuilder sb) {
        int[] homeMins = {0,0,0,0};
        int[] awayMins = {0,0,0,0};
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
        int homeTotal = sum(homeMins);
        int awayTotal = sum(awayMins);
        sb.append(String.format("%s : %d %d %d %d = %d\n",model.getHomeTeam().getName(), homeMins[0], homeMins[1], homeMins[2], homeMins[3], homeTotal));
        sb.append(String.format("%s : %d %d %d %d = %d\n",model.getAwayTeam().getName(), awayMins[0], awayMins[1], awayMins[2], awayMins[3], awayTotal));
    }

    private int sum(int[] values) {
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum;
    }

    private void reportPeriodScores(StringBuilder sb) {
        int[] homeScores = {0,0,0,0};
        int[] awayScores = {0,0,0,0};
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
        sb.append(String.format("%s : %d %d %d %d = %d\n",model.getHomeTeam().getName(), homeScores[0], homeScores[1], homeScores[2], homeScores[3], model.getHomeGoals()));
        sb.append(String.format("%s : %d %d %d %d = %d\n",model.getAwayTeam().getName(), awayScores[0], awayScores[1], awayScores[2], awayScores[2],model.getAwayGoals()));
    }

    private void reportPlayers(String team, StringBuilder sb) {
        for (ScoresheetModel.PlayerStats playerStats : model.getPlayerStats(team).values()) {
            sb.append(String.format("%d : goals=%d, assists=%d, pen.mins=%d\n", playerStats.playerNum, playerStats.goals, playerStats.assists, playerStats.penaltyMins));
        }
    }

    private void reportEvents(Class<?> eventClass, String team, StringBuilder sb) {
        for (GameEvent event : model.getEvents()) {
            if (event.getClass().equals(eventClass) && (event.getTeam().equals(team))) {
                sb.append(event.reportText());
                sb.append("\n");
            }
        }
    }

}
