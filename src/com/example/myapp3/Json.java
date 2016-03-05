package com.example.myapp3;

/**
 * Created by steve on 05/03/16.
 */
public class Json {

    public static String toJson(ScoresheetModel model) {
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
            sb.append(toJson(event,"      "));
            first = false;
        }
        sb.append("\n");
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String toJson(GameEvent event, String prefix) {
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
}
