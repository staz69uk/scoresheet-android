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
package org.steveleach.scoresheet.model;

/**
 * Utility methods for ScoresheetModel.
 *
 * This should probably die.
 *
 * Created by steve on 11/03/16.
 */
public class ModelManager {

    public static void addTestEvents(ScoresheetModel model) {
        GameRules rules = model.getRules();
        model.addEvent(new GoalEvent(model.getPeriod(),"1950","Home","E",41,13,2,rules));
        model.addEvent(new GoalEvent(model.getPeriod(),"1830","Away","E",2,1,0,rules));
        model.addEvent(new PenaltyEvent(model.getPeriod(), "1515", "Away", "Hook", "2", 2,rules));
        model.addEvent(new GoalEvent(model.getPeriod(),"0824","Home","SH",12,93,41,rules));
        model.addEvent(new PeriodEndEvent(model.getPeriod(),rules));
        model.addEvent(new GoalEvent(model.getPeriod(),"1813","Home","PP",24,41,0,rules));
        model.addEvent(new PenaltyEvent(model.getPeriod(), "1213", "Home", "Fight", "2", 5,rules));
    }
}
