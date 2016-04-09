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

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.steveleach.scoresheet.android.AndroidSystemContext;
import org.steveleach.scoresheet.support.ScoresheetStore;
import org.steveleach.scoresheet.support.FileManager;
import org.steveleach.scoresheet.support.JsonCodec;
import org.steveleach.scoresheet.model.*;
import org.steveleach.scoresheet.support.WeakList;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for the Ice Hockey Scoresheet app.
 * <p>
 * Most of the unit tests are focussed on the model package.
 *
 * @author Steve Leach
 */
@RunWith(MockitoJUnitRunner.class)
public class ScoresheetUnitTest {

    private ScoresheetModel model = null;

    @Mock
    FileManager fileManager;

    @Mock
    JsonCodec jsonCodec;

    @Mock
    AndroidSystemContext context;

    @Before
    public void setup() {
        model = new ScoresheetModel();
    }

    public static void addTestEvents(ScoresheetModel model) {
        model.addEvent(new GoalEvent(model.getPeriod(),"1950","Home","E",41,13,2));
        model.addEvent(new GoalEvent(model.getPeriod(),"1830","Away","E",2,1,0));
        model.addEvent(new PenaltyEvent(model.getPeriod(), "1515", "Away", "Hook", "2", 2));
        model.addEvent(new GoalEvent(model.getPeriod(),"0824","Home","SH",12,93,41));
        model.addEvent(new PeriodEndEvent(model.getPeriod()));
        model.addEvent(new GoalEvent(model.getPeriod(),"1813","Home","PP",24,41,0));
        model.addEvent(new PenaltyEvent(model.getPeriod(), "1213", "Home", "Fight", "2", 5));
    }

    @Test
    public void validateModel() {
        assertEquals(0, model.getHomeGoals());
        assertEquals(0, model.getAwayGoals());
        assertEquals(1, model.getPeriod());

        model.getEvents().add(goalEvent(model.getHomeTeam()));

        assertEquals(1, model.getHomeGoals());
        assertEquals(0, model.getAwayGoals());
    }

    private GoalEvent goalEvent(Team team) {
        GoalEvent event = new GoalEvent();
        event.setTeam(team.getName());
        return event;
    }

    @Test
    public void testClockConversion() {
        assertEquals("00:05", model.gameTimeFromClock(1, "1955"));
        assertEquals("15:00", model.gameTimeFromClock(1, "0500"));
        assertEquals("15:30", model.gameTimeFromClock(1, "0430"));
        assertEquals("07:26", model.gameTimeFromClock(1, "1234"));
        assertEquals("27:26", model.gameTimeFromClock(2, "1234"));
        assertEquals("47:26", model.gameTimeFromClock(3, "1234"));
        assertEquals("59:49", model.gameTimeFromClock(3, "0011"));
        assertEquals("59:49", model.gameTimeFromClock(3, "011"));
    }

    @Test
    public void testBadClockConversions() {
        assertEquals(GameEvent.GAME_TIME_ERROR, model.gameTimeFromClock(1, null));
        assertEquals(GameEvent.GAME_TIME_ERROR, model.gameTimeFromClock(1, ""));
        assertEquals(GameEvent.GAME_TIME_ERROR, model.gameTimeFromClock(1, "Hello"));
        assertEquals(GameEvent.GAME_TIME_ERROR, model.gameTimeFromClock(1, "12"));
        assertEquals(GameEvent.GAME_TIME_ERROR, model.gameTimeFromClock(1, "12:34"));
        model.setRules(null);
        assertEquals(GameEvent.GAME_TIME_ERROR, model.gameTimeFromClock(1, "1234"));
    }

    @Test
    public void testClockConversionOnAdd() {
        GoalEvent event = new GoalEvent();
        event.setPeriod(1);
        event.setClockTime("1234");
        assertEquals(GameEvent.GAME_TIME_ERROR, event.getGameTime());
        model.addEvent(event);
        assertEquals("07:26", event.getGameTime());
    }

    @Test
    public void testFullReport() {
        addTestEvents(model);
        String report = model.fullReport();

        //System.out.println(report);
        assertTrue(report.contains("01:30 - Away Goal (E) scored by 2 from 1"));
        assertTrue(report.contains("2 : goals=1, assists=0, pen.mins=2"));
        assertTrue(report.contains("Home : 2 1 0 0 = 3"));
        assertTrue(report.contains("P 2, 2m for Hook @ 04:45, 04:45 - 06:45"));
    }

    @Test
    public void validateScoresheetStore() throws JSONException {
        when(context.isExternalStorageAvailable()).thenReturn(true);
        when(context.getScoresheetFolder()).thenReturn(new File("."));
        when(jsonCodec.toJson(anyObject())).thenReturn("{a}");

        ScoresheetStore store = new ScoresheetStore(fileManager,jsonCodec,context);

        String status = store.save(model);

        assertEquals("Saved gamedata", status);
    }

    @Test
    public void testSampleData() {
        addTestEvents(model);

        assertEquals(2, model.getPeriod());
        assertEquals(3, model.getHomeGoals());
        assertEquals(1, model.getAwayGoals());
    }

    @Test
    public void testJsonRoundTrip() throws JSONException {
        JsonCodec codec = new JsonCodec();
        ScoresheetModel model1 = new ScoresheetModel();
        model1.setAwayTeam(new Team("Badguys"));
        addTestEvents(model1);

        assertNotEquals(0, model1.getEvents().size());

        String json = codec.toJson(model1);
        assertNotNull(json);
        assertTrue(json.startsWith("{"));
        assertTrue(json.contains("04:45"));
        assertTrue(json.contains("Hook"));
        assertTrue(json.endsWith("}"));

        ScoresheetModel model2 = new ScoresheetModel();
        codec.fromJson(model2, json);

        assertEquals(model1.getEvents().size(), model2.getEvents().size());
        assertEquals(model1.getAwayGoals(), model2.getAwayGoals());
        assertEquals(model1.getAwayTeam().getName(), model2.getAwayTeam().getName());
    }

    @Test
    public void testTeam() {
        Team team = new Team("Reds");
        assertEquals("Reds",team.getName());
        team.setName("Blues");
        assertEquals("Blues",team.getName());
        team = new Team();
        assertEquals("",team.getName());
    }

    @Test
    public void testModelAware() {
        ScoresheetModel model = new ScoresheetModel();
        final boolean[] hasModel = new boolean[] {false};
        ModelAware receiver = new ModelAware() {
            @Override
            public void setModel(ScoresheetModel model) {
                hasModel[0] = true;
            }
            @Override
            public void onModelUpdated(ModelUpdate update) {
                assertEquals("Something", update.getSummary());
            }
        };
        receiver.setModel(model);
        ModelUpdate update = new ModelUpdate("Something");
        receiver.onModelUpdated(update);
        assertTrue(hasModel[0]);
    }

    @Test
    public void testPeriodEnd() {
        PeriodEndEvent event = new PeriodEndEvent(2);
        model.addEvent(event);
        assertEquals("40:00 - Period 2 ended", event.toString());
    }

    @Test
    public void testModelExtras() {
        model.addOfficial(GameOfficial.Role.REFEREE, "Fred");
        assertEquals(1, model.getOfficials().size());
        assertEquals(GameOfficial.Role.REFEREE, model.getOfficials().get(0).getRole());

        assertEquals(3, model.getRules().getRegulationPeriods());
        assertEquals(20, model.getRules().getPeriodMinutes());
        assertEquals(5, model.getRules().getOvertimeMinutes());
        assertFalse(model.getRules().isAllowTie());

        model.getHomeTeam().getPlayers().put(25, "John Smith");
        model.getHomeTeam().getPlayers().put(57, "John Johnson");
        assertEquals(2, model.getHomeTeam().getPlayers().size());

        model.setHomeTeam(new Team());
        model.setAwayTeam(new Team());
        model.setRules(new GameRules());

        assertEquals(null, new ModelUpdate().getSummary());

        PenaltyEvent event = new PenaltyEvent();
        event.setGameTime(null);
        assertEquals("00:00", event.finishTime());
    }

    @Test
    public void validatePenalty() {
        PenaltyEvent event = new PenaltyEvent();
        event.setPeriod(2);
        event.setClockTime("1500");
        event.setMinutes(10);
        event.setPlayer("21");
        event.setSubType("FIGHT");
        model.addEvent(event);
        assertEquals("25:00 - Home Penalty 10m (FIGHT) for 21 (35:00)", event.toString());
    }

    @Test
    public void validateBeans() {
        BeanTester tester = new BeanTester();
        tester.testBean(GameRules.class);
        tester.testBean(GameOfficial.class);
        tester.testBean(Team.class);
        tester.testBean(ScoresheetModel.class);
    }

    @Test
    public void testModelListener() {
        ScoresheetModel model = new ScoresheetModel();

        final AtomicBoolean flag = new AtomicBoolean(false);

        ModelAware listener = new ModelAware() {
            @Override
            public void setModel(ScoresheetModel model) {
                // nothing to see here
            }

            @Override
            public void onModelUpdated(ModelUpdate update) {
                flag.set(true);
            }
        };

        model.addListener(listener);

        model.notifyListeners(new ModelUpdate("Updated"));

        assertTrue(flag.get());
    }

    @Test
    public void testBadModelListener() {
        model.addListener(new ModelAware() {
            @Override
            public void setModel(ScoresheetModel model) {}

            @Override
            public void onModelUpdated(ModelUpdate update) {
                throw new RuntimeException("Ignore more");
            }
        });
        model.notifyListeners(new ModelUpdate("Update"));
    }

    @Test
    public void verifyBasicWeakListFunctionality() {
        WeakList<String> list = new WeakList<>();
        String bob = "Bob";
        list.add(bob);
        assertTrue(list.containsItem(bob));
        assertFalse(list.containsItem("Fred"));
    }

    @Test
    public void verifyBasicFileManagerFunctionality() throws IOException {
        final String testText = "Testing";
        FileManager fileManager = new FileManager();
        File tempFile = new File(fileManager.tempDir(),"temp.txt");
        fileManager.writeTextFile(tempFile, testText);
        String content = fileManager.readTextFileContent(tempFile);
        assertEquals(testText,content);

        File tempFile2 = new File(fileManager.tempDir(),"temp2.txt");
        fileManager.copyFile(tempFile,tempFile2);
        String content2 = fileManager.readTextFileContent(tempFile2);
        assertEquals(testText,content2);

        fileManager.delete(tempFile);
        fileManager.delete(tempFile2);

        assertTrue(fileManager.exists(fileManager.tempDir()));
        fileManager.ensureDirectoryExists(fileManager.tempDir());
        assertNotNull(fileManager.dirContents(fileManager.tempDir()));
    }

    @Test
    public void verifyModelSaveAndLoad() throws IOException {
        when(fileManager.readTextFileContent(any())).thenReturn(testModelJson());
        when(fileManager.exists(any())).thenReturn(true);
        when(context.getScoresheetFolder()).thenReturn(new File("."));

        ScoresheetStore store = new ScoresheetStore(fileManager,new JsonCodec(),context);

        assertEquals(0, model.getEvents().size());

        String result = store.loadInto(model, "test.json");

        System.out.println("Result="+result);

        assertEquals(7, model.getEvents().size());
    }

    private String testModelJson() {
        ScoresheetModel sourceModel = new ScoresheetModel();
        addTestEvents(sourceModel);
        return new JsonCodec().toJson(sourceModel);
    }
}
