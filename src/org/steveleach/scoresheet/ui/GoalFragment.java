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
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.steveleach.ihscoresheet.*;
import org.steveleach.scoresheet.model.*;

/**
 * Implementation code for the new Goal UI fragment.
 *
 * @author Steve Leach
 */
public class GoalFragment extends Fragment implements ModelAware {

    private String team = "Home";
    private ScoresheetModel model = null;
    private View view = null;
    private EditText periodField = null;
    private EditText clockField = null;
    private EditText scorerField = null;
    private EditText assist1Field = null;
    private EditText assist2Field = null;
    private InputMethodManager imgr = null;
    private GoalEvent eventToEdit = null;
    private AutoCompleteTextView goalTypeField = null;

    public void setEventToEdit(GoalEvent event) {
        this.eventToEdit = event;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.goalfragment, container, false);
        TextView title = (TextView) view.findViewById(R.id.txtGoalScoredTitle);
        title.setText(getString(R.string.goalScoredTitle, team));

        periodField = (EditText)view.findViewById(R.id.fldPeriod);
        clockField = (EditText)view.findViewById(R.id.fldClock);
        scorerField = (EditText)view.findViewById(R.id.fldScoredBy);
        assist1Field = (EditText)view.findViewById(R.id.fldAssist1);
        assist2Field = (EditText)view.findViewById(R.id.fldAssist2);
        goalTypeField = (AutoCompleteTextView)view.findViewById(R.id.fldGoalType);

        clockField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(4) });
        periodField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(1) });
        scorerField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(3) });
        assist1Field.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(3) });
        assist2Field.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(3) });
        goalTypeField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(3) });

        Team teamData = model.getTeam(team);

        ScoresheetFocusChangeListener.setClockField(clockField,periodField,model);
        ScoresheetFocusChangeListener.setPlayerNumField(scorerField,view.findViewById(R.id.txtGoalScorerName),teamData);
        ScoresheetFocusChangeListener.setPlayerNumField(assist1Field,view.findViewById(R.id.txtAssist1Name),teamData);
        ScoresheetFocusChangeListener.setPlayerNumField(assist2Field,view.findViewById(R.id.txtAssist2Name),teamData);
        periodField.setOnFocusChangeListener(new ScoresheetFocusChangeListener());
        goalTypeField.setOnFocusChangeListener(new ScoresheetFocusChangeListener());

        String[] goalCodes = getActivity().getResources().getStringArray(R.array.goalCodes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_dropdown_item_1line,goalCodes);
        goalTypeField.setAdapter(adapter);
        goalTypeField.setThreshold(1);

        imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        final DefaultFragmentActivity activity = (DefaultFragmentActivity) getActivity();

        Button clearButton = (Button)view.findViewById(R.id.btnDone);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoalEvent event = (eventToEdit == null) ? new GoalEvent() : eventToEdit;

                event.setTeam(team);
                event.setPeriod(Integer.parseInt(periodField.getText().toString()));
                event.setClockTime(clockField.getText().toString());
                event.setPlayer(Integer.parseInt("0"+scorerField.getText().toString()));
                event.setAssist1(Integer.parseInt("0"+assist1Field.getText().toString()));
                event.setAssist2(Integer.parseInt("0"+assist2Field.getText().toString()));
                event.setSubType(goalTypeField.getText().toString());

                model.setChanged(true);

                if (eventToEdit == null) {
                    model.addEvent(event);
                }

                imgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

                activity.showDefaultFragment();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (eventToEdit != null) {
            periodField.setText(Integer.toString(eventToEdit.getPeriod()));
            clockField.setText(eventToEdit.getClockTime());
            scorerField.setText(Integer.toString(eventToEdit.getPlayer()));
            assist1Field.setText(eventToEdit.getAssist1() == 0 ? "" : Integer.toString(eventToEdit.getAssist1()));
            assist2Field.setText(eventToEdit.getAssist2() == 0 ? "" : Integer.toString(eventToEdit.getAssist2()));
            goalTypeField.setText(eventToEdit.getSubType());
        } else {
            periodField.setText(Integer.toString(model.getPeriod()));
        }
        clockField.requestFocus();
        imgr.showSoftInput(clockField, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {

    }

    public void setTeam(String team) {
        this.team = team;
    }
}
