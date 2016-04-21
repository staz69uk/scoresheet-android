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
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.steveleach.ihscoresheet.*;
import org.steveleach.scoresheet.model.*;

/**
 * Implementation code for the Game Report UI fragment.
 *
 * @author Steve Leach
 */
public class ReportFragment extends Fragment implements ModelAware {
    private ScoresheetModel model = new ScoresheetModel();
    private View view;
    private String title = "Report";
    private LinearLayout panel;
    public static final String GAME_REPORT = "Game Report";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.reportfrgament, container, false);

        panel = (LinearLayout) view.findViewById(R.id.panelNew);

        TextView title = (TextView)view.findViewById(R.id.reportTitle);
        title.setText(this.title);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshReport();
    }

    private void addRow(TableLayout table, String[] values, int[] widths, int rowIndex) {
        int totalWidth = getResources().getDisplayMetrics().widthPixels;
        TableRow row = new TableRow(getActivity());
        int colIndex = 0;
        for (String value : values) {
            TextView textView = new TextView(getActivity());
            textView.setText(value);
            textView.setTextAppearance(getActivity(),R.style.gameReportTextStyle);
            textView.setWidth(totalWidth * widths[colIndex++] / 100);
            textView.setHeight(28);
            if (rowIndex == 0) {
                textView.setTypeface(null, Typeface.BOLD);
            } else if (rowIndex % 2 == 1) {
                row.setBackgroundColor(getResources().getColor(R.color.gameReportEvenRow));
            }
            row.addView(textView);
        }
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        table.addView(row,rowParams);
    }

    private void addGoals(String team, ScoresheetModel model) {
        String headerText = "SCORING - " + team.toUpperCase();
        addSectionHeader(headerText);

        TableLayout table = new TableLayout(getActivity());
        int index = 0;

        int[] widths = new int[] {15,15,10,10,10};  // Column widths in %
        String[] headers = new String[] {"TIME","TYPE","G","A","A"};
        addRow(table,headers,widths,index++);

        int goalTotal = 0;
        int assistTotal1 = 0;
        int assistTotal2 = 0;

        for (GoalEvent goal : model.goals(team)) {
            String values[] = new String[] {
                goal.getGameTime(),
                goal.getSubType(),
                goal.getPlayer(),
                goal.getAssist1() == 0 ? "" : Integer.toString(goal.getAssist1()),
                goal.getAssist2() == 0 ? "" : Integer.toString(goal.getAssist2())
            };
            addRow(table,values,widths,index++);

            goalTotal++;
            assistTotal1 += goal.getAssist1() > 0 ? 1 : 0;
            assistTotal2 += goal.getAssist2() > 0 ? 1 : 0;
        }

//        String values[] = new String[] {"", "", ""+goalTotal, ""+assistTotal1, ""+assistTotal2};
//        addRow(table,values,widths,index++);

        panel.addView(table);
        addSpace();
    }

    private void addSectionHeader(String headerText) {
        TextView heading = new TextView(getActivity());
        heading.setText(headerText);
        heading.setTextAppearance(getActivity(),R.style.gameReportTextStyle);
        heading.setTypeface(null,Typeface.BOLD);
        panel.addView(heading);
    }

    private void addSpace() {
        Space space = new Space(getActivity());
        space.setMinimumHeight(12);
        panel.addView(space);
    }

    private void addPenalties(String team, ScoresheetModel model) {
        addSectionHeader("PENALTIES - " + team.toUpperCase());

        TableLayout table = new TableLayout(getActivity());

        int[] widths = new int[] {10,10,20,15,15,15};  // Column widths in %
        String[] headers = new String[] {"NO","PIM","OFFENCE","GIVEN","START","END"};
        addRow(table,headers,widths,0);

        int index = 1;
        for (PenaltyEvent penalty : model.penalties(team)) {
            String values[] = new String[] {
                    penalty.getPlayer(),
                    Integer.toString(penalty.getMinutes()),
                    penalty.getSubType(),
                    penalty.getGameTime(),
                    penalty.getGameTime(),
                    penalty.finishTime()
            };
            addRow(table,values,widths,index++);
        }
        panel.addView(table);
        addSpace();
    }

    private void addPlayerStats(String team, ScoresheetModel model) {
        addSectionHeader(team.toUpperCase() + " - TEAM");

        TableLayout table = new TableLayout(getActivity());
        int index = 0;

        int[] widths = new int[] {10,15,10,10,10};  // Column widths in %
        String[] headers = new String[] {"No","Name","G","A","PIM"};
        addRow(table,headers,widths,index++);
        
        for (ScoresheetModel.PlayerStats player : model.getPlayerStats(team).values()) {
            String[] values = new String[] {
                    Integer.toString(player.playerNum),
                    "--",
                    Integer.toString(player.goals),
                    Integer.toString(player.assists),
                    Integer.toString(player.penaltyMins)
            };
            addRow(table,values,widths,index++);
        }
        panel.addView(table);
        addSpace();
    }

    private void addGoalTotals(ScoresheetModel model) {
        TableLayout table = new TableLayout(getActivity());

        int[] widths = new int[] {30,10,10,10,10,10};  // Column widths in %
        String[] headers = new String[] {"PERIOD SCORES","1","2","3","OT","TOT"};
        addRow(table,headers,widths,0);

        int[] homeGoals = model.goalTotals(model.getHomeTeam().getName());
        showTotals(table, model.getHomeTeam().getName(), homeGoals, widths);

        int[] awayGoals = model.goalTotals(model.getAwayTeam().getName());
        showTotals(table, model.getAwayTeam().getName(), awayGoals, widths);

        panel.addView(table);
        addSpace();
    }

    private void showTotals(TableLayout table, String name, int[] values, int[] widths) {
        String[] strings = new String[values.length+1];
        strings[0] = name;
        for (int n = 0; n < values.length; n++) {
            strings[n+1] = Integer.toString(values[n]);
        }
        addRow(table,strings,widths,1);
    }

    private void addPenaltyTotals(ScoresheetModel model) {
        TableLayout table = new TableLayout(getActivity());

        int[] widths = new int[] {30,10,10,10,10,10};  // Column widths in %
        String[] headers = new String[] {"PENALTY MINS","1","2","3","OT","TOT"};
        addRow(table,headers,widths,0);

        int[] home = model.penaltyTotals(model.getHomeTeam().getName());
        showTotals(table, model.getHomeTeam().getName(), home, widths);

        int[] away = model.penaltyTotals(model.getAwayTeam().getName());
        showTotals(table, model.getAwayTeam().getName(), away, widths);

        panel.addView(table);
        addSpace();
    }

    private void addAssistTotals(ScoresheetModel model) {
        TableLayout table = new TableLayout(getActivity());

        int[] widths = new int[] {30,10,10,10,10,10};  // Column widths in %
        String[] headers = new String[] {"ASSISTS","1","2","3","OT","TOT"};
        addRow(table,headers,widths,0);

        int[] home = model.assistTotals(model.getHomeTeam().getName());
        showTotals(table, model.getHomeTeam().getName(), home, widths);

        int[] away = model.assistTotals(model.getAwayTeam().getName());
        showTotals(table, model.getAwayTeam().getName(), away, widths);

        panel.addView(table);
        addSpace();
    }


    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
        model.addListener(this);
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {
        refreshReport();
    }

    private void refreshReport() {
        if (panel != null) {
            addPlayerStats(model.getHomeTeam().getName(),model);
            addGoals(model.getHomeTeam().getName(), model);
            addPenalties(model.getHomeTeam().getName(), model);
            addPlayerStats(model.getAwayTeam().getName(),model);
            addGoals(model.getAwayTeam().getName(), model);
            addPenalties(model.getAwayTeam().getName(), model);
            addGoalTotals(model);
            addPenaltyTotals(model);
            addAssistTotals(model);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
