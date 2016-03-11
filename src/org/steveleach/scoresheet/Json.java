package org.steveleach.scoresheet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.steveleach.scoresheet.*;

import java.util.Date;

/**
 * Created by steve on 05/03/16.
 */
public class Json {

    public static String toJson(ScoresheetModel model) throws JSONException {
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

    public static String toJsonOld(ScoresheetModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        writeAttribute("  ", "homeTeamName", model.getHomeTeam().getName(), ",\n", sb);
        writeAttribute("  ", "awayTeamName", model.getAwayTeam().getName(), ",\n", sb);
        sb.append("  \"events:\": [\n");
        boolean first = true;
        for (GameEvent event : model.getEvents()) {
            if (!first) {
                sb.append(",\n");
            }
            sb.append(toJsonOld(event,"      "));
            first = false;
        }
        sb.append("\n");
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String toJsonOld(GameEvent event, String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("{\n");
        writeAttribute(prefix+"  ", "eventType", event.getEventType(), ",\n", sb);
        writeAttribute(prefix+"  ", "subType", event.getSubType(), ",\n", sb);
        writeAttribute(prefix+"  ", "period", event.getPeriod(), ",\n", sb);
        writeAttribute(prefix+"  ", "gameTime", event.getGameTime(), ",\n", sb);
        writeAttribute(prefix+"  ", "player", parseInt(event.getPlayer(),0), ",\n", sb);
        if (event instanceof GoalEvent) {
            GoalEvent goal = (GoalEvent)event;
            writeAttribute(prefix+"  ", "assist1", goal.getAssist1(), ",\n", sb);
            writeAttribute(prefix+"  ", "assist1", goal.getAssist2(), ",\n", sb);
        } else if (event instanceof PenaltyEvent) {
            PenaltyEvent penalty = (PenaltyEvent)event;
            writeAttribute(prefix+"  ", "minutes", penalty.getMinutes(), ",\n", sb);
        }
        writeAttribute(prefix+"  ", "team", event.getTeam(), "\n", sb);
        sb.append(prefix);
        sb.append("}");
        return sb.toString();
    }

    private static int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static void writeAttribute(String prefix, String name, Object value, String suffix, StringBuilder sb) {
        sb.append(prefix);
        sb.append("\"");
        sb.append(name);
        sb.append("\": ");
        if (value instanceof String) {
            sb.append("\"");
            sb.append(value);
            sb.append("\"");
        } else if (value instanceof Integer) {
            sb.append(value);
        }
        sb.append(suffix);
    }

    public static void fromJson(ScoresheetModel model, String json) throws JSONException {
        model.getEvents().clear();

        JSONObject root = new JSONObject(json);
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
