package com.example.myapp3;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.LinkedList;

import static com.example.myapp3.R.layout.basiclistentry;

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

        adapter = new ArrayAdapter<String>(getActivity(), basiclistentry, new LinkedList<String>());
        eventList.setAdapter(adapter);

        Button clearButton = (Button)view.findViewById(R.id.btnClearHistory);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clear();
                adapter.notifyDataSetChanged();
                ((MyActivity)getActivity()).clearHistory();
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
