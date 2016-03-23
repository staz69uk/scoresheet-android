package org.steveleach.scoresheet.test;

import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.steveleach.scoresheet.SystemContext;
import org.steveleach.scoresheet.io.AndroidScoresheetStore;
import org.steveleach.scoresheet.io.FileManager;
import org.steveleach.scoresheet.io.JsonCodec;
import org.steveleach.scoresheet.model.*;

/**
 * Created by steve on 05/03/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UnitTest {

    @Mock
    FileManager fileManager;

    @Mock
    JsonCodec jsonCodec;

    @Mock
    SystemContext context;

    @Test
    public void validateModel() {
        ScoresheetModel model = new ScoresheetModel();
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
        GameRules rules = new GameRules();
        GameEvent event = new GameEvent();
        event.setPeriod(1);
        assertEquals("00:05", event.gameTimeFromClock("1955", rules));
        assertEquals("15:00", event.gameTimeFromClock("0500", rules));
        assertEquals("15:30", event.gameTimeFromClock("0430", rules));
        assertEquals("07:26", event.gameTimeFromClock("1234", rules));
        event.setPeriod(2);
        assertEquals("27:26", event.gameTimeFromClock("1234", rules));
        event.setPeriod(3);
        assertEquals("47:26", event.gameTimeFromClock("1234", rules));
        assertEquals("59:49", event.gameTimeFromClock("0011", rules));
        assertEquals("59:49", event.gameTimeFromClock("011", rules));
    }

    @Test
    public void testBadClockConversions() {
        GameRules rules = new GameRules();
        GameEvent event = new GameEvent();
        event.setPeriod(1);
        assertEquals("00:00", event.gameTimeFromClock(null, rules));
        assertEquals("00:00", event.gameTimeFromClock("", rules));
        assertEquals("00:00", event.gameTimeFromClock("Hello", rules));
        assertEquals("00:00", event.gameTimeFromClock("12", rules));
        assertEquals("00:00", event.gameTimeFromClock("12:34", rules));
        assertEquals("00:00", event.gameTimeFromClock("1234", null));
    }

    @Test
    public void testFullReport() {
        ScoresheetModel model = new ScoresheetModel();
        ModelManager.addTestEvents(model);
        String report = model.fullReport();

        //System.out.println(report);
        assertTrue(report.contains("01:30 - Away Goal (E) scored by 2 from 1"));
        assertTrue(report.contains("2 : goals=1, assists=0, pen.mins=2"));
        assertTrue(report.contains("Home : 2 1 0 0 = 3"));
        assertTrue(report.contains("Player 2 : 2m for Hook, given 04:45, start 04:45, end 06:45"));
    }

    @Test
    public void validateScoresheetStore() throws JSONException {
        when(context.isExternalStorageAvailable()).thenReturn(true);
        when(jsonCodec.toJson(anyObject())).thenReturn("{a}");

        AndroidScoresheetStore store = new AndroidScoresheetStore(fileManager,jsonCodec,context);

        ScoresheetModel model = new ScoresheetModel();

        String status = store.save(model);

        assertEquals("Saved gamedata", status);
    }

    @Test
    public void testSampleData() {
        ScoresheetModel model = new ScoresheetModel();
        ModelManager.addTestEvents(model);

        assertEquals(2, model.getPeriod());
        assertEquals(3, model.getHomeGoals());
        assertEquals(1, model.getAwayGoals());
    }

    @Test
    public void testJsonRoundTrip() throws JSONException {
        JsonCodec codec = new JsonCodec();
        ScoresheetModel model1 = new ScoresheetModel();
        model1.setAwayTeam(new Team("Badguys"));
        ModelManager.addTestEvents(model1);

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
    public void testGenericGameEvent() {
        assertEquals("00:00 - Goal     Home    ", new GameEvent().toString());
    }

    @Test
    public void testPeriodEnd() {
        PeriodEndEvent event = new PeriodEndEvent(2, new GameRules());
        assertEquals("40:00 - Period 2 ended", event.toString());
    }

    @Test
    public void testModelExtras() {
        ScoresheetModel model = new ScoresheetModel();
        model.addOfficial(GameOfficial.Role.REFEREE, "Fred");
        assertEquals(1, model.getOfficials().size());
        assertEquals(GameOfficial.Role.REFEREE, model.getOfficials().get(0).getRole());

        assertEquals(3,model.getRules().getRegulationPeriods());
        assertEquals(20,model.getRules().getPeriodMinutes());
        assertEquals(5,model.getRules().getOvertimeMinutes());
        assertFalse(model.getRules().isAllowTie());

        model.getHomeTeam().getPlayers().put(25,"John Smith");
        model.getHomeTeam().getPlayers().put(57,"John Johnson");
        assertEquals(2, model.getHomeTeam().getPlayers().size());

        model.setHomeTeam(new Team());
        model.setAwayTeam(new Team());
        model.setRules(new GameRules());

        new ModelManager();

        PenaltyEvent event = new PenaltyEvent();
        event.setGameTime(null);
        assertEquals("00:00", event.finishTime());
    }

    @Test
    public void validatePenalty() {
        PenaltyEvent event = new PenaltyEvent();
        event.setPeriod(2);
        event.setClockTime("1500",new GameRules());
        event.setMinutes(10);
        event.setPlayer("21");
        event.setSubType("FIGHT");
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
}
