package org.steveleach.scoresheet;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.steveleach.scoresheet.model.ModelAware;
import org.steveleach.scoresheet.model.ModelUpdate;
import org.steveleach.scoresheet.model.PenaltyEvent;
import org.steveleach.scoresheet.model.ScoresheetModel;

/**
 * Created by steve on 05/03/16.
 */
public class PenaltyFragment extends Fragment implements ModelAware {

    private ScoresheetModel model = null;
    private View view;
    private EditText periodField;
    private EditText clockField;
    private AutoCompleteTextView penaltyField;
    private EditText playerField;
    private InputMethodManager imgr;
    private EditText minutesField;

    public void setTeam(String homeAway) {
        this.team = homeAway;
    }

    private String team = "Home";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.penaltyfragment, container, false);

        TextView title = (TextView)view.findViewById(R.id.txtPenaltyTitle);
        title.setText(team + " penalty");

        periodField = (EditText)view.findViewById(R.id.fldPenaltyPeriod);
        clockField = (EditText)view.findViewById(R.id.fldPenaltyClock);
        penaltyField = (AutoCompleteTextView)view.findViewById(R.id.fldPenaltyCode);
        playerField = (EditText)view.findViewById(R.id.fldPenaltyPlayer);
        minutesField = (EditText)view.findViewById(R.id.fldPenaltyMins);

        clockField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(4) });
        periodField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(1) });
        penaltyField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(5) });
        minutesField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(2) });
        playerField.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(3) });

        clockField.setOnFocusChangeListener(new MinLengthFocusChangeListener(3));
        playerField.setOnFocusChangeListener(new MinLengthFocusChangeListener(1));
        periodField.setOnFocusChangeListener(new MinLengthFocusChangeListener(1));
        minutesField.setOnFocusChangeListener(new MinLengthFocusChangeListener(1));
        penaltyField.setOnFocusChangeListener(new MinLengthFocusChangeListener(1));

        String[] penaltyCodes = getActivity().getResources().getStringArray(R.array.penaltyCodes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_dropdown_item_1line,penaltyCodes);
        penaltyField.setAdapter(adapter);
        penaltyField.setThreshold(1);

        imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        ((Button)view.findViewById(R.id.btnPenaltyDone)).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    penaltyDone(view);
                }
            }
        );
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        clockField.requestFocus();
        periodField.setText(Integer.toString(model.getPeriod()));
        imgr.showSoftInput(clockField, InputMethodManager.SHOW_IMPLICIT);
    }

    public void penaltyDone(View view) {
        PenaltyEvent event = new PenaltyEvent();
        event.setPeriod(model.getPeriod());
        try {
            event.setClockTime(clockField.getText().toString(),model.getRules());
            event.setTeam(team);
            event.setPlayer(playerField.getText().toString());
            event.setSubType(penaltyField.getText().toString());
            event.setMinutes(Integer.parseInt(minutesField.getText().toString()));
            model.addEvent(event);
        } catch (IllegalArgumentException e) {
            Toast.makeText(getActivity().getApplicationContext(), "Error, not created", Toast.LENGTH_LONG);
        }

        imgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        ((ScoresheetActivity)getActivity()).onModelUpdated(null);
        ((ScoresheetActivity)getActivity()).showHistory();
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {

    }
}
