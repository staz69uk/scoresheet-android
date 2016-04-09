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
 * The rules by which an individual ice hockey game is played.
 *
 * Created by steve on 12/03/16.
 */
public class GameRules {

    private int regulationPeriods = 3;
    private boolean allowTie = false;
    private int periodMinutes = 20;
    private int overtimeMinutes = 5;
    private boolean penalties = true;

    public GameRules() {}

    public GameRules(int periods, int length, int overtime, boolean allowTie, boolean penalties) {
        this.regulationPeriods = periods;
        this.periodMinutes = length;
        this.overtimeMinutes = overtime;
        this.allowTie = allowTie;
        this.penalties = penalties;
    }

    // Define different game rules sets here
    public static final GameRules UK_REC_RULES = new GameRules(3,20,5,false,true);

    public int getRegulationPeriods() {
        return regulationPeriods;
    }

    public void setRegulationPeriods(int regulationPeriods) {
        this.regulationPeriods = regulationPeriods;
    }

    public boolean isAllowTie() {
        return allowTie;
    }

    public void setAllowTie(boolean allowTie) {
        this.allowTie = allowTie;
    }

    public int getPeriodMinutes() {
        return periodMinutes;
    }

    public void setPeriodMinutes(int periodMinutes) {
        this.periodMinutes = periodMinutes;
    }

    public int getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(int overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
    }

    public boolean isPenalties() {
        return penalties;
    }

    public void setPenalties(boolean penalties) {
        this.penalties = penalties;
    }
}
