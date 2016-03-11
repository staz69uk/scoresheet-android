package org.steveleach.scoresheet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

//import org.junit.runner.RunWith;
//import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by steve on 05/03/16.
 */
//@RunWith(MockitoJUnitRunner.class)
public class UnitTest {

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

    private ScoresheetModel sampleModel() {
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
        System.out.println(sampleModel().fullReport());
    }
}
