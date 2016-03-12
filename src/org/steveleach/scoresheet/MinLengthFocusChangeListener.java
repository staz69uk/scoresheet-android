package org.steveleach.scoresheet;

import android.view.View;
import android.widget.EditText;

/**
 * Created by steve on 12/03/16.
 */
public class MinLengthFocusChangeListener implements View.OnFocusChangeListener {

    private final int minLength;

    public MinLengthFocusChangeListener(int minLength) {
        this.minLength = minLength;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            EditText field = (EditText)view;
            String content = field.getText().toString();
            if (content.length() < minLength) {
                field.setError("Minimum length is " + minLength);
            } else {
                field.setError(null);
            }
        }
    }
}
