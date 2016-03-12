package org.steveleach.scoresheet.model;

/**
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
