package com.example.myapp3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class MyActivity extends Activity {
    private List<GameEvent> events = new LinkedList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
        GameEvent event = new GoalEvent();
        event.setEventType("Goal");
        Button btn = (Button)view;
        TextView scoreText;
        if (btn.getText().equals("Home Goal")) {
            scoreText = (TextView) findViewById(R.id.txtHomeScore);
            event.setTeam("Home");
        } else {
            scoreText = (TextView) findViewById(R.id.txtAwayScore);
            event.setTeam("Away");
        }
        int current = Integer.parseInt(scoreText.getText().toString());
        scoreText.setText(Integer.toString(current+1));
        events.add(event);
        refreshEvents();

        Intent intent = new Intent(this, GoalActivity.class);
        intent.putExtra("Team",event.getTeam());
        //startActivity(intent);
    }

    public void refreshEvents() {
        EditText eventList = (EditText)findViewById(R.id.eventList);
        StringBuilder sb = new StringBuilder();
        for (GameEvent event : events) {
            sb.append(event.toString());
            sb.append("\n");
        }
        eventList.setText(sb.toString());
    }
}
