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

/**
 * Fragment code for teamName Players editor.
 *
 * @author Steve Leach
 */
public class PlayersFragment extends Fragment implements ModelAware {
    private ScoresheetActivity activity;
    private ScoresheetModel model;
    private View view;
    private TableLayout playersTable;
    private Team team = null;
    private TextView title;
    private ImageButton addPlayerButton;
    private int[] widths = new int[] {80,120,80};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.playersfragment, container, false);

        activity = (ScoresheetActivity) getActivity();

        title = (TextView)view.findViewById(R.id.playersHeader);

        playersTable = (TableLayout) view.findViewById(R.id.playersTable);

        addPlayerButton = (ImageButton)view.findViewById(R.id.btnAddPlayer);
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayerRow();
            }
        });

        ImageButton savePlayersButton = (ImageButton)view.findViewById(R.id.btnSavePlayers);
        savePlayersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askToSavePlayers();
            }
        });


        return view;
    }

    private void askToSavePlayers() {
        String text = getString(R.string.saveTeamPlayersPrompt);
        activity.yesNoDialog(text, new Runnable() {
            @Override
            public void run() {
                savePlayers();
            }
        });
    }

    private void savePlayers() {
        ScoresheetStore store = activity.scoresheetStore;
        try {
            store.saveTeam(team);
            activity.toast(getString(R.string.teamSavedMessage,team.getName()));
        } catch (JSONException|IOException e) {
            String message = getString(R.string.teamNotSavedMessage,team.getName());
            Log.e(activity.LOG_TAG, message, e);
            activity.toast(message);
        }
    }

    private void addPlayerRow() {
        Player player = new Player(0,"");
        team.getPlayers().put(0, player);
        addPlayerRow(team, player);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    private void refresh() {
        if (playersTable == null) {
            return;
        }

        loadTeamFile();

        title.setText(getString(R.string.playersHeader,team.getName()));

        playersTable.removeAllViews();

        calculateColumnWidths();

        addHeaders();

        addPlayerRows(team);
    }

    private void addPlayerRows(Team team) {
        for (Player player : team.getPlayers().values()) {
            addPlayerRow(team, player);
        }
    }

    private void addPlayerRow(Team team, Player player) {
        PlayerTableRow row = new PlayerTableRow(activity,team,player,widths);
        playersTable.addView(row);
    }

    private void calculateColumnWidths() {
        int totalWidth = getResources().getDisplayMetrics().widthPixels;
        widths[1] = totalWidth - widths[0] - widths[2] - 12;
    }

    private void addHeaders() {
        TableRow headers = new TableRow(getActivity());
        addHeader(getString(R.string.playersNumHeader),headers,widths[0]);
        addHeader(getString(R.string.playersNameHeader),headers,widths[1]);
        addHeader(getString(R.string.playersActiveHeader),headers,widths[2]);
        headers.setBackgroundColor(getResources().getColor(R.color.applight));
        headers.setPadding(2,2,2,2);
        playersTable.addView(headers);
    }

    private void addHeader(String text, TableRow headers, int width) {
        TextView view = new TextView(activity);
        view.setTextAppearance(activity,R.style.gameReportTextStyle);
        view.setText(text);
        view.setWidth(width);
        headers.addView(view);
    }

    public void loadTeamFile() {
        ScoresheetStore store = activity.scoresheetStore;

        if ((team.getPlayers().size() == 0) && store.teamFileExists(team.getName())) {
            Log.i(activity.LOG_TAG, "Loading team file for " + team.getName());
            try {
                Team savedTeam = store.loadTeam(team.getName());
                // Just copy the players, don't replace all details
                model.copyPlayers(savedTeam, team);
            } catch (IOException|JSONException e) {
                e.printStackTrace();
            }
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
