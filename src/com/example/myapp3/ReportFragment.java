package com.example.myapp3;

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
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by steve on 05/03/16.
 */
public class ReportFragment extends Fragment implements ModelAware {
    private ScoresheetModel model = new ScoresheetModel();
    private View view;
    private TextView report;
    private String title = "Report";

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (title.equals("Game Report")) {
            report.setText(model.fullReport());
        } else if (title.equals("Game Export")) {
            report.setText(Json.toJson(model));
        }

    }

    public void doCopyAll() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData myClip = ClipData.newPlainText("text", report.getText());
        clipboard.setPrimaryClip(myClip);
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
