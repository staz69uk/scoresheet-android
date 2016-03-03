package com.example.myapp3;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by steve on 01/03/16.
 */
public class GoalFragment extends Fragment implements ModelAware {

    public String homeAway = "Home";
    private ScoresheetModel model = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.goalfragment, container, false);
        TextView title = (TextView) view.findViewById(R.id.txtGoalScoredTitle);
        title.setText(homeAway + " Goal Scored");

        EditText clockField = (EditText)view.findViewById(R.id.fldClock);
        EditText scorerField = (EditText)view.findViewById(R.id.fldScoredBy);
        //view.requestFocus();

        Button clearButton = (Button)view.findViewById(R.id.btnDone);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoalEvent event = new GoalEvent();
                event.setTeam(homeAway);
                event.setGameTime(clockField.getText().toString());
                event.setPlayer(scorerField.getText().toString());
                //event.setTimeFrom(new Date());
                model.getEvents().add(event);

                if (homeAway.equals("Away")) {
                    model.incAwayGoals();
                } else {
                    model.incHomeGoals();
                }

                ((MyActivity)getActivity()).onModelUpdated(null);
                ((MyActivity)getActivity()).showHistory();
            }
        });

        return view;
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {

    }
}
