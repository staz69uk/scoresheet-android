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
package org.steveleach.scoresheet;

import android.view.View;
import android.widget.EditText;

/**
 * Add to a text field to show an error if the content is shorter than a specified value.
 *
 * @author Steve Leach
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
