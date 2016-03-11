package org.steveleach.scoresheet;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by steve on 05/03/16.
 */
public class ReportFragment extends Fragment implements ModelAware {
    private ScoresheetModel model = new ScoresheetModel();
    private View view;
    private TextView report;
    private String title = "Report";
    public static final String GAME_REPORT = "Game Report";
    public static final String GAME_EXPORT = "Game Export";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.reportfrgament, container, false);

        report = (TextView)view.findViewById(R.id.reportText);
        report.setMovementMethod(ScrollingMovementMethod.getInstance());

        TextView title = (TextView)view.findViewById(R.id.reportTitle);
        title.setText(this.title);

        Button copyAllButton = (Button)view.findViewById(R.id.btnCopyAll);
        copyAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doCopyAll();
            }
        });

        Button saveButton = (Button)view.findViewById(R.id.btnReportSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSave();
            }
        });

        return view;
    }

    private void doSave() {
        String result = "Unknown";
        File dir = getActivity().getFilesDir();
        try {
            FileOutputStream output = context().openFileOutput("gamedata.json",Context.MODE_PRIVATE);
            output.write(report.getText().toString().getBytes());
            output.close();
            result = "Saved gamedata.json";
        } catch (IOException e) {
            result = "Error saving file";
        }
        Toast.makeText(context(), result, Toast.LENGTH_LONG).show();
    }

    private Context context() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (title.equals(GAME_REPORT)) {
            report.setText(model.fullReport());
        }
    }

    public void doCopyAll() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData myClip = ClipData.newPlainText("text", report.getText());
        clipboard.setPrimaryClip(myClip);
        Toast.makeText(context(), "Copied to clipboard", Toast.LENGTH_LONG).show();
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {}

    public void setTitle(String title) {
        this.title = title;
    }
}
