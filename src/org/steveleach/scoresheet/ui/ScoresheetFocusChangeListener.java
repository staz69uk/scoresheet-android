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
package org.steveleach.scoresheet.ui;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.steveleach.scoresheet.model.ScoresheetModel;
import org.steveleach.scoresheet.model.Team;

/**
 * Add to a text field to show an error if the content is shorter than a specified value.
 *
 * @author Steve Leach
 */
public class ScoresheetFocusChangeListener implements View.OnFocusChangeListener {

    private int minLength = 0;
    private Team team = null;
    private ScoresheetModel model = null;
    private TextView playerNameField = null;
    private EditText periodField = null;

    public static void setClockField(EditText clockField, EditText periodField, ScoresheetModel model) {
        ScoresheetFocusChangeListener listener = new ScoresheetFocusChangeListener(periodField, model);
        clockField.setOnFocusChangeListener(listener);
    }

    public static void setPlayerNumField(EditText field, View namefield, Team team) {
        ScoresheetFocusChangeListener listener = new ScoresheetFocusChangeListener(namefield, team);
        field.setOnFocusChangeListener(listener);
    }

    /**
     * Constructor for generic field with minimum length of 1.
     */
    public ScoresheetFocusChangeListener() {
        this(1);
    }

    /**
     * Constructor for generic field with specified minimum length.
     */
    public ScoresheetFocusChangeListener(int minLength) {
        this.minLength = minLength;
    }

    /**
     * Constructor for player number field, with a TextView to show the player name.
     */
    private ScoresheetFocusChangeListener(View playerNamefield, Team team) {
        this.minLength = 1;
        this.playerNameField = (TextView) playerNamefield;
        this.team = team;
    }

    /**
     * Constructor for a clock field, validated against the specified scoresheet model.
     */
    private ScoresheetFocusChangeListener(EditText periodField, ScoresheetModel model) {
        this.periodField = periodField;
        this.model = model;
        this.minLength = 3;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            onLeavingfield((EditText)view);
        }
    }

    private void onLeavingfield(EditText field) {
        field.setError(null);

        checkMinimumLength(field);

        if (isPlayerNumberField()) {
            lookupPlayerName(field);
        }

        if (isClockField()) {
            checkClockTime(field);
        }
    }

    private void checkClockTime(EditText field) {
        if (tooEarly(field.getText().toString())) {
            field.setError("Game time is before last existing event");
        }
    }

    private boolean isClockField() {
        return periodField != null;
    }

    private boolean isPlayerNumberField() {
        return playerNameField != null;
    }

    private void lookupPlayerName(EditText field) {
        String content = field.getText().toString();
        int playerNum = Integer.parseInt("0"+content);
        String playerName = team.activePlayerName(playerNum);
        playerNameField.setText(playerName);
    }

    private void checkMinimumLength(EditText field) {
        String content = field.getText().toString();
        if (content.length() < minLength) {
            field.setError("Minimum length is " + minLength);
        }
    }

    /**
     * Returns true if the time specified the field is before the time of the last existing event.
     */
    private boolean tooEarly(String content) {
        int period = Integer.parseInt(periodField.getText().toString());
        String lastExistingTime = model.maxGameTime();
        String newGameTime = model.gameTimeFromClock(period,content);
        return newGameTime.compareTo(lastExistingTime) < 0;
    }
}
