package org.steveleach.scoresheet;

import org.steveleach.scoresheet.GoalEvent;
import org.steveleach.scoresheet.PenaltyEvent;
import org.steveleach.scoresheet.PeriodEndEvent;
import org.steveleach.scoresheet.ScoresheetModel;

/**
 * Created by steve on 11/03/16.
 */
public class ModelManager {

    public static void addTestEvents(ScoresheetModel model) {
        model.addEvent(new GoalEvent(model.getPeriod(),"1950","Home","E",41,13,2));
        model.addEvent(new GoalEvent(model.getPeriod(),"1830","Away","E",2,1,0));
        model.addEvent(new PenaltyEvent(model.getPeriod(), "1515", "Away", "Hook", "2", 2));
        model.addEvent(new GoalEvent(model.getPeriod(),"0824","Home","SH",12,93,41));
        model.addEvent(new PeriodEndEvent(model.getPeriod()));
        model.addEvent(new GoalEvent(model.getPeriod(),"1813","Home","PP",24,41,0));
    }
}
