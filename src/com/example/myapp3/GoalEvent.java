package com.example.myapp3;

/**
 * Created by steve on 29/02/16.
 */
public class GoalEvent extends GameEvent {

    private int scorer = 0;
    private int assist1 = 0;
    private int assist2 = 0;
    private String goalType = "Even";

    @Override
    public String toString() {
        return String.format("%s %s %s (%s) by %s", gameTime, team, eventType, goalType, getPlayer());
    }
}
