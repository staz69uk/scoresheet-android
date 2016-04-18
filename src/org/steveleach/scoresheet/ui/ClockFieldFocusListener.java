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

import android.view.View;
import android.widget.EditText;
import org.steveleach.scoresheet.model.ScoresheetModel;

/**
 * FocusChangeListener for validting fields for capturing clock time.
 *
 * @author Steve Leach
 */
public class ClockFieldFocusListener implements View.OnFocusChangeListener {
    private static final int minLength = 3;
    private final EditText periodField;
    private final ScoresheetModel model;

    public ClockFieldFocusListener(EditText periodField, ScoresheetModel model) {
        this.periodField = periodField;
        this.model = model;
    }
    
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            // Validate on losing focus
            EditText field = (EditText)view;
            String content = field.getText().toString();
            if (content.length() < minLength) {
                field.setError("Minimum length is " + minLength);
            } else if (tooEarly(content)) {
                field.setError("Game time is before last existing event");
            } else {
                field.setError(null);
            }
        }
    }

    /**
     * Returns true if the time specified the field is before the time of the last existing event.
     * @param content
     */
    private boolean tooEarly(String content) {
        int period = Integer.parseInt(periodField.getText().toString());
        String lastExistingTime = model.maxGameTime();
        String newGameTime = model.gameTimeFromClock(period,content);
        return newGameTime.compareTo(lastExistingTime) < 0;
    }
}
