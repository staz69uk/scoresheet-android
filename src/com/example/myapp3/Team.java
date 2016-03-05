package com.example.myapp3;

/**
 * Created by steve on 05/03/16.
 */
public class Team {
    private String name = null;

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
}
