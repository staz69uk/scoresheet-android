package org.steveleach.scoresheet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.steveleach.scoresheet.*;

import java.util.Date;

/**
 * Created by steve on 05/03/16.
 */
public class JsonCodec {

    public String toJson(ScoresheetModel model) throws JSONException {
        JSONObject root = new JSONObject();
        root.put("@content", "Ice Hockey Scoresheet Data");
        root.put("@version", "1.00");
        root.put("@exported", new Date());
        root.put("homeTeamName", model.getHomeTeam().getName());
        root.put("awayTeamName", model.getAwayTeam().getName());

        JSONArray jsonEvents = new JSONArray();
        for (GameEvent event : model.getEvents()) {
            JSONObject jsonEvent = new JSONObject();
            jsonEvent.put("eventType", event.getEventType());
            jsonEvent.put("subType", event.getSubType());
            jsonEvent.put("period", event.getPeriod());
            jsonEvent.put("gameTime", event.getGameTime());
            jsonEvent.put("team", event.getTeam());
            jsonEvent.put("player", event.getPlayer());
            if (event instanceof GoalEvent) {
                GoalEvent goal = (GoalEvent)event;
                jsonEvent.put("assist1", goal.getAssist1());
                jsonEvent.put("assist2", goal.getAssist2());
            } else if (event instanceof PenaltyEvent) {
                PenaltyEvent penalty = (PenaltyEvent) event;
                jsonEvent.put("minutes", penalty.getMinutes());
            }
            jsonEvents.put(jsonEvent);
        }

        root.put("events", jsonEvents);

        return root.toString(4);
    }

    public void fromJson(ScoresheetModel model, String json) throws JSONException {
        model.getEvents().clear();

        JSONObject root = new JSONObject(json);

        model.getHomeTeam().setName(root.optString("homeTeamName","Home"));
        model.getAwayTeam().setName(root.optString("awayTeamName","Home"));

        JSONArray jsonEvents = root.getJSONArray("events");
        for (int n = 0; n < jsonEvents.length(); n++) {
            JSONObject jsonEvent = (JSONObject) jsonEvents.get(n);
            String eventType = jsonEvent.getString("eventType");
            GameEvent event = null;
            if (eventType.equals("Goal")) {
                GoalEvent goal = new GoalEvent();
                goal.setAssist1(jsonEvent.optInt("assist1",0));
                goal.setAssist2(jsonEvent.optInt("assist2",0));
                event = goal;
            } else if (eventType.equals("Penalty")) {
                PenaltyEvent penalty = new PenaltyEvent();
                penalty.setMinutes(jsonEvent.optInt("minutes",2));
                event = penalty;
            } else if (eventType.equals("Period end")) {
                event = new PeriodEndEvent();
            }
            event.setSubType(jsonEvent.getString("subType"));
            event.setPeriod(jsonEvent.getInt("period"));
            event.setGameTime(jsonEvent.getString("gameTime"));
            event.setTeam(jsonEvent.getString("team"));
            event.setPlayer(jsonEvent.getString("player"));

            model.addEvent(event);
        }
    }
}
