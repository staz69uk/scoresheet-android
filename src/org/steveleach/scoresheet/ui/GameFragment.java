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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import org.steveleach.ihscoresheet.R;
import org.steveleach.scoresheet.model.ModelAware;
import org.steveleach.scoresheet.model.ModelUpdate;
import org.steveleach.scoresheet.model.ScoresheetModel;

/**
 * UI fragment for editing game details.
 *
 * @author Steve Leach
 */
public class GameFragment extends Fragment implements ModelAware {

    private ScoresheetModel model;
    private View view;
    private EditText locationField;
    private EditText homeNameField;
    private EditText awayNameField;
    private InputMethodManager imgr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.gamefragment, container, false);

        homeNameField = (EditText) view.findViewById(R.id.fldHomeTeamName);
        awayNameField = (EditText) view.findViewById(R.id.fldAwayTeamName);
        locationField = (EditText) view.findViewById(R.id.fldGameVenue);

        imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        view.findViewById(R.id.btnGameOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndClose(v);
            }
        });

        return view;
    }

    private void saveAndClose(View view) {
        model.setHomeTeamName(homeNameField.getText().toString());
        model.setAwayTeamName(awayNameField.getText().toString());
        model.setGameLocation(locationField.getText().toString());

        model.setChanged(true);
        model.notifyListeners(ModelUpdate.GAME_CHANGED);

        imgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

        final DefaultFragmentActivity activity = (DefaultFragmentActivity) getActivity();
        activity.showDefaultFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFields();
        homeNameField.requestFocus();
        imgr.showSoftInput(homeNameField, InputMethodManager.SHOW_IMPLICIT);
    }

    private void refreshFields() {
        if (homeNameField!= null) {
            homeNameField.setText(model.homeTeamName());
            awayNameField.setText(model.awayTeamName());
            locationField.setText(model.getGameLocation());
        }
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
        model.addListener(this);
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {
        refreshFields();
    }
}
