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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyActivity extends Activity implements ModelAware {
    public static final String MAIN_FRAGMENT = "MAIN_FRAGMENT";
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
        GameEvent event = new PeriodEndEvent();
        event.setPeriod(model.getPeriod());
        event.setClockTime("0000");
        model.addEvent(event);
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

    public void penaltyButtonClicked(View view) {
        Button btn = (Button)view;
        String team;
        if (btn.getId() == R.id.btnHomePen) {
            team = model.getHomeTeam().getName();
        } else {
            team = model.getAwayTeam().getName();
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu, menu);
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
                model.addEvent(new GoalEvent(1,"1950","Home","E",41,13,2));
                model.addEvent(new GoalEvent(1,"1830","Away","E",2,1,0));
                model.addEvent(new PenaltyEvent(1, "1515", "Away", "Hook", "2", 2));
                model.addEvent(new GoalEvent(1,"0824","Home","SH",12,93,41));
                model.addEvent(new PeriodEndEvent(1));
                model.addEvent(new GoalEvent(2,"1813","Home","PP",24,41,0));
                refreshModel();
            }
        });
    }

    private void importGameJson() {
        yesNoDialog("Import game data from file?", new Runnable() {
            @Override
            public void run() {
                doImportGameJson();
            }
        });
    }

    private void doImportGameJson() {
        String json = null;
        String result = "Unknown";
        File scoresDir = getScoresDirectory();
        File file = new File(scoresDir, "gamedata.json");
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                reader.close();
                json = sb.toString();
                try {
                    Json.fromJson(model,json);
                    result = "Loaded gamedata.json";
                } catch (JSONException e) {
                    result = "Error parsing gamedata.json : " + e.getMessage();
                }
                refreshModel();
            } catch (IOException e) {
                result = "Error reading gamedata.json : " + e.getMessage();
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
        String json = null;
        try {
            json = Json.toJson(model);
        } catch (JSONException e) {
            json = e.getMessage();
        }

        String baseName = "gamedata";
        String dateStr = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String mainFileName = String.format("%s-%s.json", baseName, dateStr);
        String lastFileName = baseName + ".json";
        String result = "Unknown";
        try {
            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                throw new IOException("External storage not mounted for writing");
            }
            File scoresDir = getScoresDirectory();
            scoresDir.mkdirs();
            File file = new File(scoresDir, mainFileName);
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
            copyFile(file, new File(scoresDir,lastFileName));
            result = "Saved " + lastFileName;
        } catch (IOException e) {
            result = "Error saving file " + lastFileName + " : " + e.getMessage();
        }
        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
    }

    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
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

        AlertDialog.Builder builder = new AlertDialog.Builder(MyActivity.this);
        builder
                .setMessage(prompt)
                .setPositiveButton(yesButton,listener)
                .setNegativeButton(noButton,listener)
                .show();

    }
}
