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

import static org.junit.Assert.*;
import static org.steveleach.ihscoresheet.R.id.*;

/**
 * Unit tests for UI methods.
 *
 * @author Steve Leach
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
public class ScoresheetUITest {

    ScoresheetActivity activity;
    ScoresheetModel model;

    @Before
    public void setup() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        // Create the UI
        activity = Robolectric.setupActivity(ScoresheetActivity.class);

        assertNotNull(activity);

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
        assertEquals(22, panel.getChildCount());
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
        click(btnAwayPen);
        assertEquals(PenaltyFragment.class, visibleFragmentClass());

        setField(fldPenaltyClock,"430");
        setField(fldPenaltyPlayer,"25");
        setField(fldPenaltyMins,"5");
        setField(fldPenaltyCode,"Fight");
        click(btnPenaltyDone);

        // After adding the penalty, validate the UI and model state
        assertEquals(HistoryFragment.class, visibleFragmentClass());
        assertEquals(3, model.getEvents().size());

        PenaltyEvent penalty = (PenaltyEvent) model.getEvents().get(2);
        assertEquals(2, penalty.getPeriod());
        assertEquals("35:30", penalty.getGameTime());
        assertEquals("25", penalty.getPlayer());
        assertEquals(model.getAwayTeam().getName(), penalty.getTeam());
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
        click(btnHomeGoal);
        assertEquals(GoalFragment.class, visibleFragmentClass());

        setField(fldClock,"1234");
        setField(fldScoredBy,"41");
        click(btnDone);

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
}
