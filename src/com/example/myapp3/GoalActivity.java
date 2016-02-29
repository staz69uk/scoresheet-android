package com.example.myapp3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by steve on 29/02/16.
 */
public class GoalActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goal);

        Intent intent = getIntent();
        String team = intent.getStringExtra("Team");
        TextView view = (TextView) findViewById(R.id.goalHeading);
        view.setText(team + " goal");
    }

    public void onDone(View view) {
        Intent intent = new Intent(this, MyActivity.class);

        startActivity(intent);
    }
}