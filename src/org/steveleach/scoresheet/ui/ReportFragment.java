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

import static org.steveleach.ihscoresheet.R.id.*;
import static android.view.Gravity.*;

/**
 * Implementation code for the Game Report UI fragment.
 *
 * @author Steve Leach
 */
public class ReportFragment extends Fragment implements ModelAware {
    public static final String HEADER_SEP = ",";
    private ScoresheetModel model = new ScoresheetModel();
    private View view;
    private ScoresheetActivity activity = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.reportfrgament, container, false);

        activity = (ScoresheetActivity) getActivity();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshReport();
    }

    private void addRow(TableLayout table, String[] values, int[] widths, int alignment[], int rowIndex) {
        int totalWidth = getResources().getDisplayMetrics().widthPixels;
        TableRow row = new TableRow(activity);
        int colIndex = 0;
        for (String value : values) {
            TextView textView = new TextView(activity);
            textView.setText(value);
            textView.setTextAppearance(activity,R.style.gameReportTextStyle);
            textView.setWidth(totalWidth * widths[colIndex] / 100);
            textView.setGravity(alignment[colIndex]);
            if (rowIndex == 0) {
                textView.setTypeface(null, Typeface.BOLD);
            } else if (rowIndex % 2 == 1) {
                row.setBackgroundColor(getResources().getColor(R.color.gameReportEvenRow));
            }
            row.addView(textView);
            colIndex++;
        }
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        table.addView(row,rowParams);
    }

    private String[] headers(int stringId) {
        return getString(stringId).split(HEADER_SEP);
    }

    private void addGoals(String team, ScoresheetModel model, int headerId, int tableId) {
        TextView header = (TextView) view.findViewById(headerId);
        header.setText(getString(R.string.reportGoalsHeader,team.toUpperCase()));

        TableLayout table = (TableLayout) view.findViewById(tableId);
        int index = 0;

        table.removeAllViews();

        int[] widths = new int[] {15,15,10,10,10};  // Column widths in %
        int[] alignment = new int[] {LEFT,LEFT,RIGHT,RIGHT,RIGHT};
        addRow(table,headers(R.string.reportGoalTableHeaders),widths,alignment,index++);

        for (GoalEvent goal : model.goals(team)) {
            String values[] = new String[] {
                goal.getGameTime(),
                goal.getSubType(),
                activity.playerNum(goal),
                goal.getAssist1() == 0 ? "" : Integer.toString(goal.getAssist1()),
                goal.getAssist2() == 0 ? "" : Integer.toString(goal.getAssist2())
            };
            addRow(table,values,widths,alignment,index++);
        }

    }

    private void addPenalties(String team, ScoresheetModel model, int headerId, int tableId) {
        TextView header = (TextView) view.findViewById(headerId);
        header.setText(getString(R.string.reportPenaltiesHeader,team.toUpperCase()));

        TableLayout table = (TableLayout) view.findViewById(tableId);

        table.removeAllViews();

        int[] widths = new int[] {10,10,20,15,15,15};  // Column widths in %
        int[] alignment = new int[] {LEFT,LEFT,LEFT,LEFT,LEFT,LEFT};
        addRow(table,headers(R.string.reportPenaltyTableHeaders),widths,alignment,0);

        int index = 1;
        for (PenaltyEvent penalty : model.penalties(team)) {
            String values[] = new String[] {
                    activity.playerNum(penalty),
                    Integer.toString(penalty.getMinutes()),
                    penalty.getSubType(),
                    penalty.getGameTime(),
                    penalty.getGameTime(),
                    penalty.finishTime()
            };
            addRow(table,values,widths,alignment,index++);
        }
    }

    private void addTeamRows(TableLayout table, Team team, ScoresheetModel model) {
        int index = 0;

        int[] widths = new int[] {10,15,10,10,10};  // Column widths in %
        int[] alignment = new int[] {LEFT,LEFT,RIGHT,RIGHT,RIGHT};

        addRow(table,headers(R.string.reportTeamTableHeaders),widths,alignment,index++);

        for (ScoresheetModel.PlayerStats player : model.getPlayerStats(team.getName()).values()) {
            String[] values = new String[] {
                    activity.playerNum(player.playerNum),
                    team.activePlayerName(player.playerNum),
                    Integer.toString(player.goals),
                    Integer.toString(player.assists),
                    Integer.toString(player.penaltyMins)
            };
            addRow(table,values,widths,alignment,index++);
        }
    }

    private void addGoalTotals(ScoresheetModel model) {
        TableLayout table = (TableLayout) view.findViewById(tablePeriodScores);

        table.removeAllViews();

        int[] widths = new int[] {30,10,10,10,10,10};  // Column widths in %
        int[] alignment = new int[] {LEFT,RIGHT,RIGHT,RIGHT,RIGHT,RIGHT};

        addRow(table,headers(R.string.reportScoresTableHeaders),widths,alignment,0);

        int[] homeGoals = model.goalTotals(model.getHomeTeam().getName());
        showTotals(table, model.getHomeTeam().getName(), homeGoals, widths,alignment);

        int[] awayGoals = model.goalTotals(model.getAwayTeam().getName());
        showTotals(table, model.getAwayTeam().getName(), awayGoals, widths,alignment);
    }

    private void showTotals(TableLayout table, String name, int[] values, int[] widths, int[] alignment) {
        String[] strings = new String[values.length+1];
        strings[0] = name;
        for (int n = 0; n < values.length; n++) {
            strings[n+1] = Integer.toString(values[n]);
        }
        addRow(table,strings,widths,alignment,1);
    }

    private void addPenaltyTotals(ScoresheetModel model) {
        TableLayout table = (TableLayout) view.findViewById(tablePeriodPenalties);

        table.removeAllViews();

        int[] widths = new int[] {30,10,10,10,10,10};  // Column widths in %
        int[] alignment = new int[] {LEFT,RIGHT,RIGHT,RIGHT,RIGHT,RIGHT};

        addRow(table,headers(R.string.reportAllPensTableHeaders),widths,alignment,0);

        int[] home = model.penaltyTotals(model.getHomeTeam().getName());
        showTotals(table, model.getHomeTeam().getName(), home, widths, alignment);

        int[] away = model.penaltyTotals(model.getAwayTeam().getName());
        showTotals(table, model.getAwayTeam().getName(), away, widths, alignment);
    }

    private void addAssistTotals(ScoresheetModel model) {
        TableLayout table = (TableLayout) view.findViewById(tablePeriodAssists);

        table.removeAllViews();

        int[] widths = new int[] {30,10,10,10,10,10};  // Column widths in %
        int[] alignment = new int[] {LEFT,RIGHT,RIGHT,RIGHT,RIGHT,RIGHT};

        addRow(table,headers(R.string.reportAssistsTableHeaders),widths,alignment,0);

        int[] home = model.assistTotals(model.getHomeTeam().getName());
        showTotals(table, model.getHomeTeam().getName(), home, widths,alignment);

        int[] away = model.assistTotals(model.getAwayTeam().getName());
        showTotals(table, model.getAwayTeam().getName(), away, widths,alignment);
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
        if (view != null) {
            addPlayerStats(model.getHomeTeam(),model, reportHomeTeamHeader, tableHomeTeamStats);
            addGoals(model.homeTeamName(), model, reportScoringHomeHeader, tableHomeScoring);
            addPenalties(model.homeTeamName(), model, reportPenaltiesHomeHeader, tableHomePenalties);
            addPlayerStats(model.getAwayTeam(),model, reportAwayTeamHeader, tableAwayTeamStats);
            addGoals(model.awayTeamName(), model, reportScoringAwayHeader, tableAwayScoring);
            addPenalties(model.awayTeamName(), model, reportPenaltiesAwayHeader, tableAwayPenalties);
            addGoalTotals(model);
            addPenaltyTotals(model);
            addAssistTotals(model);
        }
    }

    private void addPlayerStats(Team team, ScoresheetModel model, int headerId, int tableId) {
        TextView header = (TextView) view.findViewById(headerId);
        header.setText(getString(R.string.reportTeamHeader,team.getName().toUpperCase()));

        TableLayout table = (TableLayout) view.findViewById(tableId);
        addTeamRows(table, team, model);
    }
}
