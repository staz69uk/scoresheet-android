/*
 * Copyright (c) 2016, Steve Leach
 */
package com.example.myapp3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.steveleach.scoresheet.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main Activity for the Scoresheet app.
 */
public class ScoresheetActivity extends Activity implements ModelAware {
    public static final String MAIN_FRAGMENT = "MAIN_FRAGMENT";
    private HistoryFragment historyFragment = new HistoryFragment();
    private ScoresheetModel model = new ScoresheetModel();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.i("Staz", "ScoresheetActivity.onCreate");

        showHistory();

        if (savedInstanceState != null) {
            // Reserved
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Reserved
    }

    public void periodButtonClicked(View view) {
        GameEvent event = new PeriodEndEvent();
        event.setPeriod(model.getPeriod());
        event.setClockTime("0000");
        model.addEvent(event);
        updateScores();
        showHistory();
    }

    public void goalButtonClicked(View view) {
        String team = getTeamForView(view, R.id.btnHomeGoal);
        GoalFragment fragment = new GoalFragment();
        fragment.setModel(model);
        fragment.setTeam(team);
        showFragment(fragment);
    }

    private String getTeamForView(View view, int homeId) {
        if (view.getId() == homeId) {
            return model.getHomeTeam().getName();
        } else {
            return model.getAwayTeam().getName();
        }
    }

    public void penaltyButtonClicked(View view) {
        String team = getTeamForView(view, R.id.btnHomePen);
        PenaltyFragment fragment = new PenaltyFragment();
        fragment.setModel(model);
        fragment.setTeam(team);
        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction tx = getFragmentManager().beginTransaction();
        tx.replace(R.id.fragmentContainer, fragment, MAIN_FRAGMENT);
        tx.addToBackStack(null);
        tx.commit();
    }

    public void showHistory() {
        HistoryFragment h = new HistoryFragment();
        h.setModel(model);
        showFragment(h);
        h.onModelUpdated(null);
    }

    public void reportButtonClicked(View view) {
        showReport(ReportFragment.GAME_REPORT);
    }

    private void showReport(String title) {
        ReportFragment fragment = new ReportFragment();
        fragment.setModel(model);
        fragment.setTitle(title);
        showFragment(fragment);
    }

    public void clearHistory() {
        yesNoDialog("Clear all events?", new Runnable() {
                    @Override
                    public void run() {
                        model.getEvents().clear();
                        refreshModel();
                    }
                });
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {
        refreshModel();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.optionsmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuClear:
                clearHistory();
                return true;
            case R.id.menuExport:
                exportGameJson();
                return true;
            case R.id.menuImport:
                importGameJson();
                return true;
            case R.id.menuRefresh:
                refreshModel();
                return true;
            case R.id.menuTestData:
                addTestData();
                return true;
            case R.id.menuAbout:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAbout() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        // When linking text, force to always use default color. This works
        // around a pressed color state bug.
        TextView textView = (TextView) messageView.findViewById(R.id.txtAbout);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher_ih);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    private void addTestData() {
        yesNoDialog("Add test events?", new Runnable() {
            @Override
            public void run() {
                ModelManager.addTestEvents(model);
                refreshModel();
            }
        });
    }

    private void importGameJson() {
        yesNoDialog("Load game data from file?", new Runnable() {
            @Override
            public void run() {
                doImportGameJson();
            }
        });
    }

    private void doImportGameJson() {
        String result = "Unknown";
        File file = new File(getScoresDirectory(), "gamedata.json");
        if (file.exists()) {
            String json = null;
            try {
                json = FileSystem.readTextFileContent(file);
            } catch (IOException e) {
                result = "Error reading gamedata.json : " + e.getMessage();
            }
            if (json != null) {
                try {
                    Json.fromJson(model,json);
                    result = "Loaded gamedata.json";
                } catch (JSONException e) {
                    result = "Error parsing gamedata.json : " + e.getMessage();
                }
                refreshModel();
            }
        } else {
            result = "No exported data found";
        }
        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
    }

    private File getScoresDirectory() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        return new File(dir,"Scoresheets");
    }


    private void exportGameJson() {
        yesNoDialog("Save game data to file?", new Runnable() {
            @Override
            public void run() {
                doExportGameJson();
            }
        });
    }

    private void doExportGameJson() {
        String result = "Unknown";
        String json = null;
        try {
            json = Json.toJson(model);
        } catch (JSONException e) {
            result = "Error building JSON : " + e.getMessage();
        }

        if (json != null) {
            String baseName = "gamedata";
            String dateStr = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
            String mainFileName = String.format("%s-%s.json", baseName, dateStr);
            String lastFileName = baseName + ".json";
            try {
                String state = Environment.getExternalStorageState();
                if (!Environment.MEDIA_MOUNTED.equals(state)) {
                    throw new IOException("External storage not mounted for writing");
                }
                File scoresDir = getScoresDirectory();
                scoresDir.mkdirs();
                File file = new File(scoresDir, mainFileName);
                FileSystem.writeTextFile(file, json);
                FileSystem.copyFile(file, new File(scoresDir, lastFileName));
                result = "Saved " + lastFileName;
            } catch (IOException e) {
                result = "Error saving file " + lastFileName + " : " + e.getMessage();
            }
        }
        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
    }

    public void refreshModel() {
        updateScores();
        model.sortEvents();
        ModelAware visibleFragment = (ModelAware)getFragmentManager().findFragmentByTag(MAIN_FRAGMENT);
        visibleFragment.onModelUpdated(null);
    }

    public void yesNoDialog(String prompt, Runnable action) {
        questionDialog(prompt, "Yes", "No", action);
    }

    public void questionDialog(String prompt, String yesButton, String noButton, final Runnable action) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {
                switch (button) {
                    case DialogInterface.BUTTON_POSITIVE:
                        action.run();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ScoresheetActivity.this);
        builder
                .setMessage(prompt)
                .setPositiveButton(yesButton,listener)
                .setNegativeButton(noButton,listener)
                .show();

    }
}
