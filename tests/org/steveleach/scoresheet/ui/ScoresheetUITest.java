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
package org.steveleach.scoresheet.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.internal.Shadow;
import org.robolectric.shadows.ShadowAlertDialog;
import org.steveleach.ihscoresheet.R;
import org.steveleach.scoresheet.AbstractUITest;
import org.steveleach.scoresheet.FakeFileManager;
import org.steveleach.scoresheet.model.*;
import org.steveleach.scoresheet.support.FileManager;
import org.steveleach.scoresheet.support.JsonCodec;
import org.steveleach.scoresheet.support.ScoresheetStore;
import org.steveleach.scoresheet.support.SystemContext;
import org.steveleach.scoresheet.ui.*;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.steveleach.ihscoresheet.R.id.*;

/**
 * End-to-end tests for user interface.
 *
 * @author Steve Leach
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
public class ScoresheetUITest extends AbstractUITest {

    @Before
    public void setupTestEnvironment() {
        super.setupTestEnvironment();

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
        assertEquals(15, panel.getChildCount());
        assertTrue( panel.getChildAt(0) instanceof TextView);
        assertTrue( panel.getChildAt(1) instanceof TableLayout);

        fragment.onModelUpdated(ModelUpdate.ALL_CHANGED);
        assertEquals(15, panel.getChildCount());
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
        assertEquals(25, penalty.getPlayer());
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

        ((ModelAware)activity.getVisibleFragment()).onModelUpdated(ModelUpdate.GAME_CHANGED);

        click(btnPenaltyDone);
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
        assertEquals(41, event.getPlayer());
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

    @Test
    public void testGameDetailsUpdate() {
        assertEquals("Home", model.homeTeamName());
        assertEquals("Away", model.awayTeamName());
        assertEquals("", model.getGameLocation());

        clickMenuItem(menuGameDetails);
        assertEquals(GameFragment.class, visibleFragmentClass());

        setField(fldHomeTeamName, "Winners");
        setField(fldAwayTeamName, "Losers");
        setField(fldGameVenue, "The moon");

        click(btnGameOK);

        assertEquals(HistoryFragment.class, visibleFragmentClass());

        assertEquals("Winners", model.homeTeamName());
        assertEquals("Losers", model.awayTeamName());
        assertEquals("The moon", model.getGameLocation());
    }

    @Test
    public void testChangeModel() {
        ScoresheetModel model2 = new ScoresheetModel();
        model2.setAwayTeamName("Blues");

        activity.setModel(model2);

        assertEquals("Blues", activity.getModel().awayTeamName());
    }

    @Test
    public void testSaveLoadState() {
        model.setHomeTeamName("Reds");
        model.addEvent(new PeriodEndEvent(1));
        Bundle bundle = new Bundle();

        activity.onSaveInstanceState(bundle);

        String json = bundle.getString(activity.STATE_KEY);
        assertNotNull(json);

        ScoresheetModel model1 = new ScoresheetModel();
        new JsonCodec().fromJson(model1, json);

        assertEquals(1, model1.getEvents().size());
        assertEquals("Reds", model1.homeTeamName());

        model.reset();

        assertEquals(0, model.getEvents().size());
        assertEquals("Home", model.homeTeamName());

        activity.onRestoreInstanceState(bundle);

        assertEquals(1, model.getEvents().size());
        assertEquals("Reds", model.homeTeamName());
    }

    public View getItemViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    @Test
    public void testDeleteFile() {
        model.addEvent(new PeriodEndEvent(1));

        activity.getStore().save(model);

        assertEquals(2, fakeFileManager.fileCount());

        clickMenuItem(menuLoad);
        assertEquals(SavesFragment.class, visibleFragmentClass());

        SavesFragment fragment = (SavesFragment) activity.getVisibleFragment();

        ListView savesList = (ListView) activity.findViewById(gameSavesList);

        assertNotNull(savesList);
        assertEquals(2,savesList.getAdapter().getCount());

        String item1 = (String) savesList.getAdapter().getItem(1);
        assertTrue( item1.startsWith("gamedata"));
        assertTrue( item1.endsWith("--2-00-00.json"));

        fragment.handleContextMenu(fileMenuDelete, 1);

        verifyAlertDialogShowing("delete this saved game");
        clickDialogButton(DialogInterface.BUTTON_POSITIVE);

        assertEquals(1, fakeFileManager.fileCount());
    }

    @Test
    public void testToggleAllSaves() {
        activity.getStore().save(model);

        assertEquals(2, fakeFileManager.fileCount());

        clickMenuItem(menuLoad);
        assertEquals(SavesFragment.class, visibleFragmentClass());

        click(savesAllFilesSwitch);
    }

    @Test
    public void testRenameFile() {
        activity.getStore().save(model);

        assertEquals(2, fakeFileManager.fileCount());

        clickMenuItem(menuLoad);
        assertEquals(SavesFragment.class, visibleFragmentClass());

        SavesFragment fragment = (SavesFragment) activity.getVisibleFragment();

        ListView savesList = (ListView) activity.findViewById(gameSavesList);

        assertNotNull(savesList);
        assertEquals(2,savesList.getAdapter().getCount());

        String item1 = (String) savesList.getAdapter().getItem(1);
        assertTrue( item1.startsWith("gamedata"));
        assertTrue( item1.endsWith("--1-00-00.json"));

        fragment.handleContextMenu(fileMenuRename, 1);

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());

        EditText renameField = (EditText) dialog.findViewById(fldNewName);
        assertNotNull(renameField);

        renameField.setText("gamedata-testing");
        clickDialogButton(DialogInterface.BUTTON_POSITIVE);

        String item2 = (String) savesList.getAdapter().getItem(1);
        assertEquals("gamedata-testing.json", item2);
    }

    @Test
    public void testDeleteHistoryItem() {
        model.addEvent(new PeriodEndEvent(1));
        model.addEvent(new PeriodEndEvent(2));

        HistoryFragment fragment = (HistoryFragment) activity.getVisibleFragment();

        ListView list = (ListView)activity.findViewById(historyList2);
        assertNotNull(list);

        assertEquals(2, list.getAdapter().getCount());

        fragment.handleContextMenu(historyMenuDelete,1);

        verifyAlertDialogShowing("delete this game event");
        clickDialogButton(DialogInterface.BUTTON_POSITIVE);

        assertEquals(1, list.getAdapter().getCount());

        assertEquals(1, model.getEvents().size());

        assertEquals(2, model.getEvents().get(0).getPeriod());
    }

    @Test
    public void testMinLengthFocusChangeListener() {
        EditText field = new EditText(activity);

        ScoresheetFocusChangeListener listener = new ScoresheetFocusChangeListener(3);

        assertNull(field.getError());

        listener.onFocusChange(field, true);

        assertNull(field.getError());

        listener.onFocusChange(field, false);

        assertEquals("Minimum length is 3", field.getError());

        field.setText("12");

        listener.onFocusChange(field, false);

        assertEquals("Minimum length is 3", field.getError());

        field.setText("123");

        listener.onFocusChange(field, false);

        assertNull(field.getError());
    }

    @Test
    public void miscTests() {
        // Basically a bunch of silly stuff to get coverage up to help avoid "broken window syndrome"
        new GoalFragment().onModelUpdated(ModelUpdate.ALL_CHANGED);

        ScoresheetFragment fragment = new ScoresheetFragment() {};
        assertEquals(0, fragment.contextMenuId());
        assertEquals(R.menu.historycontextenu, new HistoryFragment().contextMenuId());
        assertEquals(R.menu.filescontextmenu, new SavesFragment().contextMenuId());

        fragment.onCreateContextMenu(null,null,null);

    }

    @Test
    public void testEditGoal() {
        addPenalty("1500", "HOOK", "2", "Home", "41");
        addGoal("1000", "E", "Home", "41", "", "");

        ListView list = (ListView)activity.findViewById(historyList2);

        assertEquals(2, list.getAdapter().getCount());

        assertEquals(HistoryFragment.class, visibleFragmentClass());

        HistoryFragment fragment = (HistoryFragment) activity.getVisibleFragment();
        fragment.handleContextMenu(historyMenuEdit, 0);

        assertEquals(GoalFragment.class, visibleFragmentClass());

        setField(fldAssist1, "12");
        click(btnDone);

        assertEquals(12, ((GoalEvent)model.getEvents().get(1)).getAssist1());
    }

    @Test
    public void testEditPenalty() {
        addPenalty("1500", "HOOK", "2", "Home", "41");
        addGoal("1000", "E", "Home", "41", "", "");

        ListView list = (ListView)activity.findViewById(historyList2);

        assertEquals(2, list.getAdapter().getCount());

        assertEquals(HistoryFragment.class, visibleFragmentClass());

        HistoryFragment fragment = (HistoryFragment) activity.getVisibleFragment();
        fragment.handleContextMenu(historyMenuEdit, 1);

        assertEquals(PenaltyFragment.class, visibleFragmentClass());

        setField(fldPenaltyCode, "HOLD");
        click(btnPenaltyDone);

        assertEquals("HOLD", model.getEvents().get(0).getSubType());
    }

    @Test
    public void testPlayerList() throws IOException {
        // Read the sample file
        FileManager fm = new FileManager();
        String json = fm.readTextFileContent(new File("testdata/Teams/team-v1_0_0.json"));
        Team team = new JsonCodec().teamFromJson(json);

        assertEquals("J Jenkins",team.playerName(11));

        FakeFileManager ffm = new FakeFileManager();
        activity.getStore().setFileManager(ffm);

        activity.getStore().saveTeam(team);

        clickMenuItem(menuGameDetails);
        assertEquals(GameFragment.class, visibleFragmentClass());
        setField(fldHomeTeamName,"Teeme");

        click(btnHomePlayers);
        assertEquals(PlayersFragment.class, visibleFragmentClass());

        ((ModelAware)activity.getVisibleFragment()).onModelUpdated(ModelUpdate.GAME_CHANGED);

        TableLayout table = (TableLayout) activity.findViewById(playersTable);
        assertEquals(6, table.getChildCount()); // 5 players plus a header
        PlayerTableRow firstRow = (PlayerTableRow)table.getChildAt(1);  // Skip header row
        assertEquals("11",firstRow.numberField.getText().toString());
        assertEquals("J Jenkins",firstRow.nameField.getText().toString());
        firstRow.numberField.setText("12");
        firstRow.onFocusChange(firstRow.numberField,false);
        firstRow.onCheckedChanged(firstRow.activeSwitch,false);

        assertNull(model.getHomeTeam().playerName(11));
        assertEquals("J Jenkins",model.getHomeTeam().playerName(12));

        click(btnAddPlayer);
        assertEquals(7, table.getChildCount());

        click(btnClosePlayers);
        assertEquals(GameFragment.class, visibleFragmentClass());
    }

    @Test
    public void testPlayerListSave() {
        FakeFileManager ffm = new FakeFileManager();
        activity.getStore().setFileManager(ffm);

        model.getHomeTeam().addPlayer(21,"A Player");

        clickMenuItem(menuGameDetails);
        assertEquals(GameFragment.class, visibleFragmentClass());
        setField(fldHomeTeamName,"Test");

        click(btnHomePlayers);
        assertEquals(PlayersFragment.class, visibleFragmentClass());

        assertEquals("Test", model.homeTeamName());

        click(btnSavePlayers);
        verifyAlertDialogShowing("save this player list");

        clickDialogButton(DialogInterface.BUTTON_POSITIVE);

        assertEquals(1, ffm.fileCount());

        String firstFileName = ffm.files.keySet().iterator().next();
        assertTrue(firstFileName.endsWith("test.json"));
    }

    @Test
    public void testChangeListener() {
        EditText field = new EditText(activity);
        TextView nameView = new TextView(activity);
        Team team = new Team();
        team.addPlayer(41,"Fred");

        ScoresheetFocusChangeListener.setPlayerNumField(field,nameView,team);

        field.setText("41");
        field.getOnFocusChangeListener().onFocusChange(field, false);

        assertEquals("Fred", nameView.getText().toString());
    }

    @Test
    public void testPlayersFragmentStoreLoadFailure() throws IOException {
        FileManager fm = mock(FileManager.class);
        activity.getStore().setFileManager(fm);

        when(fm.readTextFileContent(any())).thenThrow(IOException.class);

        clickMenuItem(menuGameDetails);
        assertEquals(GameFragment.class, visibleFragmentClass());
        setField(fldHomeTeamName,"Test");

        click(btnHomePlayers);
        assertEquals(PlayersFragment.class, visibleFragmentClass());
    }

    @Test
    public void testSupportedLanguages() {
        HelpFragment fragment = new HelpFragment();
        fragment.context = mock(SystemContext.class);

        when(fragment.context.defaultLanguage()).thenReturn("xx");

        assertEquals("en",fragment.getSupportedLanguage());
    }

    @Test
    public void testBackPress() {
        clickMenuItem(menuGameDetails);
        assertEquals(GameFragment.class, visibleFragmentClass());

        activity.onBackPressed();
        assertEquals(HistoryFragment.class, visibleFragmentClass());

        activity.onBackPressed();
        assertEquals(HistoryFragment.class, visibleFragmentClass());
    }

    @Test
    public void testListContextMenu() {
        AdapterView view = mock(AdapterView.class);
        AdapterView.AdapterContextMenuInfo info = new AdapterView.AdapterContextMenuInfo(view,21,32);
        MenuItem item = mock(MenuItem.class);
        when(item.getMenuInfo()).thenReturn(info);
        assertEquals(21, ScoresheetActivity.listContextMenuPosition(item));

        ScoresheetFragment fragment = new ScoresheetFragment() {};

        assertTrue( fragment.onContextItemSelected(item) );
    }

    @Test
    public void testSetViewImage() {
        ImageView view = new ImageView(activity);
        ScoresheetFragment.setViewImage(view,-123);
    }
}
