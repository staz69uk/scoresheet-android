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

/**
 * Record of a goal being scored in an ice hockey game.
 *
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
            assists = String.format("from %d, %d", assist1, assist2);
        } else if (assist1 > 0) {
            assists = String.format("from %d", assist1);
        }
        return String.format("%s - %s %s (%s) scored by %s %s", gameTime, team, eventType, subType, getPlayer(), assists);
    }
}
