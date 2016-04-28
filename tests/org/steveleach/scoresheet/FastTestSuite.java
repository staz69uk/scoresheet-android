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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.steveleach.scoresheet.model.*;
import org.steveleach.scoresheet.support.ScoresheetSupportTest;

/**
 * Unit test suite for Scoreboard app.
 *
 * @author Steve Leach
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ScoresheetModelTest.class,ScoresheetSupportTest.class})
public class FastTestSuite {

    /**
     * Adds a set of test events to a ScoresheetModel.
     */
    public static void addTestEvents(ScoresheetModel model) {
        model.addEvent(new GoalEvent(model.getPeriod(),"1950",model.homeTeamName(),"E",41,13,2));
        model.addEvent(new GoalEvent(model.getPeriod(),"1830",model.awayTeamName(),"E",2,1,0));
        model.addEvent(new PenaltyEvent(model.getPeriod(), "1515", model.awayTeamName(), "Hook", 2, 2));
        model.addEvent(new GoalEvent(model.getPeriod(),"0824",model.homeTeamName(),"SH",12,93,41));
        model.addEvent(new PeriodEndEvent(model.getPeriod()));
        model.addEvent(new GoalEvent(model.getPeriod(),"1813",model.homeTeamName(),"PP",24,41,0));
        model.addEvent(new PenaltyEvent(model.getPeriod(), "1213", model.homeTeamName(), "Fight", 2, 5));
    }

}
