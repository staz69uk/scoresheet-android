/*
 * Copyright (c) 2016, Steve Leach
 */
package org.steveleach.scoresheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.steveleach.scoresheet.io.AndroidScoresheetStore;
import org.steveleach.scoresheet.io.FileManager;
import org.steveleach.scoresheet.io.JsonCodec;
import org.steveleach.scoresheet.model.*;

/**
 * Main Activity for the Scoresheet app.
 */
public class ScoresheetActivity extends Activity implements ModelAware {
    public static final String MAIN_FRAGMENT = "MAIN_FRAGMENT";
    private ScoresheetModel model = new ScoresheetModel();
    private FileManager fileManager = new FileManager();
    private AndroidScoresheetStore scoresheetFolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        scoresheetFolder = new AndroidScoresheetStore(fileManager,new JsonCodec(),new SystemContext(getApplicationContext()));

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

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void periodButtonClicked(View view) {
        GameEvent event = new PeriodEndEvent();
        event.setPeriod(model.getPeriod());
        event.setClockTime("0000",model.getRules());
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
        updateScore(R.id.txtPeriod, model.getPeriod() > 3 ? "OT" : model.getPeriod());
    }

    private void updateScore(int fieldId, Object score) {
        TextView field = (TextView) findViewById(fieldId);
        field.setText(score.toString());
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

        TextView version = (TextView) messageView.findViewById(R.id.txtAppVersion);
        version.setTextColor(defaultColor);
        version.setText("Version " + getVersionName());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher_ih);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    private String getVersionName() {
        try {
            Context context = getApplicationContext();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown!";
        }
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
                String result = scoresheetFolder.loadInto(model);
                refreshModel();
                toast(result);
            }
        });
    }

    private void exportGameJson() {
        yesNoDialog("Save game data to file?", new Runnable() {
            @Override
            public void run() {
                String result = scoresheetFolder.save(model);
                toast(result);
            }
        });
    }

    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
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
