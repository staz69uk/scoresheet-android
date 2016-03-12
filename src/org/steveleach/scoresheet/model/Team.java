package org.steveleach.scoresheet.model;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by steve on 05/03/16.
 */
public class Team {
    private String name = null;
    private Map<Integer,String> players = new TreeMap<>();

    public Team(String name) {
        this.name = name;
    }

    public Team() {
        this("");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, String> getPlayers() {
        return players;
    }

    public void setPlayers(Map<Integer, String> players) {
        this.players = players;
    }
}
