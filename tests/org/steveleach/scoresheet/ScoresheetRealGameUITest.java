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
package org.steveleach.scoresheet;

import android.content.DialogInterface;
import android.widget.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.steveleach.ihscoresheet.R;
import org.steveleach.scoresheet.model.GoalEvent;
import org.steveleach.scoresheet.model.ScoresheetModel;
import org.steveleach.scoresheet.ui.*;

import java.io.FileNotFoundException;
import java.util.Map;

import static org.junit.Assert.*;
import static org.steveleach.ihscoresheet.R.id.*;

/**
 * End-to-end tests for user interface.
 *
 * @author Steve Leach
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
public class ScoresheetRealGameUITest extends AbstractUITest {

    @Before
    public void setup() {
        super.setupTestEnvironment();

        assertEquals(0, model.getEvents().size());
        assertEquals(1, model.getPeriod());
        assertEquals(0, model.getHomeGoals());
    }

    private void addPenalty(String clock, String subtype, String minutes, String team, String player) {
        if (team.equals(model.getHomeTeam().getName())) {
            click(btnHomePen);
        } else {
            click(btnAwayPen);
        }

        assertEquals(PenaltyFragment.class, visibleFragmentClass());

        setField(fldPenaltyClock, clock);
        setField(fldPenaltyPlayer, player);
        setField(fldPenaltyMins, minutes);
        setField(fldPenaltyCode, subtype);

        click(btnPenaltyDone);
    }

    private void addGoal(String clock, String subtype, String team, String player, String assist1, String assist2) {
        if (team.equals(model.getHomeTeam().getName())) {
            click(btnHomeGoal);
        } else {
            click(btnAwayGoal);
        }
        assertEquals(GoalFragment.class, visibleFragmentClass());

        setField(fldClock,clock);
        setField(fldGoalType,subtype);
        setField(fldScoredBy,player);
        setField(fldAssist1,assist1);
        setField(fldAssist2,assist2);

        click(btnDone);
    }

    private void clickNextPeriod() {
        int initialPeriod = model.getPeriod();
        int initialEvents = model.getEvents().size();
        // Click the "new period" button and validate the effects on the model
        activity.periodButtonClicked(null);
        assertEquals(initialEvents+1, model.getEvents().size());
        assertEquals(initialPeriod+1, model.getPeriod());
    }

    /**
     * Test using the events of an actual English recreational ice hockey game.
     */
    @Test
    public void testActualGame() throws FileNotFoundException {
        // Start a new game
        clickMenuItem(menuNewGame);
        verifyAlertDialogShowing("clear all events");

        clickDialogButton(DialogInterface.BUTTON_POSITIVE);

        assertEquals(0, model.getHomeGoals());
        assertEquals(0, model.getHomeGoals());

        // 1st Period

        addPenalty("1747", "TRIP", "2", "Away", "69");
        addPenalty("1620", "SLASH", "2", "Away", "79");
        addPenalty("1353", "TOO-M", "2", "Home", "24");
        addGoal("1307", "SH", "Home", "13", "", "");
        addGoal("1222", "PP", "Away", "89", "82", "86");
        addPenalty("1104", "HOOK", "2", "Home", "27");
        addGoal("0706", "E", "Home", "90", "93", "28");
        addGoal("554", "E", "Away", "92", "86", "");
        addGoal("138", "E", "Away", "66", "19", "");

        assertEquals(2, model.getHomeGoals());
        assertEquals(3, model.getAwayGoals());

        clickNextPeriod();

        // 2nd Period

        addGoal("1915", "E", "Home", "93", "", "");
        addPenalty("1711", "ROUGH", "2", "Home", "13");
        addPenalty("1711", "ROUGH", "2", "Away", "33");
        addGoal("1621", "SH", "Home", "93", "28", "");
        addGoal("1425", "E", "Away", "79", "69", "");
        addGoal("1210", "E", "Away", "79", "33", "");
        addGoal("808", "E", "Away", "33", "", "");
        addGoal("0758", "E", "Away", "33", "79", "");
        addGoal("0703", "E", "Home", "24", "73", "23");
        addGoal("0259", "E", "Away", "13", "16", "");
        addGoal("0230", "E", "Home", "27", "21", "29");
        addGoal("0058", "E", "Home", "73", "21", "28");
        addGoal("0001", "E", "Home", "24", "93", "");

        assertEquals(8, model.getHomeGoals());
        assertEquals(8, model.getAwayGoals());

        clickNextPeriod();

        // 3rd Period

        addGoal("1948", "E", "Away", "89", "82", "");
        addGoal("1806", "E", "Away", "79", "33", "");
        addPenalty("1504", "SLASH", "2", "Home", "28");
        addGoal("1454", "PP", "Away", "56", "33", "");
        addGoal("1056", "E", "Home", "23", "93", "24");
        addPenalty("0892", "TRIP", "2", "Home", "27");
        addPenalty("0341", "CROSS", "2", "Away", "82");
        addPenalty("0135", "TRIP", "2", "Away", "66");

        clickNextPeriod();

        assertEquals(9, model.getHomeGoals());
        assertEquals(11, model.getAwayGoals());

        ListView history = (ListView) activity.findViewById(R.id.historyList2);
        assertEquals(33, history.getAdapter().getCount());  // 20 goals, 10 penalties, 3 period-ends

        // Save the game

        clickMenuItem(R.id.menuSave);
        verifyAlertDialogShowing("save the game data");

        clickDialogButton(DialogInterface.BUTTON_POSITIVE);
        String content = fakeFileManager.getContentOf("gamedata.json");
        assertNotNull(content);
        assertTrue(content.contains("TOO-M"));
        assertTrue(content.contains("07:38"));
        assertTrue(content.contains("45:06"));

        // Validate game details

        click(btnReport);
        assertEquals(ReportFragment.class, visibleFragmentClass());

        int[] homeGoals = model.goalTotals("Home");
        assertEquals(2, homeGoals[0]);
        assertEquals(6, homeGoals[1]);
        assertEquals(1, homeGoals[2]);
        assertEquals(0, homeGoals[3]);
        assertEquals(9, homeGoals[4]);

        int[] awayGoals = model.goalTotals("Away");
        assertEquals(3, awayGoals[0]);
        assertEquals(5, awayGoals[1]);
        assertEquals(3, awayGoals[2]);
        assertEquals(0, awayGoals[3]);
        assertEquals(11, awayGoals[4]);

        int[] homePens = model.penaltyTotals("Home");
        assertEquals(4, homePens[0]);
        assertEquals(2, homePens[1]);
        assertEquals(4, homePens[2]);
        assertEquals(0, homePens[3]);
        assertEquals(10, homePens[4]);

        int[] awayPens = model.penaltyTotals("Away");
        assertEquals(4, awayPens[0]);
        assertEquals(2, awayPens[1]);
        assertEquals(4, awayPens[2]);
        assertEquals(0, awayPens[3]);
        assertEquals(10, awayPens[4]);

        Map<Integer, ScoresheetModel.PlayerStats> homeStats = model.getPlayerStats("Home");
        ScoresheetModel.PlayerStats home93stats = homeStats.get(93);
        assertEquals(2, home93stats.goals);
        assertEquals(3, home93stats.assists);
        assertEquals(0, home93stats.penaltyMins);

        GoalEvent goal2339 = model.goals("Home").get(3);
        assertEquals("23:39", goal2339.getGameTime());
        assertEquals("SH", goal2339.getSubType());
        assertEquals(93, goal2339.getPlayer());
        assertEquals(28, goal2339.getAssist1());
        assertEquals(0, goal2339.getAssist2());

    }
}
