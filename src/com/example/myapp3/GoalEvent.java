package com.example.myapp3;

/**
 * Created by steve on 29/02/16.
 */
public class GoalEvent extends GameEvent {

    @Override
    public String toString() {
        return String.format("%s %s %s", gameTime, team, eventType);
    }
}
