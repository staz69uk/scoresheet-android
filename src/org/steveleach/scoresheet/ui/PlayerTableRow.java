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

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TableRow;
import org.steveleach.scoresheet.model.Player;

import static android.text.InputType.*;

/**
 * A row in the Players table.
 *
 * @author Steve Leach
 */
public class PlayerTableRow
                extends TableRow
                implements View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener {
    private Player player = null;
    private EditText numberField;
    private EditText nameField;
    private Switch activeSwitch;

    public PlayerTableRow(Context context, Player player, int[] widths) {
        super(context);

        setPlayer(player);

        numberField = makeField(context,widths[0]);
        numberField.setText(Integer.toString(player.getNumber()));
        numberField.setInputType(TYPE_CLASS_NUMBER);

        nameField = makeField(context,widths[1]);
        nameField.setText(player.getName());
        nameField.setInputType(TYPE_CLASS_TEXT|TYPE_TEXT_VARIATION_PERSON_NAME|TYPE_TEXT_FLAG_CAP_WORDS);

        activeSwitch = new Switch(context);
        activeSwitch.setWidth(widths[2]);
        activeSwitch.setChecked(player.isPlaying());
        activeSwitch.setOnCheckedChangeListener(this);

        addView(activeSwitch);
    }

    private EditText makeField(Context context, int width) {
        EditText field = new EditText(context);
        field.setWidth(width);
        field.setOnFocusChangeListener(this);
        addView(field);
        return field;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            // Update when field loses focus
            updatePlayer();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        updatePlayer();
    }

    private void updatePlayer() {
        player.setNumber(Integer.parseInt("0" + numberField.getText().toString().trim()));
        player.setName(nameField.getText().toString().trim());
        player.setPlaying(activeSwitch.isChecked());
        Log.d("STAZ", player.toString());
    }
}
