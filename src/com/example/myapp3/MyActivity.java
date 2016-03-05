package com.example.myapp3;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class MyActivity extends Activity implements ModelAware {
    private GoalFragment goalFragment = new GoalFragment();
    private HistoryFragment historyFragment = new HistoryFragment();
    private ScoresheetModel model = new ScoresheetModel();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.i("Staz", "MyActivity.onCreate");

        historyFragment.setModel(model);
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

    public void periodButtonClicked(View view) {
        GameEvent event = new GameEvent();
        switch (model.getPeriod()) {
            case 1:
                event.setGameTime("20:00");
                break;
            case 2:
                event.setGameTime("40:00");
                break;
            case 3:
                event.setGameTime("60:00");
                break;
        }
        event.setEventType("Period End");
        event.setTeam("");
        model.getEvents().add(event);
        model.incPeriod();
        updateScores();
        showHistory();
    }

    public void goalButtonClicked(View view) {
        Button btn = (Button)view;
        String homeAway;
        if (btn.getId() == R.id.btnHomeGoal) {
            homeAway = model.getHomeTeam().getName();
        } else {
            homeAway = model.getAwayTeam().getName();
        }
        GoalFragment fragment = new GoalFragment();
        fragment.setModel(model);
        fragment.homeAway = homeAway;
        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction tx = getFragmentManager().beginTransaction();
        tx.replace(R.id.fragmentContainer, fragment);
        tx.addToBackStack(null);
        tx.commit();
    }

    public void showHistory() {
        HistoryFragment h = new HistoryFragment();
        h.setModel(model);
        showFragment(h);
        h.onModelUpdated(null);
    }

    public void clearHistory() {
        model.getEvents().clear();
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {
        updateScores();
    }

    private void updateScores() {
        updateScore(R.id.txtHomeScore, model.getHomeGoals());
        updateScore(R.id.txtAwayScore, model.getAwayGoals());
        updateScore(R.id.txtPeriod, model.getPeriod());
    }

    private void updateScore(int fieldId, int score) {
        TextView field = (TextView) findViewById(fieldId);
        field.setText(Integer.toString(score));
    }
}
