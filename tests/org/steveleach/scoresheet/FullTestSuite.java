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
import org.steveleach.scoresheet.model.GoalEvent;
import org.steveleach.scoresheet.model.PenaltyEvent;
import org.steveleach.scoresheet.model.PeriodEndEvent;
import org.steveleach.scoresheet.model.ScoresheetModel;

/**
 * Unit test suite for Scoreboard app.
 *
 * @author Steve Leach
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ScoresheetModelTest.class,ScoresheetSupportTest.class,
        ScoresheetUITest.class, ScoresheetRealGameUITest.class})
public class FullTestSuite extends FastTestSuite {

}
