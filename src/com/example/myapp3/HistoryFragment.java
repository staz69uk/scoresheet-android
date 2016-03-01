package com.example.myapp3;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.example.myapp3.R.layout.basiclistentry;

/**
 * Created by steve on 01/03/16.
 */
public class HistoryFragment extends Fragment {
    private TextView eventList = null;
    private ListView eventList2 = null;
    private String historyText = ""+new Date();
    private List<GameEvent> events = new LinkedList<GameEvent>();
    private ArrayAdapter<String> adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.historyfragment, container, false);
        eventList = (TextView)view.findViewById(R.id.historyText1);
        eventList2 = (ListView)view.findViewById(R.id.historyList);

        adapter = new ArrayAdapter<String>(getActivity(), basiclistentry, new LinkedList<String>());
        eventList2.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        eventList.setText(historyText);

        adapter.clear();
        for (GameEvent event : events) {
            adapter.add(event.toString());
        }
        adapter.notifyDataSetChanged();
    }

    public void showEvents(List<GameEvent> events) {
        this.events = events;
        this.historyText = "Refreshed " + events.get(events.size()-1).toString();
    }

}
