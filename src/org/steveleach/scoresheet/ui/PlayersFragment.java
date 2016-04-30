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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("STAZ","onResume");
        refresh();
    }

    public void setTeam(String teamName) {
        team = teamName;
    }

    private void refresh() {
        if (playersTable == null) {
            return;
        }

        Log.d("STAZ","Refresh");

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
        Log.d("STAZ","onModelUpdated");
        refresh();
    }

}
