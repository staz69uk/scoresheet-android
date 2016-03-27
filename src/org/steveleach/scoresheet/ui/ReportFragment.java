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
import org.steveleach.scoresheet.R;
import org.steveleach.scoresheet.model.ModelAware;
import org.steveleach.scoresheet.model.ModelUpdate;
import org.steveleach.scoresheet.model.ScoresheetModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Implementation code for the Game Report UI fragment.
 *
 * @author Steve Leach
 */
public class ReportFragment extends Fragment implements ModelAware {
    private ScoresheetModel model = new ScoresheetModel();
    private View view;
    private TextView report;
    private String title = "Report";
    public static final String GAME_REPORT = "Game Report";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.reportfrgament, container, false);

        report = (TextView)view.findViewById(R.id.reportText);
        report.setMovementMethod(ScrollingMovementMethod.getInstance());

        TextView title = (TextView)view.findViewById(R.id.reportTitle);
        title.setText(this.title);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (title.equals(GAME_REPORT)) {
            report.setText(model.fullReport());
        }
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
        model.addListener(this);
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {
        report.setText(model.fullReport());
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
