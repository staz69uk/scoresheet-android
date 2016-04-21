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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowEnvironment;
import org.steveleach.scoresheet.model.GoalEvent;
import org.steveleach.scoresheet.model.PenaltyEvent;
import org.steveleach.scoresheet.model.PeriodEndEvent;
import org.steveleach.scoresheet.model.ScoresheetModel;
import org.steveleach.scoresheet.ui.*;
import org.steveleach.ihscoresheet.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class ScoresheetUITest {

    ScoresheetActivity activity;
    ScoresheetModel model;
    FakeFileManager fakeFileManager;

    @Before
    public void setup() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        // Create the UI
        activity = Robolectric.setupActivity(ScoresheetActivity.class);

        assertNotNull(activity);

        fakeFileManager =  new FakeFileManager();
        activity.setFileManager(fakeFileManager);

        // Get access to the underlying data model and validate its initial state
        model = activity.getModel();

        assertEquals(0, model.getEvents().size());
        assertEquals(1, model.getPeriod());
        assertEquals(0, model.getHomeGoals());
    }

    @Test
    public void basicIntegrationTest() {
        showDefaultFragment();
        clickNextPeriod();
        clickAddGoal();
        clickAddPenalty();
        selectHelpMenu();
        showDefaultFragment();
        clickReportButton();
        selectLoadMenu();
        showDefaultFragment();
    }

    @Test
    public void clearEventsTest() {
        // Set up the model with an event
        model.addEvent(new PeriodEndEvent(1));
        assertEquals(1, model.getEvents().size());

        clickMenuItem(menuNewGame);
        verifyAlertDialogShowing("clear all events");

        clickDialogButton(DialogInterface.BUTTON_NEGATIVE);

        // The above UI activity should not change the model
        assertEquals(1, model.getEvents().size());

        clickMenuItem(menuNewGame);
        verifyAlertDialogShowing("clear all events");

        clickDialogButton(DialogInterface.BUTTON_POSITIVE);

        // The above UI activity should result in all the events being removed from the model
        assertEquals(0, model.getEvents().size());
    }

    @Test
    public void aboutDialogTest() {
        clickMenuItem(menuAbout);

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        ShadowAlertDialog shadow = Shadows.shadowOf(dialog);
        assertEquals("Ice Hockey Score Sheet", shadow.getTitle());
        shadow.dismiss();
    }

    @Test
    public void refreshTest() {
        clickMenuItem(menuRefresh);
    }

    private void clickDialogButton(int buttonID) {
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        Button button = dialog.getButton(buttonID);
        assertNotNull(button);
        button.performClick();
        assertFalse(dialog.isShowing());
    }

    private void clickMenuItem(int optionId) {
        activity.onOptionsItemSelected(new RoboMenuItem(optionId));
    }

    @NotNull
    private AlertDialog verifyAlertDialogShowing(String expectedMessage) {
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        ShadowAlertDialog shadow = Shadows.shadowOf(dialog);
        assertNotNull(shadow.getMessage());
        assertTrue("Dialog text: " + expectedMessage, shadow.getMessage().toString().contains(expectedMessage));
        return dialog;
    }

    private void selectLoadMenu() {
        clickMenuItem(menuLoad);
        assertTrue( "Saves fragment should be visible", activity.getVisibleFragment() instanceof SavesFragment);
    }

    private void clickReportButton() {
        click(btnReport);
        assertTrue( "Report fragment should be visible", activity.getVisibleFragment() instanceof ReportFragment);

        ReportFragment fragment = (ReportFragment) activity.getVisibleFragment();
        ViewGroup root = (ViewGroup) fragment.getView();
        assertNotNull(root);
        ViewGroup panel = (ViewGroup) root.findViewById(panelNew);
        assertEquals(24, panel.getChildCount());
        assertTrue( panel.getChildAt(0) instanceof TextView);
        assertTrue( panel.getChildAt(1) instanceof TableLayout);
        assertTrue( panel.getChildAt(2) instanceof Space);
    }

    private void selectHelpMenu() {
        // Select "help" from the menu
        clickMenuItem(menuHelp);
        assertTrue( "Help fragment should be visible", activity.getVisibleFragment() instanceof HelpFragment);
    }

    private void clickAddPenalty() {
        // Click the "away penalty" button and add penalty details
        addPenalty("0430", "Fight", "5", "Away", "25");

        // After adding the penalty, validate the UI and model state
        assertEquals(HistoryFragment.class, visibleFragmentClass());
        assertEquals(3, model.getEvents().size());

        PenaltyEvent penalty = (PenaltyEvent) model.getEvents().get(2);
        assertEquals(2, penalty.getPeriod());
        assertEquals("35:30", penalty.getGameTime());
        assertEquals("25", penalty.getPlayer());
        assertEquals(model.getAwayTeam().getName(), penalty.getTeam());
    }

    private void addPenalty(String clock, String subtype, String minutes, String team, String player) {
        if (team.equals(model.getHomeTeam().getName())) {
            click(btnHomePen);
        } else {
            click(btnAwayPen);
        }

        assertEquals(PenaltyFragment.class, visibleFragmentClass());

        setField(fldPenaltyClock,clock);
        setField(fldPenaltyPlayer,player);
        setField(fldPenaltyMins,minutes);
        setField(fldPenaltyCode,subtype);

        click(btnPenaltyDone);
    }

    private void click(int id) {
        activity.findViewById(id).performClick();
    }

    private Class<?> visibleFragmentClass() {
        return activity.getVisibleFragment().getClass();
    }

    private void setField(int fieldId, String fieldValue) {
        ((EditText)activity.findViewById(fieldId)).setText(fieldValue);
    }

    private void clickAddGoal() {
        // Click the "home goal" button and add goal details
        addGoal("1234","E","Home","41","","");

        // After adding the goal, validate the UI and model state
        assertEquals(HistoryFragment.class, visibleFragmentClass());
        assertEquals(2, model.getEvents().size());

        GoalEvent event = (GoalEvent) model.getEvents().get(1);
        assertEquals(2, event.getPeriod());
        assertEquals("27:26", event.getGameTime());
        assertEquals("41", event.getPlayer());
        assertEquals(model.getHomeTeam().getName(), event.getTeam());
        assertEquals(1, model.getHomeGoals());
        assertEquals(0, model.getAwayGoals());
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

    private void showDefaultFragment() {
        // Make sure the history fragment is shown by default
        activity.showDefaultFragment();
        assertTrue( "History fragment should be visible", activity.getVisibleFragment() instanceof HistoryFragment);
    }

    @Test
    public void testGameSave() throws IOException {
        assertEquals(0, fakeFileManager.fileCount());

        clickMenuItem(menuSave);
        verifyAlertDialogShowing("save the game data");

        clickDialogButton(DialogInterface.BUTTON_POSITIVE);

        assertEquals(2, fakeFileManager.fileCount());

        String content = fakeFileManager.getContentOf("gamedata.json");
        assertNotNull(content);
        assertTrue(content.contains("Ice Hockey Scoresheet Data"));
    }

    @Test
    public void testGameLoad() {
        model.addEvent(new PeriodEndEvent());
        assertEquals(1, model.getEvents().size());

        assertEquals(0, fakeFileManager.fileCount());
        activity.getStore().save(model);
        assertEquals(2, fakeFileManager.fileCount());

        model.clearEvents();
        assertEquals(0, model.getEvents().size());

        clickMenuItem(menuLoad);
        assertEquals(SavesFragment.class, visibleFragmentClass());

        ListView savesList = (ListView) activity.findViewById(gameSavesList);
        assertNotNull(savesList);
        assertEquals(2,savesList.getAdapter().getCount());
        Shadows.shadowOf(savesList).performItemClick(0);

        verifyAlertDialogShowing("load game details");
        clickDialogButton(DialogInterface.BUTTON_POSITIVE);

        assertEquals(HistoryFragment.class, visibleFragmentClass());

        assertEquals(1, model.getEvents().size());
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
        assertEquals("93", goal2339.getPlayer());
        assertEquals(28, goal2339.getAssist1());
        assertEquals(0, goal2339.getAssist2());

    }
}
