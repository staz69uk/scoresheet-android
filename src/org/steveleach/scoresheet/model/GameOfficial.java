package org.steveleach.scoresheet.model;

/**
 * Created by steve on 12/03/16.
 */
public class GameOfficial {

    private Role role;

    private String name;

    private enum Role {
        REFEREE, LINESMAN, SUPERVISOR, SCORER, TIMEKEEPER, GOALJUDGE
    }

    public GameOfficial() {
        // required
    }

    public GameOfficial(Role role, String name) {
        this.role = role;
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
