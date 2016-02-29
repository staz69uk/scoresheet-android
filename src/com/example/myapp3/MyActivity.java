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
        startActivity(intent);
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
