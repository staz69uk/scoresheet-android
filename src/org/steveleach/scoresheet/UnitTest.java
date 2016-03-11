package org.steveleach.scoresheet;

import android.content.Context;
import junit.framework.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
        GameEvent event = new GameEvent();
        event.setPeriod(1);
        assertEquals("00:05", event.gameTimeFromClock("1955"));
        assertEquals("15:00", event.gameTimeFromClock("0500"));
        assertEquals("15:30", event.gameTimeFromClock("0430"));
        assertEquals("07:26", event.gameTimeFromClock("1234"));
        event.setPeriod(2);
        assertEquals("27:26", event.gameTimeFromClock("1234"));
        event.setPeriod(3);
        assertEquals("47:26", event.gameTimeFromClock("1234"));
        assertEquals("59:49", event.gameTimeFromClock("0011"));
    }

    private GoalEvent goalEvent(Team team, int period, String time, String player, int assist1, int assist2) {
        GoalEvent event = goalEvent(team);
        event.setSubType("Even");
        event.setPeriod(period);
        event.setGameTime(time);
        event.setPlayer(player);
        event.setAssist1(assist1);
        event.setAssist2(assist2);
        return event;
    }

    private PenaltyEvent penaltyEvent(Team team, int period, String time, String player, int minutes) {
        PenaltyEvent event = new PenaltyEvent();
        event.setTeam(team.getName());
        event.setPeriod(period);
        event.setGameTime(time);
        event.setPlayer(player);
        event.setSubType("HOOK");
        event.setMinutes(minutes);
        return event;
    }

    private ScoresheetModel _sampleModel() {
        ScoresheetModel model = new ScoresheetModel();
        model.addEvent(goalEvent(model.getHomeTeam(), 1, "05:00", "23", 1, 0 ));
        model.addEvent(goalEvent(model.getHomeTeam(), 1, "06:00", "35", 23, 4));
        model.addEvent(penaltyEvent(model.getHomeTeam(), 1, "06:00", "35", 2));
        model.addEvent(new PeriodEndEvent());
        model.addEvent(goalEvent(model.getAwayTeam(), 2, "29:00", "6", 0, 0));
        model.addEvent(new PeriodEndEvent());
        model.addEvent(goalEvent(model.getHomeTeam(), 3, "42:00", "16", 4, 1));

        return model;
    }

    @Test
    public void testFullReport() {
        ScoresheetModel model = new ScoresheetModel();
        ModelManager.addTestEvents(model);
        String report = model.fullReport();

        //System.out.println(report);
        assertTrue(report.contains("01:30 - Away Goal (E) scored by 2  from 1"));
        assertTrue(report.contains("2 : goals=1, assists=0, pen.mins=2"));
        assertTrue(report.contains("Home : 2 1 0 = 3"));
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
}
