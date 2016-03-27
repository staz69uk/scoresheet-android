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
package org.steveleach.scoresheet;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.steveleach.scoresheet.model.GoalEvent;
import org.steveleach.scoresheet.model.ModelAware;
import org.steveleach.scoresheet.model.ModelUpdate;
import org.steveleach.scoresheet.model.ScoresheetModel;

import static android.view.inputmethod.EditorInfo.*;

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
    private InputMethodManager imgr = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.goalfragment, container, false);
        TextView title = (TextView) view.findViewById(R.id.txtGoalScoredTitle);
        title.setText(team + " Goal Scored");

        periodField = (EditText)view.findViewById(R.id.fldPeriod);
        clockField = (EditText)view.findViewById(R.id.fldClock);
        EditText scorerField = (EditText)view.findViewById(R.id.fldScoredBy);
        EditText assist1Field = (EditText)view.findViewById(R.id.fldAssist1);
        EditText assist2Field = (EditText)view.findViewById(R.id.fldAssist2);
        AutoCompleteTextView goalTypeField = (AutoCompleteTextView)view.findViewById(R.id.fldGoalType);

        //goalTypeField.setImeOptions(IME_ACTION_NEXT|TYPE_CLASS_TEXT|TYPE_TEXT_FLAG_CAP_CHARACTERS|TYPE_TEXT_VARIATION_SHORT_MESSAGE|TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        clockField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(4) });
        periodField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(1) });
        scorerField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(3) });
        assist1Field.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(3) });
        assist2Field.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(3) });
        goalTypeField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(3) });

        clockField.setOnFocusChangeListener(new MinLengthFocusChangeListener(3));
        scorerField.setOnFocusChangeListener(new MinLengthFocusChangeListener(1));
        periodField.setOnFocusChangeListener(new MinLengthFocusChangeListener(1));
        goalTypeField.setOnFocusChangeListener(new MinLengthFocusChangeListener(1));

        String[] goalCodes = getActivity().getResources().getStringArray(R.array.goalCodes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_dropdown_item_1line,goalCodes);
        goalTypeField.setAdapter(adapter);
        goalTypeField.setThreshold(1);

        imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        Button clearButton = (Button)view.findViewById(R.id.btnDone);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoalEvent event = new GoalEvent();
                event.setTeam(team);
                event.setPeriod(Integer.parseInt(periodField.getText().toString()));
                event.setClockTime(clockField.getText().toString(),model.getRules());
                event.setPlayer(scorerField.getText().toString());
                event.setAssist1(Integer.parseInt("0"+assist1Field.getText().toString()));
                event.setAssist2(Integer.parseInt("0"+assist2Field.getText().toString()));
                event.setSubType(goalTypeField.getText().toString());

                model.addEvent(event);

                imgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

                ((ScoresheetActivity)getActivity()).onModelUpdated(null);
                ((ScoresheetActivity)getActivity()).showHistory();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        clockField.requestFocus();
        periodField.setText(Integer.toString(model.getPeriod()));
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
