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
    private static final SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public static final String FORMAT_VERSION = "1.2.0";
    public static final String TEAM_FORMAT_VERSION = "1.0.0";

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
        root.put("@version", FORMAT_VERSION);
        root.put("@exported", timeStampFormat.format(new Date()));
        root.put("homeTeamName", model.getHomeTeam().getName());    // deprecated
        root.put("awayTeamName", model.getAwayTeam().getName());    // deprecated
        root.put("location", model.getGameLocation());
        root.put("gameDate", dateOnlyFormat.format(model.getGameDateTime()));
        root.put("homeTeam", teamJson(model.getHomeTeam()));
        root.put("awayTeam", teamJson(model.getAwayTeam()));

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
                jsonEvent.put("plusMins", penalty.getPlusMins());
            }
            jsonEvents.put(jsonEvent);
        }

        root.put("events", jsonEvents);

        return root.toString(4);
    }

    private JSONObject teamJson(Team team) throws JSONException {
        JSONObject jsonTeam = new JSONObject();
        jsonTeam.put("name", team.getName());
        jsonTeam.put("players", teamPlayerListJson(team));
        return jsonTeam;
    }

    /**
     * Populates a model from a JSON string representation.
     *
     * @param model
     *          the model to load the data into
     * @param json
     * @throws JSONException
     */
    public void fromJson(ScoresheetModel model, String json) throws JSONException {
        model.getEvents().clear();

        if  ((json == null) || (json.length() == 0)) {
            throw new JSONException("No content provided for JSON decoder");
        }

        JSONObject root = new JSONObject(json);

        if (root.has("homeTeam")) {
            loadTeam(root.getJSONObject("homeTeam"), model.getHomeTeam());
            loadTeam(root.getJSONObject("awayTeam"), model.getAwayTeam());
        } else {
            model.getHomeTeam().setName(root.optString("homeTeamName", "Home"));
            model.getAwayTeam().setName(root.optString("awayTeamName", "Home"));
        }
        model.setGameLocation(root.optString("location",""));

        model.setGameDateTime(getDate(root.optString("gameDate","")));

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
                penalty.setPlusMins(jsonEvent.optInt("plusMins",0));
                event = penalty;
            } else if (eventType.equals("Period end")) {
                event = new PeriodEndEvent();
            }
            event.setSubType(jsonEvent.getString("subType"));
            event.setPeriod(jsonEvent.getInt("period"));
            event.setGameTime(jsonEvent.getString("gameTime"));
            event.setTeam(jsonEvent.getString("team"));
            event.setPlayer(getInt(jsonEvent.get("player")));

            model.addEvent(event);
        }
    }

    private void loadTeam(JSONObject jsonTeam, Team team) throws JSONException {
        team.setName(jsonTeam.optString("name",""));
        loadTeamPlayers(jsonTeam.getJSONArray("players"), team);
    }

    public static Date getDate(String dateStr) {
        if (dateStr.length() == 10) {
            try {
                return dateOnlyFormat.parse(dateStr);
            } catch (ParseException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static int getInt(Object obj) {
        if (obj instanceof Integer) {
            return ((Integer)obj).intValue();
        } else {
            return Integer.parseInt("0"+obj.toString());
        }
    }

    /**
     * Returns a JSON String representation of the specified team.
     *
     * @param team
     *          the team to convert
     * @return
     *          a string representing the team in JSON format
     * @throws JSONException
     */
    public String teamToJson(Team team) throws JSONException {
        JSONObject root = new JSONObject();
        root.put("@content", "Ice Hockey Scoresheet Team");
        root.put("@version", TEAM_FORMAT_VERSION);
        root.put("@exported", timeStampFormat.format(new Date()));
        root.put("shortName", team.getName());
        JSONArray jsonPlayers = teamPlayerListJson(team);
        root.put("players", jsonPlayers);
        return root.toString(4);
    }

    private JSONArray teamPlayerListJson(Team team) throws JSONException {
        JSONArray jsonPlayers = new JSONArray();
        for (Player player : team.getPlayers().values()) {
            JSONObject jsonPlayer = new JSONObject();
            jsonPlayer.put("number", player.getNumber());
            jsonPlayer.put("name", player.getName());
            jsonPlayer.put("active", player.isPlaying());
            jsonPlayers.put(jsonPlayer);
        }
        return jsonPlayers;
    }

    /**
     * Populates a Team from a JSON string representation.
     *
     * @param json
     * @throws JSONException
     */
    public Team teamFromJson(String json) throws JSONException {
        Team team = new Team();
        team.getPlayers().clear();
        JSONObject root = new JSONObject(json);

        team.setName(root.optString("shortName",""));

        JSONArray jsonPlayers = root.getJSONArray("players");
        loadTeamPlayers(jsonPlayers, team);
        return team;
    }

    private void loadTeamPlayers(JSONArray jsonPlayers, Team team) throws JSONException {
        for (int n = 0; n < jsonPlayers.length(); n++) {
            JSONObject jsonPlayer = (JSONObject) jsonPlayers.get(n);

            Player player = new Player();
            player.setNumber(jsonPlayer.getInt("number"));
            player.setName(jsonPlayer.optString("name",""));
            player.setPlaying(jsonPlayer.optBoolean("active",true));

            team.getPlayers().put(player.getNumber(), player);
        }
    }
}
