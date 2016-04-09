/*  Copyright 2016 Steve Leach

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.steveleach.scoresheet.ui;

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
import org.json.JSONException;
import org.steveleach.scoresheet.*;
import org.steveleach.scoresheet.support.ScoresheetStore;
import org.steveleach.scoresheet.support.FileManager;
import org.steveleach.scoresheet.support.JsonCodec;
import org.steveleach.scoresheet.android.AndroidSystemContext;
import org.steveleach.scoresheet.support.SystemContext;
import org.steveleach.scoresheet.model.*;

/**
 * Main Activity for the Scoresheet app.
 *
 * @author Steve Leach
 */
public class ScoresheetActivity extends Activity implements ModelAware, DefaultFragmentActivity {
    private static final String MAIN_FRAGMENT = "MAIN_FRAGMENT";
    private static final String STATE_KEY = "MODEL_JSON";
    private static final String LOG_TAG = "IHSS";
    private ScoresheetModel model = new ScoresheetModel();
    private FileManager fileManager = new FileManager();
    private JsonCodec jsonCodec = new JsonCodec();
    private ScoresheetStore scoresheetStore;
    private SystemContext context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = new AndroidSystemContext(getApplicationContext());
        scoresheetStore = new ScoresheetStore(fileManager,jsonCodec,context);

        showHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.addListener(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String jsonText = savedInstanceState.getString(STATE_KEY);
        if (jsonText != null) {
            try {
                jsonCodec.fromJson(model, jsonText);
            } catch (JSONException e) {
                toast("Error parsing json: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "ScoresheetActivity.onSaveInstanceState");
        try {
            String modelJson = jsonCodec.toJson(model);
            outState.putCharSequence(STATE_KEY, modelJson);
        } catch (JSONException e) {
            toast("Error creating json: " + e.getMessage());
        }
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void periodButtonClicked(View view) {
        GameEvent event = new PeriodEndEvent();
        event.setPeriod(model.getPeriod());
        event.setClockTime("0000");
        model.addEvent(event);
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

    void showHistory() {
        HistoryFragment h = new HistoryFragment();
        h.setModel(model);
        showFragment(h);
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

    private void clearHistory() {
        yesNoDialog(R.string.clearEventsPrompt, new Runnable() {
                    @Override
                    public void run() {
                        model.clearEvents();
                    }
                });
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
        CharSequence otText = getText(R.string.overTimeAbbrev);
        updateScore(R.id.txtHomeScore, model.getHomeGoals());
        updateScore(R.id.txtAwayScore, model.getAwayGoals());
        updateScore(R.id.txtPeriod, model.getPeriod() > model.getRules().getRegulationPeriods() ? otText : model.getPeriod());
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
            case R.id.menuAbout:
                showAbout();
                return true;
            case R.id.menuHelp:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showHelp() {
        HelpFragment fragment = new HelpFragment();
        showFragment(fragment);
    }

    private void showAbout() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        TextView version = (TextView) messageView.findViewById(R.id.txtAppVersion);
        String versionText = getApplicationContext().getString(R.string.appVersionText, getVersionName());
        version.setText(versionText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
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

    private void importGameJson() {
        SavesFragment fragment = new SavesFragment();
        fragment.configure(model, scoresheetStore);
        showFragment(fragment);
    }

    private void exportGameJson() {
        yesNoDialog(R.string.saveGamePrompt, new Runnable() {
            @Override
            public void run() {
                ScoresheetStore.StoreResult result = scoresheetStore.save(model);
                toast(result.text);
            }
        });
    }

    private void toast(String message) {
        Log.d(LOG_TAG, message);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void refreshModel() {
        updateScores();
        model.sortEvents();
        ModelAware visibleFragment = (ModelAware)getFragmentManager().findFragmentByTag(MAIN_FRAGMENT);
        if (visibleFragment == null) {
            // This should not happen
            HistoryFragment fragment = new HistoryFragment();
            fragment.setModel(model);
            showFragment(fragment);
        }
        model.notifyListeners(new ModelUpdate());
    }

    void yesNoDialog(int promptId, Runnable action) {
        questionDialog(getString(promptId), getString(R.string.yesOption), getString(R.string.noOption), action);
    }

    void yesNoDialog(String prompt, Runnable action) {
        questionDialog(prompt, getString(R.string.yesOption), getString(R.string.noOption), action);
    }

    private void questionDialog(String prompt, String yesButton, String noButton, final Runnable action) {
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

    @Override
    public void showDefaultFragment() {
        showHistory();
    }
}
