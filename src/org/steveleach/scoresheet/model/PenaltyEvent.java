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
 * Describes a penalty given to a player during an ice hockey game.
 *
 * Created by steve on 05/03/16.
 */
public class PenaltyEvent extends GameEvent {
    private int minutes = 2;

    public PenaltyEvent() {
        super();
        eventType = "Penalty";
    }

    public PenaltyEvent(int period, String clockTime, String team, String subType, String player, int minutes) {
        this();
        super.period = period;
        super.setClockTime(clockTime);
        super.subType = subType;
        super.team = team;
        super.player = player;
        this.minutes = minutes;

}
    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    @Override
    public String toString() {
        return String.format("%s - %s %s %dm (%s) for %s (%s)", gameTime, team, eventType, minutes, subType, player, finishTime());
    }

    public String finishTime() {
        try {
            String startTime = getGameTime();
            int mins = Integer.parseInt(startTime.substring(0, 2));
            int secs = Integer.parseInt(startTime.substring(3, 5));
            mins += getMinutes();
            return String.format("%02d:%02d", mins, secs);
        } catch (NullPointerException | NumberFormatException e) {
            return "00:00";
        }
    }

    @Override
    public String reportText() {
        return String.format("P %s, %dm for %s @ %s, %s - %s", getPlayer(), getMinutes(), getSubType(), getGameTime(), getGameTime(), finishTime());
    }
}
