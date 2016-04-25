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

import org.junit.Before;
import org.junit.Test;
import org.steveleach.scoresheet.model.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

import static org.steveleach.scoresheet.FastTestSuite.*;

/**
 * Unit tests for the Ice Hockey Scoresheet model.
 * <p>
 * Most of the unit tests are focussed on the model package.
 *
 * @author Steve Leach
 */
public class ScoresheetModelTest {

    private ScoresheetModel model = null;

    @Before
    public void setup() {
        model = new ScoresheetModel();
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
    public void testReverseClockConversions() {
        assertEquals("1500", model.clockTimeFromGameTime("05:00"));
        assertEquals("1500", model.clockTimeFromGameTime("25:00"));
        assertEquals("1500", model.clockTimeFromGameTime("45:00"));
        assertEquals("0500", model.clockTimeFromGameTime("15:00"));
        assertEquals("0336", model.clockTimeFromGameTime("16:24"));
        assertEquals("0001", model.clockTimeFromGameTime("59:59"));
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
    public void testReverseClockConversionOnAdd() {
        GoalEvent event = new GoalEvent();
        event.setGameTime("16:24");
        assertEquals("0000", event.getClockTime());
        model.addEvent(event);
        assertEquals("0336", event.getClockTime());
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
    public void testSampleData() {
        addTestEvents(model);

        assertEquals(2, model.getPeriod());
        assertEquals(3, model.getHomeGoals());
        assertEquals(1, model.getAwayGoals());
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

        model.clearEvents();
        model.addEvent(new PeriodEndEvent(1));
        model.removeEvent(0);
    }

    @Test
    public void validatePenalty() {
        PenaltyEvent event = new PenaltyEvent();
        event.setPeriod(2);
        event.setClockTime("1500");
        event.setMinutes(10);
        event.setPlayer(21);
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
        tester.testBean(GoalEvent.class);
        tester.testBean(PenaltyEvent.class);
        tester.testBean(PeriodEndEvent.class);
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
    public void testAssistCount() {
        GoalEvent goal = new GoalEvent();
        assertEquals(0, goal.assists());
        goal.setAssist1(1);
        assertEquals(1, goal.assists());
        goal.setAssist2(2);
        assertEquals(2, goal.assists());
    }

    @Test
    public void testGoalTotals() {
        model.clearEvents();
        assertEquals(5, model.goalTotals(model.homeTeamName()).length);
        assertEquals(0, model.goalTotals(model.homeTeamName())[4]);
        model.addEvent(new GoalEvent(1,"1234",model.homeTeamName(),"E",41,2,3));
        assertEquals(5, model.goalTotals(model.homeTeamName()).length);
        assertEquals(1, model.goalTotals(model.homeTeamName())[4]);
    }

    @Test
    public void testPenaltyTotals() {
        model.clearEvents();
        assertEquals(5, model.penaltyTotals(model.awayTeamName()).length);
        assertEquals(0, model.penaltyTotals(model.awayTeamName())[4]);
        model.addEvent(new PenaltyEvent(1,"1234",model.awayTeamName(),"HOOK",41,2));
        assertEquals(5, model.penaltyTotals(model.awayTeamName()).length);
        assertEquals(2, model.penaltyTotals(model.awayTeamName())[4]);
    }

    @Test
    public void testMaxGameTime() {
        assertEquals(GameEvent.GAME_TIME_ZERO, model.maxGameTime());

        model.addEvent(new GoalEvent(1, "1500", "Home", "E", 41, 0, 0));

        assertEquals("05:00", model.maxGameTime());

        model.addEvent(new GoalEvent(2, "1500", "Home", "E", 41, 0, 0));

        assertEquals("25:00", model.maxGameTime());
    }

    @Test
    public void testChangeTeamName() {
        // Given
        model.addEvent(new GoalEvent(1, "1500", "Home", "E", 41, 0, 0));
        model.addEvent(new GoalEvent(1, "1000", "Away", "E", 14, 0, 0));

        // When
        model.setHomeTeamName("Reds");
        model.setAwayTeamName("Blues");

        // Then
        assertEquals("Reds", model.getEvents().get(0).getTeam());
        assertEquals("Blues", model.getEvents().get(1).getTeam());
    }

    @Test
    public void verifyHomeAway() {
        HomeAway<String> values = new HomeAway<>("A","B");
        assertEquals("(A,B)", values.toString());
    }

    @Test
    public void testScoreAt() {
        model.addEvent(new GoalEvent(1, "1500", "Home", "E", 1, 0, 0));
        model.addEvent(new GoalEvent(1, "1000", "Away", "E", 1, 0, 0));
        model.addEvent(new GoalEvent(1, "0500", "Home", "E", 1, 0, 0));
        model.addEvent(new GoalEvent(2, "1500", "Home", "E", 1, 0, 0));
        model.addEvent(new GoalEvent(2, "0500", "Away", "E", 1, 0, 0));
        model.addEvent(new GoalEvent(3, "1000", "Home", "E", 1, 0, 0));

        assertEquals(0, model.scoreAt("00:01").getHome().intValue());
        assertEquals(0, model.scoreAt("00:01").getAway().intValue());

        assertEquals(2, model.scoreAt("20:00").getHome().intValue());
        assertEquals(1, model.scoreAt("20:00").getAway().intValue());

        assertEquals(3, model.scoreAt("40:00").getHome().intValue());
        assertEquals(2, model.scoreAt("40:00").getAway().intValue());

        assertEquals(4, model.scoreAt("60:00").getHome().intValue());
        assertEquals(2, model.scoreAt("60:00").getAway().intValue());
    }

    @Test
    public void testHashCodeAndEquals() {
        GoalEvent goal1 = new GoalEvent(1, "1526", "Home", "E", 41, 20, 30);
        GoalEvent goal1a = new GoalEvent(1, "1526", "Home", "E", 41, 20, 30);
        GoalEvent goal2 = new GoalEvent(2, "1526", "Away", "E", 41, 20, 30);
        PeriodEndEvent periodEnd1 = new PeriodEndEvent(1);
        PenaltyEvent penalty1 = new PenaltyEvent(2, "1234", "Away", "HOOK", 23, 2);

        assertEquals(goal1, goal1);
        assertEquals(goal1, goal1a);
        assertNotEquals(goal1, goal2);
        assertNotEquals(goal1, periodEnd1);
        assertNotEquals(goal1, penalty1);

        assertEquals(goal1.hashCode(), goal1a.hashCode());
        assertNotEquals(goal1.hashCode(), goal2.hashCode());
        assertNotEquals(goal1.hashCode(), penalty1.hashCode());
    }

    @Test
    public void testRemoveEvent() {
        GoalEvent event1 = new GoalEvent(1, "1526", "Home", "E", 41, 20, 30);
        model.addEvent(event1);
        model.addEvent(new GoalEvent(2, "1526", "Away", "E", 41, 20, 30));
        model.removeEvent(event1);
        assertEquals(1, model.getEvents().size());
    }

    @Test
    public void removeMissingEvent() {
        GoalEvent event1 = new GoalEvent(1, "1526", "Home", "E", 41, 20, 30);
        model.addEvent(new GoalEvent(2, "1526", "Away", "E", 41, 20, 30));
        model.removeEvent(event1);
        assertEquals(1, model.getEvents().size());
    }

    @Test
    public void testAssistTotals() {
        model.addEvent(new GoalEvent(1, "1526", "Home", "E", 41, 20, 30));
        model.addEvent(new GoalEvent(2, "1526", "Away", "E", 41, 20, 30));
        model.addEvent(new GoalEvent(3, "0026", "Home", "E", 15, 20, 0));

        int[] totals = model.assistTotals("Home");
        assertEquals(3, totals[4]);
    }
}
