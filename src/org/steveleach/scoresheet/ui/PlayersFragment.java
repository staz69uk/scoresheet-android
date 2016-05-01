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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.playersfragment, container, false);

        activity = (ScoresheetActivity) getActivity();

        title = (TextView)view.findViewById(R.id.playersHeader);

        playersTable = (TableLayout) view.findViewById(R.id.playersTable);

        return view;
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

        int[] widths = calculateColumnWidths();

        addHeaders(widths);

        addPlayerRows(team, widths);
    }

    private void addPlayerRows(Team team, int[] widths) {
        for (Player player : team.getPlayers().values()) {
            PlayerTableRow row = new PlayerTableRow(activity,player,widths);
            playersTable.addView(row);
        }
    }

    private int[] calculateColumnWidths() {
        int[] widths = new int[] {80,120,80};
        int totalWidth = getResources().getDisplayMetrics().widthPixels;
        widths[1] = totalWidth - widths[0] - widths[2] - 12;
        return widths;
    }

    private void addHeaders(int[] widths) {
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

    private void loadTeamFile() {
        ScoresheetStore store = activity.scoresheetStore;

        if ((team.getPlayers().size() == 0) && store.teamFileExists(team.getName())) {
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
