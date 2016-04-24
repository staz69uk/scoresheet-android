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

import java.text.SimpleDateFormat;

/**
 * An event that takes place during an Ice Hockey game.
 *
 * @author Steve Leach
 */
public abstract class GameEvent {
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
    public static final String GAME_TIME_ERROR = "99:99";
    public static final String GAME_TIME_ZERO = "00:00";
    protected int period = 0;
    protected int player = 0;
    protected String team = "Home";
    protected String gameTime = GAME_TIME_ERROR;
    protected String clockTime = "0000";
    protected String eventType = "Goal";
    protected String subType = "";

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
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

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getClockTime() {
        return clockTime;
    }

    public void setClockTime(String clockTime) {
        this.clockTime = clockTime;
    }


//    public void _setClockTime(String clockTime, GameRules rules) {
//        setGameTime(gameTimeFromClock(clockTime, rules));
//    }
//
//    public String _gameTimeFromClock(String clockTime, GameRules rules) {
//        if ((clockTime == null) || (clockTime.length() < 3)) {
//            return "00:00";
//        }
//        if (clockTime.length() == 3) {
//            clockTime = "0" + clockTime;
//        }
//        try {
//            int periodMins = rules.getPeriodMinutes();
//            int mins = getIntValue(clockTime, 0, 2);
//            int secs = getIntValue(clockTime, 2, 4);
//            int remainingSecs = mins * 60 + secs;
//            int totalSecsPlayed = periodMins * 60 - remainingSecs + ((period - 1) * periodMins * 60);
//            int minsPlayed = totalSecsPlayed / 60;
//            int secsPlayed = totalSecsPlayed % 60;
//            return String.format("%02d:%02d", minsPlayed, secsPlayed);
//        } catch (NullPointerException | NumberFormatException e) {
//            return "00:00";
//        }
//    }
//
//    public int getIntValue(String text, int start, int end) {
//        return Integer.parseInt(text.substring(start,end));
//    }

    /**
     * The text that should be displayed on the {@link GameReport} for this event.
     * @return
     */
    public String reportText() {
        return this.toString();
    }

}
