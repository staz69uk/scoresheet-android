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
package org.steveleach.scoresheet.support;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.steveleach.scoresheet.model.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Converts a ScoresheetModel to/from a JSON representation.
 *
 * @author Steve Leach
 */
public class JsonCodec {

    private static final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Returns a JSON String representation of the specified scoresheet model.
     *
     * @param model
     *          the model to convert
     * @return
     *          a string representing the model in JSON format
     * @throws JSONException
     */
    public String toJson(ScoresheetModel model) throws JSONException {
        JSONObject root = new JSONObject();
        root.put("@content", "Ice Hockey Scoresheet Data");
        root.put("@version", "1.1.0");
        root.put("@exported", new Date());
        root.put("homeTeamName", model.getHomeTeam().getName());
        root.put("awayTeamName", model.getAwayTeam().getName());
        root.put("location", model.getGameLocation());
        root.put("gameDate", dateOnlyFormat.format(model.getGameDateTime()));

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

    /**
     * Populates a model from a JSON string representation.
     *
     * @param model
     *          the model to askToLoad the data into
     * @param json
     * @throws JSONException
     */
    public void fromJson(ScoresheetModel model, String json) throws JSONException {
        model.getEvents().clear();

        JSONObject root = new JSONObject(json);

        model.getHomeTeam().setName(root.optString("homeTeamName","Home"));
        model.getAwayTeam().setName(root.optString("awayTeamName","Home"));
        model.setGameLocation(root.optString("location",""));

        String dateStr = root.optString("gameDate","");
        if (dateStr.length() == 10) {
            try {
                model.setGameDateTime(dateOnlyFormat.parse(dateStr));
            } catch (ParseException e) {
                e.printStackTrace(); // TODO
            }
        }

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

            Object playerObj = jsonEvent.get("player");
            if (playerObj instanceof Integer) {
                event.setPlayer((Integer)playerObj);
            } else {
                event.setPlayer(Integer.parseInt("0"+playerObj.toString()));
            }

            model.addEvent(event);
        }
    }
}
