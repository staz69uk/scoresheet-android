package com.example.myapp3;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MyActivity extends Activity {
    private List<GameEvent> events = new LinkedList<>();
    private GoalFragment goalFragment = new GoalFragment();
    private HistoryFragment historyFragment = new HistoryFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        showFragment(historyFragment);

        if (savedInstanceState != null) {
            TextView homeScore = (TextView) findViewById(R.id.txtHomeScore);
            homeScore.setText(""+savedInstanceState.getInt("homeScore"));

            TextView awayScore = (TextView) findViewById(R.id.txtAwayScore);
            homeScore.setText(""+savedInstanceState.getInt("awayScore"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        TextView homeScore = (TextView) findViewById(R.id.txtHomeScore);
        outState.putInt("homeScore", Integer.parseInt(homeScore.getText().toString()));

        TextView awayScore = (TextView) findViewById(R.id.txtAwayScore);
        outState.putInt("awayScore", Integer.parseInt(awayScore.getText().toString()));

        super.onSaveInstanceState(outState);
    }

    public void goalButtonClicked(View view) {
        Button btn = (Button)view;
        TextView scoreText;
        if (btn.getText().equals("Home Goal")) {
            scoreText = (TextView) findViewById(R.id.txtHomeScore);
        } else {
            scoreText = (TextView) findViewById(R.id.txtAwayScore);
        }
        int current = Integer.parseInt(scoreText.getText().toString());
        scoreText.setText(Integer.toString(current+1));

        // Replace the history fragment with the goal fragment
        showFragment(goalFragment);
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction tx = getFragmentManager().beginTransaction();
        tx.replace(R.id.fragmentContainer, fragment);
        tx.addToBackStack("X1");
        tx.commit();
    }

    public void goalDoneButtonClicked(View view) {
        GameEvent event = new GoalEvent();
        event.setGameTime(""+new Date());
        events.add(event);
        HistoryFragment h = new HistoryFragment();
        h.showEvents(events);
        showFragment(h);
    }

}
