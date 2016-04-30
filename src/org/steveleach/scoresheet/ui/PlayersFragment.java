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

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONException;
import org.steveleach.ihscoresheet.R;
import org.steveleach.scoresheet.model.*;
import org.steveleach.scoresheet.support.ScoresheetStore;

import java.io.IOException;
import static android.text.InputType.*;

/**
 * Fragment code for team Players editor.
 *
 * @author Steve Leach
 */
public class PlayersFragment extends Fragment implements ModelAware {

    private ScoresheetModel model;
    private View view;
    private TableLayout playersTable;
    private String team = null;
    private TextView title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.playersfragment, container, false);

        title = (TextView)view.findViewById(R.id.playersHeader);

        playersTable = (TableLayout) view.findViewById(R.id.playersTable);

        view.findViewById(R.id.btnAddPlayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayer();
            }
        });

        return view;
    }

    private void addPlayer() {
        model.getHomeTeam().addPlayer(0,"");
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void setTeam(String teamName) {
        team = teamName;
    }

    public class PlayerTableRow extends TableRow implements View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener {
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

    private void refresh() {
        if (playersTable == null) {
            return;
        }

        title.setText(getString(R.string.playersHeader,team));
        Team team = loadTestTeam();

        playersTable.removeAllViews();

        int[] widths = calculateColumnWidths();

        addHeaders(widths);

        for (Player player : team.getPlayers().values()) {
            PlayerTableRow row = new PlayerTableRow(getActivity(),player,widths);
            playersTable.addView(row);
        }
    }

    private int[] calculateColumnWidths() {
        int[] widths = new int[] {80,120,80};
        int totalWidth = getResources().getDisplayMetrics().widthPixels;
        ;
        widths[1] = totalWidth - widths[0] - widths[2] - 12;
        return widths;
    }

    private void addHeaders(int[] widths) {
        TableRow headers = new TableRow(getActivity());
        addHeader("No.",headers,widths[0]);
        addHeader("Name",headers,widths[1]);
        addHeader("Active?",headers,widths[2]);
        headers.setBackgroundColor(getResources().getColor(R.color.applight));
        headers.setPadding(2,2,2,2);
        playersTable.addView(headers);
    }

    private void addHeader(String text, TableRow headers, int width) {
        Context context = getActivity();
        TextView view = new TextView(context);
        view.setTextAppearance(context,R.style.gameReportTextStyle);
        view.setText(text);
        view.setWidth(width);
        headers.addView(view);
    }

    private Team loadTestTeam() {
        ScoresheetStore store = ((ScoresheetActivity)getActivity()).scoresheetStore;
        try {
            Team team = store.getTeam("smoke.json");
            //((PlayersAdapter)list.getAdapter()).setTeam(team);
            return team;
        } catch (IOException|JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
        refresh();
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {
        refresh();
    }

}
