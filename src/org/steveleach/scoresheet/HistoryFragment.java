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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.steveleach.scoresheet.model.GameEvent;
import org.steveleach.scoresheet.model.ModelAware;
import org.steveleach.scoresheet.model.ModelUpdate;
import org.steveleach.scoresheet.model.ScoresheetModel;

import java.util.LinkedList;

/**
 * Implementation code for the new Game History UI fragment.
 *
 * @author Steve Leach
 */
public class HistoryFragment extends Fragment implements ModelAware {
    private ListView eventList = null;
    private ArrayAdapter<String> adapter = null;
    private ScoresheetModel model = new ScoresheetModel();
    private Button nextPeriodButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.historyfragment, container, false);
        eventList = (ListView)view.findViewById(R.id.historyList);

        adapter = new ArrayAdapter<String>(getActivity(), R.layout.basiclistentry, new LinkedList<String>());
        eventList.setAdapter(adapter);

        nextPeriodButton = (Button)view.findViewById(R.id.btnNextPeriod);

        eventList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                String item = (String) adapterView.getItemAtPosition(position);
                ((ScoresheetActivity)getActivity()).yesNoDialog("Delete '" + item + "'?", new Runnable() {
                    @Override
                    public void run() {
                        model.getEvents().remove((int)id);
                        ((ScoresheetActivity)getActivity()).refreshModel();
                        Toast.makeText(getActivity().getApplicationContext(), "Deleting '" + item + "'", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        if (nextPeriodButton != null) {
            nextPeriodButton.setEnabled(model.getPeriod() <= 3);
        }
        if (adapter != null) {
            adapter.clear();
            for (GameEvent event : model.getEvents()) {
                adapter.add(event.toString());
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
        model.addListener(this);
        refreshList();
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {
        refreshList();
    }
}
