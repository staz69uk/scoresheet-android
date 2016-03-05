package com.example.myapp3;

import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

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

    private GameEvent goalEvent(Team team) {
        GoalEvent event = new GoalEvent();
        event.setTeam(team.getName());
        return event;
    }

    @Test
    public void testClockConversion() {
        GameClock clock = new GameClock(1);
        assertEquals("00:05", clock.gameTimeFromClock("1955"));
        assertEquals("15:00", clock.gameTimeFromClock("0500"));
        assertEquals("15:30", clock.gameTimeFromClock("0430"));
        assertEquals("07:26", clock.gameTimeFromClock("1234"));
        clock.setPeriod(2);
        assertEquals("27:26", clock.gameTimeFromClock("1234"));
        clock.setPeriod(3);
        assertEquals("47:26", clock.gameTimeFromClock("1234"));
        assertEquals("59:49", clock.gameTimeFromClock("0011"));
    }

}
