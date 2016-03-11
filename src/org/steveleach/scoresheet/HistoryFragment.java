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
 * Created by steve on 01/03/16.
 */
public class HistoryFragment extends Fragment implements ModelAware {
    private ListView eventList = null;
    private ArrayAdapter<String> adapter = null;
    private ScoresheetModel model = new ScoresheetModel();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.historyfragment, container, false);
        eventList = (ListView)view.findViewById(R.id.historyList);

        adapter = new ArrayAdapter<String>(getActivity(), R.layout.basiclistentry, new LinkedList<String>());
        eventList.setAdapter(adapter);

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
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {
        refreshList();
    }
}
